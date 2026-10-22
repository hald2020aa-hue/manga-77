package com.mangaxw.clean;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.*;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.atomic.AtomicBoolean;

public class BrowserActivity extends Activity {
    private WebView web;
    private EditText address;
    private TextView status;
    private String pageUrl="";
    private final AtomicBoolean busy=new AtomicBoolean(false);
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout root=Ui.root(this);setContentView(root);
        LinearLayout loc=Ui.row(this);root.addView(loc);
        address=Ui.edit(this,"رابط الموقع أو الفصل");loc.addView(address,new LinearLayout.LayoutParams(0,Ui.dp(this,47),1));
        Button go=Ui.accent(this,"اذهب");loc.addView(go,Ui.layout(this,-2,Ui.dp(this,47),3));
        go.setOnClickListener(v->navigate(address.getText().toString()));
        LinearLayout actions=Ui.row(this);root.addView(actions);
        Button back=Ui.button(this,"◀");Ui.weight(actions,back);back.setOnClickListener(v->{if(web.canGoBack())web.goBack();});
        Button next=Ui.button(this,"▶");Ui.weight(actions,next);next.setOnClickListener(v->{if(web.canGoForward())web.goForward();});
        Button star=Ui.button(this,"حفظ عمل");Ui.weight(actions,star);star.setOnClickListener(v->saveBookmark());
        Button reader=Ui.accent(this,"استخراج الصفحات");Ui.weight(actions,reader);reader.setOnClickListener(v->extract());
        status=Ui.text(this,"متصفح المواقع — حظر طلبات الإعلانات المعروفة",12,false);status.setTextColor(Ui.MUTED);root.addView(status);
        web=new WebView(this);root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
        WebSettings settings=web.getSettings();
        settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(true);settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);settings.setUseWideViewPort(true);
        settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(true);
        if(android.os.Build.VERSION.SDK_INT>=26)settings.setSafeBrowsingEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);
        web.setWebChromeClient(new WebChromeClient(){
            @Override public void onProgressChanged(WebView view,int progress){status.setText("تحميل الصفحة: "+progress+"%");}
        });
        web.setWebViewClient(new WebViewClient(){
            @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest request){
                return AdFilter.isAd(request.getUrl())?AdFilter.emptyResponse():null;
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest req){
                String s=req.getUrl().getScheme();
                return !("https".equalsIgnoreCase(s)||"http".equalsIgnoreCase(s));
            }
            @Override public void onPageStarted(WebView v,String url,Bitmap icon){
                pageUrl=url;address.setText(url);
            }
            @Override public void onPageFinished(WebView v,String url){
                pageUrl=url;address.setText(url);status.setText("جاهز — "+(v.getTitle()==null?"":v.getTitle()));
                v.evaluateJavascript(AdFilter.CLEANUP_JS,null);
                if(url.startsWith("http://")||url.startsWith("https://")){
                    String title=v.getTitle();if(title==null||title.trim().isEmpty())title=url;
                    AppStore.upsert(BrowserActivity.this,"history",AppStore.item(title,url),"url");
                }
            }
        });
        String incoming=getIntent().getStringExtra("url");if(incoming==null)incoming="https://www.google.com";
        navigate(incoming);
    }
    private void navigate(String value){
        try{String url=AppStore.normalize(value);web.loadUrl(url);}catch(Exception e){Ui.message(this,e.getMessage());}
    }
    private void saveBookmark(){
        String url=web.getUrl();if(url==null)return;
        String title=web.getTitle();if(title==null||title.isEmpty())title=url;
        AppStore.upsert(this,"bookmarks",AppStore.item(title,url),"url");
        Ui.message(this,"تم حفظ العمل في المكتبة");
    }
    private void extract(){
        if(busy.getAndSet(true))return;
        status.setText("جارٍ فحص الصور المعروضة في الفصل...");
        web.evaluateJavascript(ChapterExtractor.SCRIPT,result->{
            busy.set(false);
            JSONArray images=ChapterExtractor.parseResult(result);
            if(images.length()==0){
                new AlertDialog.Builder(this).setTitle("لم أعثر على صفحات تلقائيًا")
                    .setMessage("تأكد إنك داخل صفحة فصل، ثم مرّر الصفحة حتى تظهر الصور وجرب مرة ثانية. تقدر تقرأ الصفحة نفسها من المتصفح بدون قيود مصادر.")
                    .setPositiveButton("تمام",null).show();
                status.setText("لم يتم العثور على صور كبيرة");return;
            }
            String title=web.getTitle();if(title==null||title.trim().isEmpty())title="فصل مانجا";
            final String chapter=title, base=web.getUrl(),agent=web.getSettings().getUserAgentString();
            status.setText("تم اكتشاف "+images.length()+" صورة");
            new AlertDialog.Builder(this).setTitle("تم اكتشاف "+images.length()+" صفحة")
                .setItems(new String[]{"قراءة الفصل الآن","تنزيل الصور للعمل دون إنترنت","إلغاء"},(d,index)->{
                    if(index==0){Intent intent=new Intent(this,ReaderActivity.class);
                        intent.putExtra("images",images.toString());intent.putExtra("title",chapter);
                        intent.putExtra("base",base);startActivity(intent);
                    }else if(index==1){
                        status.setText("جارٍ تنزيل الفصل في الخلفية أثناء بقاء التطبيق مفتوحًا...");
                        DownloadUtil.download(this,chapter,base,agent,images,(msg)->status.setText(msg));
                    }
                }).show();
        });
    }
    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){
        if(web!=null){web.stopLoading();web.destroy();web=null;}
        super.onDestroy();
    }
}

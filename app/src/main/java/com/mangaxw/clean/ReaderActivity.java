package com.mangaxw.clean;

import android.app.Activity;
import android.os.Bundle;
import android.net.Uri;
import android.webkit.*;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.URLConnection;

public class ReaderActivity extends Activity {
    private WebView web;
    private boolean horizontal=false;
    private String offlineId="";

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        String data=getIntent().getStringExtra("images");
        offlineId=getIntent().getStringExtra("downloadId");
        if(offlineId==null)offlineId="";
        String title=getIntent().getStringExtra("title");
        String base=getIntent().getStringExtra("base");
        JSONArray images;
        if(!offlineId.isEmpty()){
            if(!offlineId.matches("[0-9a-fA-F-]{36}")){finish();return;}
            JSONObject record=null;
            JSONArray saved=AppStore.get(this,"downloads");
            for(int i=0;i<saved.length();i++){
                JSONObject x=saved.optJSONObject(i);
                if(x!=null&&offlineId.equals(x.optString("id"))){record=x;break;}
            }
            if(record==null){Ui.message(this,"الفصل غير موجود");finish();return;}
            title=record.optString("name","فصل محفوظ");
            images=new JSONArray();
            for(int i=0;i<record.optInt("count");i++)images.put("https://xw.local/image/"+offlineId+"/"+i);
            base="https://xw.local/reader/";
        }else{
            try{images=new JSONArray(data==null?"[]":data);}catch(Exception e){images=new JSONArray();}
        }
        LinearLayout root=Ui.root(this);setContentView(root);
        TextView header=Ui.text(this,title==null?"قارئ الفصل":title,17,true);
        header.setSingleLine(true);header.setEllipsize(android.text.TextUtils.TruncateAt.END);root.addView(header);
        LinearLayout nav=Ui.row(this);root.addView(nav);
        Button close=Ui.button(this,"العودة");Ui.weight(nav,close);close.setOnClickListener(v->finish());
        Button layout=Ui.accent(this,"عرض أفقي");Ui.weight(nav,layout);
        layout.setOnClickListener(v->{
            horizontal=!horizontal;layout.setText(horizontal?"عرض عمودي":"عرض أفقي");
            web.evaluateJavascript("document.body.classList.toggle('horizontal',"+horizontal+");",null);
        });
        Button top=Ui.button(this,"أعلى");Ui.weight(nav,top);
        top.setOnClickListener(v->web.evaluateJavascript("window.scrollTo(0,0);",null));
        TextView count=Ui.text(this,"عدد الصفحات: "+images.length()+"  •  يمكنك التكبير بإصبعين",12,false);
        count.setTextColor(Ui.MUTED);root.addView(count);
        web=new WebView(this);root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
        web.setBackgroundColor(Ui.BG);
        WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);
        s.setBuiltInZoomControls(true);s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);s.setLoadWithOverviewMode(true);
        s.setAllowFileAccess(false);s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.setWebViewClient(new WebViewClient(){
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest req){
                Uri url=req.getUrl();
                if("xw.local".equalsIgnoreCase(url.getHost())){
                    if(!"https".equalsIgnoreCase(url.getScheme()))return AdFilter.emptyResponse();
                    java.util.List<String> segments=url.getPathSegments();
                    if(segments.size()!=3||!"image".equals(segments.get(0)))return AdFilter.emptyResponse();
                    String id=segments.get(1),num=segments.get(2);
                    if(!id.equals(offlineId)||!id.matches("[0-9a-fA-F-]{36}")||!num.matches("[0-9]{1,4}"))return AdFilter.emptyResponse();
                    try{
                        File img=new File(DownloadUtil.directory(ReaderActivity.this,id),
                            String.format(java.util.Locale.US,"%04d.img",Integer.parseInt(num)));
                        BufferedInputStream stream=new BufferedInputStream(new FileInputStream(img));
                        stream.mark(32);
                        String type=URLConnection.guessContentTypeFromStream(stream);
                        stream.reset();
                        return new WebResourceResponse(type==null?"image/jpeg":type,null,stream);
                    }catch(Exception e){return AdFilter.emptyResponse();}
                }
                return AdFilter.isAd(url)?AdFilter.emptyResponse():null;
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){return true;}
        });
        StringBuilder html=new StringBuilder("<!DOCTYPE html><html lang='ar'><head><meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=5'>");
        html.append("<style>html,body{margin:0;background:#0c111b;color:#eee}body{text-align:center;}img{display:block;width:100%;height:auto;margin:0 auto 3px;object-fit:contain;}body.horizontal{white-space:nowrap;overflow-x:auto;overflow-y:hidden;height:95vh;}body.horizontal img{display:inline-block;max-height:91vh;height:auto;width:auto;max-width:none;vertical-align:top;margin-right:4px;}p{font:14px sans-serif;padding:20px}</style></head><body>");
        for(int i=0;i<images.length();i++){
            String src=images.optString(i,"");
            if(src.startsWith("https://")||src.startsWith("http://"))
                html.append("<img loading='lazy' referrerpolicy='strict-origin-when-cross-origin' alt='صفحة ")
                    .append(i+1).append("' src='").append(android.text.TextUtils.htmlEncode(src)).append("'>");
        }
        html.append(images.length()==0?"<p>لم يتم العثور على صور لعرضها.</p>":"");
        html.append("</body></html>");
        try{String safeBase=base==null?"https://xw.local/reader/":AppStore.normalize(base);
            web.loadDataWithBaseURL(safeBase,html.toString(),"text/html","UTF-8",null);
        }catch(Exception e){web.loadDataWithBaseURL("https://xw.local/reader/",html.toString(),"text/html","UTF-8",null);}
    }
    @Override protected void onDestroy(){if(web!=null){web.stopLoading();web.destroy();web=null;}super.onDestroy();}
}

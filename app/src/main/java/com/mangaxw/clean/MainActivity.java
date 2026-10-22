package com.mangaxw.clean;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private String tab="sources";
    private LinearLayout content;
    private static final int BACKUP_REQUEST=10,RESTORE_REQUEST=11;

    @Override public void onCreate(Bundle saved){super.onCreate(saved);render();}
    @Override public void onResume(){super.onResume();render();}
    private void render(){
        LinearLayout root=Ui.root(this);setContentView(root);
        TextView header=Ui.text(this,"MANGA XW   ✦   CLEAN",24,true);header.setTextColor(Ui.GOLD);root.addView(header);
        root.addView(Ui.text(this,"قارئ مستقل • بدون شبكات إعلانات • أضف مواقعك بنفسك",12,false));
        HorizontalScrollView bar=new HorizontalScrollView(this);bar.setHorizontalScrollBarEnabled(false);
        LinearLayout tabs=Ui.row(this);bar.addView(tabs);root.addView(bar);
        String[][] items={{"sources","المواقع"},{"bookmarks","المكتبة"},{"history","السجل"},{"downloads","التنزيلات"},{"settings","الإعدادات"}};
        for(String[] item:items){Button b=tab.equals(item[0])?Ui.accent(this,item[1]):Ui.button(this,item[1]);
            tabs.addView(b,Ui.layout(this,-2,Ui.dp(this,46),3));
            b.setOnClickListener(v->{tab=item[0];render();});}
        ScrollView scroll=Ui.scroll(this,root);
        content=new LinearLayout(this);content.setPadding(0,Ui.dp(this,8),0,Ui.dp(this,14));content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content);
        switch(tab){
            case "sources":showSources();break;
            case "bookmarks":showCollection("bookmarks","الأعمال المحفوظة");break;
            case "history":showCollection("history","سجل المواقع والفصول");break;
            case "downloads":showDownloads();break;
            default:showSettings();
        }
    }
    private void heading(String text){
        TextView t=Ui.text(this,text,19,true);content.addView(t);
    }
    private void note(String text){TextView t=Ui.text(this,text,13,false);t.setTextColor(Ui.MUTED);content.addView(t);}
    private void showSources(){
        heading("مصادرك الخاصة");note("أضف رابط أي موقع HTTP أو HTTPS دون قوائم حظر داخلية.");
        Button add=Ui.accent(this,"＋ إضافة موقع جديد");content.addView(add,Ui.layout(this,-1,Ui.dp(this,54),3));
        add.setOnClickListener(v->sourceDialog(null));
        JSONArray items=AppStore.get(this,"sources");if(items.length()==0)note("ما أضفت أي موقع بعد. استخدم زر الإضافة.");
        for(int i=items.length()-1;i>=0;i--){JSONObject item=items.optJSONObject(i);if(item==null)continue;
            addRow(item.optString("name","موقع"),item.optString("url",""),
                ()->open(item.optString("url","")),
                ()->sourceDialog(item),
                ()->confirmDelete("sources",item.optString("url","")));
        }
        Button direct=Ui.button(this,"فتح رابط مباشرة دون حفظه");content.addView(direct,Ui.layout(this,-1,Ui.dp(this,50),5));
        direct.setOnClickListener(v->directUrl());
    }
    private void showCollection(String key,String label){
        heading(label);JSONArray items=AppStore.get(this,key);
        if(items.length()==0){note("لا توجد عناصر حتى الآن.");return;}
        for(int i=items.length()-1;i>=0;i--){JSONObject x=items.optJSONObject(i);if(x==null)continue;
            String url=x.optString("url","");
            addRow(x.optString("name","بدون عنوان"),url,()->open(url),null,()->confirmDelete(key,url));
        }
    }
    private void showDownloads(){
        heading("الفصول المحفوظة بدون إنترنت");
        note("حمّل الصور من أي فصل مدعوم، ثم افتحه هنا بدون اتصال.");
        JSONArray d=AppStore.get(this,"downloads");if(d.length()==0)note("لا توجد تنزيلات.");
        for(int i=d.length()-1;i>=0;i--){JSONObject item=d.optJSONObject(i);if(item==null)continue;
            String id=item.optString("id");
            addRow(item.optString("name","فصل محفوظ"),item.optInt("count",0)+" صفحة محفوظة",
                ()->{Intent x=new Intent(this,ReaderActivity.class);x.putExtra("downloadId",id);startActivity(x);},
                null,()->new AlertDialog.Builder(this).setMessage("حذف صور هذا الفصل؟")
                  .setNegativeButton("إلغاء",null).setPositiveButton("حذف",(d1,w)->{
                      DownloadUtil.deleteChapter(this,id);AppStore.remove(this,"downloads","id",id);render();}).show());
        }
    }
    private void showSettings(){
        heading("الإعدادات والنسخ الاحتياطية");
        note("لا يحتوي هذا المشروع على SDK للإعلانات، ولا قوائم لمنع مواقع أو عناوين أعمال بعينها.");
        Button save=Ui.accent(this,"تصدير نسخة JSON");
        content.addView(save,Ui.layout(this,-1,Ui.dp(this,50),4));
        save.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/json");
            i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"manga-xw-backup.json");startActivityForResult(i,BACKUP_REQUEST);});
        Button load=Ui.button(this,"استيراد نسخة JSON");content.addView(load,Ui.layout(this,-1,Ui.dp(this,50),4));
        load.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/json");
            i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,RESTORE_REQUEST);});
        heading("ملاحظات مهمة");
        note("بعض المواقع تحتاج تسجيل دخول أو حماية Cloudflare؛ ويعمل تصفحها في WebView بحسب سياسة الموقع نفسه.");
        note("استخراج الصور تلقائيًا يعتمد على بنية الصفحة؛ وقد يحتاج الموقع إلى فتح الصور يدويًا أو التمرير قبل الاستخراج.");
        note("النسخة الاحتياطية تشمل المواقع والمكتبة والسجل، ولا تشمل ملفات الصور المنزّلة.");
    }
    private void addRow(String title,String subtitle,Runnable onOpen,Runnable onEdit,Runnable onDelete){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(Ui.shape(Ui.CARD,14,this));card.setPadding(Ui.dp(this,10),Ui.dp(this,8),Ui.dp(this,10),Ui.dp(this,9));
        content.addView(card,Ui.layout(this,-1,-2,5));
        TextView a=Ui.text(this,title,16,true);card.addView(a);
        TextView b=Ui.text(this,subtitle,12,false);b.setTextColor(Ui.MUTED);b.setMaxLines(2);card.addView(b);
        LinearLayout buttons=Ui.row(this);card.addView(buttons);
        Button open=Ui.accent(this,"فتح");Ui.weight(buttons,open);open.setOnClickListener(v->onOpen.run());
        if(onEdit!=null){Button edit=Ui.button(this,"تعديل");Ui.weight(buttons,edit);edit.setOnClickListener(v->onEdit.run());}
        Button delete=Ui.button(this,"حذف");Ui.weight(buttons,delete);delete.setOnClickListener(v->onDelete.run());
    }
    private void open(String url){
        try{url=AppStore.normalize(url);Intent i=new Intent(this,BrowserActivity.class);i.putExtra("url",url);startActivity(i);}
        catch(Exception e){Ui.message(this,e.getMessage());}
    }
    private void directUrl(){
        EditText entry=Ui.edit(this,"https://example.com");
        new AlertDialog.Builder(this).setTitle("فتح رابط").setView(entry)
            .setPositiveButton("فتح",(d,w)->open(entry.getText().toString())).setNegativeButton("إلغاء",null).show();
    }
    private void sourceDialog(JSONObject existing){
        LinearLayout f=Ui.root(this);
        EditText name=Ui.edit(this,"اسم الموقع");f.addView(name,Ui.layout(this,-1,-2,4));
        EditText url=Ui.edit(this,"https://example.org");f.addView(url,Ui.layout(this,-1,-2,4));
        if(existing!=null){name.setText(existing.optString("name"));url.setText(existing.optString("url"));}
        new AlertDialog.Builder(this).setTitle(existing==null?"إضافة موقع":"تعديل الموقع").setView(f)
            .setPositiveButton("حفظ",(d,w)->{
                try{
                    String label=name.getText().toString().trim(),link=AppStore.normalize(url.getText().toString());
                    if(label.isEmpty())label=Uri.parse(link).getHost();
                    if(existing!=null)AppStore.remove(this,"sources","url",existing.optString("url"));
                    AppStore.upsert(this,"sources",AppStore.item(label,link),"url");render();
                }catch(Exception e){Ui.message(this,e.getMessage());}
            }).setNegativeButton("إلغاء",null).show();
    }
    private void confirmDelete(String collection,String url){
        new AlertDialog.Builder(this).setMessage("حذف هذا العنصر؟")
            .setPositiveButton("حذف",(d,w)->{AppStore.remove(this,collection,"url",url);render();})
            .setNegativeButton("إلغاء",null).show();
    }
    @Override protected void onActivityResult(int request,int result,Intent intent){
        super.onActivityResult(request,result,intent);
        if(result!=RESULT_OK||intent==null||intent.getData()==null)return;
        try{
            if(request==BACKUP_REQUEST){
                try(OutputStream out=getContentResolver().openOutputStream(intent.getData())){
                    if(out==null)throw new IOException("Cannot write file");
                    out.write(AppStore.backup(this).getBytes(StandardCharsets.UTF_8));
                }Ui.message(this,"تم تصدير النسخة");
            }else if(request==RESTORE_REQUEST){
                byte[] data;
                try(InputStream in=getContentResolver().openInputStream(intent.getData());ByteArrayOutputStream dst=new ByteArrayOutputStream()){
                    if(in==null)throw new IOException("Cannot open file");
                    byte[] buffer=new byte[8192];int n;
                    while((n=in.read(buffer))!=-1){dst.write(buffer,0,n);if(dst.size()>5_000_000)throw new IOException("حجم الملف كبير");}
                    data=dst.toByteArray();
                }
                AppStore.restore(this,new String(data,StandardCharsets.UTF_8));Ui.message(this,"تم الاستيراد");render();
            }
        }catch(Exception e){Ui.message(this,"لم تكتمل العملية: "+e.getMessage());}
    }
}

package com.mangaxw.clean;

import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DownloadUtil {
    private DownloadUtil(){}
    private static final ExecutorService WORKER=Executors.newSingleThreadExecutor();
    public interface Status{void send(String status);}
    public static File directory(Activity a,String id){return new File(new File(a.getFilesDir(),"chapters"),id);}
    public static void deleteChapter(Activity a,String id){
        // Chapter IDs originate in this app. Never accept traversal outside chapters root.
        if(!id.matches("[0-9a-fA-F-]{36}"))return;
        File f=directory(a,id);if(f.isDirectory()){
            File[] files=f.listFiles();if(files!=null)for(File file:files)file.delete();
        }f.delete();
    }
    public static void download(Activity activity,String name,String pageUrl,String userAgent,JSONArray images,Status status){
        final String id=AppStore.newId();
        final String cookies=CookieManager.getInstance().getCookie(pageUrl);
        final Handler ui=new Handler(Looper.getMainLooper());
        WORKER.execute(()->{
            File folder=directory(activity,id);
            if(!folder.mkdirs()&&!folder.isDirectory()){
                ui.post(()->status.send("فشل إنشاء مجلد التنزيل"));return;
            }
            int count=0;String failure="";
            for(int i=0;i<images.length();i++){
                String src=images.optString(i,"");
                try{
                    String normalized=AppStore.normalize(src);
                    HttpURLConnection c=(HttpURLConnection)new URL(normalized).openConnection();
                    c.setConnectTimeout(15000);c.setReadTimeout(25000);c.setInstanceFollowRedirects(true);
                    c.setRequestProperty("User-Agent",userAgent);
                    c.setRequestProperty("Referer",pageUrl);
                    if(cookies!=null&&!cookies.isEmpty())c.setRequestProperty("Cookie",cookies);
                    int response=c.getResponseCode();
                    if(response<200||response>=300)throw new IOException("HTTP "+response);
                    String type=c.getContentType();
                    if(type!=null&&!type.toLowerCase().startsWith("image/"))throw new IOException("محتوى ليس صورة");
                    File target=new File(folder,String.format(java.util.Locale.US,"%04d.img",count));
                    try(InputStream input=c.getInputStream();FileOutputStream out=new FileOutputStream(target)){
                        byte[] buffer=new byte[32768];int n;long bytes=0;
                        while((n=input.read(buffer))!=-1){
                            bytes+=n;if(bytes>30L*1024*1024)throw new IOException("الصورة أكبر من 30 MB");
                            out.write(buffer,0,n);
                        }
                    }catch(Exception ex){target.delete();throw ex;}
                    count++;
                    final int progress=count;
                    ui.post(()->status.send("تم تنزيل "+progress+" من "+images.length()+" صفحة"));
                    c.disconnect();
                }catch(Exception e){failure=e.getMessage();break;}
            }
            final int done=count;final String why=failure;
            if(done==0)deleteChapter(activity,id);
            else{
                JSONObject record=new JSONObject();try{
                    record.put("id",id);record.put("name",name);record.put("url",pageUrl);
                    record.put("count",done);record.put("date",System.currentTimeMillis());
                }catch(JSONException ignored){}
                AppStore.upsert(activity,"downloads",record,"id");
            }
            ui.post(()->status.send(done==images.length()?
                "اكتمل تنزيل "+done+" صفحة":("نُزّل "+done+" صفحة من "+images.length()+(why.isEmpty()?"":" — "+why))));
        });
    }
}

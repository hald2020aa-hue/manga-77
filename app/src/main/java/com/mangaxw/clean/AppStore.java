package com.mangaxw.clean;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.net.URI;
import java.util.UUID;

public final class AppStore {
    private AppStore(){}
    private static final String PREF = "xw_clean_private_state", KEY = "json";
    private static final String[] COLLECTIONS={"sources","bookmarks","history","downloads"};

    private static JSONObject read(Context c) {
        SharedPreferences p=c.getApplicationContext().getSharedPreferences(PREF,Context.MODE_PRIVATE);
        try {return new JSONObject(p.getString(KEY,"{}"));}catch(Exception e){return new JSONObject();}
    }
    private static void write(Context c,JSONObject state){
        c.getApplicationContext().getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putString(KEY,state.toString()).apply();
    }
    public static synchronized JSONArray get(Context c,String collection) {
        JSONObject st=read(c);JSONArray data=st.optJSONArray(collection);
        if(data==null)return new JSONArray();
        try{return new JSONArray(data.toString());}catch(JSONException e){return new JSONArray();}
    }
    public static synchronized void upsert(Context c,String collection,JSONObject value,String uniqueField){
        JSONArray existing=get(c,collection),result=new JSONArray();
        String target=value.optString(uniqueField,"");
        for(int i=0;i<existing.length();i++){
            JSONObject item=existing.optJSONObject(i);
            if(item!=null&&!item.optString(uniqueField).equals(target)) result.put(item);
        }
        result.put(value);
        JSONObject st=read(c);try{st.put(collection,result);}catch(JSONException ignored){}write(c,st);
    }
    public static synchronized void remove(Context c,String collection,String key,String target){
        JSONArray existing=get(c,collection),result=new JSONArray();
        for(int i=0;i<existing.length();i++){
            JSONObject item=existing.optJSONObject(i);
            if(item!=null&&!item.optString(key).equals(target))result.put(item);
        }
        JSONObject st=read(c);try{st.put(collection,result);}catch(JSONException ignored){}write(c,st);
    }
    public static JSONObject item(String name,String url){
        JSONObject j=new JSONObject();
        try{j.put("name",name);j.put("url",url);j.put("lastUsed",System.currentTimeMillis());}catch(JSONException ignored){}
        return j;
    }
    public static String newId(){return UUID.randomUUID().toString();}
    public static synchronized String backup(Context c){
        JSONObject st=read(c),out=new JSONObject();
        try{
            out.put("format","manga-xw-clean-v1");out.put("createdAt",System.currentTimeMillis());
            // Downloads are local binary files and are deliberately excluded from JSON backups.
            for(String col:new String[]{"sources","bookmarks","history"})out.put(col,st.optJSONArray(col)==null?new JSONArray():st.optJSONArray(col));
        }catch(JSONException ignored){}
        return out.toString();
    }
    public static synchronized void restore(Context c,String json) throws JSONException {
        JSONObject data=new JSONObject(json);
        if(!"manga-xw-clean-v1".equals(data.optString("format")))throw new JSONException("صيغة النسخ الاحتياطي غير متوافقة");
        JSONObject st=read(c);
        for(String col:new String[]{"sources","bookmarks","history"}){
            JSONArray arr=data.optJSONArray(col);if(arr==null)throw new JSONException("Missing collection: "+col);
            st.put(col,arr);
        }
        write(c,st);
    }
    public static String normalize(String input){
        if(input==null)throw new IllegalArgumentException("الرابط فارغ");
        String link=input.trim();
        if(!link.matches("(?i)^https?://.*")) {
            if(link.contains("://")||link.matches("(?i)^[a-z][a-z0-9+.-]*:.*"))throw new IllegalArgumentException("المسموح روابط HTTP وHTTPS فقط");
            link="https://"+link;
        }
        try{
            URI uri=new URI(link);
            String scheme=uri.getScheme();
            if(!"http".equalsIgnoreCase(scheme)&&!"https".equalsIgnoreCase(scheme))throw new IllegalArgumentException("نوع الرابط غير مدعوم");
            if(uri.getRawUserInfo()!=null)throw new IllegalArgumentException("روابط تتضمن بيانات دخول غير مدعومة");
            // Unicode and Arabic hostnames are mapped to IDN ASCII, without domain blocklists.
            if(uri.getHost()==null&&uri.getRawAuthority()!=null){
                String authority=uri.getRawAuthority();
                if(authority.indexOf('@')>=0)throw new IllegalArgumentException("عنوان الموقع غير صحيح");
                int portIndex=authority.lastIndexOf(':');
                String host=authority,port="";
                if(portIndex>0&&authority.substring(portIndex+1).matches("[0-9]+")){
                    host=authority.substring(0,portIndex);port=authority.substring(portIndex);
                }
                String ascii=java.net.IDN.toASCII(host);
                int start=link.indexOf("://")+3;
                link=link.substring(0,start)+ascii+port+link.substring(start+authority.length());
                uri=new URI(link);
            }
            if(uri.getHost()==null||uri.getHost().isEmpty())throw new IllegalArgumentException("اسم الموقع غير صحيح");
            return uri.toASCIIString();
        }catch(java.net.URISyntaxException e){throw new IllegalArgumentException("صيغة الرابط غير صحيحة");}
    }
}

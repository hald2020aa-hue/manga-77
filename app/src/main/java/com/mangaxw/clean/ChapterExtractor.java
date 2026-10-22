package com.mangaxw.clean;

import org.json.JSONArray;
import org.json.JSONTokener;

/** Generic, best-effort parser; user controls which URLs to open. */
public final class ChapterExtractor {
    private ChapterExtractor(){}
    public static final String SCRIPT = "(function(){"
        + "var urls=[];var seen=new Set();"
        + "document.querySelectorAll('img,source').forEach(function(e){"
        + "var a=[e.getAttribute('data-src'),e.getAttribute('data-original'),"
        + "e.getAttribute('data-lazy-src'),e.getAttribute('data-url'),"
        + "e.getAttribute('src'),e.currentSrc];"
        + "var inReader=!!e.closest('.reading-content,.chapter-content,.reader-content,#chapter,#reader,#chapter-content,.read-container');"
        + "var good=inReader||e.naturalHeight>320||e.clientHeight>260||/page|chapter|scan|comic|manga|webtoon|reader/i.test((e.className||'').toString()+' '+(e.id||'')+' '+(e.parentElement?e.parentElement.className:''));"
        + "if(!good)return;"
        + "for(var i=0;i<a.length;i++){if(!a[i]||typeof a[i]!=='string')continue;try{var url=new URL(a[i],location.href);"
        + "if((url.protocol==='http:'||url.protocol==='https:')&&!seen.has(url.href)){seen.add(url.href);urls.push(url.href);break;}}catch(x){}}"
        + "});return JSON.stringify(urls.slice(0,400));"
        + "})()";
    public static JSONArray parseResult(String response){
        try{
            Object decoded=new JSONTokener(response).nextValue();
            String arr=decoded instanceof String?(String)decoded:response;
            return new JSONArray(arr);
        }catch(Exception e){return new JSONArray();}
    }
}

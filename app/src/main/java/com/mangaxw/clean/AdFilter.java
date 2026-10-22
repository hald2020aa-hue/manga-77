package com.mangaxw.clean;

import android.net.Uri;
import android.webkit.WebResourceResponse;
import java.io.ByteArrayInputStream;
import java.util.Locale;

/** Blocks known ad network requests. No business/content host is denied. */
public final class AdFilter {
    private AdFilter(){}
    private static final String[] AD_HOSTS = {
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "adservice.google.com", "ads.google.com", "unityads.unity3d.com",
        "ads.vungle.com", "applovin.com", "adcolony.com",
        "inmobi.com", "pubmatic.com", "openx.net", "criteo.com",
        "taboola.com", "outbrain.com", "adsrvr.org", "adnxs.com",
        "rubiconproject.com", "bidmachine.io", "amazon-adsystem.com"
    };
    public static boolean isAd(Uri uri){
        if(uri==null)return false;
        String host=uri.getHost();if(host==null)return false;
        host=host.toLowerCase(Locale.US);
        for(String domain:AD_HOSTS) if(host.equals(domain)||host.endsWith("."+domain))return true;
        return false;
    }
    public static WebResourceResponse emptyResponse(){
        return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));
    }
    public static final String CLEANUP_JS = "(function(){"
        + "var selectors=['ins.adsbygoogle','[id^=google_ads_]','[class*=ad-banner]',"
        + "'[class*=advertisement]','[id*=advertisement]','.adsbygoogle',"
        + "'iframe[src*=doubleclick]','iframe[src*=googlesyndication]'];"
        + "selectors.forEach(function(s){document.querySelectorAll(s).forEach(function(n){n.remove();});});"
        + "})();";
}

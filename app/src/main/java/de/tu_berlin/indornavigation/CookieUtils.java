package de.tu_berlin.indornavigation;

import android.webkit.CookieManager;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Jan on 13. 11. 2015.
 */
public class CookieUtils {

    /**
     * Function retrieves cookie value from cookie manager.
     *
     * @param domain - cookie domain
     * @param cookieName - name of the cookie
     * @return Function returns String containing cookie value. If cookie with provided name doesn't
     *         exist, function returns null.
     */
    public static String getCookie(String domain, String cookieName) {

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(domain);
        if (cookies != null) {

            cookies = StringUtils.deleteWhitespace(cookies);

            for (String cookie : cookies.split(";")) {

                int index = cookie.indexOf('=');
                if (cookie.substring(0, index).equals(cookieName)) {
                    return cookie.substring(index + 1);
                }
            }
        }
        return null;
    }
}

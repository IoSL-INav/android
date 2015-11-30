package de.tu_berlin.indoornavigation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LoginActivity extends AppCompatActivity {

    // TODO: sample url for test purposes
    private static final String URL = "http://www.google.de/";

    /**
     * Opens web view with TU Berlin login form. After successful redirection (TODO: checks url)
     * saves authentication token from cookie to Shared Preferences, close this activity
     * and open Main Activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // opens web view, set some settings
        WebView webview = new WebView(this);
        setContentView(webview);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setJavaScriptEnabled(true);

        // define web view client, with onPageFinished function
        webview.setWebViewClient(new WebViewClient() {

            /**
             * When page its loaded, checks if the login was successful and saves auth token.
             */
            public void onPageFinished(WebView view, String url) {

                // TODO: change the name of the cookie
                String token = CookieUtils.getCookie(URL, "NID");

                SharedPreferences sharedPreferences = getSharedPreferences(AuthUtils.PREFS_NAME, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("authToken", token);
                editor.commit();

                closeWebView();
            }

        });

        webview.loadUrl(URL);

    }

    /**
     * Deletes cookies, closes web view and opens main activity.
     */
    private void closeWebView() {

        CookieUtils.deleteCookies();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}

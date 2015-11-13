package de.tu_berlin.indornavigation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LoginActivity extends AppCompatActivity {

    private static final String URL = "http://www.google.de/";
    private String token = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        WebView webview = new WebView(this);
        setContentView(webview);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setJavaScriptEnabled(true);

        webview.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url){

                token = Utils.getCookie(URL, "NID");
                System.out.println("we were looking for this value: " + token);
            }

        });

        webview.loadUrl(URL);

    }

}

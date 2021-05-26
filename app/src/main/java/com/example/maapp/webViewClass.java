package com.example.maapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class webViewClass extends AppCompatActivity {
    WebView webView;
    View view;
    Intent intent;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_class);
        webView = findViewById(R.id.webView);
        view = new TMXView(this);

        String link = "";
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                link= null;
            } else {
                link= extras.getString("link");
            }
        } else {
            link= (String) savedInstanceState.getSerializable("link");
        }

        if(!link.isEmpty()){
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webView.setWebViewClient(new WebViewClient() {
                final ProgressDialog prDialog = new ProgressDialog(view.getContext());
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    prDialog.setTitle("Loading, please wait...");
                    prDialog.show();

                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    prDialog.dismiss();
                    super.onPageFinished(view, url);
                }
            });

            webView.loadUrl(link);
        }
    }
}

/// I need a new View to show Progress Dialog, it doesn't work in other case
class TMXView extends View {
    public TMXView(Context context) {
        super(context);
        // Load map
    }
}
package com.eiraj.intel.drone.activities.Dashboard;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.eiraj.intel.drone.R;

public class Video_Play extends AppCompatActivity {

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video__play);

        setupProgressDialog();
        setupWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebView webview = findViewById(R.id.webView);
        webview.setWebViewClient(new Callback());
        webview.getSettings().setJavaScriptEnabled(true);
        String pdf = "http://119.81.99.46/pdf/drone_app_instructions-converted.pdf";
        webview.loadUrl("http://docs.google.com/gview?embedded=true&url=" + pdf);
        //webview.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + pdf); // older method
    }

    private void setupProgressDialog() {
        pd = new ProgressDialog(Video_Play.this);
        pd.setMessage("Loading, please wait...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(
                WebView view, String url) {
            return (false);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (pd.isShowing())
                pd.dismiss();
        }
    }
}

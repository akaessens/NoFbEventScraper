package com.akdev.nofbeventscraper;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ActionBar action_bar = getSupportActionBar();
        if (action_bar != null) {
            action_bar.setDisplayHomeAsUpEnabled(true);
        }

        WebView webview_help = findViewById(R.id.webview_help);

        webview_help.loadUrl("file:////android_asset/help.html");

    }
}

package com.akdev.nofbeventscraper;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ActionBar action_bar = getSupportActionBar();
        if (action_bar != null) {
            action_bar.setDisplayHomeAsUpEnabled(true);
        }

        WebView webview_about = findViewById(R.id.webview_about);

        webview_about.loadUrl("file:////android_asset/about.html");
    }


}

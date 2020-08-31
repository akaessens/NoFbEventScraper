package com.akdev.nofbeventscraper;

import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

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

        int night_mode_flags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (night_mode_flags == Configuration.UI_MODE_NIGHT_YES) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webview_about.getSettings(),
                        WebSettingsCompat.FORCE_DARK_ON);
            }
        }
        webview_about.loadUrl("file:///android_res/raw/about.html");
    }


}

package com.akdev.nofbeventscraper;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class IntentReceiver extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Get data from intent: if launched by other application
         * via "share to" or "open with"
         */
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);

        String data = intent.getDataString();
        String extra = intent.getStringExtra(Intent.EXTRA_TEXT);

        String input = (data != null) ? data : extra;

        Intent main = new Intent(this, MainActivity.class);
        main.putExtra("InputLink", input);

        this.startActivity(main);

        finish();
    }
}
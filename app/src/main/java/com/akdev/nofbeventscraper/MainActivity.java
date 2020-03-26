package com.akdev.nofbeventscraper;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.provider.CalendarContract;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private Button paste_button;
    private Button cancel_button;
    private Button ok_button;

    private TextInputEditText field_uri_input;
    private TextInputEditText field_event_name;
    private TextInputEditText field_event_start;
    private TextInputEditText field_event_end;
    private TextInputEditText field_event_location;
    private TextInputEditText field_event_description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cancel_button = (Button) findViewById(R.id.cancel_button);
        ok_button = (Button) findViewById(R.id.ok_button);
        paste_button = (Button) findViewById(R.id.paste_button);

        field_uri_input = (TextInputEditText) findViewById(R.id.field_uri_input);
        field_event_name = (TextInputEditText) findViewById(R.id.field_event_name);
        field_event_start = (TextInputEditText) findViewById(R.id.field_event_start);
        field_event_end = (TextInputEditText) findViewById(R.id.field_event_end);
        field_event_location = (TextInputEditText) findViewById(R.id.field_event_location);
        field_event_description = (TextInputEditText) findViewById(R.id.field_event_description);

        //final MainActivity mainactivity = this;

        paste_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                if (clipboard != null) {
                    String str = clipboard.getPrimaryClip().getItemAt(0).getText().toString();

                    try {
                        new URL(str).toURI();

                        if (str.substring(0,32).equals("https://www.facebook.com/events/")) {
                            str = str.replace("www.", "m.");
                        }
                        else if (str.substring(0,30).equals("https://m.facebook.com/events/")) {

                        }
                        else {
                            throw new Exception();
                        }

                        field_uri_input.setText(str);

                        startScraping();
                    }
                    catch (Exception e) {
                        clear();
                    }
                }
            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear();
            }
        });

        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // time
                String start_str = field_event_start.getText().insert(22, ":").toString();
                String end_str = field_event_end.getText().insert(22, ":").toString();

                LocalDateTime start = LocalDateTime.parse(start_str, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                LocalDateTime end = LocalDateTime.parse(end_str, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                ZoneId zoneId = ZoneId.systemDefault();
                long start_epoch = start.atZone(zoneId).toEpochSecond() * 1000;
                long end_epoch = end.atZone(zoneId).toEpochSecond() * 1000;

                String name = field_event_name.getText().toString();
                String location = field_event_location.getText().toString();
                String description = field_event_description.getText().toString();
                String uri = field_uri_input.getText().toString();

                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra(CalendarContract.Events.TITLE, field_event_name.getText().toString());
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start_epoch);
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end_epoch);
                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, field_event_location.getText().toString());
                intent.putExtra(CalendarContract.Events.DESCRIPTION, uri + "\n" + description);
                startActivity(intent);
            }
        });

        field_uri_input.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyevent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    startScraping();
                    return true;
                }
                return false;
            }
        });

        Intent intent = getIntent();
        //String action = intent.getAction();
        Uri data = intent.getData();

        if (data != null) {
            // opening external fb link
            field_uri_input.setText(data.toString());
            startScraping();
        }

    }

    public void startScraping() {
        FbScraper scraper = new FbScraper(this, field_uri_input.getText().toString());
        scraper.execute();

    }
    public void clear() {
        field_uri_input.setText("");
        field_event_name.setText("");
        field_event_start.setText("");
        field_event_end.setText("");
        field_event_location.setText("");
        field_event_description.setText("");
    }

    public void update(FbEvent event) {
        field_event_name.setText(event.name);
        field_event_start.setText(event.start_date);
        field_event_end.setText(event.end_date);
        field_event_location.setText(event.location);
        field_event_description.setText(event.description);
    }
}


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/


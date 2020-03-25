package com.akdev.nofbeventscraper;


import android.content.ClipboardManager;
import android.content.Context;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.CalendarContract;
import android.view.Gravity;
import android.view.LayoutInflater;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonShowPopupWindowClick(view);
            }
        });
    }

    @Override
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
    }

    public void onButtonShowPopupWindowClick(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        Button cancel_button = (Button)popupView.findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        Button ok_button = (Button)popupView.findViewById(R.id.ok_button);
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextInputEditText uri_input = (TextInputEditText) popupView.findViewById(R.id.text_input);
                TextInputEditText field_name = (TextInputEditText) popupView.findViewById(R.id.field_event_name);
                TextInputEditText field_start = (TextInputEditText) popupView.findViewById(R.id.field_event_start);
                TextInputEditText field_end = (TextInputEditText) popupView.findViewById(R.id.field_event_end);
                TextInputEditText field_location = (TextInputEditText) popupView.findViewById(R.id.field_event_location);


                // time
                String start_str = field_start.getText().insert(22, ":").toString();
                String end_str = field_end.getText().insert(22, ":").toString();

                LocalDateTime start = LocalDateTime.parse(start_str, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                LocalDateTime end = LocalDateTime.parse(end_str, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                ZoneId zoneId = ZoneId.systemDefault();
                long start_epoch = start.atZone(zoneId).toEpochSecond()*1000;
                long end_epoch = end.atZone(zoneId).toEpochSecond()*1000;

                //Calendar calendar = Calendar.getInstance();


                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra(CalendarContract.Events.TITLE, field_name.getText().toString());
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start_epoch);
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end_epoch);
                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, field_location.getText().toString());
                intent.putExtra(CalendarContract.EXTRA_CUSTOM_APP_URI, uri_input.getText().toString());
                startActivity(intent);

                popupWindow.dismiss();
            }
        });

        Button paste_button = (Button)popupView.findViewById(R.id.paste_button);
        final TextInputEditText text_input = (TextInputEditText) popupView.findViewById(R.id.text_input);

        paste_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                if (clipboard != null) {
                    String str = clipboard.getPrimaryClip().getItemAt(0).getText().toString();

                    str = "https://m.facebook.com/events/2402761143327832/";
                    text_input.setText(str);
                }

                FbScraper scraper = new FbScraper(popupView, text_input.getText().toString());

                FbEvent event;

                scraper.execute();
            }
        });

        /* dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });*/
    }
}

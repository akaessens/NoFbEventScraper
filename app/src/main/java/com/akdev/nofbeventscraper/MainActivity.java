package com.akdev.nofbeventscraper;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.provider.CalendarContract;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


public class MainActivity extends AppCompatActivity {

    private Button paste_button;
    private Button ok_button;

    private TextInputEditText field_uri_input;
    private TextInputEditText field_event_name;
    private TextInputEditText field_event_start;
    private TextInputEditText field_event_end;
    private TextInputEditText field_event_location;
    private TextInputEditText field_event_description;
    private ImageView         toolbar_image_view;
    private CollapsingToolbarLayout toolbar_layout;
    private TextInputLayout   input_layout;

    private FbScraper         scraper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ok_button = (Button) findViewById(R.id.ok_button);
        paste_button = (Button) findViewById(R.id.paste_button);

        field_uri_input = (TextInputEditText) findViewById(R.id.field_uri_input);
        input_layout = (TextInputLayout) findViewById(R.id.textInputLayout);
        field_event_name = (TextInputEditText) findViewById(R.id.field_event_name);
        field_event_start = (TextInputEditText) findViewById(R.id.field_event_start);
        field_event_end = (TextInputEditText) findViewById(R.id.field_event_end);
        field_event_location = (TextInputEditText) findViewById(R.id.field_event_location);
        field_event_description = (TextInputEditText) findViewById(R.id.field_event_description);
        toolbar_image_view = (ImageView) findViewById(R.id.image_view);
        toolbar_layout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);


        ok_button.setEnabled(false);

        toolbar_image_view.setImageResource(R.drawable.ic_banner_foreground);

        AppBarLayout app_bar_layout = (AppBarLayout) findViewById(R.id.app_bar);
        app_bar_layout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    toolbar_layout.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if(isShow) {
                    toolbar_layout.setTitle(" ");
                    isShow = false;
                }
            }
        });

        paste_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    String str = clipboard.getPrimaryClip().getItemAt(0).getText().toString();

                    clear(true);
                    field_uri_input.setText(str);
                }
                catch (NullPointerException e) {
                    e.printStackTrace();
                    error("Error: Clipboard empty");
                }
                startScraping();
            }
        });


        input_layout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear(true);
            }
        });
        input_layout.setErrorIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear(true);
            }
        });

        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Long start_epoch = convertTimeToEpoch(field_event_start.getText().toString());
                    Long end_epoch = convertTimeToEpoch(field_event_end.getText().toString());

                    String name = parseField(field_event_name);

                    String location = parseField(field_event_location);
                    String description = parseField(field_event_description);
                    String uri = parseField(field_uri_input);

                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.putExtra(CalendarContract.Events.TITLE, name);
                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start_epoch);
                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end_epoch);
                    intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
                    intent.putExtra(CalendarContract.Events.DESCRIPTION, uri + "\n\n" + description);
                    startActivity(intent);
                }
                catch (Exception e )
                {
                    e.printStackTrace();
                }

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

        //get data from deep link
        Intent intent = getIntent();
        Uri data = intent.getData();
        String shared_text = intent.getStringExtra(Intent.EXTRA_TEXT);

        if (data != null) {
            // opening external fb link
            field_uri_input.setText(data.toString());
            startScraping();
        }
        else if (shared_text != null) {
            //share to nofb
            field_uri_input.setText(shared_text);
            startScraping();
        }

    }
    private String parseField(TextInputEditText field) {
        try {
            return field.getText().toString();
        }
        catch (Exception e) {
            return null;
        }
    }


    Long convertTimeToEpoch(String time_str) {
        try {
            ZonedDateTime datetime = ZonedDateTime.parse(time_str, DateTimeFormatter.ISO_DATE_TIME);

            return datetime.toEpochSecond() * 1000;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException e) {
            //e.printStackTrace();
            return false;
        }
        return true;
    }
    String checkURI(String str)
    {
        try {

            // check for a valid uri
            new URL(str).toURI();

            if (str.matches(".*(facebook.com/events/[0-9]*).*")) {
                return  str.replaceAll(".*(facebook.com/events/[0-9]*).*",
                        "https://www.$1");
            }
            else {
                error("Error: Invalid URL");
                clear(false);
                return "";
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            error("Error: Invalid URL");
            clear(false);
            return "";
        }
    }

    public void startScraping() {

        error(null);

        try {
            String str = checkURI(field_uri_input.getText().toString());


            if (!str.equals(""))
            {
                field_uri_input.setText(str);
                scraper = new FbScraper(this, field_uri_input.getText().toString());
                scraper.execute();
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            error("Error: Invalid URL");
        }


    }
    public void error(String str) {
        //Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        input_layout.setError(str);
    }
    public void clear(boolean clearUri) {
        if (clearUri) {
            field_uri_input.setText("");
        }
        field_event_name.setText("");
        field_event_name.setError(null);
        field_event_start.setText("");
        field_event_start.setError(null);
        field_event_end.setText("");
        field_event_end.setError(null);
        field_event_location.setText("");
        field_event_location.setError(null);
        field_event_description.setText("");
        field_event_description.setError(null);
        toolbar_image_view.setImageDrawable(null);

        if (scraper!=null)
        {
            scraper.cancel(true);
            scraper = null;
        }
        ok_button.setEnabled(false);
        toolbar_image_view.setImageResource(R.drawable.ic_banner_foreground);
    }

    public void update(FbEvent event) {
        field_event_name.setText(event.name);
        input_layout.setError(null);

        if (event.name.equals(""))
        {
            field_event_name.setError("no event name detected");
        }
        field_event_start.setText(event.start_date);

        if (event.start_date.equals(""))
        {
            field_event_start.setError("no event start date detected");
        }
        field_event_end.setText(event.end_date);

        if (event.end_date.equals(""))
        {
            field_event_end.setError("no event end date detected");
        }

        field_event_location.setText(event.location);

        if (event.location.equals(""))
        {
            field_event_location.setError("no event location detected");
        }
        field_event_description.setText(event.description);

        if (event.description.equals(""))
        {
            field_event_description.setError("no event description detected");
        }


        try {
            Picasso.get().load(event.image_url).into(toolbar_image_view);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        ok_button.setEnabled(true);
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
        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        if (id == R.id.action_help) {
            startActivity(new Intent(this, HelpActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
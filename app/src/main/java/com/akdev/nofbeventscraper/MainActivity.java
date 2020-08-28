package com.akdev.nofbeventscraper;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.Objects;

import static com.akdev.nofbeventscraper.FbEvent.dateTimeToEpoch;

public class MainActivity extends AppCompatActivity {

    protected Button ok_button;
    protected Button paste_button;

    protected TextInputEditText edit_text_uri_input;
    protected TextInputEditText edit_text_event_name;
    protected TextInputEditText edit_text_event_start;
    protected TextInputEditText edit_text_event_end;
    protected TextInputEditText edit_text_event_location;
    protected TextInputEditText edit_text_event_description;

    protected TextInputLayout layout_uri_input;
    protected TextInputLayout layout_event_location;

    protected ImageView image_view_toolbar;
    protected CollapsingToolbarLayout layout_toolbar;

    protected FbScraper scraper;
    protected FbEvent event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ok_button = (Button) findViewById(R.id.ok_button);
        paste_button = (Button) findViewById(R.id.paste_button);

        edit_text_uri_input = (TextInputEditText) findViewById(R.id.edit_text_uri_input);
        edit_text_event_name = (TextInputEditText) findViewById(R.id.edit_text_event_name);
        edit_text_event_start = (TextInputEditText) findViewById(R.id.edit_text_event_start);
        edit_text_event_end = (TextInputEditText) findViewById(R.id.edit_text_event_end);
        edit_text_event_location = (TextInputEditText) findViewById(R.id.edit_text_event_location);
        edit_text_event_description = (TextInputEditText) findViewById(R.id.edit_text_event_description);

        layout_uri_input = (TextInputLayout) findViewById(R.id.layout_uri_input);
        layout_event_location = (TextInputLayout) findViewById(R.id.layout_event_location);
        layout_toolbar = (CollapsingToolbarLayout) findViewById(R.id.layout_toolbar);
        image_view_toolbar = (ImageView) findViewById(R.id.image_view);


        /*
         * Default view settings
         */
        ok_button.setEnabled(false);
        layout_event_location.setEndIconVisible(false);
        image_view_toolbar.setImageResource(R.drawable.ic_banner_foreground);

        /*
         * Display title only when toolbar is collapsed
         */
        AppBarLayout app_bar_layout = (AppBarLayout) findViewById(R.id.app_bar);
        app_bar_layout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean show = true;
            int scroll_range = -1;

            @Override
            public void onOffsetChanged(AppBarLayout app_bar_layout, int vertical_offset) {
                if (scroll_range == -1) {
                    scroll_range = app_bar_layout.getTotalScrollRange();
                }
                if (scroll_range + vertical_offset == 0) {
                    layout_toolbar.setTitle(getString(R.string.app_name));
                    show = true;
                } else if (show) {
                    layout_toolbar.setTitle(" ");
                    show = false;
                }
            }
        });

        /*
         * Paste button: get last entry from clipboard
         */
        paste_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    String str = clipboard.getPrimaryClip().getItemAt(0).getText().toString();

                    clear(true);
                    edit_text_uri_input.setText(str);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    error("Error: Clipboard empty");
                }
                startScraping();
            }
        });

        /*
         * Clear button: delete all text in all fields
         */
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear(true);
            }
        };
        layout_uri_input.setEndIconOnClickListener(listener);
        layout_uri_input.setErrorIconOnClickListener(listener);

        /*
         * Maps button: launch maps intent
         */
        layout_event_location.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String map_search = "geo:0,0?q=" + edit_text_event_location.getText();

                Uri intent_uri = Uri.parse(map_search);
                Intent map_intent = new Intent(Intent.ACTION_VIEW, intent_uri);
                if (map_intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(map_intent);
                }
            }
        });

        /*
         * Add to calendar button: launch calendar application
         */
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Long start_epoch = dateTimeToEpoch(event.start_date);
                Long end_epoch = dateTimeToEpoch(event.end_date);

                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra(CalendarContract.Events.TITLE, event.name);
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start_epoch);
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end_epoch);
                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.location);

                // prepend url in description
                String desc = event.url + "\n\n" + event.description;
                intent.putExtra(CalendarContract.Events.DESCRIPTION, desc);
                startActivity(intent);
            }
        });

        /*
         * Enter button in uri input: start scraping
         */
        edit_text_uri_input.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keycode, KeyEvent keyevent) {
                //If the key event is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keycode == KeyEvent.KEYCODE_ENTER)) {
                    startScraping();
                    return true;
                }
                return false;
            }
        });


        /*
         * Get data from intent: if launched by other application
         * via "share to" or "open with"
         */
        Intent intent = getIntent();
        Uri data = intent.getData();
        String shared_text = intent.getStringExtra(Intent.EXTRA_TEXT);

        if (data != null) {
            // opening external fb link
            edit_text_uri_input.setText(data.toString());
            startScraping();
        } else if (shared_text != null) {
            //share to nofb
            edit_text_uri_input.setText(shared_text);
            startScraping();
        }

    }

    /**
     * launch the FbScraper asynchronous task with the current text in the input text field.
     */
    public void startScraping() {

        error(null);

        String url = Objects.requireNonNull(edit_text_uri_input.getText()).toString();
        scraper = new FbScraper(new WeakReference<>(this), url);
        scraper.execute();
    }

    public void error(String str) {
        layout_uri_input.setError(str);
    }

    /**
     * Clears all event text field strings and errors and also the input field depending if wanted.
     * Loads the default banner into the toolbar image view and disables unneeded buttons.
     *
     * @param clear_uri Choose whether to clear the input uri field, too
     */
    public void clear(boolean clear_uri) {

        if (clear_uri) {
            edit_text_uri_input.setText("");
            layout_uri_input.setError(null);
        }
        edit_text_event_name.setText("");
        edit_text_event_start.setText("");
        edit_text_event_end.setText("");
        edit_text_event_location.setText("");
        edit_text_event_description.setText("");

        edit_text_event_name.setError(null);
        edit_text_event_start.setError(null);
        edit_text_event_end.setError(null);
        edit_text_event_location.setError(null);
        edit_text_event_description.setError(null);

        try {
            scraper.cancel(true);
            scraper = null;
        } catch (Exception e) {
            e.printStackTrace();
        }


        ok_button.setEnabled(false);
        layout_event_location.setEndIconVisible(false);
        image_view_toolbar.setImageResource(R.drawable.ic_banner_foreground);
    }

    /**
     * Updates the text fields with the event information provided.
     * If something is missing, the corresponding test field will show an error.
     *
     * @param scraped_event the event information that was scraped by FbScraper
     */
    public void update(FbEvent scraped_event) {

        this.event = scraped_event;

        edit_text_uri_input.setText(event.url);

        if (event.name.equals("")) {
            edit_text_event_name.setError("no event name detected");
        } else {
            edit_text_event_name.setText(event.name);
        }

        if (event.start_date == null) {
            edit_text_event_start.setError("no event start date detected");
        } else {
            String str = FbEvent.dateTimeToString(event.start_date);
            edit_text_event_start.setText(str);
        }

        if (event.end_date == null) {
            edit_text_event_end.setError("no event end date detected");
        } else {
            String str = FbEvent.dateTimeToString(event.end_date);
            edit_text_event_end.setText(str);
        }

        if (event.location.equals("")) {
            edit_text_event_location.setError("no event location detected");
        } else {
            edit_text_event_location.setText(event.location);
            layout_event_location.setEndIconVisible(true);
        }

        if (event.description.equals("")) {
            edit_text_event_description.setError("no event description detected");
        } else {
            edit_text_event_description.setText(event.description);
        }

        Picasso.get()
                .load(event.image_url)
                .placeholder(R.drawable.ic_banner_foreground)
                .into(image_view_toolbar);

        ok_button.setEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }
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
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
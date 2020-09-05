package com.akdev.nofbeventscraper;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

import static com.akdev.nofbeventscraper.FbEvent.createEventList;
import static com.akdev.nofbeventscraper.FbEvent.dateTimeToEpoch;

public class MainActivity extends AppCompatActivity {

    protected Button ok_button;
    protected ExtendedFloatingActionButton paste_button;

    protected TextInputEditText edit_text_uri_input;

    protected TextInputLayout layout_uri_input;


    protected FbScraper scraper;
    protected List<FbEvent> events;
    EventAdapter adapter;
    LinearLayoutManager linear_layout_manager;

    @Override
    public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        if (! state.getBoolean("events_empty") ) {
            startScraping();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        state.putBoolean("events_empty", events.isEmpty());


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit_text_uri_input = (TextInputEditText) findViewById(R.id.edit_text_uri_input);
        layout_uri_input = (TextInputLayout) findViewById(R.id.layout_uri_input);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ok_button = (Button) findViewById(R.id.ok_button);
        paste_button = (ExtendedFloatingActionButton) findViewById(R.id.paste_button);
        ok_button.setEnabled(false);

        /*
         * initialize recycler view with empty list of events
         * scroll horizontal with snapping
         */
        RecyclerView recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        events = createEventList();
        adapter = new EventAdapter(events);
        recycler_view.setAdapter(adapter);
        linear_layout_manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler_view.setLayoutManager(linear_layout_manager);
        recycler_view.setHasFixedSize(true);
        SnapHelper snap_helper = new LinearSnapHelper();
        snap_helper.attachToRecyclerView(recycler_view);


        /*
         * Paste button: get last entry from clipboard
         */
        paste_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    String str = Objects.requireNonNull(clipboard.getPrimaryClip())
                            .getItemAt(0).getText().toString();

                    clear(true);
                    edit_text_uri_input.setText(str);

                    startScraping();
                } catch (Exception e) {
                    e.printStackTrace();
                    error(R.string.error_clipboard_empty);
                }
            }
        });

        /*
         * Clear button: delete all events
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
         * Add to calendar button: launch calendar application with current event
         */
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FbEvent event = events.get(linear_layout_manager.findFirstCompletelyVisibleItemPosition());

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

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
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

    public void error(Integer resId) {
        if (resId != null) {
            layout_uri_input.setError(getString(resId));
        } else {
            layout_uri_input.setError(null);
        }
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

        if (scraper != null) {
            scraper.cancel(true);
            scraper = null;
        }

        this.events.clear();
        adapter.notifyDataSetChanged();

        ok_button.setEnabled(false);
    }

    /**
     * Updates the text fields with the event information provided.
     * If something is missing, the corresponding test field will show an error.
     *
     * @param events the event information that was scraped by FbScraper
     */
    public void update(List<FbEvent> events) {

        this.events.clear();
        this.events.addAll(events);

        adapter.notifyDataSetChanged();

        /*Picasso.get()
                .load(event.image_url)
                .placeholder(R.drawable.ic_banner_foreground)
                .into(image_view_toolbar);*/

        ok_button.setEnabled(true);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (menu instanceof MenuBuilder) {
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
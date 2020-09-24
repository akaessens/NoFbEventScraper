package com.akdev.nofbeventscraper;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

import static com.akdev.nofbeventscraper.FbEvent.createEventList;

public class MainActivity extends AppCompatActivity {

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

        if (!state.getBoolean("events_empty")) {
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

        edit_text_uri_input = findViewById(R.id.edit_text_uri_input);
        layout_uri_input = findViewById(R.id.layout_uri_input);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        paste_button = findViewById(R.id.paste_button);

        /*
         * initialize recycler view with empty list of events
         * scroll horizontal with snapping
         */
        RecyclerView recycler_view = findViewById(R.id.recycler_view);
        events = createEventList();
        adapter = new EventAdapter(events);
        recycler_view.setAdapter(adapter);
        linear_layout_manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler_view.setLayoutManager(linear_layout_manager);
        recycler_view.setHasFixedSize(true);


        /*
         * Display title only when toolbar is collapsed
         */
        AppBarLayout app_bar_layout = findViewById(R.id.app_bar);

        app_bar_layout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean show = true;
            int scroll_range = -1;
            CollapsingToolbarLayout layout_toolbar = findViewById(R.id.layout_toolbar);

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
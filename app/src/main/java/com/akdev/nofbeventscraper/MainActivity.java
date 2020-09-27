package com.akdev.nofbeventscraper;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;

import static com.akdev.nofbeventscraper.FbEvent.createEventList;

public class MainActivity extends AppCompatActivity {

    protected ExtendedFloatingActionButton paste_button;
    protected TextInputEditText edit_text_uri_input;
    protected TextInputLayout layout_uri_input;


    protected FbScraper scraper;
    protected List<FbEvent> events;
    EventAdapter adapter;
    LinearLayoutManager linear_layout_manager;

    private List<FbEvent> getSavedEvents() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Gson gson = new Gson();
        String json = prefs.getString("events", "");

        Type event_list_type = new TypeToken<List<FbEvent>>() {
        }.getType();
        List<FbEvent> list = gson.fromJson(json, event_list_type);

        if (list == null) {
            list = createEventList();
        }

        return list;
    }

    /**
     * Callback for Restoring data
     */
    @Override
    public void onResume() {
        super.onResume();

        events.clear();
        events.addAll(getSavedEvents());
        adapter.notifyDataSetChanged();

        Intent intent = getIntent();
        String data = intent.getStringExtra("InputLink");

        if (data != null) {
            intent.removeExtra("InputLink");
            edit_text_uri_input.setText(data);
            startScraping();
        }
    }

    /**
     * Save events list to SharedPreferences as JSON
     */
    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefs_edit = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(events);
        prefs_edit.putString("events", json);
        prefs_edit.apply();
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
         * initialize recycler view with saved list of events
         * scroll horizontal with snapping
         */
        RecyclerView recycler_view = findViewById(R.id.recycler_view);
        this.events = getSavedEvents();
        adapter = new EventAdapter(events);
        recycler_view.setAdapter(adapter);
        linear_layout_manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler_view.setLayoutManager(linear_layout_manager);

        recycler_view.setItemAnimator(new FadeInAnimator());



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

                    edit_text_uri_input.setText(str);

                    startScraping();
                } catch (Exception e) {
                    e.printStackTrace();
                    input_helper(getString(R.string.error_clipboard_empty), true);
                }
            }
        });

        /*
         * Error in input: clear input on click
         */
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                input_helper(getString(R.string.helper_add_link), true);
                edit_text_uri_input.setText(null);
                if (scraper != null) {
                    scraper.killAllTasks();
                }
                input_helper(getString(R.string.helper_add_link), false);
            }
        };
        layout_uri_input.setErrorIconOnClickListener(listener);
        layout_uri_input.setEndIconOnClickListener(listener);


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


    }

    /**
     * launch the FbScraper asynchronous task with the current text in the input text field.
     */
    public void startScraping() {

        input_helper(null, false);

        String url = Objects.requireNonNull(edit_text_uri_input.getText()).toString();

        scraper = new FbScraper(new WeakReference<>(this), url);

        scraper.run();
    }

    public void input_helper(String str, boolean error) {

        if (str == null) {
            str = " ";
        } // keep spacing

        if (error) {
            layout_uri_input.setError(str);
        } else {
            layout_uri_input.setError(null);
            layout_uri_input.setHelperText(str);
        }
    }

    /**
     * Adds new events to the start of the events list.
     *
     * @param new_event the event that was scraped by FbScraper
     */
    public void addEvent(FbEvent new_event) {

        if (new_event != null) {
            this.events.add(0, new_event);
            this.adapter.notifyItemInserted(0);
        }
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

        int id = item.getItemId();

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
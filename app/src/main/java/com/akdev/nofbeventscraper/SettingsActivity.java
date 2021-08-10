package com.akdev.nofbeventscraper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar action_bar = getSupportActionBar();
        if (action_bar != null) {
            action_bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            /*
             * reset events click action: delete saved events and display snackbar
             */
            Preference button = findPreference("event_reset");
            if (button != null) {
                button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {

                        final SharedPreferences prefs = preference.getSharedPreferences();

                        final String events = prefs.getString("events", "");
                        prefs.edit().remove("events").apply();

                        final String history = prefs.getString("history", "");
                        prefs.edit().remove("history").apply();

                        Snackbar.make(getActivity().findViewById(android.R.id.content),
                                getString(R.string.preferences_event_snackbar), Snackbar.LENGTH_SHORT)
                                .setAction(R.string.undo, new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        prefs.edit().putString("events", events).apply();
                                        prefs.edit().putString("history", history).apply();
                                    }
                                }).show();

                        return true;
                    }
                });
            }

        }
    }
}
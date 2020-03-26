package com.akdev.nofbeventscraper;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class FbScraper extends AsyncTask<Void, Void, Void> {

    private String url;
    private String error;
    private MainActivity main;
    public FbEvent event;

    public FbScraper(MainActivity main, String url) {
        this.url = url;
        this.main = main;

    }

    @Override
    protected Void doInBackground(Void... voids) {

        Document document = null;

        try {
            document = Jsoup.connect(url).get();

            try {
                String json = document.select("script[type = application/ld+json]").first().data();

                JSONObject reader = new JSONObject(json);

                String event_name = reader.getString("name");
                String event_start = reader.getString("startDate");
                String event_end = reader.getString("endDate");
                String event_description = reader.getString("description");
                String location = reader.getJSONObject("location").getString("name");

                //String image_url = reader.getString("image");

                if (event_name == null || event_start == null || event_end == null) {
                    this.event = null;
                    throw new Exception();
                } else {
                    this.event = new FbEvent(event_name, event_start, event_end, event_description, location, null);
                }

            } catch (Exception e) {
                e.printStackTrace();
                this.error = "Error: Scraping event data failed";
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.error = "Error: URL not available";
        }

        return null;
    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
    }

    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (this.event != null) {
            this.main.update(event);
        }
        else {
            main.toast(error);
            this.main.clear(false);
        }
    }
}


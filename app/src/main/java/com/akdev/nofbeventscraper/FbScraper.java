package com.akdev.nofbeventscraper;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.SpannableStringBuilder;
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
    private FbEvent event;

    FbScraper(MainActivity main, String url) {
        this.url = url;
        this.main = main;

    }
    private String readFromLocJson(String location_json) {

        String name = "";
        String street_address = "";
        String postal_code = "";
        String address_locality = "";
        try {
            JSONObject reader = new JSONObject(location_json);

            name = reader.getString("name");

            JSONObject address = reader.getJSONObject("address");
            street_address = ", " + address.getString("streetAddress");
            postal_code = ", " + address.getString("postalCode");
            address_locality = " " + address.getString("addressLocality");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return name +  street_address + postal_code + address_locality;
    }

    private String fixTimezone(String time_in) {

        try {

            Editable editable = new SpannableStringBuilder(time_in);

            return editable.insert(22, ":").toString();

        } catch (Exception e) {
            return null;
        }
    }

    private String readFromJson(JSONObject reader, String field) {
        try {
            return reader.getString(field);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    protected Void doInBackground(Void... voids) {

        Document document = null;

        try {
            document = Jsoup.connect(url).get();

            try {
                String json = document.select("script[type = application/ld+json]").first().data();

                JSONObject reader = new JSONObject(json);

                String event_name = readFromJson(reader, "name");
                String event_start = fixTimezone(readFromJson(reader, "startDate"));
                String event_end = fixTimezone(readFromJson(reader, "endDate"));

                String event_description = readFromJson(reader, "description");
                String location_json = readFromJson(reader, "location");


                String location = readFromLocJson(location_json);

                if (event_name == null) {
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


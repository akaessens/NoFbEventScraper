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
    protected String fixLocation(String location_json) {

        String name = "";

        try {
            JSONObject reader = new JSONObject(location_json);

            name = reader.getString("name");
            JSONObject address = reader.getJSONObject("address");

            String type = address.getString("@type");

            if (type.equals("PostalAddress"))
            {
                String postal_code = address.getString("postalCode");
                String address_locality = address.getString("addressLocality");
                String address_country = address.getString("addressCountry");
                String street_address = address.getString("streetAddress");

                return name + ", " + street_address + ", " + postal_code + " " + address_locality;
            }
            else
            {
                return name;
            }


        } catch (JSONException e) {
            e.printStackTrace();
            return name;
        }
    }

    protected String fixTimezone(String time_in) {

        try {

            Editable editable = new SpannableStringBuilder(time_in);

            return editable.insert(22, ":").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    protected String fixLinks(String description_in) {
        try {
            // @[152580919265:274:MagentaMusik 360] -> m.facebook.com/152580919265
            return description_in.replaceAll("@\\[([0-9]{10,}):[0-9]{3}:([^\\]]*)\\]", "m.facebook.com/$1 ($2)");

        } catch (Exception e) {
            e.printStackTrace();
            return description_in;
        }
    }

    private String readFromJson(JSONObject reader, String field) {
        try {
            return reader.getString(field);
        }
        catch (Exception e) {
            e.printStackTrace();
            return "";
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

                String event_description = fixLinks(readFromJson(reader, "description"));
                String location = fixLocation(readFromJson(reader, "location"));

                String image_url = "";

                try {
                    image_url = readFromJson(reader, "image"); // get from json

                    // get from event header
                    image_url = document.getElementsByClass("scaledImageFitWidth").first().attr("src");
                } catch (Exception e) {
                    e.printStackTrace();
                    this.error = "Error: no image found";
                }

                if (event_name == null) {
                    this.event = null;
                    throw new Exception();
                } else {
                    this.event = new FbEvent(event_name, event_start, event_end, event_description, location, image_url);
                    //this.event = new FbEvent("", "", "", "", "", "");
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


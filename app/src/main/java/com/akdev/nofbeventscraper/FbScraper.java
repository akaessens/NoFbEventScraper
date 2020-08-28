package com.akdev.nofbeventscraper;

import android.os.AsyncTask;
import android.text.Editable;
import android.text.SpannableStringBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FbScraper extends AsyncTask<Void, Void, Void> {

    private String error;
    private String input_str;
    private WeakReference<MainActivity> main; // no context leak with WeakReference
    private FbEvent event;

    FbScraper(WeakReference<MainActivity> main, String str) {
        this.main = main;
        this.input_str = str;
    }

    protected String fixURI(String str) throws URISyntaxException, MalformedURLException {

        // check for url format
        new URL(str).toURI();

        Pattern pattern = Pattern.compile("(facebook.com/events/[0-9]*)");
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            // rewrite url to m.facebook and dismiss any query strings or referrals
            return "https://m." + matcher.group(1);
        } else {
            throw new URISyntaxException(str, "Does not contain event.");
        }

    }

    protected String fixLocation(String location_json) {

        String location_name = "";

        try {
            JSONObject reader = new JSONObject(location_json);

            location_name = reader.getString("name");
            JSONObject address = reader.getJSONObject("address");

            String type = address.getString("@type");

            if (type.equals("PostalAddress")) {
                String postal_code = address.getString("postalCode");
                String address_locality = address.getString("addressLocality");
                String street_address = address.getString("streetAddress");
                // included in locality
                //String address_country = address.getString("addressCountry");


                return location_name + ", "
                        + street_address + ", "
                        + postal_code + " "
                        + address_locality;
            } else {
                return location_name;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return location_name;
        }
    }

    protected ZonedDateTime toZonedDateTime(String time_in) {

        try {
            // time in is missing a : in the timezone offset
            Editable editable = new SpannableStringBuilder(time_in);
            String time_str = editable.insert(22, ":").toString();

            // parse e.g. 2011-12-03T10:15:30+01:00
            return ZonedDateTime.parse(time_str, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected String fixDescriptionLinks(String description_in) {
        try {
            /* @[152580919265:274:SiteDescription]
             * to
             * SiteDescription [m.facebook.com/152580919265] */

            return description_in.replaceAll("@\\[([0-9]{10,}):[0-9]{3}:([^]]*)]",
                    "$2 [m.facebook.com/$1]");

        } catch (Exception e) {
            e.printStackTrace();
            return description_in;
        }
    }

    private String readFromJson(JSONObject reader, String field) {
        try {
            return reader.getString(field);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {

        try {
            String url = fixURI(input_str);
            // useragent needed with Jsoup > 1.12
            Document document = Jsoup.connect(url).userAgent("Mozilla").get();
            String json = document
                    .select("script[type = application/ld+json]")
                    .first().data();

            JSONObject reader = new JSONObject(json);

            event = new FbEvent();
            event.url = url;
            event.name = readFromJson(reader, "name");
            event.start_date = toZonedDateTime(readFromJson(reader, "startDate"));
            event.end_date = toZonedDateTime(readFromJson(reader, "endDate"));
            event.description = fixDescriptionLinks(readFromJson(reader, "description"));
            event.location = fixLocation(readFromJson(reader, "location"));
            event.image_url = readFromJson(reader, "image");

        } catch (URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
            this.error = "Error: URL invalid.";
        } catch (JSONException e) {
            e.printStackTrace();
            this.error = "Error: Scraping event data failed";
        } catch (IOException e) {
            e.printStackTrace();
            this.error = "Error: Unable to connect.";
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
            main.get().update(event);
        } else {
            main.get().error(error);
            main.get().clear(false);
        }
    }
}


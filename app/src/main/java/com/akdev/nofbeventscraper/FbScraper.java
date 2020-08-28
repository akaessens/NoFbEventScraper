package com.akdev.nofbeventscraper;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.SpannableStringBuilder;

import androidx.preference.PreferenceManager;

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

/**
 * This class can asynchronously scrape public facebook events
 * and gather the most important information. It is stored in a FbEvent object.
 */
public class FbScraper extends AsyncTask<Void, Void, Void> {

    private String error;
    private String input_url;
    private WeakReference<MainActivity> main; // no context leak with WeakReference
    private FbEvent event;

    /**
     * Constructor with WeakReference to the main activity, to update it's text fields.
     *
     * @param main WeakReference of main activity to prevent context leak
     * @param input_url Input url to scrape from
     */
    FbScraper(WeakReference<MainActivity> main, String input_url) {
        this.main = main;
        this.input_url = input_url;
    }

    /**
     * Strips the facebook event link of the input url.
     *
     * @param url input url
     * @return facebook event url String if one was found
     * @throws URISyntaxException if event not found
     * @throws MalformedURLException
     */
    protected String fixURI(String url) throws URISyntaxException, MalformedURLException {

        // check for url format
        new URL(url).toURI();

        String regex = "(facebook.com/events/[0-9]*)(/\\?event_time_id=[0-9]*)?";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {

            String url_prefix = "https://m.";
            if (main != null){
                SharedPreferences shared_prefs = PreferenceManager.getDefaultSharedPreferences(main.get());
                url_prefix = shared_prefs.getString("url_preference", url_prefix);
            }

            // rewrite url to m.facebook and dismiss any query strings or referrals
            String ret = url_prefix + matcher.group(1);
            if (matcher.group(2) != null) {
                ret += matcher.group(2);
            }
             return ret;
        } else {
            throw new URISyntaxException(url, "Does not contain event.");
        }

    }

    /**
     * Strips the event location from the json string.
     * This can be a name only or a complete postal address.
     * @param location_json JSON formatted string
     * @return String representation of the location.
     */
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

    /**
     * Parses a time string from the facebook event.
     * Corrects format to ISO date time and parse into ZonedDateTime
     *
     * @param time_in time string from the event
     * @return ZonedDateTime parsed from input or null
     */
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

    /**
     * Replaces all occurrences of a facebook internal links in
     * an event description into an actual URL.
     *
     * @param description_in description string from the event
     * @return corrected String with internal links resolved
     */
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

    /**
     * Read a single field from a JSONObject
     * @param reader JSONObject to read from
     * @param field Which field to read
     * @return String of the value of the field or empty string
     */
    private String readFromJson(JSONObject reader, String field) {
        try {
            return reader.getString(field);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Started by scraper.execute().
     * Gets the HTML doc from the input string and scrapes the event information from it.
     * @param voids
     * @return
     */
    @Override
    protected Void doInBackground(Void... voids) {

        try {
            String url = fixURI(input_url);
            // useragent needed with Jsoup > 1.12
            Document document = Jsoup.connect(url).userAgent("Mozilla").get();
            String json = document
                    .select("script[type = application/ld+json]")
                    .first().data();

            JSONObject reader = new JSONObject(json);

            event = new FbEvent(
                    url,
                    readFromJson(reader, "name"),
                    toZonedDateTime(readFromJson(reader, "startDate")),
                    toZonedDateTime(readFromJson(reader, "endDate")),
                    fixDescriptionLinks(readFromJson(reader, "description")),
                    fixLocation(readFromJson(reader, "location")),
                    readFromJson(reader, "image")
            );

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

    /**
     * When scraping is finished, main activity will be updated.
     * If an error occurred, main activity is given an error string.
     * @param aVoid
     */
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (main != null) {
            if (this.event != null) {
                main.get().update(event);
            } else {
                main.get().error(error);
                main.get().clear(false);
            }
        }
    }
}


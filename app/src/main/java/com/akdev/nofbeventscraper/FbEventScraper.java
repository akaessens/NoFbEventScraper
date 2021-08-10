package com.akdev.nofbeventscraper;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class can asynchronously scrape public facebook events
 * and gather the most important information. It is stored in a FbEvent object.
 */
public class FbEventScraper extends AsyncTask<Void, Void, Void> {

    private FbScraper scraper;
    private int error;
    private String url;
    private FbEvent event;

    /**
     * Constructor with reference to scraper to return results.
     *
     * @param scraper   Reference to FbScraper
     * @param input_url Input url to scrape from
     */
    FbEventScraper(FbScraper scraper, String input_url) {

        this.scraper = scraper;
        this.url = input_url;
        this.error = 0;
    }

    /**
     * Strips the event location from the json string.
     * This can be a name only or a complete postal address.
     *
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
     * Parses a time string from the facebook event into a Date
     *
     * @param time_in time string from the event
     * @return Date parsed from input or null
     */
    protected Date parseToDate(String time_in) {

        try {
            // parse e.g. 2011-12-03T10:15:30+0100
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());

            return sdf.parse(time_in);

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
     *
     * @param reader JSONObject to read from
     * @param field  Which field to read
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
     * Started by execute().
     * Gets the HTML doc from the input string and scrapes the event information from it.
     *
     * @param voids
     * @return
     */
    @Override
    protected Void doInBackground(Void... voids) {

        Log.d("scraperLog", "doInBackground: "+url);

        try {
            Document document = DocumentReceiver.getDocument(url);
            if (document == null) {
                throw new IOException();
            }

            String name = "", location = "", description = "", image_url = "";
            Date start_date = null, end_date = null;
            try {

                String json = document
                        .select("script[type = application/ld+json]")
                        .first().data();

                JSONObject reader = new JSONObject(json);

                // get all fields from json event information
                name = readFromJson(reader, "name");
                start_date = parseToDate(readFromJson(reader, "startDate"));
                end_date = parseToDate(readFromJson(reader, "endDate"));
                description = fixDescriptionLinks(readFromJson(reader, "description"));
                location = fixLocation(readFromJson(reader, "location"));
                image_url = readFromJson(reader, "image");

                // try to find a high-res image
                try {
                    image_url = document.select("div[id=event_header_primary]")
                            .select("img").first().attr("src");
                } catch (Exception ignored) {
                }

            } catch (JSONException | NullPointerException e) {
                // json event information mot found. get at least title and image
                name = document.title();
                description = scraper.main.get().getString(R.string.error_scraping);
                try {
                    image_url = document.select("div[id*=event_header]")
                            .select("img").first().attr("src");
                } catch (Exception ignored) {
                }
            }

            this.event = new FbEvent(url, name, start_date, end_date, description, location, image_url);

        } catch (HttpStatusException e) {
            this.error = R.string.error_url;
        }
        catch (IOException e) {
            e.printStackTrace();
            this.error = R.string.error_connection;
        } catch (Exception e) {
            e.printStackTrace();
            this.error = R.string.error_unknown;
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * When scraping is finished, the scraper callback will receive the event.
     *
     * @param aVoid
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        this.scraper.scrapeEventResultCallback(this.event, this.error);
    }
}


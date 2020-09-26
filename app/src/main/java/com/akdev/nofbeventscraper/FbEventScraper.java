package com.akdev.nofbeventscraper;

import android.content.SharedPreferences;
import android.os.AsyncTask;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.akdev.nofbeventscraper.FbEvent.createEventList;

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
     * Constructor with WeakReference to the main activity, to update it's text fields.
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

        try {
            // use default android user agent
            String user_agent = "Mozilla/5.0 (X11; Linux x86_64)";
            Document document = Jsoup.connect(url).userAgent(user_agent).get();

            if (document == null) {
            }
            String json = document
                    .select("script[type = application/ld+json]")
                    .first().data();

            JSONObject reader = new JSONObject(json);


            String name = readFromJson(reader, "name");
            Date start_date = parseToDate(readFromJson(reader, "startDate"));
            Date end_date = parseToDate(readFromJson(reader, "endDate"));
            String description = fixDescriptionLinks(readFromJson(reader, "description"));
            String location = fixLocation(readFromJson(reader, "location"));

            String image_url = readFromJson(reader, "image"); // get from json

            try {
                // possibly get higher res image from event header
                image_url = document.select("div[id=event_header_primary]")
                        .select("img").first().attr("src");

            } catch (Exception e) {
                // ignore
            }

            this.event = new FbEvent(url, name, start_date, end_date, description, location, image_url);

        } catch (JSONException e) {
            e.printStackTrace();
            this.error = R.string.error_scraping;
        } catch (IOException e) {
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
     * When scraping is finished, the scraper callback will receive the Event.
     * @param aVoid
     */
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        this.scraper.scrapeEventResultCallback(this.event, this.error);
    }
}


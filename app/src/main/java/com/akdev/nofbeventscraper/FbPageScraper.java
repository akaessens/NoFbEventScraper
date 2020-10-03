package com.akdev.nofbeventscraper;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class can asynchronously scrape public facebook pages for event ids
 * It returns a String list of event urls
 */
public class FbPageScraper extends AsyncTask<Void, Void, Void> {

    private FbScraper scraper;
    private int error;
    private String url;
    private List<String> event_links = new ArrayList<>();

    /**
     * Constructor with reference to scraper to return results.
     *
     * @param scraper  Reference to FbScraper
     * @param page_url Input url to scrape from
     */
    FbPageScraper(FbScraper scraper, String page_url) {

        this.scraper = scraper;
        this.url = page_url;
        this.error = 0;
    }

    /**
     * Started by execute().
     * Gets the HTML doc from the input string and scrapes the event links from it.
     *
     * @param voids
     * @return
     */
    @Override
    protected Void doInBackground(Void... voids) {


        do {
            try {
                // use default android user agent
                String user_agent = "Mozilla/5.0 (X11; Linux x86_64)";
                Document document = Jsoup.connect(url).userAgent(user_agent).get();

                if (document == null) {
                    throw new IOException();
                }

                /*
                 * get all event id's from current url and add to the list
                 */
                String regex = "(/events/[0-9]*)(/\\?event_time_id=[0-9]*)?";

                List<String> event_links_href = document
                        .getElementsByAttributeValueMatching("href", Pattern.compile(regex))
                        .eachAttr("href");

                for (String event_id : event_links_href) {
                    this.event_links.add("https://mbasic.facebook.com" + event_id);
                }

                /*
                 * check if more events should be scraped
                 */
                SharedPreferences shared_prefs = PreferenceManager
                        .getDefaultSharedPreferences(scraper.main.get());

                int max = shared_prefs.getInt("page_event_max", 5);

                if (event_links.size() < max) {
                    // find "next page
                    try {
                        String next_url = document
                                .getElementsByAttributeValueMatching("href", "has_more=1")
                                .first().attr("href");

                        this.url = "https://mbasic.facebook.com" + next_url;
                    } catch (NullPointerException e) {
                        url = null;
                        event_links = event_links.subList(0, max);
                    }


                } else {
                    url = null;
                    event_links = event_links.subList(0, max);
                }

            } catch (IOException e) {
                e.printStackTrace();
                this.error = R.string.error_connection;
            } catch (Exception e) {
                e.printStackTrace();
                this.error = R.string.error_unknown;
            }
        } while (url != null);

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * When scraping is finished, the scraper callback will receive the link list.
     *
     * @param aVoid
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        this.scraper.scrapePageResultCallback(this.event_links, this.error);
    }
}


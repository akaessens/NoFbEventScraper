package com.akdev.nofbeventscraper;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.akdev.nofbeventscraper.FbEvent.createEventList;

public class FbScraper {

    protected List<FbEvent> events;
    url_type_enum url_type = url_type_enum.EVENT;
    private int error;
    private String input_url;
    private WeakReference<MainActivity> main; // no context leak with WeakReference

    /**
     * Constructor with WeakReference to the main activity, to add events.
     *
     * @param main      WeakReference of main activity to prevent context leak
     * @param input_url Input url to scrape from
     */
    FbScraper(WeakReference<MainActivity> main, String input_url) {
        this.main = main;
        this.input_url = input_url;
        this.events = createEventList();

        run();
    }

    protected String getPageUrl(String url) throws URISyntaxException, MalformedURLException {
        throw new URISyntaxException(url, "not implemented");
    }

    /**
     * Strips the facebook event link of the input url.
     *
     * @param url input url
     * @return facebook event url String if one was found
     * @throws URISyntaxException    if event not found
     * @throws MalformedURLException
     */
    protected String getEventUrl(String url) throws URISyntaxException, MalformedURLException {

        // check for url format
        new URL(url).toURI();

        String regex = "(facebook.com/events/[0-9]*)(/\\?event_time_id=[0-9]*)?";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {

            String url_prefix = "https://m.";
            if (main != null) {
                SharedPreferences shared_prefs = PreferenceManager.getDefaultSharedPreferences(main.get());
                url_prefix = shared_prefs.getString("url_preference", url_prefix);
            }

            // rewrite url to m.facebook and dismiss any query strings or referrals
            String ret = url_prefix + matcher.group(1);
            if (matcher.group(2) != null) {
                // add event time identifier
                ret += matcher.group(2);
            }
            return ret;
        } else {
            throw new URISyntaxException(url, "Does not contain event.");
        }

    }

    void scrapeEvent(String event_url) {
        FbEventScraper scraper = new FbEventScraper(this, event_url);
        scraper.execute();
    }

    void scrapeEventResultCallback(FbEvent event, int error) {

        if (url_type == url_type_enum.EVENT) {
            if (event != null) {
                main.get().addEvent(event);
                main.get().input_helper(R.string.done, false);
            } else {
                main.get().input_helper(error, true);
            }
        } else {
            main.get().addEvent(event);
        }
    }

    void scrapePage(String page_url) {
        /*
        FbPageScraper scraper = new FbPageScraper(this, page_url);

        scraper.execute();
         */
    }

    protected void scrapePageResultCallback(String[] event_urls, int error) {

        if (event_urls != null) {
            for (String event_url : event_urls) {
                scrapeEvent(event_url);
            }
        } else if (url_type == url_type_enum.PAGE) {
            main.get().input_helper(error, true);
        }
    }

    void run() {

        try {
            String event_url = getEventUrl(input_url);
            url_type = url_type_enum.EVENT;
            scrapeEvent(event_url);

            return;

        } catch (URISyntaxException | MalformedURLException e) {
            url_type = url_type_enum.INVALID;
        }

        try {
            String page_url = getPageUrl(input_url);
            url_type = url_type_enum.PAGE;
            scrapePage(page_url);

        } catch (URISyntaxException | MalformedURLException e) {
            url_type = url_type_enum.INVALID;
            main.get().input_helper(R.string.error_url, true);
        }
    }


    enum url_type_enum {EVENT, PAGE, INVALID}
}
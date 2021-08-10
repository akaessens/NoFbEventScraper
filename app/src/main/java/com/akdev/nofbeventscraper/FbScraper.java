package com.akdev.nofbeventscraper;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FbScraper {

    protected List<AsyncTask> tasks;
    protected WeakReference<MainActivity> main; // no context leak with WeakReference
    url_type_enum url_type = url_type_enum.EVENT;
    private String input_url;

    /**
     * Constructor with WeakReference to the main activity, to add events.
     *
     * @param main      WeakReference of main activity to prevent context leak
     * @param input_url Input url to scrape from
     */
    FbScraper(WeakReference<MainActivity> main, String input_url) {
        this.main = main;
        this.input_url = input_url;
        this.tasks = new ArrayList<>();
    }

    protected String getShortened(String url) throws IOException, URISyntaxException {
        // check for url format
        new URL(url).toURI();

        String regex = "(fb.me/)(e/)?([^/?]*)|(facebook.com/event_invite/[a-zA-Z0-9]*)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            //only mbasic does have event ids displayed in HTML
            String url_prefix = "https://mbasic.";

            // create URL
            return url_prefix + matcher.group();

        } else {
            throw new URISyntaxException(url, "Does not contain page.");
        }

    }

    /**
     * Checks if valid URL,
     * strips the facebook page id from the input link and create an URL that can be scraped from.
     *
     * @param url input URL
     * @return new mbasic url that can be scraped for event id's
     * @throws URISyntaxException    if page not found
     * @throws MalformedURLException
     */
    protected String getPageUrl(String url) throws URISyntaxException, MalformedURLException {

        // check for url format
        new URL(url).toURI();

        String regex = "(facebook.com/)(pg/)?([^/?]*)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            //only mbasic does have event ids displayed in HTML
            String url_prefix = "https://mbasic.facebook.com/";
            String url_suffix = "?v=events";

            // create URL
            return url_prefix + matcher.group(3) + url_suffix;

        } else {
            throw new URISyntaxException(url, "Does not contain page.");
        }
    }

    /**
     * Strips the facebook event link from the input event url.
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

    /**
     * cancel vestigial async tasks
     */
    void killAllTasks() {

        if (!tasks.isEmpty()) {
            for (AsyncTask task : tasks) {
                try {
                    task.cancel(true);
                    task = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * start an EventScraper async task and add to tasks list
     *
     * @param event_url
     */
    void scrapeEvent(String event_url) {
        FbEventScraper scraper = new FbEventScraper(this, event_url);

        Log.d("scraperLog", "scrapeEvent: "+event_url);
        tasks.add(scraper);
        scraper.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Callback for finished EventSCraper async task
     *
     * @param event Contains event information if scraping successful
     * @param error resId for error message
     */
    void scrapeEventResultCallback(FbEvent event, int error) {

        if (event != null) {
            Log.d("scraperLog", "scrapeEventResultCallback: "+event.url);
            main.get().addEvent(event);
            main.get().input_helper(main.get().getString(R.string.done), false);
        } else if (url_type == url_type_enum.EVENT) {
            main.get().input_helper(main.get().getString(error), true);
        }
    }

    /**
     * start a page scraper and add to list of tasks
     *
     * @param page_url
     */
    void scrapePage(String page_url) {
        FbPageScraper scraper = new FbPageScraper(this, page_url);

        Log.d("scraperLog", "scrapePage: "+page_url);

        tasks.add(scraper);
        scraper.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Callback for page scraper async task
     *
     * @param event_urls List of event urls scraped from the event
     * @param error      resId of error message if task list is empty
     */
    protected void scrapePageResultCallback(List<String> event_urls, int error) {

        if (event_urls.size() > 0) {
            Log.d("scraperLog", "scrapePageResultCallback: "+event_urls.toString());
            for (String event_url : event_urls) {
                try {
                    String url = getEventUrl(event_url);
                    Log.d("scraperLog", "scrapePageResultCallback: "+url);
                    scrapeEvent(url);
                } catch (URISyntaxException | MalformedURLException e) {
                    // ignore this event
                }
            }
        } else {
            main.get().input_helper(main.get().getString(error), true);
        }
    }

    protected void redirectUrl (String url) {
        FbRedirectionResolver resolver = new FbRedirectionResolver(this, url);

        Log.d("scraperLog", "redirectUrl: "+url);

        resolver.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    protected void redirectionResultCallback(String url) {
        this.input_url = url;

        Log.d("scraperLog", "redirectUrlCb: "+url);

        // now try again with expanded url
        this.run();
    }

    /**
     * Start scraping input url
     */
    void run() {

        // check if shortened url
        try {
            String shortened = getShortened(input_url);
            url_type = url_type_enum.SHORT;
            redirectUrl(shortened);

            return;

        } catch (IOException | URISyntaxException e) {
            url_type = url_type_enum.INVALID;
        }

        // check if input url is an event
        try {
            String event_url = getEventUrl(input_url);
            url_type = url_type_enum.EVENT;
            scrapeEvent(event_url);

            return;

        } catch (URISyntaxException | MalformedURLException e) {
            url_type = url_type_enum.INVALID;
        }
        // check if input url is a page
        try {
            String page_url = getPageUrl(input_url);
            url_type = url_type_enum.PAGE;
            scrapePage(page_url);

        } catch (URISyntaxException | MalformedURLException e) {
            url_type = url_type_enum.INVALID;
            main.get().input_helper(main.get().getString(R.string.error_url), true);
        }
    }

    // enum for storing url type in this class
    enum url_type_enum {SHORT, EVENT, PAGE, INVALID}
}
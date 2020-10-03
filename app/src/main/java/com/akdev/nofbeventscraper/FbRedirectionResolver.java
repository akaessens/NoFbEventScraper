package com.akdev.nofbeventscraper;

import android.os.AsyncTask;

import java.net.HttpURLConnection;
import java.net.URL;

public class FbRedirectionResolver extends AsyncTask<Void, Void, Void> {

    private String input_url;
    private FbScraper scraper;
    private String redirected_url;

    public FbRedirectionResolver (FbScraper scraper, String input_url) {
        this.input_url = input_url;
        this.scraper = scraper;
    }

    protected Void doInBackground(Void... voids) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(input_url).openConnection();

            con.setInstanceFollowRedirects(false);

            con.connect();

            redirected_url = con.getHeaderField("Location");

        } catch (Exception e) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        scraper.redirectionResultCallback(redirected_url);
    }
}
package com.akdev.nofbeventscraper;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class FbScraper extends AsyncTask<Void, Void, Void> {

    private String url;
    private View popupView;
    public FbEvent event;

    public FbScraper(View popupView, String url) {
        this.url = url;
        this.popupView = popupView;
    }

    @Override
    protected Void doInBackground(Void... voids){

        Document document = null;

        try {
            document = Jsoup.connect(url).get();

            Log.d("url", url);

            //String event_name = document.title();

            String json = document.select("#u_0_j").first().data();

            try {
                JSONObject reader = new JSONObject(json);

                String event_name = reader.getString("name");
                String event_start = reader.getString("startDate");
                String event_end = reader.getString("endDate");
                String event_description = reader.getString("description");
                String location = reader.getJSONObject("location").getString("name");


                String image_url = reader.getString("image");

                this.event = new FbEvent(event_name, event_start, event_end, event_description, location, image_url);

            } catch (JSONException e) {
                e.printStackTrace();
            }




        } catch (IOException e) {
            e.printStackTrace();
        }







        return null;
    }

    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        TextInputEditText field_name = (TextInputEditText) this.popupView.findViewById(R.id.field_event_name);
        TextInputEditText field_start = (TextInputEditText) this.popupView.findViewById(R.id.field_event_start);
        TextInputEditText field_end = (TextInputEditText) this.popupView.findViewById(R.id.field_event_end);
        TextInputEditText field_location = (TextInputEditText) this.popupView.findViewById(R.id.field_event_location);

        field_name.setText(event.name);
        field_start.setText(event.start_date);
        field_end.setText(event.end_date);
        field_location.setText(event.location);

    }
}


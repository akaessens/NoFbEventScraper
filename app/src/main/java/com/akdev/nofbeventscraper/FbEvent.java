package com.akdev.nofbeventscraper;

import android.app.usage.UsageEvents;
import android.util.EventLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Objects of this class store immutable information about
 * a single event that was created by the FbScraper.
 */
public class FbEvent {

    public final String url;
    public final String name;
    public final Date start_date;
    public final Date end_date;
    public final String description;
    public final String location;
    public final String image_url;

    public FbEvent() {
        url = "url";
        name= "name";
        start_date = null;
        end_date = null;
        description = "description";
        location = "location";
        image_url = null;
    }

    public FbEvent(String url, String name, Date start_date, Date end_date,
                   String description, String location, String image_url) {
        this.url = url;
        this.name = name;
        this.start_date = start_date;
        this.end_date = end_date;
        this.description = description;
        this.location = location;
        this.image_url = image_url;
    }

    public static ArrayList<FbEvent> createEventList(int num_events) {
        ArrayList<FbEvent> events = new ArrayList<FbEvent>();

        for (int i = 1; i <= num_events; i++) {
            events.add(new FbEvent());
        }

        return events;
    }

    /**
     * Converts datetime to epoch.
     *
     * @param date Date object
     * @return Event begin time in milliseconds from the epoch for calendar intent or null
     */
    static Long dateTimeToEpoch(Date date) {
        try {
            return date.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a locally formatted String representation of a Date
     *
     * @param date
     * @return locally formatted String of date or empty String
     */
    static String dateTimeToString(Date date) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm z",
                    Locale.getDefault());
            return formatter.format(date);
        } catch (Exception e) {
            return "";
        }
    }
}

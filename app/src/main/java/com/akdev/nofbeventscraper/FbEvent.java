package com.akdev.nofbeventscraper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class FbEvent {

    public final String url;
    public final String name;
    public final ZonedDateTime start_date;
    public final ZonedDateTime end_date;
    public final String description;
    public final String location;
    public final String image_url;


    public FbEvent(String url, String name, ZonedDateTime start_date, ZonedDateTime end_date,
                   String description, String location, String image_url) {
        this.url = url;
        this.name = name;
        this.start_date = start_date;
        this.end_date = end_date;
        this.description = description;
        this.location = location;
        this.image_url = image_url;
    }


    static Long dateTimeToEpoch(ZonedDateTime datetime) {
        try {
            return datetime.toEpochSecond() * 1000;
        } catch (Exception e) {
            return null;
        }
    }

    static String dateTimeToString(ZonedDateTime datetime) {
        try {
            return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(datetime);
        } catch (Exception e) {
            return "";
        }
    }
}

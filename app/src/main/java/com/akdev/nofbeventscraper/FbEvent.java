package com.akdev.nofbeventscraper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * Objects of this class store immutable information about
 * a single event that was created by the FbScraper.
 */
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

    /**
     * Converts datetime to epoch.
     *
     * @param zoned_date_time ZonedDateTime object
     * @return Event begin time in milliseconds from the epoch for calendar intent or null
     */
    static Long dateTimeToEpoch(ZonedDateTime zoned_date_time) {
        try {
            return zoned_date_time.toEpochSecond() * 1000;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a String representation of a ZonedDateTime
     *
     * @param zoned_date_time
     * @return Locally formatted String of zoned_date_time or empty String
     */
    static String dateTimeToString(ZonedDateTime zoned_date_time) {
        try {
            return DateTimeFormatter
                    .ofLocalizedDateTime(FormatStyle.LONG)
                    .format(zoned_date_time);
        } catch (Exception e) {
            return "";
        }
    }
}

package com.akdev.nofbeventscraper;

public class FbEvent {
    public String name;
    public String start_date;
    public String end_date;
    public String description;
    public String location;
    public String image_url;

    public FbEvent (String name, String start_date, String end_date, String description, String location, String image_url) {
        this.name = name;
        this.start_date = start_date;
        this.end_date = end_date;
        this.description = description;
        this.location = location;
        this.image_url = image_url;
    }
}

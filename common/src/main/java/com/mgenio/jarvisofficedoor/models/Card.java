package com.mgenio.jarvisofficedoor.models;

/**
 * Created by Austin Nelson on 2/21/2017.
 */

public class Card {

    private String key;
    private String facility;
    private String card;
    private String location;
    private String accessKey;
    private long timestamp;

    public Card() {

    }

    public Card(String facility, String card, String location, String accessKey, long timestamp) {
        setFacility(facility);
        setCard(card);
        setLocation(location);
        setAccessKey(accessKey);
        setTimestamp(timestamp);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

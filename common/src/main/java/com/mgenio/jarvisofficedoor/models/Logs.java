package com.mgenio.jarvisofficedoor.models;

/**
 * Created by Austin Nelson on 2/20/2017.
 */

public class Logs {

    private String key;
    private String location;
    private String accessKeyName;
    private String accessKey;
    private String pin;
    private String facility;
    private String card;
    private long timestamp;
    private boolean authorized;


    public Logs() {

    }

    /**
     * APP
     *
     * @param timestamp
     * @param location
     * @param accessKey
     */
    public Logs(long timestamp, String location, String accessKey, String accessKeyName) {
        setTimestamp(timestamp);
        setLocation(location);
        setAccessKey(accessKey);
        setAccessKeyName(accessKeyName);
    }

    /**
     * PIN
     *
     * @param location
     * @param pin
     * @param timestamp
     */
    public Logs(String location, String pin, long timestamp) {
        setLocation(location);
        setPin(pin);
        setTimestamp(timestamp);
    }

    /**
     * CARD
     *
     * @param location
     * @param facility
     * @param card
     * @param timestamp
     */
    public Logs(String location, String facility, String card, long timestamp) {
        setLocation(location);
        setFacility(facility);
        setCard(card);
        setTimestamp(timestamp);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAccessKeyName() {
        return accessKeyName;
    }

    public void setAccessKeyName(String accessKeyName) {
        this.accessKeyName = accessKeyName;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }
}

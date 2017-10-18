package com.mgenio.jarvisofficedoor.models;

/**
 * Created by Austin Nelson on 2/17/2017.
 */

public class Pin {

    private String key;
    private String pin;
    private String accessKey;
    private String location;
    private long expirationDate;

    public Pin() {

    }

    public Pin(String pin, String accessKey, String location, long expirationDate) {
        setPin(pin);
        setAccessKey(accessKey);
        setLocation(location);
        setExpirationDate(expirationDate);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

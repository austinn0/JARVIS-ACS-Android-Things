package com.mgenio.jarvisofficedoor.models;

/**
 * Created by Austin Nelson on 3/2/2017.
 */

public class Door {

    private String key;
    private String accessKey;
    private String accessKeyName;
    private String location;

    public Door() {

    }

    public Door(String accessKey, String accessKeyName, String location) {
        setAccessKey(accessKey);
        setAccessKeyName(accessKeyName);
        setLocation(location);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAccessKeyName() {
        return accessKeyName;
    }

    public void setAccessKeyName(String accessKeyName) {
        this.accessKeyName = accessKeyName;
    }
}

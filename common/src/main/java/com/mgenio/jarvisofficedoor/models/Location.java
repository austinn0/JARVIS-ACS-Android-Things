package com.mgenio.jarvisofficedoor.models;

/**
 * Created by Austin Nelson on 2/17/2017.
 */

public class Location {

    private String key;
    private String name;
    private String mode;

    public Location() {

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}

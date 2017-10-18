package com.mgenio.jarvisofficedoor.models;

/**
 * Created by Austin Nelson on 6/5/2017.
 */

public class Artist {

    private String id;
    private String name;

    public Artist() {
    }

    public Artist(String id, String name) {
        setId(id);
        setName(name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

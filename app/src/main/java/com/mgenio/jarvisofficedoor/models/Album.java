package com.mgenio.jarvisofficedoor.models;

/**
 * Created by Austin Nelson on 6/5/2017.
 */

public class Album {

    private String id;
    private String name;
    private Artist artist;
    private String imageUrl;

    public Album() {
    }

    public Album(String id, String name, Artist artist, String imageUrl) {
        setId(id);
        setName(name);
        setArtist(artist);
        setImageUrl(imageUrl);
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

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

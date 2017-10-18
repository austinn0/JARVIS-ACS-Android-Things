package com.mgenio.jarvisofficedoor.models;


/**
 * Created by Austin Nelson on 6/5/2017.
 */

public class Track {

    private String id;
    private String name;
    private String previewUrl;
    private Album album;

    public Track() {

    }

    public Track(String id, String name, String previewUrl, Album album) {
        setId(id);
        setName(name);
        setPreviewUrl(previewUrl);
        setAlbum(album);
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

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }
}

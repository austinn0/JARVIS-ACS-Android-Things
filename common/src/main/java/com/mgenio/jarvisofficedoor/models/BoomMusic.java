package com.mgenio.jarvisofficedoor.models;

/**
 * Created by Austin Nelson on 2/17/2017.
 */

public class BoomMusic {

    private String key;
    private String accessKey;
    private String spotifyUrl;

    public BoomMusic() {

    }

    public BoomMusic(String accessKey, String spotifyUrl) {
        setAccessKey(accessKey);
        setSpotifyUrl(spotifyUrl);
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

    public String getSpotifyUrl() {
        return spotifyUrl;
    }

    public void setSpotifyUrl(String spotifyUrl) {
        this.spotifyUrl = spotifyUrl;
    }
}

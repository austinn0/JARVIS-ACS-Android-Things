package com.mgenio.jarvisofficedoor.models;

import android.support.annotation.Nullable;

/**
 * Created by Austin Nelson on 2/17/2017.
 */

public class AccessKey {

    private String key;
    private String type;
    private String name;
    private String location;
    private String imageUrl;
    private boolean enabled;
    private boolean registered;
    private boolean deleted;
    private long expirationDate;

    @Nullable
    private boolean selected;

    public AccessKey() {

    }

    public AccessKey(String type, String location, String name, boolean enabled, boolean registered, long expirationDate) {
        setType(type);
        setName(name);
        setLocation(location);
        setEnabled(enabled);
        setRegistered(registered);
        setExpirationDate(expirationDate);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        if (null == name) {
            return "Unprovisioned User";
        } else {
            return name;
        }
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

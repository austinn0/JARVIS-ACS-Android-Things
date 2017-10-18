package com.mgenio.jarvisofficedoor.models;

/**
 * Created by Austin Nelson on 2/9/2017.
 */

public class Blacklist {

    private String deviceId;

    public Blacklist() {

    }

    public Blacklist(String deviceId) {
        setDeviceId(deviceId);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}

package com.mgenio.jarvisofficedoor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;

import java.util.Set;
import java.util.UUID;

/**
 * Created by Austin Nelson on 2/9/2017.
 */

public class Utils {

    final public static String PREFS_KEY = "com.mgenio.warmspire";

    private static int screenWidth = 0;
    private static int screenHeight = 0;

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    // Store string setting
    public static void storeSetting(String settingKey, String settingValue, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(settingKey, settingValue);
        // Commit the edits!
        editor.apply();
    }

    // Get string setting
    public static String getSetting(String settingName, String defaultValue, Context context) {
        if (null == context) {
            return "";
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString(settingName, defaultValue);
    }

    // Store string setting
    public static void storeSetting(String settingKey, boolean settingValue, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(settingKey, settingValue);
        // Commit the edits!
        editor.apply();
    }

    // Get string setting
    public static boolean getSetting(String settingName, boolean defaultValue, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(settingName, defaultValue);
    }

    // Store string setting
    public static void storeSetting(String settingKey, Set<String> settingValue, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(settingKey, settingValue);
        // Commit the edits!
        editor.apply();
    }

    // Get string setting
    public static Set<String> getSetting(String settingName, Set<String> defaultValue, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getStringSet(settingName, defaultValue);
    }

    public static int getScreenHeight(Context c) {
        if (screenHeight == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
        }

        return screenHeight;
    }

    public static int getScreenWidth(Context c) {
        if (screenWidth == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        }

        return screenWidth;
    }

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static String getDeviceId(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();

        return deviceId;
    }
}

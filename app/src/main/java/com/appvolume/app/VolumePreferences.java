package com.appvolume.app;

import android.content.Context;
import android.content.SharedPreferences;

public class VolumePreferences {

    private static final String PREFS_NAME = "AppVolumePrefs";

    public static void saveAppVolume(Context ctx, String packageName, int volume, int maxVolume) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        float percent = (float) volume / maxVolume;
        prefs.edit().putFloat(packageName, percent).apply();
    }

    public static float getAppVolumePercent(Context ctx, String packageName) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(packageName, 1.0f); // 100% por defecto
    }

    public static int getAppVolume(Context ctx, String packageName, int maxVolume) {
        float percent = getAppVolumePercent(ctx, packageName);
        return Math.round(percent * maxVolume);
    }
}

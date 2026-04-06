package com.appvolume.app;

import android.graphics.drawable.Drawable;

public class AppVolumeModel {
    private String appName;
    private String packageName;
    private int currentVolume;
    private int maxVolume;
    private Drawable icon;

    public AppVolumeModel(String appName, String packageName, int currentVolume, int maxVolume, Drawable icon) {
        this.appName = appName;
        this.packageName = packageName;
        this.currentVolume = currentVolume;
        this.maxVolume = maxVolume;
        this.icon = icon;
    }

    public String getAppName() { return appName; }
    public String getPackageName() { return packageName; }
    public int getCurrentVolume() { return currentVolume; }
    public void setCurrentVolume(int v) { this.currentVolume = v; }
    public int getMaxVolume() { return maxVolume; }
    public Drawable getIcon() { return icon; }

    public boolean isSystemStream() {
        return packageName.startsWith("STREAM_");
    }

    public int getStreamType() {
        switch (packageName) {
            case "STREAM_MUSIC": return android.media.AudioManager.STREAM_MUSIC;
            case "STREAM_RING": return android.media.AudioManager.STREAM_RING;
            case "STREAM_NOTIFICATION": return android.media.AudioManager.STREAM_NOTIFICATION;
            default: return android.media.AudioManager.STREAM_MUSIC;
        }
    }
}

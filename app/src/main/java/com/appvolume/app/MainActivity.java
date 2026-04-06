package com.appvolume.app;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppVolumeAdapter adapter;
    private AudioManager audioManager;
    private List<AppVolumeModel> appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab_refresh);
        fab.setOnClickListener(v -> loadApps());

        checkUsageStatsPermission();
        loadApps();
        startVolumeService();
    }

    private void checkUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode != AppOpsManager.MODE_ALLOWED) {
            new AlertDialog.Builder(this)
                    .setTitle("Permiso necesario")
                    .setMessage("Para ver las apps activas necesitamos acceso a estadísticas de uso. Por favor, actívalo en la siguiente pantalla.")
                    .setPositiveButton("Ir a configuración", (d, w) -> {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Omitir", null)
                    .show();
        }
    }

    private void loadApps() {
        appList = new ArrayList<>();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        // Apps principales con audio
        String[] priorityPackages = {
            "com.spotify.music",
            "com.google.android.youtube",
            "com.google.android.apps.youtube.music",
            "com.netflix.mediaclient",
            "com.tencent.ig",
            "com.activision.callofduty.shooter",
            "com.mojang.minecraftpe",
            "com.roblox.client",
            "com.king.candycrushsaga",
            "com.supercell.clashofclans",
            "com.facebook.katana",
            "com.instagram.android",
            "com.whatsapp",
            "com.discord",
            "com.amazon.mp3",
            "com.soundcloud.android",
            "com.deezer.android",
            "tv.twitch.android.app"
        };

        // Primero agregamos apps prioritarias si están instaladas
        for (String pkg : priorityPackages) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                String name = pm.getApplicationLabel(info).toString();
                appList.add(new AppVolumeModel(name, pkg, currentVolume, maxVolume, pm.getApplicationIcon(info)));
            } catch (PackageManager.NameNotFoundException ignored) {}
        }

        // Luego agregamos otras apps de usuario
        for (ApplicationInfo appInfo : installedApps) {
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                boolean alreadyAdded = false;
                for (AppVolumeModel m : appList) {
                    if (m.getPackageName().equals(appInfo.packageName)) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (!alreadyAdded) {
                    String name = pm.getApplicationLabel(appInfo).toString();
                    try {
                        appList.add(new AppVolumeModel(name, appInfo.packageName,
                                currentVolume, maxVolume, pm.getApplicationIcon(appInfo)));
                    } catch (Exception ignored) {}
                }
            }
        }

        // Controles del sistema siempre presentes arriba
        AppVolumeModel musicStream = new AppVolumeModel(
                "Música / Media",
                "STREAM_MUSIC",
                audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                null
        );
        AppVolumeModel ringStream = new AppVolumeModel(
                "Llamadas / Tono",
                "STREAM_RING",
                audioManager.getStreamVolume(AudioManager.STREAM_RING),
                audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
                null
        );
        AppVolumeModel notifStream = new AppVolumeModel(
                "Notificaciones",
                "STREAM_NOTIFICATION",
                audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION),
                audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
                null
        );

        appList.add(0, notifStream);
        appList.add(0, ringStream);
        appList.add(0, musicStream);

        adapter = new AppVolumeAdapter(this, appList, audioManager);
        recyclerView.setAdapter(adapter);

        Toast.makeText(this, appList.size() + " apps cargadas", Toast.LENGTH_SHORT).show();
    }

    private void startVolumeService() {
        Intent serviceIntent = new Intent(this, VolumeService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
}

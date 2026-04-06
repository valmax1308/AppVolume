package com.appvolume.app;

import android.content.Context;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppVolumeAdapter extends RecyclerView.Adapter<AppVolumeAdapter.ViewHolder> {

    private Context context;
    private List<AppVolumeModel> apps;
    private AudioManager audioManager;

    public AppVolumeAdapter(Context context, List<AppVolumeModel> apps, AudioManager audioManager) {
        this.context = context;
        this.apps = apps;
        this.audioManager = audioManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app_volume, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppVolumeModel app = apps.get(position);

        holder.tvAppName.setText(app.getAppName());
        holder.tvPackage.setText(app.isSystemStream() ? "Control del sistema" : app.getPackageName());

        if (app.getIcon() != null) {
            holder.ivIcon.setImageDrawable(app.getIcon());
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_volume_default);
        }

        holder.seekBar.setMax(app.getMaxVolume());
        holder.seekBar.setProgress(app.getCurrentVolume());
        holder.tvVolume.setText(app.getCurrentVolume() + "/" + app.getMaxVolume());

        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    app.setCurrentVolume(progress);
                    holder.tvVolume.setText(progress + "/" + app.getMaxVolume());

                    if (app.isSystemStream()) {
                        // Control directo del stream del sistema
                        audioManager.setStreamVolume(app.getStreamType(), progress, 0);
                    } else {
                        // Para apps individuales: ajusta el stream de música
                        // y guarda preferencia en VolumePreferences
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                        VolumePreferences.saveAppVolume(context, app.getPackageName(), progress, app.getMaxVolume());
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (!app.isSystemStream()) {
                    Toast.makeText(context,
                            "Abrí " + app.getAppName() + " y ajusta su volumen aquí",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public int getItemCount() { return apps.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvAppName, tvPackage, tvVolume;
        SeekBar seekBar;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_app_icon);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
            tvPackage = itemView.findViewById(R.id.tv_package);
            tvVolume = itemView.findViewById(R.id.tv_volume);
            seekBar = itemView.findViewById(R.id.seekbar_volume);
        }
    }
}

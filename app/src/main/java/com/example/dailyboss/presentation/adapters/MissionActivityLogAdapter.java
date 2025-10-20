package com.example.dailyboss.presentation.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.domain.model.MissionActivityLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MissionActivityLogAdapter extends RecyclerView.Adapter<MissionActivityLogAdapter.ViewHolder> {

    private final Context context;
    private List<MissionActivityLog> logs;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm, dd.MM.", Locale.getDefault());

    public MissionActivityLogAdapter(Context context, List<MissionActivityLog> logs) {
        this.context = context;
        this.logs = logs;
    }

    public void updateLogs(List<MissionActivityLog> newLogs) {
        this.logs.clear();
        this.logs.addAll(newLogs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mission_activity_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MissionActivityLog log = logs.get(position);
        holder.tvLogMessage.setText(log.getDisplayMessage());
        holder.tvLogTimestamp.setText(formatTimeAgo(log.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    private String formatTimeAgo(Date timestamp) {
        long diffMillis = System.currentTimeMillis() - timestamp.getTime();

        if (diffMillis < TimeUnit.MINUTES.toMillis(1)) {
            return "Upravo sada";
        } else if (diffMillis < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
            return String.format(Locale.getDefault(), "Pre %d %s", minutes, (minutes == 1) ? "minut" : "minuta");
        } else if (diffMillis < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
            return String.format(Locale.getDefault(), "Pre %d %s", hours, (hours == 1) ? "sat" : "sati");
        } else {
            return dateFormat.format(timestamp);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogMessage;
        TextView tvLogTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLogMessage = itemView.findViewById(R.id.tvLogMessage);
            tvLogTimestamp = itemView.findViewById(R.id.tvLogTimestamp);
        }
    }
}
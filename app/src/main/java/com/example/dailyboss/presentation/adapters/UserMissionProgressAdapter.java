package com.example.dailyboss.presentation.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.domain.model.UserMissionProgress;

import java.util.List;
import java.util.Locale;

public class UserMissionProgressAdapter extends RecyclerView.Adapter<UserMissionProgressAdapter.ViewHolder> {

    private final Context context;
    private List<UserMissionProgress> progressList;

    public UserMissionProgressAdapter(Context context, List<UserMissionProgress> progressList) {
        this.context = context;
        this.progressList = progressList;
    }

    public void updateProgress(List<UserMissionProgress> newProgressList) {
        Log.d("UserMissionProgressAdapter", "updateProgress called with " + newProgressList.size() + " items.");
        this.progressList.clear();
        this.progressList.addAll(newProgressList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_mission_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserMissionProgress progress = progressList.get(position);
        holder.tvMemberUsername.setText(progress.getUsername());
        holder.tvMemberDamageDealt.setText(String.format(Locale.getDefault(), "Ukupna Šteta: %d HP", progress.calculateTotalDamageDealt()));

        // Dodatni detalji za prikaz u summary stringu
        String summary = String.format(Locale.getDefault(),
                "Kupovine: %d, Udarci: %d, Zadaci (L/N/V): %d, Ostali zadaci: %d, Bez nerešenih: %s",
                progress.getBuyInShopCount(),
                progress.getRegularBossHitCount(),
                progress.getEasyNormalImportantTaskCount(),
                progress.getOtherTasksCount(),
                progress.isNoUnresolvedTasksCompleted() ? "Da" : "Ne");
        holder.tvMemberProgressSummary.setText(summary);
    }

    @Override
    public int getItemCount() {
        Log.d("UserMissionProgressAdapter", "getItemCount: " + progressList.size());
        return progressList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberUsername;
        TextView tvMemberDamageDealt;
        TextView tvMemberProgressSummary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberUsername = itemView.findViewById(R.id.tvMemberUsername);
            tvMemberDamageDealt = itemView.findViewById(R.id.tvMemberDamageDealt);
            tvMemberProgressSummary = itemView.findViewById(R.id.tvMemberProgressSummary);
        }
    }
}
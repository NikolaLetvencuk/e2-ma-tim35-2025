package com.example.dailyboss.presentation.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.domain.model.Badge;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private final Context context;
    private final List<Badge> badges;

    public BadgeAdapter(Context context, List<Badge> badges) {
        this.context = context;
        this.badges = badges;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Badge badge = badges.get(position);
        holder.tvBadgeName.setText(badge.getName());

        String iconName = badge.getIconPath();
        int resId = 0;
        if (iconName != null) {
            resId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        }

        if (resId != 0) {
            holder.imgBadgeIcon.setImageResource(resId);
        } else {
            holder.imgBadgeIcon.setImageResource(R.drawable.ic_badge_default);
        }
    }


    @Override
    public int getItemCount() {
        return badges.size();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBadgeIcon;
        TextView tvBadgeName;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBadgeIcon = itemView.findViewById(R.id.imgBadgeIcon);
            tvBadgeName = itemView.findViewById(R.id.tvBadgeName);
        }
    }
}
package com.example.dailyboss.presentation.adapters; // Proverite da li je putanja paketa ispravna

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R; // Uverite se da je R klasa ispravno uvežena
import com.example.dailyboss.domain.model.User;

import java.util.List;

public class AllianceMembersAdapter extends RecyclerView.Adapter<AllianceMembersAdapter.MemberViewHolder> {

    private final List<User> members;
    private String allianceLeaderId;
    private final Context context;

    public AllianceMembersAdapter(Context context, List<User> members, String allianceLeaderId) {
        this.context = context;
        this.members = members;
        this.allianceLeaderId = allianceLeaderId;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alliance_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User member = members.get(position);
        holder.tvMemberUsername.setText(member.getUsername());

        // Postavi ulogu
        if (member.getId().equals(allianceLeaderId)) {
            holder.tvMemberRole.setText("Vođa");
            holder.ivLeaderIcon.setVisibility(View.VISIBLE);
        } else {
            holder.tvMemberRole.setText("Član");
            holder.ivLeaderIcon.setVisibility(View.GONE);
        }

        // Učitavanje avatara iz drawable resursa
        String avatarName = member.getAvatar(); // Pretpostavljamo da getAvatar() vraća ime resursa (npr. "ic_avatar_male")

        if (avatarName != null && !avatarName.isEmpty()) {
            int resourceId = context.getResources().getIdentifier(
                    avatarName,
                    "drawable",
                    context.getPackageName()
            );

            if (resourceId != 0) {
                holder.ivMemberAvatar.setImageResource(resourceId);
            } else {
                // Ako resurs nije pronađen ili ime nije ispravno, koristi default
                holder.ivMemberAvatar.setImageResource(R.drawable.ic_badge_default); // PROVERITE DA IMATE OVAJ RESURS!
            }
        } else {
            // Ako avatarName nije postavljen, koristi default
            holder.ivMemberAvatar.setImageResource(R.drawable.ic_badge_default);
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberUsername;
        TextView tvMemberRole;
        ImageView ivMemberAvatar;
        ImageView ivLeaderIcon;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberUsername = itemView.findViewById(R.id.tvMemberUsername);
            tvMemberRole = itemView.findViewById(R.id.tvMemberRole);
            ivMemberAvatar = itemView.findViewById(R.id.ivMemberAvatar);
            ivLeaderIcon = itemView.findViewById(R.id.ivLeaderIcon);
        }
    }

    public void updateMembers(List<User> newMembers) {
        this.members.clear();
        this.members.addAll(newMembers);
        notifyDataSetChanged();
    }

    public void setAllianceLeaderId(String leaderId) {
        this.allianceLeaderId = leaderId;
        // Opcionalno: Ako treba da se ponovo iscrta kako bi se kruna pravilno prikazala
        // notifyDataSetChanged();
    }
}
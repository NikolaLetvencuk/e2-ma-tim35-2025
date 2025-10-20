// app/src/main/java/com/example/dailyboss/presentation/adapters/FriendAdapter.java
package com.example.dailyboss.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R; // Morate imati R.id.tvFriendUsername
import com.example.dailyboss.domain.model.Alliance;
import com.example.dailyboss.domain.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private Map<User, Alliance> friendStatusMap;
    private final List<User> friendList;
    private OnFriendOptionClickListener listener;

    public interface OnFriendOptionClickListener {
        void onFriendOptionsClick(View anchorView, User friend);
    }

    public void setOnFriendOptionClickListener(OnFriendOptionClickListener listener) {
        this.listener = listener;
    }

    public FriendAdapter(Map<User, Alliance> friendStatusMap) {
        this.friendStatusMap = friendStatusMap;
        this.friendList = new ArrayList<>(friendStatusMap.keySet());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User friend = friendList.get(position);
        holder.tvUsername.setText(friend.getUsername());
        Alliance friendsAlliance = friendStatusMap.get(friend);

        if (friendsAlliance != null) {
            holder.tvFriendLevel.setText("[ " + friendsAlliance.getName() + " ]");
        } else {
            holder.tvFriendLevel.setText("Nema saveza");
            holder.tvFriendLevel.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.calendar_black, null));
        }
        String avatarName = friend.getAvatar();

        if (avatarName != null && !avatarName.isEmpty()) {
            int resourceId = holder.itemView.getContext().getResources().getIdentifier(
                    avatarName,
                    "drawable",
                    holder.itemView.getContext().getPackageName()
            );

            if (resourceId != 0) {
                holder.friendImage.setImageResource(resourceId);
            } else {
                holder.friendImage.setImageResource(R.drawable.ic_badge_default);
            }
        } else {
            holder.friendImage.setImageResource(R.drawable.ic_badge_default);
        }

        holder.btnFriendOptions.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendOptionsClick(v, friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public void updateFriends(Map<User, Alliance> newFriendStatusMap) {
        this.friendStatusMap.clear();
        this.friendStatusMap.putAll(newFriendStatusMap);

        this.friendList.clear();
        this.friendList.addAll(newFriendStatusMap.keySet());

        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvUsername;
        final ImageView friendImage;
        final TextView tvFriendLevel;
        final ImageView btnFriendOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvFriendUsername);
            friendImage = itemView.findViewById(R.id.friendImage);
            tvFriendLevel = itemView.findViewById(R.id.tvFriendLevel);
            btnFriendOptions = itemView.findViewById(R.id.btnFriendOptions);
        }
    }
}
// app/src/main/java/com/example/dailyboss/presentation/adapters/UserSearchAdapter.java
package com.example.dailyboss.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.domain.model.User;

import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {

    private final List<User> userList;
    private final OnUserActionListener listener;

    public interface OnUserActionListener {
        void onSendRequest(User user);
    }

    public UserSearchAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvUsername.setText(user.getUsername());

        String avatarName = user.getAvatar();

        if (avatarName != null && !avatarName.isEmpty()) {
            int resourceId = holder.itemView.getContext().getResources().getIdentifier(
                    avatarName,
                    "drawable",
                    holder.itemView.getContext().getPackageName()
            );

            if (resourceId != 0) {

                holder.imgAvatar.setImageResource(resourceId);
            } else {

                holder.imgAvatar.setImageResource(R.drawable.ic_badge_default);
            }
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_badge_default);
        }

        holder.btnSendRequest.setOnClickListener(v -> listener.onSendRequest(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUsers(List<User> newUsers) {
        this.userList.clear();
        this.userList.addAll(newUsers);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgAvatar;
        final TextView tvUsername;
        final Button btnSendRequest;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnSendRequest = itemView.findViewById(R.id.btnSendRequest);
        }
    }
}
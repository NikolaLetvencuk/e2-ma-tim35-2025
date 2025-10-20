package com.example.dailyboss.presentation.adapters; // Prilagodi svoj package name

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.domain.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> usersList;
    private String currentUserId;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onSendFriendRequest(User user);
    }

    public UserAdapter(List<User> usersList, String currentUserId, OnUserActionListener listener) {
        this.usersList = usersList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void updateUsers(List<User> newUsers) {
        this.usersList = newUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search, parent, false); // Kreiraj item_user_search.xml
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = usersList.get(position);
        holder.tvUsername.setText(user.getUsername());

        if (user.getId().equals(currentUserId)) {
            holder.btnSendRequest.setVisibility(View.GONE);
        } else {
            holder.btnSendRequest.setVisibility(View.VISIBLE);
            holder.btnSendRequest.setText("Add Friend");
            holder.btnSendRequest.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSendFriendRequest(user);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        Button btnSendRequest;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnSendRequest = itemView.findViewById(R.id.btnSendRequest);
        }
    }
}
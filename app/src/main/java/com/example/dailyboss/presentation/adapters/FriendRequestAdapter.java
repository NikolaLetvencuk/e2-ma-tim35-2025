package com.example.dailyboss.presentation.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R; // Morate imati R.id.btnAccept/btnReject
import com.example.dailyboss.domain.model.Friendship;
import com.example.dailyboss.domain.model.User;

import java.util.List;
import java.util.Map;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private final List<Friendship> requestList;
    // Mapiranje: Receiver ID -> Sender User Objekat
    private final Map<String, User> senderMap;
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAccept(Friendship friendship);
        void onReject(Friendship friendship);
    }

    public FriendRequestAdapter(List<Friendship> requestList, Map<String, User> senderMap, OnRequestActionListener listener) {
        this.requestList = requestList;
        this.senderMap = senderMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Friendship request = requestList.get(position);
        User sender = senderMap.get(request.getSenderId());
        Log.d("", "onBindViewHolder: " + sender);
        if (sender != null) {
            holder.tvUsername.setText(sender.getUsername());
        } else {
            // Ako profil pošiljaoca nije pronađen (npr. nije keširan)
            holder.tvUsername.setText("Unknown User (" + request.getSenderId().substring(0, 4) + "...)");
        }

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(request));
        holder.btnReject.setOnClickListener(v -> listener.onReject(request));
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    // Potrebna metoda za ažuriranje liste
    public void updateRequests(List<Friendship> newRequests, Map<String, User> newSenderMap) {
        this.requestList.clear();
        this.requestList.addAll(newRequests);
        this.senderMap.clear();
        this.senderMap.putAll(newSenderMap);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvUsername;
        final Button btnAccept;
        final Button btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Pretpostavimo da item_friend_request.xml ima ove ID-eve
            tvUsername = itemView.findViewById(R.id.tvRequestUsername);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
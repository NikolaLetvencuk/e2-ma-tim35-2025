package com.example.dailyboss.presentation.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.domain.model.AllianceMessage;
import com.example.dailyboss.data.repository.AllianceChatRepository; // Potrebno za dohvatanje username-a
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class AllianceChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final Context context;
    private final List<AllianceMessage> messages;
    private final String currentUserId;
    private final AllianceChatRepository chatRepository;

    public AllianceChatAdapter(Context context, AllianceChatRepository chatRepository) {
        this.context = context;
        this.messages = new ArrayList<>();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        this.chatRepository = chatRepository;
    }

    public void setMessages(List<AllianceMessage> newMessages) {
        this.messages.clear();
        this.messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        AllianceMessage message = messages.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AllianceMessage message = messages.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageContent;
        TextView tvMessageTimestamp;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTimestamp = itemView.findViewById(R.id.tvMessageTimestamp);
        }

        void bind(AllianceMessage message) {
            tvMessageContent.setText(message.getContent());
            if (message.getTimestamp() != null) {
                tvMessageTimestamp.setText(DateFormat.format("HH:mm", message.getTimestamp()));
            } else {
                tvMessageTimestamp.setText("");
            }
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName;
        TextView tvMessageContent;
        TextView tvMessageTimestamp;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTimestamp = itemView.findViewById(R.id.tvMessageTimestamp);
        }

        void bind(AllianceMessage message) {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    String senderUsername = Tasks.await(chatRepository.getUsernameForSender(message.getSenderId()));
                    itemView.post(() -> tvSenderName.setText(senderUsername));
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("ChatAdapter", "Greška pri dohvatanju username-a: " + e.getMessage());
                    itemView.post(() -> tvSenderName.setText("Nepoznat korisnik"));
                }
            });

            tvMessageContent.setText(message.getContent());
            if (message.getTimestamp() != null) {
                tvMessageTimestamp.setText(DateFormat.format("HH:mm", message.getTimestamp()));
            } else {
                tvMessageTimestamp.setText("");
            }
        }
    }
}
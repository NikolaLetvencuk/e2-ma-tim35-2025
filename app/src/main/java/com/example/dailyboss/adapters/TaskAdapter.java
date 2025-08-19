package com.example.dailyboss.adapters;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.dto.TaskItemDto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskItemDto> tasks;
    private OnItemClickListener listener; // Deklaracija listenera

    public TaskAdapter(List<TaskItemDto> tasks, OnItemClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    public TaskAdapter(List<TaskItemDto> tasks) {
        this.tasks = tasks;
        this.listener = null;
    }
    public interface OnItemClickListener {
        void onItemClick(TaskItemDto task);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskItemDto task = tasks.get(position);
        holder.bind(task);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle, tvTaskDesc, tvTaskTime;
        com.google.android.material.card.MaterialCardView cardTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDesc = itemView.findViewById(R.id.tvTaskDesc);
            tvTaskTime = itemView.findViewById(R.id.tvTaskTime);
            cardTask = itemView.findViewById(R.id.cardTask);
        }

        public void bind(TaskItemDto task) {
            tvTaskTitle.setText(task.getTitle());
            tvTaskDesc.setText(task.getDescription());

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String time = sdf.format(new Date(task.getStartTime()));
            tvTaskTime.setText(time + " - " + task.getStatus());

            Log.d(TAG, "bind: task.getColor() = " + task.getColor());

            String color = task.getColor();
            if (color != null && !color.isEmpty()) {
                try {
                    cardTask.setCardBackgroundColor(Color.parseColor(color.trim()));

                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid color format: " + color);
                }
            }
        }
    }
}
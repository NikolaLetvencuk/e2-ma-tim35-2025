package com.example.dailyboss.adapters;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.data.TaskInstanceDao;
import com.example.dailyboss.dto.TaskItemDto;
import com.example.dailyboss.enums.TaskStatus;
import com.example.dailyboss.model.TaskInstance;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskItemDto> tasks;
    private OnItemClickListener listener;
    private Context context;

    public TaskAdapter(Context context, List<TaskItemDto> tasks, OnItemClickListener listener) {
        this.context = context;
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
                .inflate(R.layout.item_task_list, parent, false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskItemDto task = tasks.get(position);
        TaskInstanceDao taskInstanceDao = new TaskInstanceDao(context);

        holder.bind(task);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(task);
            }
        });

        holder.btnTaskOptions.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.inflate(R.menu.task_options_menu);

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                TaskInstance taskInstance = taskInstanceDao.findTaskById(task.getInstanceId());

                if (id == R.id.action_done) {
                    task.setStatus("Done");
                    taskInstance.setStatus(TaskStatus.DONE);
                } else if (id == R.id.action_cancelled) {
                    task.setStatus("Canceled");
                    taskInstance.setStatus(TaskStatus.CANCELED);
                } else if (id == R.id.action_paused) {
                    task.setStatus("Paused");
                    taskInstance.setStatus(TaskStatus.PAUSED);
                } else if (id == R.id.action_active) {
                    task.setStatus("Active");
                    taskInstance.setStatus(TaskStatus.ACTIVE);
                }

                taskInstanceDao.update(taskInstance);
                notifyItemChanged(holder.getAdapterPosition());
                return true;
            });

            popupMenu.show();
        });

    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDescription, taskTimeStatus;
        View taskColorIndicator;
        ImageButton btnTaskOptions;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskTimeStatus = itemView.findViewById(R.id.taskTimeStatus);
            taskColorIndicator = itemView.findViewById(R.id.taskColorIndicator);
            btnTaskOptions = itemView.findViewById(R.id.btnTaskOptions); // inicijalizacija
        }

        public void bind(TaskItemDto task) {
            taskTitle.setText(task.getTitle());
            taskDescription.setText(task.getDescription());
            taskTimeStatus.setText(task.getFormattedTimeStatus());

            try {
                taskColorIndicator.setBackgroundColor(Color.parseColor(task.getColor()));
            } catch (Exception e) {
                taskColorIndicator.setBackgroundColor(Color.GRAY);
            }
        }
    }
}
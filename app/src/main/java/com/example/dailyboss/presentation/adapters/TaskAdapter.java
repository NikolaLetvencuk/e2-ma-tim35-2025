package com.example.dailyboss.presentation.adapters;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.data.dao.TaskInstanceDao;
import com.example.dailyboss.data.dao.TaskTemplateDao;
import com.example.dailyboss.data.dto.TaskItemDto;
import com.example.dailyboss.data.repository.TaskInstanceRepositoryImpl;
import com.example.dailyboss.data.repository.TaskTemplateRepositoryImpl;
import com.example.dailyboss.domain.enums.TaskStatus;
import com.example.dailyboss.domain.model.TaskInstance;
import com.example.dailyboss.domain.model.TaskTemplate;
import com.example.dailyboss.service.TaskCompletionService;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskItemDto> tasks;
    private OnItemClickListener listener;
    private OnTaskDeletedListener deleteListener;
    private OnLevelUpListener onLevelUpListener;
    private TaskInstanceDao taskInstanceDao;
    private TaskInstanceRepositoryImpl taskInstanceRepository;
    private TaskTemplateDao taskTemplateDao;
    private Context context;

    public TaskAdapter(Context context, List<TaskItemDto> tasks, OnItemClickListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
        this.taskInstanceDao = new TaskInstanceDao(context);
        this.taskInstanceRepository = new TaskInstanceRepositoryImpl(context);
        this.taskTemplateDao = new TaskTemplateDao(context);
    }

    public TaskAdapter(List<TaskItemDto> tasks) {
        this.tasks = tasks;
        this.listener = null;
    }
    
    public interface OnItemClickListener {
        void onItemClick(TaskItemDto task);
    }
    
    public interface OnTaskDeletedListener {
        void onTaskDeleted();
    }
    
    public interface OnLevelUpListener {
        void onLevelUp(int newLevel, String newTitle);
    }
    
    public void setOnTaskDeletedListener(OnTaskDeletedListener listener) {
        this.deleteListener = listener;
    }
    
    public void setOnLevelUpListener(OnLevelUpListener listener) {
        this.onLevelUpListener = listener;
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
        TaskCompletionService taskCompletionService = new TaskCompletionService(context);
        
        taskCompletionService.setLevelUpCallback(new TaskCompletionService.LevelUpCallback() {
            @Override
            public void onLevelUp(int newLevel, String newTitle) {
                if (onLevelUpListener != null) {
                    onLevelUpListener.onLevelUp(newLevel, newTitle);
                }
            }
        });

        holder.bind(task);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(task);
            }
        });

        holder.btnTaskOptions.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.inflate(R.menu.task_options_menu);
            
            TaskInstanceDao taskInstanceDao = new TaskInstanceDao(context);
            com.example.dailyboss.domain.model.TaskInstance instance = taskInstanceDao.findTaskById(task.getInstanceId());
            
            if (instance != null && (instance.getStatus() == TaskStatus.DONE || instance.getStatus() == TaskStatus.CANCELLED)) {
                popupMenu.getMenu().findItem(R.id.action_done).setVisible(false);
                popupMenu.getMenu().findItem(R.id.action_cancelled).setVisible(false);
                popupMenu.getMenu().findItem(R.id.action_paused).setVisible(false);
                popupMenu.getMenu().findItem(R.id.action_active).setVisible(false);
            } else {
                popupMenu.getMenu().findItem(R.id.action_remove).setVisible(false);
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                
                if (id == R.id.action_remove) {
                    if (instance != null && (instance.getStatus() == TaskStatus.DONE || instance.getStatus() == TaskStatus.CANCELLED)) {
                        boolean deleted = taskInstanceDao.deleteById(task.getInstanceId());
                        if (deleted) {
                            int adapterPosition = holder.getAdapterPosition();
                            if (adapterPosition != RecyclerView.NO_POSITION) {
                                tasks.remove(adapterPosition);
                                notifyItemRemoved(adapterPosition);
                                notifyItemRangeChanged(adapterPosition, tasks.size());
                            }
                            
                            if (deleteListener != null) {
                                deleteListener.onTaskDeleted();
                            }
                            
                            android.widget.Toast.makeText(context, "🗑️ Zadatak obrisan", android.widget.Toast.LENGTH_SHORT).show();
                        } else {
                            android.widget.Toast.makeText(context, "❌ Greška pri brisanju", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        android.widget.Toast.makeText(context, "⚠️ Možeš obrisati samo Done/Cancelled zadatke", android.widget.Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                
                TaskStatus newStatus = null;

                if (id == R.id.action_done) {
                    task.setStatus("Done");
                    newStatus = TaskStatus.DONE;
                } else if (id == R.id.action_cancelled) {
                    task.setStatus("Canceled");
                    newStatus = TaskStatus.CANCELLED;
                } else if (id == R.id.action_paused) {
                    task.setStatus("Paused");
                    newStatus = TaskStatus.PAUSED;
                } else if (id == R.id.action_active) {
                    task.setStatus("Active");
                    newStatus = TaskStatus.ACTIVE;
                }

                if (newStatus != null) {
                    android.util.Log.d("TaskAdapter", "Pozivam completeTaskWithResult za instanceId: " + task.getInstanceId() + ", status: " + newStatus);
                    
                    TaskCompletionService.TaskCompletionResult result =
                            taskCompletionService.completeTaskWithResult(task.getInstanceId(), newStatus);

                    //TaskInstance taskInstance = taskInstanceDao.findTaskById(task.getInstanceId());
                    //TaskTemplate taskTemplate = taskTemplateDao.getById(taskInstance.getTemplateId());
                    //taskInstanceRepository.updateTaskStatus(task.getInstanceId(), newStatus, taskTemplate.getCategoryId());

                    android.util.Log.d("TaskAdapter", "Result - success: " + result.success + ", xpGained: " + result.xpGained + ", leveledUp: " + result.leveledUp);
                    
                    if (result.success) {
                        if (newStatus == TaskStatus.DONE) {
                            String message;
                            if (result.xpGained > 0) {
                                message = String.format("✅ Zadatak završen! +%d XP", result.xpGained);
                            } else {
                                message = "✅ Zadatak završen! (XP = 0)";
                            }
                            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show();
                        } else if (newStatus == TaskStatus.CANCELLED) {
                            android.widget.Toast.makeText(context, "❌ Zadatak otkazan", android.widget.Toast.LENGTH_SHORT).show();
                        } else if (newStatus == TaskStatus.PAUSED) {
                            android.widget.Toast.makeText(context, "⏸️ Zadatak pauziran", android.widget.Toast.LENGTH_SHORT).show();
                        } else if (newStatus == TaskStatus.ACTIVE) {
                            android.widget.Toast.makeText(context, "▶️ Zadatak aktiviran", android.widget.Toast.LENGTH_SHORT).show();
                        }
                        
                        task.setStatus(newStatus.toString());
                        notifyItemChanged(holder.getAdapterPosition());
                        
                        if (deleteListener != null) {
                            deleteListener.onTaskDeleted();
                        }
                    } else {
                        android.util.Log.e("TaskAdapter", "TaskCompletionService vratio success=false");
                        android.widget.Toast.makeText(context, "❌ Greška pri završavanju zadatka", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
                
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
            btnTaskOptions = itemView.findViewById(R.id.btnTaskOptions);
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
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
import com.example.dailyboss.domain.model.Equipment;
import com.example.dailyboss.domain.model.UserEquipment;

import java.util.List;

/**
 * Adapter za prikaz opreme sa mogućnošću aktivacije
 */
public class EquipmentActivationAdapter extends RecyclerView.Adapter<EquipmentActivationAdapter.EquipmentActivationViewHolder> {
    
    private List<UserEquipment> userEquipmentList;
    private List<Equipment> allAvailableEquipment;
    private OnEquipmentActivationListener listener;
    
    public interface OnEquipmentActivationListener {
        void onEquipmentActivated(UserEquipment userEquipment);
        void onEquipmentDeactivated(UserEquipment userEquipment);
    }
    
    public EquipmentActivationAdapter(List<UserEquipment> userEquipmentList, List<Equipment> allAvailableEquipment, OnEquipmentActivationListener listener) {
        this.userEquipmentList = userEquipmentList;
        this.allAvailableEquipment = allAvailableEquipment;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public EquipmentActivationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_equipment_activation, parent, false);
        return new EquipmentActivationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EquipmentActivationViewHolder holder, int position) {
        UserEquipment userEquipment = userEquipmentList.get(position);
        Equipment equipment = findEquipmentById(userEquipment.getEquipmentId());
        
        if (equipment != null) {
            holder.bind(equipment, userEquipment, listener);
        } else {
            holder.bindUnknown(userEquipment, listener);
        }
    }
    
    @Override
    public int getItemCount() {
        return userEquipmentList != null ? userEquipmentList.size() : 0;
    }
    
    public void updateData(List<UserEquipment> newUserEquipmentList, List<Equipment> newAllAvailableEquipment) {
        this.userEquipmentList = newUserEquipmentList;
        this.allAvailableEquipment = newAllAvailableEquipment;
        android.util.Log.d("EquipmentActivationAdapter", "Updated data - User equipment: " + userEquipmentList.size() + ", Available equipment: " + allAvailableEquipment.size());
    }
    
    private Equipment findEquipmentById(String equipmentId) {
        if (allAvailableEquipment == null) {
            android.util.Log.e("EquipmentActivationAdapter", "allAvailableEquipment is null");
            return null;
        }
        
        android.util.Log.d("EquipmentActivationAdapter", "Searching for equipment ID: " + equipmentId + " in " + allAvailableEquipment.size() + " available equipment");
        
        for (Equipment equipment : allAvailableEquipment) {
            android.util.Log.d("EquipmentActivationAdapter", "Checking equipment: " + equipment.getId() + " - " + equipment.getName());
            if (equipment.getId().equals(equipmentId)) {
                android.util.Log.d("EquipmentActivationAdapter", "Found equipment: " + equipment.getName());
                return equipment;
            }
        }
        
        android.util.Log.w("EquipmentActivationAdapter", "Equipment not found: " + equipmentId);
        return null;
    }
    
    static class EquipmentActivationViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivEquipmentIcon;
        private TextView tvEquipmentName;
        private TextView tvEquipmentDescription;
        private TextView tvEquipmentStatus;
        private Button btnActivate;
        private Button btnDeactivate;
        
        public EquipmentActivationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivEquipmentIcon = itemView.findViewById(R.id.ivEquipmentIcon);
            tvEquipmentName = itemView.findViewById(R.id.tvEquipmentName);
            tvEquipmentDescription = itemView.findViewById(R.id.tvEquipmentDescription);
            tvEquipmentStatus = itemView.findViewById(R.id.tvEquipmentStatus);
            btnActivate = itemView.findViewById(R.id.btnActivate);
            btnDeactivate = itemView.findViewById(R.id.btnDeactivate);
        }
        
        public void bind(Equipment equipment, UserEquipment userEquipment, OnEquipmentActivationListener listener) {
            // Postavi sliku
            int iconResource = getIconResource(equipment.getIconPath());
            ivEquipmentIcon.setImageResource(iconResource);
            
            // Postavi tekst
            tvEquipmentName.setText(equipment.getName());
            tvEquipmentDescription.setText(equipment.getDescription());
            
            // Postavi status
            if (userEquipment.isActive()) {
                tvEquipmentStatus.setText("Status: Aktivna");
                tvEquipmentStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                btnActivate.setVisibility(View.GONE);
                btnDeactivate.setVisibility(View.VISIBLE);
            } else {
                tvEquipmentStatus.setText("Status: Neaktivna");
                tvEquipmentStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                btnActivate.setVisibility(View.VISIBLE);
                btnDeactivate.setVisibility(View.GONE);
            }
            
            // Postavi click listener-e
            btnActivate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEquipmentActivated(userEquipment);
                }
            });
            
            btnDeactivate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEquipmentDeactivated(userEquipment);
                }
            });
        }
        
        public void bindUnknown(UserEquipment userEquipment, OnEquipmentActivationListener listener) {
            ivEquipmentIcon.setImageResource(R.drawable.ic_profile);
            tvEquipmentName.setText("Nepoznata Oprema");
            tvEquipmentDescription.setText("Oprema nije pronađena");
            
            if (userEquipment.isActive()) {
                tvEquipmentStatus.setText("Status: Aktivna");
                tvEquipmentStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                btnActivate.setVisibility(View.GONE);
                btnDeactivate.setVisibility(View.VISIBLE);
            } else {
                tvEquipmentStatus.setText("Status: Neaktivna");
                tvEquipmentStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                btnActivate.setVisibility(View.VISIBLE);
                btnDeactivate.setVisibility(View.GONE);
            }
            
            btnActivate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEquipmentActivated(userEquipment);
                }
            });
            
            btnDeactivate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEquipmentDeactivated(userEquipment);
                }
            });
        }
        
        private int getIconResource(String iconPath) {
            if (iconPath == null) return R.drawable.ic_profile;
            
            switch (iconPath) {
                case "ic_potion1":
                    return R.drawable.ic_potion1;
                case "ic_potion2":
                    return R.drawable.ic_potion2;
                case "ic_potion3":
                    return R.drawable.ic_potion3;
                case "ic_potion4":
                    return R.drawable.ic_potion4;
                case "ic_gloves":
                    return R.drawable.ic_gloves;
                case "ic_shield":
                    return R.drawable.ic_shield;
                case "ic_boots":
                    return R.drawable.ic_boots;
                case "ic_sword":
                    return R.drawable.ic_sword;
                default:
                    return R.drawable.ic_profile;
            }
        }
    }
}

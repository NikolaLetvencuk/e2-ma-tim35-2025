package com.example.dailyboss.presentation.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.domain.model.Equipment;
import com.example.dailyboss.domain.model.UserEquipment;
import com.example.dailyboss.data.repository.EquipmentRepository;

import java.util.ArrayList;
import java.util.List;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder> {

    private final Context context;
    private final List<UserEquipment> userEquipmentList;
    private final List<Equipment> allEquipmentList;
    private final OnEquipmentActionListener listener;
    private final String currentUserId;

    public interface OnEquipmentActionListener {
        void onToggleActive(UserEquipment userEquipment, boolean isActive);
        void onUpgradeWeapon(UserEquipment userEquipment, Equipment equipment);
    }

    public EquipmentAdapter(Context context, List<UserEquipment> allUserEquipment, List<Equipment> allEquipmentList,
                            OnEquipmentActionListener listener, String currentUserId) {
        this.context = context;
        this.allEquipmentList = allEquipmentList;
        this.listener = listener;
        this.currentUserId = currentUserId;
        // Filtriramo samo opremu trenutnog korisnika
        this.userEquipmentList = new ArrayList<>();
        for (UserEquipment ue : allUserEquipment) {
            if (currentUserId.equals(ue.getUserId())) {
                this.userEquipmentList.add(ue);
            }
        }
    }
    
    /**
     * Ažuriraj podatke adaptera
     */
    public void updateData(List<UserEquipment> newUserEquipment, List<Equipment> newAllEquipment) {
        android.util.Log.d("EquipmentAdapter", "Ažuriram podatke za userId: " + currentUserId + ", primljeno opreme: " + newUserEquipment.size());
        this.userEquipmentList.clear();
        for (UserEquipment ue : newUserEquipment) {
            android.util.Log.d("EquipmentAdapter", "Proveravam opremu userId: " + ue.getUserId() + " vs currentUserId: " + currentUserId);
            if (currentUserId.equals(ue.getUserId())) {
                this.userEquipmentList.add(ue);
                android.util.Log.d("EquipmentAdapter", "Dodana oprema: " + ue.getEquipmentId());
            }
        }
        android.util.Log.d("EquipmentAdapter", "Ukupno opreme u adapteru: " + this.userEquipmentList.size());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.equipment_item_layout, parent, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        UserEquipment userEquipment = userEquipmentList.get(position);
        Equipment equipment = findEquipmentById(userEquipment.getEquipmentId());

        if (equipment != null) {
            holder.equipmentName.setText(equipment.getName());
            holder.equipmentDescription.setText(equipment.getDescription());

            // Prikaz ikonice iz iconPath
            if (equipment.getIconPath() != null && !equipment.getIconPath().isEmpty()) {
                int drawableId = context.getResources().getIdentifier(
                        equipment.getIconPath(), "drawable", context.getPackageName());
                if (drawableId != 0) {
                    holder.equipmentIcon.setImageResource(drawableId);
                } else {
                    holder.equipmentIcon.setImageResource(R.drawable.ic_launcher_background);
                }
            } else {
                holder.equipmentIcon.setImageResource(R.drawable.ic_launcher_background);
            }

            // Prikaz količine i statusa
            String statusText;
            if (equipment.isConsumable()) {
                statusText = "Količina: " + userEquipment.getQuantity();
                if (userEquipment.isActive()) statusText += " (Aktivno)";
            } else {
                statusText = "Tip: " + equipment.getType();
                statusText += userEquipment.isActive() ? " (Opremljeno)" : " (Nije opremljeno)";
            }

            if (equipment.getBonusType() != null && equipment.getBonusValue() > 0) {
                // Za oružje (sword i bow), prikaži currentBonusValue umesto osnovnog bonusValue
                if (equipment.getId().equals("sword") || equipment.getId().equals("bow")) {
                    statusText += "\nBonus: " + String.format("%.2f", userEquipment.getCurrentBonusValue() * 100) + "% " + equipment.getBonusType().replace("_", " ");
                } else {
                    statusText += "\nBonus: " + String.format("%.0f", equipment.getBonusValue() * 100) + "% " + equipment.getBonusType().replace("_", " ");
                }
            }
            if (equipment.getDurationBattles() > 0) {
                statusText += "\nPreostalo borbi: " + userEquipment.getRemainingDurationBattles();
            } else if (equipment.getDurationDays() > 0) {
                statusText += "\nTraje X dana"; // Može se izračunati po timestamp-u
            }

            holder.equipmentQuantityStatus.setText(statusText);

            // Switch za aktivaciju
            holder.toggleActiveSwitch.setOnCheckedChangeListener(null);
            holder.toggleActiveSwitch.setChecked(userEquipment.isActive());
            holder.toggleActiveSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) listener.onToggleActive(userEquipment, isChecked);
            });

            // Vidljivost switch-a
            if ((equipment.isConsumable() && equipment.getDurationBattles() > 0) || !equipment.isConsumable()) {
                holder.toggleActiveSwitch.setVisibility(View.VISIBLE);
            } else {
                holder.toggleActiveSwitch.setVisibility(View.GONE);
            }

            // Upgrade dugme za oružje (sword i bow)
            if (equipment.getId().equals("sword") || equipment.getId().equals("bow")) {
                holder.btnUpgrade.setVisibility(View.VISIBLE);
                holder.btnUpgrade.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onUpgradeWeapon(userEquipment, equipment);
                    }
                });
            } else {
                holder.btnUpgrade.setVisibility(View.GONE);
            }

        } else {
            holder.equipmentName.setText("Nepoznata Oprema");
            holder.equipmentDescription.setText("ID: " + userEquipment.getEquipmentId());
            holder.equipmentIcon.setImageResource(R.drawable.ic_launcher_background);
            holder.equipmentQuantityStatus.setText("");
            holder.toggleActiveSwitch.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return userEquipmentList.size();
    }

    private Equipment findEquipmentById(String equipmentId) {
        for (Equipment eq : allEquipmentList) {
            if (eq.getId().equals(equipmentId)) return eq;
        }
        return null;
    }

    static class EquipmentViewHolder extends RecyclerView.ViewHolder {
        ImageView equipmentIcon;
        TextView equipmentName, equipmentDescription, equipmentQuantityStatus;
        Switch toggleActiveSwitch;
        android.widget.Button btnUpgrade;

        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            equipmentIcon = itemView.findViewById(R.id.equipment_icon);
            equipmentName = itemView.findViewById(R.id.equipment_name);
            equipmentDescription = itemView.findViewById(R.id.equipment_description);
            equipmentQuantityStatus = itemView.findViewById(R.id.equipment_quantity_status);
            toggleActiveSwitch = itemView.findViewById(R.id.equipment_toggle_active);
            btnUpgrade = itemView.findViewById(R.id.btn_upgrade);
        }
    }
}


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
import com.example.dailyboss.domain.enums.EquipmentBonusType;
import com.example.dailyboss.domain.enums.EquipmentType;
import com.example.dailyboss.domain.model.Equipment;
import com.example.dailyboss.service.EquipmentService;

import java.util.List;

/**
 * Adapter za prikaz opreme u prodavnici
 */
public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {
    
    private List<Equipment> equipmentList;
    private OnShopItemClickListener listener;
    private EquipmentService equipmentService;
    private String userId;
    
    public interface OnShopItemClickListener {
        void onItemClick(Equipment equipment);
    }
    
    public ShopAdapter(List<Equipment> equipmentList, OnShopItemClickListener listener, EquipmentService equipmentService, String userId) {
        this.equipmentList = equipmentList;
        this.listener = listener;
        this.equipmentService = equipmentService;
        this.userId = userId;
    }
    
    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop_equipment, parent, false);
        return new ShopViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Equipment equipment = equipmentList.get(position);
        holder.bind(equipment);
    }
    
    @Override
    public int getItemCount() {
        return equipmentList.size();
    }
    
    class ShopViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvName;
        private TextView tvDescription;
        private TextView tvPrice;
        private TextView tvType;
        private Button btnBuy;
        
        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivEquipmentIcon);
            tvName = itemView.findViewById(R.id.tvEquipmentName);
            tvDescription = itemView.findViewById(R.id.tvEquipmentDescription);
            tvPrice = itemView.findViewById(R.id.tvEquipmentPrice);
            tvType = itemView.findViewById(R.id.tvEquipmentType);
            btnBuy = itemView.findViewById(R.id.btnBuy);
            
            btnBuy.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(equipmentList.get(position));
                }
            });
        }
        
        public void bind(Equipment equipment) {
            tvName.setText(equipment.getName());
            tvDescription.setText(equipment.getDescription());
            
            // Koristi dinamičku cenu umesto hardkodovane
            int dynamicPrice = equipmentService.getEquipmentPrice(equipment.getId(), userId);
            tvPrice.setText(dynamicPrice + " 🪙");
            
            // Postavi tip opreme
            EquipmentType type = EquipmentType.fromCode(equipment.getType());
            tvType.setText(type.getDisplayName());
            
            // Postavi ikonu na osnovu iconPath iz Equipment objekta
            android.util.Log.d("ShopAdapter", "Postavljam sliku za opremu: " + equipment.getName() + ", iconPath: " + equipment.getIconPath());
            
            // Mapiranje starih iconPath vrednosti na nove slike
            int drawableId = 0;
            if (equipment.getIconPath() != null && !equipment.getIconPath().isEmpty()) {
                switch (equipment.getIconPath()) {
                    case "potion_20":
                        drawableId = R.drawable.ic_potion1;
                        break;
                    case "potion_40":
                        drawableId = R.drawable.ic_potion2;
                        break;
                    case "potion_permanent_5":
                        drawableId = R.drawable.ic_potion3;
                        break;
                    case "potion_permanent_10":
                        drawableId = R.drawable.ic_potion4;
                        break;
                    case "gloves":
                        drawableId = R.drawable.ic_gloves;
                        break;
                    case "shield":
                        drawableId = R.drawable.ic_shield;
                        break;
                    case "boots":
                        drawableId = R.drawable.ic_boots;
                        break;
                    case "sword":
                        drawableId = R.drawable.ic_sword;
                        break;
                    case "bow":
                        drawableId = R.drawable.ic_sword;
                        break;
                    default:
                        android.util.Log.e("ShopAdapter", "Nepoznat iconPath: " + equipment.getIconPath());
                        drawableId = R.drawable.ic_launcher_background;
                        break;
                }
                
                android.util.Log.d("ShopAdapter", "Postavljam drawableId: " + drawableId + " za: " + equipment.getName());
                ivIcon.setImageResource(drawableId);
            } else {
                android.util.Log.e("ShopAdapter", "iconPath je null ili prazan za: " + equipment.getName());
                ivIcon.setImageResource(R.drawable.ic_launcher_background);
            }
            
            // Postavi boju na osnovu tipa
            int colorResId = getColorForType(equipment.getType());
            tvType.setTextColor(itemView.getContext().getColor(colorResId));
        }
        
        
        private int getColorForType(String type) {
            switch (type) {
                case "POTION":
                    return R.color.potion_color;
                case "ARMOR":
                    return R.color.armor_color;
                case "WEAPON":
                    return R.color.weapon_color;
                default:
                    return R.color.text_primary;
            }
        }
    }
}

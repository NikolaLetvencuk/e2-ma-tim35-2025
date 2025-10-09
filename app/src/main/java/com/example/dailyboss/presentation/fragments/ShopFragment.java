package com.example.dailyboss.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.data.SharedPreferencesHelper;
import com.example.dailyboss.domain.model.Equipment;
import com.example.dailyboss.presentation.adapters.ShopAdapter;
import com.example.dailyboss.service.EquipmentService;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment za prikaz prodavnice opreme
 */
public class ShopFragment extends Fragment implements ShopAdapter.OnShopItemClickListener {
    
    private RecyclerView recyclerView;
    private ShopAdapter adapter;
    private List<Equipment> availableEquipment = new ArrayList<>();
    private EquipmentService equipmentService;
    private String currentUserId;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicijalizuj servise
        equipmentService = new EquipmentService(requireContext());
        
        // Uzmi trenutnog korisnika
        SharedPreferencesHelper prefs = new SharedPreferencesHelper(requireContext());
        currentUserId = prefs.getLoggedInUserId();
        
        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.rvShop);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Inicijalizuj adapter
        String currentUserId = prefs.getLoggedInUserId();
        adapter = new ShopAdapter(availableEquipment, this, equipmentService, currentUserId);
        recyclerView.setAdapter(adapter);
        
        // Učitaj opremu
        loadEquipment();
    }
    
    private void loadEquipment() {
        // Koristi EquipmentService da učitam opremu
        availableEquipment.clear();
        
        // Za sada koristim hardkodovane podatke, kasnije ćemo učitati iz baze
        // Dodaj napitke
        availableEquipment.add(new Equipment(
                "potion_20_pp", "Napitak Snage (20%)", 
                "Jednokratno povećanje snage za 20%", 
                "potion_20", "POTION", 
                "POWER_POINTS", 0.20, 
                1, 0, 50, true, false
        ));
        
        availableEquipment.add(new Equipment(
                "potion_40_pp", "Napitak Snage (40%)", 
                "Jednokratno povećanje snage za 40%", 
                "potion_40", "POTION", 
                "POWER_POINTS", 0.40, 
                1, 0, 70, true, false
        ));
        
        availableEquipment.add(new Equipment(
                "potion_5_permanent", "Trajni Napitak Snage (5%)", 
                "Trajno povećanje snage za 5%", 
                "potion_permanent_5", "POTION", 
                "POWER_POINTS", 0.05, 
                0, 0, 200, false, true
        ));
        
        availableEquipment.add(new Equipment(
                "potion_10_permanent", "Trajni Napitak Snage (10%)", 
                "Trajno povećanje snage za 10%", 
                "potion_permanent_10", "POTION", 
                "POWER_POINTS", 0.10, 
                0, 0, 1000, false, true
        ));
        
        // Dodaj odeću
        availableEquipment.add(new Equipment(
                "gloves", "Rukavice", 
                "Povećanje snage za 10% (traje 2 borbe)", 
                "gloves", "ARMOR", 
                "POWER_POINTS", 0.10, 
                2, 0, 60, false, true
        ));
        
        availableEquipment.add(new Equipment(
                "shield", "Štit", 
                "Povećanje šanse uspešnog napada za 10% (traje 2 borbe)", 
                "shield", "ARMOR", 
                "ATTACK_CHANCE", 0.10, 
                2, 0, 60, false, true
        ));
        
        availableEquipment.add(new Equipment(
                "boots", "Čizme", 
                "Šansa povećanja broja napada za 40% (traje 2 borbe)", 
                "boots", "ARMOR", 
                "ATTACK_COUNT", 0.40, 
                2, 0, 80, false, true
        ));
        
        adapter.notifyDataSetChanged();
    }
    
    @Override
    public void onItemClick(Equipment equipment) {
        // Kupi opremu
        equipmentService.buyEquipment(currentUserId, equipment.getId(), new EquipmentService.EquipmentOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                // Možda ažuriraj UI da pokažeš novi broj novčića
            }
            
            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}

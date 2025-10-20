package com.example.dailyboss.presentation.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.data.repository.EquipmentRepository;
import com.example.dailyboss.data.repository.UserStatisticRepository;
import com.example.dailyboss.domain.model.Equipment;
import com.example.dailyboss.domain.model.UserEquipment;
import com.example.dailyboss.presentation.adapters.EquipmentActivationAdapter;
import com.example.dailyboss.service.EquipmentService;

import java.util.ArrayList;
import java.util.List;


public class EquipmentActivationFragment extends Fragment implements EquipmentActivationAdapter.OnEquipmentActivationListener {
    
    private static final String TAG = "EquipmentActivationFragment";
    
    private RecyclerView recyclerViewEquipment;
    private Button btnStartBattle;
    private TextView tvActiveEquipment;
    
    private EquipmentRepository equipmentRepository;
    private UserStatisticRepository userStatisticRepository;
    private String currentUserId;
    
    private List<UserEquipment> userEquipmentList;
    private List<Equipment> allAvailableEquipment;
    private EquipmentActivationAdapter adapter;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Context context = requireContext();
        equipmentRepository = new EquipmentRepository(context);
        userStatisticRepository = new UserStatisticRepository(context);
        
        Bundle args = getArguments();
        if (args != null) {
            currentUserId = args.getString("userId");
        }
        
        if (currentUserId == null) {
            Log.e(TAG, "currentUserId is null, trying to get from SharedPreferences");
            com.example.dailyboss.data.SharedPreferencesHelper prefs = new com.example.dailyboss.data.SharedPreferencesHelper(context);
            currentUserId = prefs.getLoggedInUserId();
            Log.d(TAG, "Got currentUserId from SharedPreferences: " + currentUserId);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_equipment_activation, container, false);
        
        recyclerViewEquipment = view.findViewById(R.id.recyclerViewEquipment);
        btnStartBattle = view.findViewById(R.id.btnStartBattle);
        tvActiveEquipment = view.findViewById(R.id.tvActiveEquipment);
        
        recyclerViewEquipment.setLayoutManager(new LinearLayoutManager(getContext()));
        
        userEquipmentList = new ArrayList<>();
        allAvailableEquipment = new ArrayList<>();
        
        adapter = new EquipmentActivationAdapter(userEquipmentList, allAvailableEquipment, this);
        recyclerViewEquipment.setAdapter(adapter);
        
        btnStartBattle.setOnClickListener(v -> startBattle());
        
        loadEquipmentData();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            loadEquipmentData();
        }
    }
    
    private void loadEquipmentData() {
        if (currentUserId == null) {
            Log.e(TAG, "currentUserId is null, cannot load equipment");
            return;
        }
        
        equipmentRepository.getAllUserEquipmentForUser(currentUserId, new EquipmentRepository.UserEquipmentOperationListener() {
            @Override
            public void onSuccess(UserEquipment userEquipment) {
            }
            
            @Override
            public void onSuccessList(List<UserEquipment> userEquipment) {
                userEquipmentList.clear();
                userEquipmentList.addAll(userEquipment);
                
                if (allAvailableEquipment.isEmpty()) {
                    allAvailableEquipment = equipmentRepository.getAllAvailableEquipment();
                    
                    if (allAvailableEquipment.isEmpty()) {
                        Log.d(TAG, "No equipment in database, creating default equipment");
                        EquipmentService equipmentService = new EquipmentService(getContext());
                        allAvailableEquipment = equipmentService.createDefaultEquipment();
                        

                        for (Equipment equipment : allAvailableEquipment) {
                            equipmentRepository.addEquipment(equipment, new EquipmentRepository.EquipmentOperationListener() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "Saved equipment: " + equipment.getName());
                                }
                                
                                @Override
                                public void onFailure(String errorMessage) {
                                    Log.e(TAG, "Failed to save equipment: " + errorMessage);
                                }
                            });
                        }
                    }
                }
                
                Log.d(TAG, "Loaded " + userEquipment.size() + " user equipment items");
                Log.d(TAG, "Loaded " + allAvailableEquipment.size() + " available equipment items");
                
                for (Equipment equipment : allAvailableEquipment) {
                    Log.d(TAG, "Available equipment: " + equipment.getId() + " - " + equipment.getName());
                }
                
                for (UserEquipment userEq : userEquipment) {
                    Log.d(TAG, "User equipment: " + userEq.getEquipmentId());
                }
                
                if (adapter == null) {
                    adapter = new EquipmentActivationAdapter(userEquipmentList, allAvailableEquipment, EquipmentActivationFragment.this);
                    recyclerViewEquipment.setAdapter(adapter);
                } else {
                    adapter.updateData(userEquipmentList, allAvailableEquipment);
                    adapter.notifyDataSetChanged();
                }
                
                updateActiveEquipmentDisplay();
                
                Log.d(TAG, "Loaded " + userEquipment.size() + " equipment items for user: " + currentUserId);
                

                if (userEquipment.isEmpty()) {
                    Log.d(TAG, "User has no equipment, opening Battle directly");
                    startBattle();
                }
            }
            
            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Failed to load user equipment: " + errorMessage);
                Toast.makeText(getContext(), "Greška pri učitavanju opreme: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateActiveEquipmentDisplay() {
        if (userEquipmentList == null) return;
        
        int activeCount = 0;
        StringBuilder activeEquipmentText = new StringBuilder("Aktivna oprema: ");
        
        for (UserEquipment userEquipment : userEquipmentList) {
            if (userEquipment.isActive()) {
                Equipment equipment = findEquipmentById(userEquipment.getEquipmentId());
                if (equipment != null) {
                    if (activeCount > 0) {
                        activeEquipmentText.append(", ");
                    }
                    activeEquipmentText.append(equipment.getName());
                    activeCount++;
                }
            }
        }
        
        if (activeCount == 0) {
            activeEquipmentText.append("Nema aktivne opreme");
        }
        
        tvActiveEquipment.setText(activeEquipmentText.toString());
    }
    
    private Equipment findEquipmentById(String equipmentId) {
        if (allAvailableEquipment == null) return null;
        
        for (Equipment equipment : allAvailableEquipment) {
            if (equipment.getId().equals(equipmentId)) {
                return equipment;
            }
        }
        return null;
    }
    
    @Override
    public void onEquipmentActivated(UserEquipment userEquipment) {

        Equipment equipment = findEquipmentById(userEquipment.getEquipmentId());
        if (equipment != null) {

            userEquipment.setRemainingDurationBattles(equipment.getDurationBattles());
            Log.d(TAG, "Setting duration for " + equipment.getName() + " to " + equipment.getDurationBattles() + " battles");
        }
        
        userEquipment.setActive(true);
        equipmentRepository.updateUserEquipment(userEquipment);
        
        for (int i = 0; i < userEquipmentList.size(); i++) {
            if (userEquipmentList.get(i).getId().equals(userEquipment.getId())) {
                userEquipmentList.set(i, userEquipment);
                break;
            }
        }
        
        updateActiveEquipmentDisplay();
        adapter.notifyDataSetChanged();
        
        String equipmentName = equipment != null ? equipment.getName() : "Nepoznata oprema";
        
        Toast.makeText(getContext(), "Aktivirana oprema: " + equipmentName, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Activated equipment: " + equipmentName);
    }
    
    @Override
    public void onEquipmentDeactivated(UserEquipment userEquipment) {

        userEquipment.setActive(false);
        equipmentRepository.updateUserEquipment(userEquipment);
        

        for (int i = 0; i < userEquipmentList.size(); i++) {
            if (userEquipmentList.get(i).getId().equals(userEquipment.getId())) {
                userEquipmentList.set(i, userEquipment);
                break;
            }
        }
        

        updateActiveEquipmentDisplay();
        adapter.notifyDataSetChanged();
        
        Equipment equipment = findEquipmentById(userEquipment.getEquipmentId());
        String equipmentName = equipment != null ? equipment.getName() : "Nepoznata oprema";
        
        Toast.makeText(getContext(), "Deaktivirana oprema: " + equipmentName, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Deactivated equipment: " + equipmentName);
    }
    
    private void startBattle() {
        BattleFragment battleFragment = new BattleFragment();
        Bundle args = new Bundle();
        args.putString("userId", currentUserId);
        battleFragment.setArguments(args);
        
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, battleFragment)
                .addToBackStack(null)
                .commit();
    }
}

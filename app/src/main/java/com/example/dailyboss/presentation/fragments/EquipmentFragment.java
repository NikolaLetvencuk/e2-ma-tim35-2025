package com.example.dailyboss.presentation.fragments; // Prilagodi package name

import android.os.Bundle;
import android.util.Log;
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
import com.example.dailyboss.data.repository.EquipmentRepository;
import com.example.dailyboss.data.repository.UserEquipmentRepository;
import com.example.dailyboss.domain.model.Equipment;
import com.example.dailyboss.domain.model.UserEquipment;
import com.example.dailyboss.presentation.adapters.EquipmentAdapter;

import java.util.ArrayList;
import java.util.List;

public class EquipmentFragment extends Fragment implements EquipmentAdapter.OnEquipmentActionListener {

    private RecyclerView recyclerView;
    private EquipmentAdapter adapter;
    private List<UserEquipment> currentUserEquipment = new ArrayList<>();
    private List<Equipment> allAvailableEquipment = new ArrayList<>(); // Svi Equipment objekti
    private String currentUserId;
    private EquipmentRepository equipmentRepository;

    public EquipmentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equipment, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        equipmentRepository = new EquipmentRepository(getContext());

        recyclerView = view.findViewById(R.id.equipment_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getArguments() != null && getArguments().getString("userId") != null) {
            currentUserId = getArguments().getString("userId");
            Log.d("EquipmentFragment", "Postavljen2 currentUserId iz arguments: " + currentUserId);
            adapter = new EquipmentAdapter(getContext(), currentUserEquipment, allAvailableEquipment, this, currentUserId);
            recyclerView.setAdapter(adapter);

            if (currentUserId != null) {
                loadEquipmentData();
            } else {
                Log.e("EquipmentFragment", "Nije moguće dobiti currentUserId!");
            }
        } else {
            com.example.dailyboss.data.SharedPreferencesHelper prefs = new com.example.dailyboss.data.SharedPreferencesHelper(getContext());
            currentUserId = prefs.getLoggedInUserId();
            Log.d("EquipmentFragment", "Postavljen currentUserId iz SharedPreferences: " + currentUserId);
        }
    }




    private void loadEquipmentData() {
        Log.d("EquipmentFragment", "Učitavam opremu za korisnika: " + currentUserId);

        com.example.dailyboss.service.EquipmentService equipmentService = new com.example.dailyboss.service.EquipmentService(getContext());
        allAvailableEquipment.clear();
        allAvailableEquipment.addAll(equipmentService.createDefaultEquipment());
        Log.d("EquipmentFragment", "Učitano " + allAvailableEquipment.size() + " dostupnih opreme");
        
        equipmentRepository.getAllUserEquipmentForUser(currentUserId, new EquipmentRepository.UserEquipmentOperationListener() {
            @Override
            public void onSuccess(UserEquipment userEquipment) {
                Log.d("EquipmentFragment", "onSuccess(UserEquipment) pozvana, ignorisano za ucitavanje liste.");
            }
            @Override
            public void onSuccessList(List<UserEquipment> userEquipmentList) {
                Log.d("EquipmentFragment", "Pronađeno " + userEquipmentList.size() + " opreme za korisnika: " + currentUserId);

                currentUserEquipment.clear();
                currentUserEquipment.addAll(userEquipmentList);

                if (adapter != null) {
                    adapter.updateData(currentUserEquipment, allAvailableEquipment);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                if (!isAdded()) return;

                Log.d("EquipmentFragment", "Korisnik nema opreme: " + errorMessage + " za userId: " + currentUserId);

                currentUserEquipment.clear();
                if (adapter != null) {
                    adapter.updateData(currentUserEquipment, allAvailableEquipment);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onToggleActive(UserEquipment userEquipment, boolean isActive) {
        userEquipment.setActive(isActive);

        equipmentRepository.updateUserEquipment(userEquipment);

        adapter.notifyDataSetChanged();

        Equipment equipmentDetails = findEquipmentDetails(userEquipment.getEquipmentId());
        String equipmentName = (equipmentDetails != null) ? equipmentDetails.getName() : "Oprema";

        if (isActive) {
            Toast.makeText(getContext(), equipmentName + " je aktivirana/opremljena!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), equipmentName + " je deaktivirana/skinuta!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpgradeWeapon(UserEquipment userEquipment, Equipment equipment) {
        com.example.dailyboss.service.EquipmentService equipmentService = new com.example.dailyboss.service.EquipmentService(getContext());

        int upgradeCost = equipmentService.getUpgradeCost(currentUserId);
        com.example.dailyboss.data.repository.UserStatisticRepository userStatisticRepository = new com.example.dailyboss.data.repository.UserStatisticRepository(getContext());
        com.example.dailyboss.domain.model.UserStatistic stats = userStatisticRepository.getUserStatistic(currentUserId);

        if (stats == null) {
            Toast.makeText(getContext(), "Greška: Statistike korisnika nisu pronađene", Toast.LENGTH_SHORT).show();
            return;
        }

        if (stats.getCoins() < upgradeCost) {
            Toast.makeText(getContext(), "Nedovoljno novčića za upgrade! Potrebno: " + upgradeCost + ", Imate: " + stats.getCoins(), Toast.LENGTH_LONG).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
            .setTitle("Upgrade Oružja")
            .setMessage("Da li želite da unapredite " + equipment.getName() + "?\n\n" +
                       "Cena: " + upgradeCost + " 🪙\n" +
                       "Trenutni bonus: " + String.format("%.2f", userEquipment.getCurrentBonusValue() * 100) + "%\n" +
                       "Novi bonus: " + String.format("%.2f", (userEquipment.getCurrentBonusValue() + 0.0001) * 100) + "%")
            .setPositiveButton("Upgrade", (dialog, which) -> {
                boolean success = equipmentService.upgradeWeapon(currentUserId, userEquipment.getEquipmentId());
                if (success) {
                    Toast.makeText(getContext(), equipment.getName() + " je uspešno unapređen!", Toast.LENGTH_SHORT).show();

                    loadEquipmentData();
                } else {
                    Toast.makeText(getContext(), "Greška pri upgrade-u oružja", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Otkaži", null)
            .show();
    }

    private Equipment findEquipmentDetails(String equipmentId) {
        for (Equipment eq : allAvailableEquipment) {
            if (eq.getId().equals(equipmentId)) {
                return eq;
            }
        }
        return null;
    }
}
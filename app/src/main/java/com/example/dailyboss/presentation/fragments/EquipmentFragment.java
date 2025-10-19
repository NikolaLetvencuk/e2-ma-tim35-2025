package com.example.dailyboss.presentation.fragments; // Prilagodi package name

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

    public EquipmentFragment() {
        // Obavezni prazan konstruktor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_equipment, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.equipment_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getArguments() != null) {
            currentUserId = getArguments().getString("userId");
            loadEquipmentData();
        }
        // Inicijalizacija adaptera
        adapter = new EquipmentAdapter(getContext(), currentUserEquipment, allAvailableEquipment, this);
        recyclerView.setAdapter(adapter);

        // Učitavanje podataka (ovo bi u realnoj aplikaciji došlo iz ViewModel-a, baze, API-ja...)
    }

    private void loadEquipmentData() {
        UserEquipmentRepository userEquipmentRepo = new UserEquipmentRepository(getContext());
        EquipmentRepository equipmentRepo = new EquipmentRepository(getContext());

        // 1. Dohvati sve opreme dostupne u bazi
        List<Equipment> allEquipment = equipmentRepo.getAllAvailableEquipment();
        if (allEquipment != null) {
            allAvailableEquipment.clear();
            allAvailableEquipment.addAll(allEquipment);
        }

        // 2. Dohvati sve UserEquipment za trenutnog korisnika
        userEquipmentRepo.getAllUserEquipmentForUser(currentUserId, new UserEquipmentRepository.EquipmentDataListener() {
            @Override
            public void onSuccess(UserEquipment userEquipment) {
                currentUserEquipment.clear();
                currentUserEquipment.add(userEquipment);

                // Ažuriraj adapter sa stvarnim podacima iz baze
                if (adapter != null) {
                    adapter.updateData(currentUserEquipment, allAvailableEquipment);
                }
            }

            @Override
            public void onSuccess(List<UserEquipment> userEquipmentList) {
                currentUserEquipment.clear();
                currentUserEquipment.addAll(userEquipmentList);

                // Ažuriraj adapter sa stvarnim podacima iz baze
                if (adapter != null) {
                    adapter.updateData(currentUserEquipment, allAvailableEquipment);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Greška pri učitavanju opreme: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onToggleActive(UserEquipment userEquipment, boolean isActive) {
        // Ova metoda se poziva kada korisnik prebaci switch za opremu
        // Ovde bi trebalo da ažuriraš stanje u bazi podataka/ViewModel-u
        userEquipment.setActive(isActive);
        adapter.notifyDataSetChanged(); // Obavesti adapter o promeni

        Equipment equipmentDetails = findEquipmentDetails(userEquipment.getEquipmentId());
        String equipmentName = (equipmentDetails != null) ? equipmentDetails.getName() : "Oprema";

        if (isActive) {
            Toast.makeText(getContext(), equipmentName + " je aktivirana/opremljena!", Toast.LENGTH_SHORT).show();
            // Implementirati logiku za aktiviranje bonusa
        } else {
            Toast.makeText(getContext(), equipmentName + " je deaktivirana/skinuta!", Toast.LENGTH_SHORT).show();
            // Implementirati logiku za deaktiviranje bonusa
        }
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
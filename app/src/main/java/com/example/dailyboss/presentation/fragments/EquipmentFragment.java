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

        // Inicijalizuj repository
        equipmentRepository = new EquipmentRepository(getContext());

        recyclerView = view.findViewById(R.id.equipment_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 1. DOBIJANJE KORISNIČKOG ID-ja (Prioritet Arguments, pa SharedPreferences)
        // OVO JE SADA ISPRAVNO I NE TREBA GA MENJATI
        if (getArguments() != null && getArguments().getString("userId") != null) {
            currentUserId = getArguments().getString("userId");
            Log.d("EquipmentFragment", "Postavljen2 currentUserId iz arguments: " + currentUserId);
            // 2. INICIJALIZACIJA ADAPTERA (Mora biti pre loadEquipmentData, ali bez podataka)
            adapter = new EquipmentAdapter(getContext(), currentUserEquipment, allAvailableEquipment, this, currentUserId);
            recyclerView.setAdapter(adapter);

            // 3. UČITAVANJE PODATAKA
            if (currentUserId != null) {
                loadEquipmentData();
            } else {
                Log.e("EquipmentFragment", "Nije moguće dobiti currentUserId!");
            }
        } else {
            // Fallback: dobij userId iz SharedPreferences
            com.example.dailyboss.data.SharedPreferencesHelper prefs = new com.example.dailyboss.data.SharedPreferencesHelper(getContext());
            currentUserId = prefs.getLoggedInUserId();
            Log.d("EquipmentFragment", "Postavljen currentUserId iz SharedPreferences: " + currentUserId);
        }
    }



    // U EquipmentFragment.java

    private void loadEquipmentData() {
        Log.d("EquipmentFragment", "Učitavam opremu za korisnika: " + currentUserId);

        // 1. UČITAJ SVE DOSTUPNE OPREME
        com.example.dailyboss.service.EquipmentService equipmentService = new com.example.dailyboss.service.EquipmentService(getContext());
        allAvailableEquipment.clear();
        // 💡 NAPOMENA: Ako su vam Equipment podaci statični, ovo je ok. Ako nisu,
        // i ovo bi trebalo da ide kroz asinhroni repozitorijum.
        allAvailableEquipment.addAll(equipmentService.createDefaultEquipment());
        Log.d("EquipmentFragment", "Učitano " + allAvailableEquipment.size() + " dostupnih opreme");
        
        // Dohvati sve UserEquipment za trenutnog korisnika
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

                // 🚀 KRITIČNO: Ažuriraj adapter sa novim podacima
                if (adapter != null) {
                    // Kada se podaci konačno učitaju, OBAVEZNO obavesti adapter
                    adapter.updateData(currentUserEquipment, allAvailableEquipment);
                    adapter.notifyDataSetChanged(); // Dodajte notifyDataSetChanged() ako updateData to ne radi
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                if (!isAdded()) return;

                Log.d("EquipmentFragment", "Korisnik nema opreme: " + errorMessage + " za userId: " + currentUserId);

                // 🚀 Čak i ako korisnik nema opremu, moramo osvežiti adapter
                // sa praznom listom korisničke opreme, ali sa punom listom SVE opreme
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
        // Ova metoda se poziva kada korisnik prebaci switch za opremu
        // Ovde bi trebalo da ažuriraš stanje u bazi podataka/ViewModel-u
        userEquipment.setActive(isActive);

        // Sačuvaj promene u bazi podataka
        equipmentRepository.updateUserEquipment(userEquipment);

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

    @Override
    public void onUpgradeWeapon(UserEquipment userEquipment, Equipment equipment) {
        // Upgrade oružja
        com.example.dailyboss.service.EquipmentService equipmentService = new com.example.dailyboss.service.EquipmentService(getContext());

        // Proveri da li korisnik ima dovoljno novčića
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

        // Potvrdi upgrade
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
                    // Ažuriraj prikaz
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
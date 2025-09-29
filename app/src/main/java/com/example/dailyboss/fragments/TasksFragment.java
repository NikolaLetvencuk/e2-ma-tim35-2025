package com.example.dailyboss.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.dailyboss.R;

// Pretpostavimo da će TasksFragment služiti kao kontejner i kontrolisati prebacivanje.
// CalendarFragment već postoji.
// Morate obezbediti drugi fragment (npr. TaskListContentFragment) za prikaz liste.
// Za demonstraciju, koristiću TaskListFragment za listu.
// Ako nemate TaskListFragment, moraćete da ga kreirate.

public class TasksFragment extends Fragment {

    // Koristimo ovu varijablu za praćenje trenutnog prikaza
    private boolean isCalendarView = false;

    // Potrebna su nam oba fragmenta za prebacivanje
    private Fragment calendarFragment;
    private Fragment listFragment; // Ovo bi trebalo da bude fragment sa RecyclerView-om

    // Novo: Dodajemo reference na nova dugmad
    // Ažurirano: Sada su obični Buttoni (ne ImageButton)
    private Button btnCalendarView;
    private Button btnListView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_task, container, false);

        calendarFragment = new CalendarFragment();
        listFragment = new TaskListFragment();


        // NOVO: Inicijalizacija novih dugmadi kao Button
        btnCalendarView = view.findViewById(R.id.btnCalendarView);
        btnListView = view.findViewById(R.id.btnListView);

        // Postavljanje početnog prikaza (npr. Lista)
        if (savedInstanceState == null) {
            loadFragment(listFragment);
            updateButtonState(false); // false označava Lista je aktivna
        }

        // 1. Logika za Calendar dugme
        btnCalendarView.setOnClickListener(v -> {
            loadFragment(calendarFragment);
            updateButtonState(true); // true označava Kalendar je aktivan
        });

        // 2. Logika za Listu dugme
        btnListView.setOnClickListener(v -> {
            loadFragment(listFragment);
            updateButtonState(false); // false označava Lista je aktivna
        });


        // 3. Logika za "+ Add Task" dugme
        Button btnNewTask = view.findViewById(R.id.btnNewTask);

        if (btnNewTask != null) {
            btnNewTask.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CreateTaskFragment())
                        .addToBackStack(null) // Dodaje transakciju na back stack
                        .commit();
            });
        }

        // 4. Logika za "Back" dugme (ostaje nepromenjena, ali je dugme sakriveno u XML-u)
        // ImageButton btnBack = view.findViewById(R.id.btnBack);
        // btnBack.setOnClickListener(v -> { ... });

        return view;
    }

    // ---

    /**
     * Učitava dati fragment u fragment_container.
     */
    private void loadFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        if (fragment instanceof CalendarFragment) {
            Toast.makeText(getContext(), "Prikaz Kalendara", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Prikaz Liste", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Vizuelno ažurira dugmad (List/Calendar) da bi se signalizirao aktivni prikaz.
     */
    private void updateButtonState(boolean isCalendarActive) {
        // Koristite boje iz vašeg Color resursa ili direktno Android boje
        int activeColor = getResources().getColor(R.color.calendar_black); // 🚨 Definišite u colors.xml
        int inactiveColor = getResources().getColor(android.R.color.darker_gray);

        // Simetrično postavljanje stanja
        if (isCalendarActive) {
            btnCalendarView.setTextColor(activeColor);
            btnListView.setTextColor(inactiveColor);

        } else {
            btnCalendarView.setTextColor(inactiveColor);
            btnListView.setTextColor(activeColor);
        }

        // Možete dodati i logiku za podvlačenje (border bottom)
        // ili promenu boje pozadine (background) da bi izgledalo vernije tab-u.
    }
}
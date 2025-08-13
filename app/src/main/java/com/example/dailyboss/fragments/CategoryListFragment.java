package com.example.dailyboss.fragments;

import android.app.AlertDialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.adapters.CategoryAdapter;
import com.example.dailyboss.model.Category;
import com.example.dailyboss.service.CategoryService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class CategoryListFragment extends Fragment {

    private CategoryService categoryService;
    private CategoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCategories);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.category_item_spacing);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(spacingInPixels, spacingInPixels, spacingInPixels, spacingInPixels);
            }
        });
        categoryService = new CategoryService(getContext());
        List<Category> categories = categoryService.getAllCategories();

        adapter = new CategoryAdapter(categories, (category, pos) -> {
            // otvori edit dialog, ili napravi toast itd.
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddCategory);
        fabAdd.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            View dialogView = inflater.inflate(R.layout.dialog_add_category, null);

            final int[] selectedColor = {0}; // ovde čuvamo izabranu boju

            // Poveži paletu boja
            dialogView.findViewById(R.id.colorRed).setOnClickListener(c -> selectedColor[0] = 0xFFF44336);
            dialogView.findViewById(R.id.colorBlue).setOnClickListener(c -> selectedColor[0] = 0xFF2196F3);
            dialogView.findViewById(R.id.colorGreen).setOnClickListener(c -> selectedColor[0] = 0xFF4CAF50);
            dialogView.findViewById(R.id.colorYellow).setOnClickListener(c -> selectedColor[0] = 0xFFFFEB3B);
            dialogView.findViewById(R.id.colorPurple).setOnClickListener(c -> selectedColor[0] = 0xFF9C27B0);

            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Create new category")
                    .setView(dialogView)
                    .setPositiveButton("Save", (d, which) -> {
                        EditText et = dialogView.findViewById(R.id.editTextCategoryName);
                        String name = et.getText().toString().trim();
                        String colorHex = String.format("#%06X", selectedColor[0]);

                        try {
                            boolean success = categoryService.addCategory(name, colorHex);
                            if (success) {
                                Toast.makeText(requireContext(), "Kategorija uspešno dodata!", Toast.LENGTH_SHORT).show();
                                adapter.updateCategories(categoryService.getAllCategories());
                            } else {
                                Toast.makeText(requireContext(), "Greška pri dodavanju kategorije!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IllegalArgumentException e) {
                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();
        });
    }
}

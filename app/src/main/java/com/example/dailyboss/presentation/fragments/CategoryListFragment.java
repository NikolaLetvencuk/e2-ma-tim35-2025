package com.example.dailyboss.presentation.fragments;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.presentation.adapters.CategoryAdapter;
import com.example.dailyboss.domain.model.Category;
import com.example.dailyboss.data.repository.CategoryRepositoryImpl;
import com.example.dailyboss.data.repository.TaskInstanceRepositoryImpl;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryListFragment extends Fragment {

    private CategoryRepositoryImpl categoryRepositoryImpl;
    private CategoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCategories);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.category_item_spacing);
        int extraBottomMargin = 100;

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int itemCount = parent.getAdapter().getItemCount();
                int spanCount = layoutManager.getSpanCount();

                outRect.set(spacingInPixels, spacingInPixels, spacingInPixels, spacingInPixels);

                if (position >= itemCount - (itemCount % spanCount == 0 ? spanCount : itemCount % spanCount)) {
                    outRect.bottom = extraBottomMargin;
                }
            }
        });

        categoryRepositoryImpl = new CategoryRepositoryImpl(getContext());
        TaskInstanceRepositoryImpl taskInstanceRepositoryImpl = new TaskInstanceRepositoryImpl(getContext());

        List<Category> categories = categoryRepositoryImpl.getAllCategories();

        List<Pair<Category, Integer>> categoriesWithTaskCount = new ArrayList<>();
        for (Category category : categories) {
            int taskCount = taskInstanceRepositoryImpl.countByCategoyId(category.getId());
            categoriesWithTaskCount.add(new Pair<>(category, taskCount));
        }

        adapter = new CategoryAdapter(categoriesWithTaskCount, (category, pos) -> {
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ExtendedFloatingActionButton fabAdd = view.findViewById(R.id.fabAddCategory);
        fabAdd.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void showAddCategoryDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);

        EditText et = dialogView.findViewById(R.id.editTextCategoryName);
        View selectedColorView = dialogView.findViewById(R.id.selectedColorView);
        ColorPickerView colorPickerView = dialogView.findViewById(R.id.colorPickerView);

        final int[] selectedColor = {Color.GRAY};
        selectedColorView.setBackgroundColor(selectedColor[0]);

        colorPickerView.setColorListener(new ColorListener() {
            @Override
            public void onColorSelected(int color, boolean fromUser) {
                selectedColor[0] = color;
                selectedColorView.setBackgroundColor(color);
            }
        });

        LinearLayout row1 = dialogView.findViewById(R.id.suggestedColorsRow1);
        LinearLayout row2 = dialogView.findViewById(R.id.suggestedColorsRow2);

        addSuggestedColorClickListeners(row1, selectedColorView, selectedColor);
        addSuggestedColorClickListeners(row2, selectedColorView, selectedColor);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Create new category")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.show();

        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        saveButton.setOnClickListener(v -> {
            String name = et.getText().toString().trim();
            String colorHex = String.format("#%06X", (0xFFFFFF & selectedColor[0]));

            try {
                boolean success = categoryRepositoryImpl.addCategory(name, colorHex);
                if (success) {
                    Toast.makeText(requireContext(), "Category successfully added!", Toast.LENGTH_SHORT).show();

                    List<Category> newCategories = categoryRepositoryImpl.getAllCategories();
                    TaskInstanceRepositoryImpl taskInstanceRepositoryImpl = new TaskInstanceRepositoryImpl(getContext());
                    List<Pair<Category, Integer>> newCategoriesWithCount = new ArrayList<>();
                    for (Category category : newCategories) {
                        int taskCount = taskInstanceRepositoryImpl.countByCategoyId(String.valueOf(category.getId()));
                        newCategoriesWithCount.add(new Pair<>(category, taskCount));
                    }
                    adapter.updateCategories(newCategoriesWithCount);

                    dialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Error adding category!", Toast.LENGTH_SHORT).show();
                }
            } catch (IllegalArgumentException e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSuggestedColorClickListeners(LinearLayout row, View selectedColorView, int[] selectedColor) {
        for (int i = 0; i < row.getChildCount(); i++) {
            View colorView = row.getChildAt(i);
            colorView.setOnClickListener(v -> {
                selectedColor[0] = ((ColorDrawable) colorView.getBackground()).getColor();
                selectedColorView.setBackgroundColor(selectedColor[0]);
            });
        }
    }
}
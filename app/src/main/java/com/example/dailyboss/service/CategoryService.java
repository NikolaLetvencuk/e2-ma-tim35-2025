package com.example.dailyboss.service;

import android.content.Context;
import android.graphics.Color;

import com.example.dailyboss.data.CategoryDao;
import com.example.dailyboss.model.Category;

import java.util.List;
import java.util.UUID;

public class CategoryService {

    private final CategoryDao categoryDAO;

    public CategoryService(Context context) {

        this.categoryDAO = new CategoryDao(context);
    }

    /**
     * Dodaje novu kategoriju nakon validacije.
     * @param name naziv kategorije (ne sme biti prazan)
     * @param color boja kategorije (ne sme biti zauzeta)
     * @return true ako je uspešno dodat, false ako nije
     * @throws IllegalArgumentException ako validacija nije prošla
     */

    public boolean addCategory(String name, String color) throws IllegalArgumentException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        if (color == null || isColorSimilar(color)) {
            throw new IllegalArgumentException("Color has already taken");
        }

        String id = UUID.randomUUID().toString();
        Category category = new Category(id, color, name);
        android.util.Log.d("CategoryService",
                "Dodajem kategoriju: ID=" + category.getId() + ", Name=" + category.getName() + ", Color=" + category.getColor());
        return  categoryDAO.insert(category);
    }

    public List<Category> getAllCategories() {
        return categoryDAO.getAll();
    }

    private static final double COLOR_THRESHOLD = 20.0; // prag razlike, manji = strožije

    private boolean isColorSimilar(String newColorHex) {
        List<String> existingColors = categoryDAO.getAllColors();
        int newColorInt = Color.parseColor(newColorHex);

        int newR = Color.red(newColorInt);
        int newG = Color.green(newColorInt);
        int newB = Color.blue(newColorInt);

        for (String existingHex : existingColors) {
            int existingInt = Color.parseColor(existingHex);
            int r = Color.red(existingInt);
            int g = Color.green(existingInt);
            int b = Color.blue(existingInt);

            double distance = Math.sqrt(Math.pow(newR - r, 2) +
                    Math.pow(newG - g, 2) +
                    Math.pow(newB - b, 2));
            if (distance <= COLOR_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    /**
     * Menja boju postojeće kategorije ako nova boja nije zauzeta.
     * @param id ID kategorije za izmenu
     * @param newColor nova boja
     * @return true ako je uspešno ažurirano
     * @throws IllegalArgumentException ako je boja zauzeta
     */
    public boolean updateCategoryColor(String id, String newColor) throws IllegalArgumentException {
        List<Category> categories = categoryDAO.getAll();
        for (Category category : categories) {
            if (category.getColor().equalsIgnoreCase(newColor) && !category.getId().equals(id)) {
                throw new IllegalArgumentException("Color has been already taken");
            }
        }

        return categoryDAO.updateColor(id, newColor);
    }
}

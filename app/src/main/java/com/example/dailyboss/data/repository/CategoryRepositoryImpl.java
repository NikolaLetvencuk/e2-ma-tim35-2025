package com.example.dailyboss.data.repository;

import android.content.Context;
import android.graphics.Color;

import com.example.dailyboss.data.SharedPreferencesHelper;
import com.example.dailyboss.data.dao.CategoryDao;
import com.example.dailyboss.domain.model.Category;
import com.example.dailyboss.domain.repository.ICategoryRepository;

import java.util.List;
import java.util.UUID;

public class CategoryRepositoryImpl implements ICategoryRepository {

    private final CategoryDao categoryDao;
    private final Context context;
    private final SharedPreferencesHelper prefs;
    private String userId;

    public CategoryRepositoryImpl(Context context) {
        this.categoryDao = new CategoryDao(context);
        this.prefs = new SharedPreferencesHelper(context);
        this.context = context.getApplicationContext();
        this.userId = prefs.getLoggedInUserId();
    }

    @Override
    public boolean addCategory(String name, String color) throws IllegalArgumentException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        if (color == null || isColorSimilar(color)) {
            throw new IllegalArgumentException("Color has already taken");
        }

        String id = UUID.randomUUID().toString();
        Category category = new Category(id, color, name, userId);
        android.util.Log.d("CategoryService",
                "Dodajem kategoriju: ID=" + category.getId() + ", Name=" + category.getName() + ", Color=" + category.getColor());
        return categoryDao.insert(category);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryDao.getAll();
    }

    @Override
    public boolean updateCategoryColor(String id, String newColor) throws IllegalArgumentException {
        List<Category> categories = categoryDao.getAll();
        for (Category category : categories) {
            if (category.getColor().equalsIgnoreCase(newColor) && !category.getId().equals(id)) {
                throw new IllegalArgumentException("Color has been already taken");
            }
        }
        return categoryDao.updateColor(id, newColor);
    }

    @Override
    public String getColorById(String id) {
        return categoryDao.getColorById(id);
    }

    private static final double COLOR_THRESHOLD = 20.0;

    private boolean isColorSimilar(String newColorHex) {
        List<String> existingColors = categoryDao.getAllColors();
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
}
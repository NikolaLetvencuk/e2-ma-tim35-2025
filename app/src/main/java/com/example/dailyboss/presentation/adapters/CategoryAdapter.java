package com.example.dailyboss.presentation.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.domain.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Category category, int position);
    }

    private final List<Pair<Category, Integer>> categoriesWithCount;
    private final OnItemClickListener listener;

    public CategoryAdapter(List<Pair<Category, Integer>> categoriesWithCount, OnItemClickListener listener) {
        this.categoriesWithCount = categoriesWithCount;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        View calendarView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_calendar, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Pair<Category, Integer> item = categoriesWithCount.get(position);
        Category category = item.first;
        int taskCount = item.second;

        holder.name.setText(category.getName());
        holder.textTaskCount.setText(String.valueOf(taskCount) + " tasks");

        GradientDrawable bgDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {Color.parseColor(category.getColor()), Color.WHITE}
        );
        bgDrawable.setCornerRadius(16f);
        holder.categoryItemLayout.setBackground(bgDrawable);
        try {
            bgDrawable.setColor(Color.parseColor(category.getColor()));
        } catch (IllegalArgumentException e) {
            bgDrawable.setColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v ->
                listener.onItemClick(category, holder.getAdapterPosition())
        );
    }

    @Override
    public int getItemCount() {
        return categoriesWithCount.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        View categoryItemLayout;
        TextView textTaskCount;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textCategoryName);
            categoryItemLayout = itemView.findViewById(R.id.categoryItemLayout);
            textTaskCount = itemView.findViewById(R.id.textTaskCount);
        }
    }

    public void updateCategories(List<Pair<Category, Integer>> newCategoriesWithCount) {
        categoriesWithCount.clear();
        categoriesWithCount.addAll(newCategoriesWithCount);
        notifyDataSetChanged();
    }
}
package com.example.files.adapters;

import static com.example.files.MainActivity.closeAllFragments;
import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.OpenCategory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.files.R;
import com.example.files.utils.MainActivityUtils.Categories;
import com.example.files.models.CategoryViewHolder;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoryViewHolder> {

    List<Categories.Category> categories;
    Context context;
    Context getContext() {
        return context;
    }

    public CategoriesAdapter(List<Categories.Category> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new CategoryViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Categories.Category category = categories.get(position);
        holder.title.setText(category.title);
        holder.icon.setImageDrawable(category.icon);

        holder.itemView.setOnClickListener(v -> {
            if (instance.fragmentInLayout()) closeAllFragments();
            if (instance.permissionGranted()) OpenCategory(category.category);
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }
}

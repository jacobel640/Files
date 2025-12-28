package com.example.files.models;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.files.R;

public class CategoryViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public ImageView icon;

    public CategoryViewHolder(@NonNull View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.tv_category);
        icon = itemView.findViewById(R.id.iv_category);
    }
}

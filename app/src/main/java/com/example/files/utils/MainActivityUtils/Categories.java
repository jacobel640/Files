package com.example.files.utils.MainActivityUtils;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.showCategories;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.files.R;
import com.example.files.adapters.CategoriesAdapter;

import java.util.Arrays;
import java.util.List;

public class Categories {

    private final LinearLayout categoriesSection;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    public int calculateNoOfColumns() {
        if (instance.getResources().getConfiguration()
                .isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)) return 1;

        DisplayMetrics displayMetrics = instance.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 120; // You can vary the value held by the scalingFactor
        // variable. The smaller it is the more no. of columns you can display, and the
        // larger the value the less no. of columns will be calculated. It is the scaling
        // factor to tweak to your needs.
        int columnCount = (int) (dpWidth / scalingFactor);
        return (Math.max(columnCount, 3)); // if column no. is less than 2, we still display 2 columns
    }

    @SuppressLint({"InflateParams", "UseCompatLoadingForDrawables"})
    public Categories(LinearLayout categoriesSection) {

        this.categoriesSection = categoriesSection;
    }

    public Categories setOrRefreshCategories() {

        categoriesSection.setVisibility(showCategories ? View.VISIBLE : View.GONE);

        RecyclerView recyclerView = categoriesSection.findViewById(R.id.rv_categories);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(calculateNoOfColumns(), StaggeredGridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(staggeredGridLayoutManager);
//        gridLayout.setUseDefaultMargins(true);
//        gridLayout.removeAllViews();

        List<Category> categories = Arrays.asList(
                new Category("picture", getString(R.string.pictures), getDrawable(R.drawable.ctg_photo)),
                new Category("audio", getString(R.string.audio), getDrawable(R.drawable.ctg_audio)),
                new Category("video", getString(R.string.video), getDrawable(R.drawable.ctg_video)),
                new Category("apk", getString(R.string.installations), getDrawable(R.drawable.ctg_apk)),
                new Category("downloads", getString(R.string.downloads), getDrawable(R.drawable.ctg_downloads)),
                new Category("archive", getString(R.string.compressed), getDrawable(R.drawable.ctg_archive)));

        CategoriesAdapter adapter = new CategoriesAdapter(categories);
        recyclerView.setAdapter(adapter);

        return this;
    }

    public void refreshCategories(boolean rotate) {

        if (rotate) {
            if (staggeredGridLayoutManager != null)
                staggeredGridLayoutManager.setSpanCount(calculateNoOfColumns());
        } else categoriesSection.setVisibility(showCategories ? View.VISIBLE : View.GONE);
    }

    public int calculateGridColumns() {
        DisplayMetrics displayMetrics = instance.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 120;
        int columnCount = (int) (dpWidth / scalingFactor);
        return (Math.max(columnCount, 3)); // if column no. is less than 2, we still display 2 columns
    }

    public static class Category {
        public String category;
        public String title;
        public Drawable icon;

        @SuppressLint("InflateParams")
        Category(String category, String title, Drawable drawable) {
            this.category = category;
            this.title = title;
            this.icon = drawable;
        }
    }
    
    public static String getString(int resId) {
        return instance.getString(resId);
    }
    
    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getDrawable(int resId) {
        return instance.getDrawable(resId);
    }
}

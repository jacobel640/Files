package com.example.files.view;

import static com.example.files.Statics.dpToPixels;
import static com.example.files.Statics.hasNavigationBar;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

public class ViewUtils {

    public static void refreshRecyclerPadding(RecyclerView recyclerView, boolean addSpace) {
        if (recyclerView == null) {
            Log.d("##### refreshRecyclerPadding #####", "recyclerView is null");
            return;
        }
        float bottom = hasNavigationBar() ? 30f : 20;
        if (addSpace) bottom = hasNavigationBar() ? 105f : 95f;
        recyclerView.setPadding(0, dpToPixels(145f), 0, dpToPixels(bottom));
//        Log.d("##### ViewUtils.refreshRecyclerPadding #####", "hasNavigationBar: " + hasNavigationBar());
    }
}

package com.example.files.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearSmoothScroller;

public class CenterSmoothScroller extends LinearSmoothScroller {

    public CenterSmoothScroller(Context context) {
        super(context);
    }

    @Override
    public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
        return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
    }

    @Override
    public float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        float MILLISECONDS_PER_INCH = 150f;
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
    }
}

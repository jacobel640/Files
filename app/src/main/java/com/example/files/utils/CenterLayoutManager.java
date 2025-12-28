package com.example.files.utils;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CenterLayoutManager extends LinearLayoutManager {

    public CenterLayoutManager(Context context) {
        super(context);
    }

    public void smoothScrollToCenter(RecyclerView recyclerView, RecyclerView.State state, int position) {
        CenterSmoothScroller centerSmoothScroller = new CenterSmoothScroller(recyclerView.getContext());
        centerSmoothScroller.setTargetPosition(position);
        startSmoothScroll(centerSmoothScroller);
    }

    public void scrollToCenter(Context context, int position) {
        int height = context.getResources().getDisplayMetrics().heightPixels;
        int offset = height/2;

        //if you know the item height, you can place the center of the item at the center of the screen
        //  by subtracting half the height of that item from the offset:
//        val height = getApplicationContext().resources.displayMetrics.heightPixels
//        //(say item is 40dp tall)
//        val itemHeight = 40F * getApplicationContext().resources.displayMetrics.scaledDensity
//        val offset = height/2 - itemHeight/2

        //depending on if you have a toolbar or other headers above the RecyclerView,
        //  you may want to subtract their height as well:
//        val height = getApplicationContext().resources.displayMetrics.heightPixels
//        //(say item is 40dp tall):
//        val itemHeight = 40F * getApplicationContext().resources.displayMetrics.scaledDensity
//        //(say toolbar is 56dp tall, which is the default action bar height for portrait mode)
//        val toolbarHeight = 56F * getApplicationContext().resources.displayMetrics.scaledDensity
//        val offset = height/2 - itemHeight/2 - toolbarHeight

        //call scrollToPositionWithOffset with the desired offset
        super.scrollToPositionWithOffset(position, offset);
    }
}

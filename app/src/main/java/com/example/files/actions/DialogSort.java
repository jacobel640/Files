package com.example.files.actions;

import static com.example.files.MainActivity.editor;
import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.order;
import static com.example.files.Statics.sort;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.icu.text.Collator;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.files.JFileAdapter;
import com.example.files.R;
import com.example.files.models.JFile;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Locale;

public class DialogSort extends BottomSheetDialog {

    Activity activity;
    ArrayList<JFile> jFileList;
    JFileAdapter jFileAdapter; // Recommended to sort the array before adding it to the adapter to prevent interrupting
    static Collator collator = Collator.getInstance(Locale.getDefault());;
    boolean isNormalSize;

    int checkedOrderButton = order, checkedSortButton = sort;

    public DialogSort() {
        super(instance);
        this.activity = instance;
        this.isNormalSize = isNormalScreen(activity);
    }

    public DialogSort(JFileAdapter jFileAdapter) {
        super(instance);
        this.activity = instance;
        this.jFileAdapter = jFileAdapter;
        this.jFileList = jFileAdapter.jFileList;
        this.isNormalSize = isNormalScreen(activity);
        createDialogSort();
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "NonConstantResourceId"})
    private void createDialogSort() {

        setContentView(R.layout.dialog_sort);
//        setCancelable(true);
//        getWindow().setWindowAnimations(R.style.DialogAnimation);
//        getWindow().setBackgroundDrawable(activity.getDrawable(R.color.transparent));
//        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        if (!activity.getResources().getConfiguration()
//                .isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE))
//            getWindow().setGravity(Gravity.BOTTOM);

        TextView apply = findViewById(R.id.sort_apply);
        TextView cancel = findViewById(R.id.sort_cancel);
        apply.setClipToOutline(true);
        cancel.setClipToOutline(true);

//        RadioGroup groupSort = findViewById(R.id.sort);
//        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(
//                RadioGroup.LayoutParams.MATCH_PARENT,
//                RadioGroup.LayoutParams.WRAP_CONTENT);

        MaterialButtonToggleGroup groupSort = findViewById(R.id.sort_selection);
        switch (sort) {
            case 0:
                groupSort.check(R.id.sort_by_name);
                break;
            case 1:
                groupSort.check(R.id.sort_by_size);
                break;
            case 2:
                groupSort.check(R.id.sort_by_date);
                break;
            case 3:
                groupSort.check(R.id.sort_by_type);
                break;
        }

        groupSort.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                switch (checkedId) {
                    case R.id.sort_by_name:
                        checkedSortButton = 0;
                        Log.d("##### checkedId #####", "sort_by_name");
                        break;
                    case R.id.sort_by_size:
                        checkedSortButton = 1;
                        Log.d("##### checkedId #####", "sort_by_size");
                        break;
                    case R.id.sort_by_date:
                        checkedSortButton = 2;
                        Log.d("##### checkedId #####", "sort_by_date");
                        break;
                    case R.id.sort_by_type:
                        checkedSortButton = 3;
                        Log.d("##### checkedId #####", "sort_by_type");
                        break;
                    default:
                        checkedSortButton = 0;
                        Log.d("##### checkedId #####", "default");
                }
            }
        });

        // add 5 radio buttons to the group
//        RadioButton radioButton;
//        for (int i = 0; i < 4; i++){
//            radioButton = new RadioButton(activity);
//            radioButton.setText(activity.getResources().getStringArray(R.array.sort)[i]);
//            radioButton.setId(i);
//            radioButton.setTextSize(16);
////            if (//!activity.getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE) &&
////                    activity.getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL))
//            if (isNormalSize) radioButton.setPadding(0,40,30,40);
//            if (i == sort) radioButton.setChecked(true);
//            groupSort.addView(radioButton, layoutParams);
//        }

        MaterialButtonToggleGroup groupOrder = findViewById(R.id.order_selection);
        groupOrder.check(order == 0 ? R.id.ascending_order : R.id.descending_order);

        groupOrder.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) checkedOrderButton = checkedId == R.id.ascending_order ? 0 : 1;
        });

        cancel.setOnClickListener(view -> dismiss());
        apply.setOnClickListener(view -> {

            editor.putInt("SORT", checkedSortButton).apply();
            editor.putInt("ORDER", checkedOrderButton).apply();
            dismiss();
            new Handler().post(this::sort);
        });

    }

    private boolean isNormalScreen(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        instance.getWindowManager().getDefaultDisplay().getMetrics(dm);
        //double x = Math.pow(mWidthPixels/dm.xdpi,2);
        //double y = Math.pow(mHeightPixels/dm.ydpi,2);
        //double screenInches = Math.sqrt(x+y);
        return context.getResources().getConfiguration()
                .isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void sort() {
        if (jFileList != null && !jFileList.isEmpty()) compare(jFileList, sort, order == 1);
        jFileAdapter.notifyDataSetChanged();
    }

    public static void sort(ArrayList<JFile> jFileList) {
        if (jFileList != null && !jFileList.isEmpty()) compare(jFileList, sort, order == 1);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void sortAndNotify(JFileAdapter jFileAdapter) {
        if (jFileAdapter.getItemCount() > 0) compare(jFileAdapter.jFileList, sort, order == 1);
        jFileAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void compareAndNotify(JFileAdapter jFileAdapter, int sort, boolean reverse) {
        compare(jFileAdapter.jFileList, sort, reverse);
        jFileAdapter.notifyDataSetChanged();
    }

    // TODO comparings

    public static void compare(ArrayList<JFile> jFiles, int sort, boolean reverse) {
        switch (sort) {
            case 0:
                if (reverse) jFiles.sort(DialogSort::compareNameReverse);
                else jFiles.sort(DialogSort::compareName);
                break;
            case 1:
                if (reverse) jFiles.sort(DialogSort::compareSizeReverse);
                else jFiles.sort(DialogSort::compareSize);
                break;
            case 2:
                if (reverse) jFiles.sort(DialogSort::compareDateReverse);
                else jFiles.sort(DialogSort::compareDate);
                break;
            case 3:
                if (reverse) jFiles.sort(DialogSort::compareTypeReverse);
                else jFiles.sort(DialogSort::compareType);
                break;
        }
    }

    public static void compareRecentFile(ArrayList<JFile> jFiles) {
        jFiles.sort(DialogSort::compareRecentFile);
    }

    public static int compareRecentFile(JFile file1, JFile file2) {

        return Long.compare(file2.lastModified(), file1.lastModified());
    }

    public static int compareName(JFile file1, JFile file2) {

        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        } else if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }

        return compare(file1.getNameTLC(), file2.getNameTLC());
    }

    public static int compareNameReverse(JFile file1, JFile file2) {

        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        } else if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }

        return compare(file2.getNameTLC(), file1.getNameTLC());
    }

    public static int compareSize(JFile file1, JFile file2) {

        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        } else if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }

        int sizeOrder = Long.compare(file1.getSize(), file2.getSize());
        if (sizeOrder != 0) return sizeOrder;
        else return compare(file1.getNameTLC(), file2.getNameTLC());
    }

    public static int compareSizeReverse(JFile file1, JFile file2) {

        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        } else if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }

        int sizeOrder = Long.compare(file2.getSize(), file1.getSize());
        if (sizeOrder != 0) return sizeOrder;
        else return compare(file1.getNameTLC(), file2.getNameTLC());
    }

    public static int compareDate(JFile file1, JFile file2) {

        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        } else if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }

        return Long.compare(file1.lastModified(), file2.lastModified());
    }

    public static int compareDateReverse(JFile file1, JFile file2) {

        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        } else if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }

        return Long.compare(file2.lastModified(), file1.lastModified());
    }

    public static int compareType(JFile file1, JFile file2) {

        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        } else if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }

        int extOrder = file1.getExtension().compareTo(file2.getExtension());
        if (extOrder != 0) return extOrder;
        else return compare(file1.getNameTLC(), file2.getNameTLC());
    }

    public static int compareTypeReverse(JFile file1, JFile file2) {

        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        } else if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }

        int extOrder = file2.getExtension().compareTo(file1.getExtension());
        if (extOrder != 0) return extOrder;
        else return compare(file1.getNameTLC(), file2.getNameTLC());
    }

    public static int compare(JFile file1, JFile file2, int sort, boolean reverse) {

        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        } else if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }

        if (sort == 0) {

            // sort by name
            if (reverse) return compare(file2.getNameTLC(),
                    file1.getNameTLC());
            else return compare(file1.getNameTLC(),
                    file2.getNameTLC());
        } else if (sort == 1) {

            // sort by size
            int sizeOrder;
            if (reverse) sizeOrder = Long.compare(file2.getSize(), file1.getSize());
            else sizeOrder = Long.compare(file1.getSize(), file2.getSize());
            if (sizeOrder != 0) return sizeOrder;
            else return compare(file1.getNameTLC(),
                    file2.getNameTLC());
        } else if (sort == 2) {

            // sort by last modified
            if (reverse) return Long.compare(file2.lastModified(), file1.lastModified());
            else return Long.compare(file1.lastModified(), file2.lastModified());
        } else if (sort == 3) {

            // sort by type
            int extOrder;
            if (reverse) extOrder = file2.getExtension().compareTo(file1.getExtension());
            else extOrder = file1.getExtension().compareTo(file2.getExtension());
            if (extOrder != 0) return extOrder;
            else return compare(file1.getNameTLC(),
                    file2.getNameTLC());
        }

        return 0;
    }

    // http://www.davekoelle.com/files/AlphanumComparator.java
    private static boolean isDigit(char ch) {
        return ((ch >= 48) && (ch <= 57));
    }

    /**
     * Length of string is passed in for improved efficiency (only need to calculate it once)
     **/
    private static String getChunk(String s, int sLength, int marker) {
        StringBuilder chunk = new StringBuilder();
        char c = s.charAt(marker);
        chunk.append(c);
        marker++;
        if (isDigit(c)) {
            while (marker < sLength) {
                c = s.charAt(marker);
                if (!isDigit(c))
                    break;
                chunk.append(c);
                marker++;
            }
        } else {
            while (marker < sLength) {
                c = s.charAt(marker);
                if (isDigit(c))
                    break;
                chunk.append(c);
                marker++;
            }
        }
        return chunk.toString();
    }

    public static int compare(String s1, String s2) {
        if ((s1 == null) || (s2 == null)) {
            return 0;
        }

        int thisMarker = 0;
        int thatMarker = 0;
        int s1Length = s1.length();
        int s2Length = s2.length();

        while (thisMarker < s1Length && thatMarker < s2Length) {
            String thisChunk = getChunk(s1, s1Length, thisMarker);
            thisMarker += thisChunk.length();

            String thatChunk = getChunk(s2, s2Length, thatMarker);
            thatMarker += thatChunk.length();

            // If both chunks contain numeric characters, sort them numerically
            int result;
            if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0))) {
                // Simple chunk comparison by length.
                int thisChunkLength = thisChunk.length();
                result = thisChunkLength - thatChunk.length();
                // If equal, the first different number counts
                if (result == 0) {
                    for (int i = 0; i < thisChunkLength; i++) {
                        result = thisChunk.charAt(i) - thatChunk.charAt(i);
                        if (result != 0) {
                            return result;
                        }
                    }
                }
            } else {
                result = collator.compare(thisChunk, thatChunk);
            }

            if (thisChunk.startsWith(".") && !thatChunk.startsWith(".")) result = 1;
            else if (!thisChunk.startsWith(".") && thatChunk.startsWith(".")) result = -1;

            if (result != 0)
                return result;
        }

        return s1Length - s2Length;
    }

}

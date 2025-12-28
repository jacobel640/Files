package com.example.files.utils.MainActivityUtils;

import static com.example.files.MainActivity.closeAllFragments;
import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.openRecent;
import static com.example.files.Statics.showRecent;
import static com.example.files.fragments.FragmentBase.FragmentType.MAIN;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.files.JFileAdapter;
import com.example.files.R;
import com.example.files.models.JFile;
import com.example.files.models.ViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class RecentFiles {

    LinearLayout recentSection;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    RecyclerView rvRecent;
    ArrayList<JFile> listRecent;
    JFileAdapter adapterRecent;

    public RecentFiles(LinearLayout recentSection) {

        this.recentSection = recentSection;
        this.rvRecent = recentSection.findViewById(R.id.rvRecent);
        recent();
    }


    public RecentFiles recent() {
        if (!instance.getResources().getConfiguration()
                .isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)) {
            listRecent = new ArrayList<>();
            adapterRecent = new JFileAdapter(listRecent, JFileAdapter.ViewType.GRID, MAIN);
            staggeredGridLayoutManager = new StaggeredGridLayoutManager(calculateNoOfColumns(),
                    StaggeredGridLayoutManager.HORIZONTAL);

            if (instance.permissionGranted()) recentScanner();
            LinearLayoutManager manager = new LinearLayoutManager(instance);
            manager.canScrollHorizontally();
            manager.setOrientation(RecyclerView.HORIZONTAL);
            rvRecent.setLayoutManager(manager);
            rvRecent.setAdapter(adapterRecent);
            rvRecent.setClipToOutline(true);
            TextView showAll = instance.findViewById(R.id.show_all);
            showAll.setClipToOutline(true);
            showAll.setOnClickListener(v -> {
                if (instance.fragmentInLayout()) closeAllFragments();
                if (instance.permissionGranted()) openRecent();
                else instance.requestStoragePermissions();
            });

        } else {
            TextView showAll = instance.findViewById(R.id.show_all);
            showAll.setClipToOutline(true);
            showAll.setOnClickListener(v -> {
                if (instance.fragmentInLayout()) closeAllFragments();
                if (instance.permissionGranted()) openRecent();
            });
        }
        return this;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void recentScanner() {

        if (showRecent) recentSection.setVisibility(View.VISIBLE);
        else {
            recentSection.setVisibility(View.GONE);
            return;
        }

//        staggeredGridLayoutManager.setSpanCount(calculateNoOfColumns());

        if (!instance.getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)) {
            listRecent.clear();
            ContentResolver contentResolver = instance.getContentResolver();
            Uri uri;
            uri = MediaStore.Files.getContentUri("external");
            String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " ASC";

            @SuppressLint("Recycle")
            Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder);

            int i = 0, max = 10;
            //looping through all rows and adding to the list
            if (cursor != null && cursor.moveToLast()) {
                do {
                    String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATA));
                    if (filter(new File(String.valueOf(Uri.parse(data))))) {
                        i++;
                        JFile jFile = new JFile(new File(String.valueOf(Uri.parse(data))), instance);
                        adapterRecent.jFileList.add(jFile);
                    }

                } while (cursor.moveToPrevious() && i <= max);
            }
            adapterRecent.notifyDataSetChanged();

            adapterRecent.setAdapterListener(new JFileAdapter.AdapterListener() {
                @Override
                public void onItemClick(ViewHolder holder, int position) {
                    if (position == 10) openRecent();
                }

                @Override
                public void onItemLongClick(ViewHolder holder, int position) {
                    if (position == 10) openRecent();
                }

                @Override
                public void onSelectItemListener(int selectedSize) { }
            });
        }
    }

    public boolean filter(File file) {
        return  file.isFile() &&
                file.getName().contains(".") &&
                !file.isDirectory() && file.length() != 0 &&
                !file.getAbsolutePath().startsWith("/storage/emulated/0/Android") &&
                !Objects.requireNonNull(file.getParentFile()).getPath().endsWith("WhatsApp/Databases") &&
                !file.getParentFile().getPath().endsWith("WhatsApp/Backups") &&
                isRecent(file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase());
    }

    public static boolean isRecent(String fileName) {
        switch (fileName) { // ignored cases:
            case "m3u": // playlist file
            case "log": // logs...
            case "tmp": // temporary files
            case "temp":
            case "bak": // expressions of backup files
            case "bkup":
            case "backup":
                return false;
            default: // else...
                return true;
        }
    }

    public int calculateNoOfColumns() {
        DisplayMetrics displayMetrics = instance.getResources().getDisplayMetrics();
//        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        int scalingFactor = 120; // You can vary the value held by the scalingFactor
        // variable. The smaller it is the more no. of columns you can display, and the
        // larger the value the less no. of columns will be calculated. It is the scaling
        // factor to tweak to your needs.
        int columnCount = (int) (scalingFactor / dpHeight);
        return (Math.max(columnCount, 3)); // if column no. is less than 2, we still display 2 columns
    }

}

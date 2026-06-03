package com.example.files.fragments;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.CATEGORY_VIEW_TYPE;
import static com.example.files.Statics.TAG_RECENT;
import static com.example.files.Statics.multiSelected;
import static com.example.files.fragments.FragmentBase.FragmentType.RECENT;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.example.files.JFileAdapter;
import com.example.files.R;
import com.example.files.Statics;
import com.example.files.utils.AsyncTask;
import com.example.files.models.JFile;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class RecentFragment extends FragmentBase {

    String chosen = "";
    long m60DaysAgo;

    public RecentFragment() {
        super(FragmentType.RECENT);
        this.category = "recent";
        this.uri =  MediaStore.Files.getContentUri("external");
        // Required empty public constructor
        m60DaysAgo = m60DaysAgo();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onCreateView(View v) {

        pathNavigator.tvPath.setText(R.string.recent_files);
        binding.folderName.setText(R.string.recent_files);
    }

    @Override
    @SuppressLint("NotifyDataSetChanged")
    public void loadList() {

        objects = new ArrayList<>();
        new AsyncTask(() -> objects = getRecentFiles(),
                () -> {
                    binding.emptyFolder.setVisibility(objects.isEmpty() ? View.VISIBLE : View.GONE);
                    jFileAdapter = new JFileAdapter(objects, CATEGORY_VIEW_TYPE, RECENT);
                    setListeners();
                    setListListener();
                    binding.rvFiles.setAdapter(jFileAdapter);
                    if (selected) highlightSelectedItem(true);
                    /*else*/ bool = true;
                }).execute();
    }

    public ArrayList<JFile> getRecentFiles() {

        ArrayList<JFile> recent = new ArrayList<>();

        String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED;
        ContentResolver contentResolver = requireActivity().getContentResolver();
        @SuppressLint("Recycle")
        Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder);
        int count = 0;
        if (cursor != null && cursor.moveToLast()) {
            do {
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATA));
                JFile jFile = new JFile(String.valueOf(Uri.parse(data)), instance);
                if (filter(jFile)) {
                    if (count == 2000) break;
                    if (jFile.lastModified() > m60DaysAgo) {
                        recent.add(jFile);
                        count++;
                    }
                    else break;
                }

            } while (cursor.moveToPrevious());
        }
        return recent;
    }

    public JFile getFirst() {
        String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED;
        ContentResolver contentResolver = requireActivity().getContentResolver();
        @SuppressLint("Recycle")
        Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder);
        if (cursor != null && cursor.moveToLast()) {
            String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATA));
            JFile jFile = new JFile(String.valueOf(Uri.parse(data)), instance);

            if (filter(jFile)) return jFile;
        }

        return null;
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
        switch (fileName) {
            case "": // folder
            case "m3u": // playlist file
            case "log": // logs...
            case "tmp": // temporary files
            case "temp":
            case "bak": // expressions of backup files
            case "bkup":
            case "backup":
            case "crypt1": // whatsapp backup files
            case "crypt12":
                return false;
            default: // else...
                return true;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh(){
        if (multiSelected) instance.eventListener.onMultiSelectedChange(false);

        new Handler().post(() -> {
            ArrayList<JFile> remove = new ArrayList<>();
            for (JFile jFile : jFileAdapter.jFileList) {
                if (!jFile.exists()) remove.add(jFile);
            }
            jFileAdapter.jFileList.removeAll(remove);

            JFile latest = getFirst();
            if (latest != null) {
                if (!jFileAdapter.jFileList.get(0).getPath().equals(latest.getPath())) {
                    jFileAdapter.jFileList = getRecentFiles();
                }
            }

            binding.rvFiles.post(() -> jFileAdapter.notifyDataSetChanged());
            binding.emptyFolder.post(() -> binding.emptyFolder.setVisibility
                    (jFileAdapter.jFileList.isEmpty() ? View.VISIBLE : View.GONE));
        });
    }

    public long m60DaysAgo() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -60);
        return calendar.getTimeInMillis();
    }

    public void menuRefresh(){
        new Handler().post(() -> {
            if (bool) {
                if (multiSelected) instance.eventListener.onMultiSelectedChange(false);
                jFileAdapter.jFileList.clear();
                loadList();
            }
        });
    }

    public boolean notVisible() {
        if (!bool) return true;
        return !Statics.isVisible(TAG_RECENT);
        // TODO check if is same fragment by compering stack count with current place on stack
    }

}
package com.example.files.fragments;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.FOLDER_VIEW_TYPE;
import static com.example.files.Statics.TAG_FOLDER;
import static com.example.files.Statics.folder;
import static com.example.files.Statics.showFileSize;
import static com.example.files.Statics.showHiddenFiles;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.example.files.JFileAdapter;
import com.example.files.R;
import com.example.files.Statics;
import com.example.files.utils.AsyncTask;
import com.example.files.actions.DialogSort;
import com.example.files.models.JFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class FilesFragment extends FragmentBase {

    public FilesFragment() {
        super(FragmentType.FILES);
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.parent = folder;
        this.objects = new ArrayList<>();
        this.lastRefresh = new Date().getTime();
        this.isArchive = (parent.getPath().startsWith(
                "/storage/emulated/0/Android/data/com.example.files/files/zips/"));
    }

    @Override
    void onCreateView(View v) {}

    @Override
    public void loadList() {
        new AsyncTask(() -> {
            JFile parent = new JFile(this.parent.getAbsolutePath(), instance);
            if (parent.countFiles() > 0)
                for(JFile jFile : parent.listJFiles()){
                    if(showHiddenFiles) objects.add(jFile);
                    else if(!jFile.isHidden()) objects.add(jFile);
                }
        }, this::loadAdapter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected void loadAdapter() {
        jFileAdapter = new JFileAdapter(objects, FOLDER_VIEW_TYPE, FragmentType.FILES);
        binding.emptyFolder.setVisibility(objects.isEmpty() ? View.VISIBLE : View.GONE);
        setListeners();
        binding.rvFiles.setAdapter(jFileAdapter);
        new DialogSort().sortAndNotify(jFileAdapter);
        if (showFileSize) triggerSizeCounting();
        if (selected) highlightSelectedItem(false);
        LayoutAnimationController layoutAnimation = AnimationUtils
                .loadLayoutAnimation(instance, R.anim.layout_anim_ltr);
        binding.rvLayout.setLayoutAnimation(layoutAnimation);
        Log.d("##### onCreate() #####", "create");
    }

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void refresh() {
        if (!running.compareAndSet(false, true)) {
            Log.d("refresh()", "already running");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            ArrayList<JFile> updatedList = scanFiles();

            requireActivity().runOnUiThread(() -> {
                applyResult(updatedList);
                running.set(false);
            });
        });
    }

    private ArrayList<JFile> scanFiles() {
        File[] files =  parent.listFiles();
        if (files == null) return new ArrayList<>();

        Map<String, JFile> existing = new HashMap<>();
        for (JFile jFile : jFileAdapter.jFileList) {
            if (jFile.exists()) {
                existing.put(jFile.getPath(), jFile);
            }
        }

        ArrayList<JFile> result = new ArrayList<>();

        for (File file : files) {
            if (!showHiddenFiles && file.getName().startsWith(".")) continue;

            JFile jf = existing.remove(file.getPath());
            if (jf != null) {
                result.add(jf);
            } else {
                result.add(new JFile(file, requireActivity()));
            }
        }

        return result;
    }

    private void applyResult(ArrayList<JFile> list) {
        jFileAdapter.jFileList = list;

        binding.emptyFolder.setVisibility(
                list.isEmpty() ? View.VISIBLE : View.GONE
        );

        if (showFileSize) {
            triggerSizeCounting();
        }

        new DialogSort().sortAndNotify(jFileAdapter);
    }

    @Override
    public boolean notVisible() {
        return !Statics.isVisible(TAG_FOLDER);
        // TODO check if is same fragment by compering stack count with current place on stack
    }

}
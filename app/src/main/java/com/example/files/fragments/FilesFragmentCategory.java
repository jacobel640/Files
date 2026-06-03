package com.example.files.fragments;

import static com.example.files.MainActivity.enableTextButton;
import static com.example.files.MainActivity.instance;
import static com.example.files.MainActivity.textBtnState;
import static com.example.files.Statics.CATEGORY_VIEW_TYPE;
import static com.example.files.Statics.TAG_CATEGORY;
import static com.example.files.Statics.multiSelected;
import static com.example.files.fragments.FragmentBase.FragmentType.CATEGORY;
import static com.example.files.models.JFile.Type.ARCHIVE;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.files.JFileAdapter;
import com.example.files.R;
import com.example.files.Statics;
import com.example.files.utils.AsyncTask;
import com.example.files.models.JFile;

import java.util.ArrayList;

public class FilesFragmentCategory extends FragmentBase {

    public FilesFragmentCategory() {
        super(CATEGORY);
        // Required empty public constructor
    }

    public FilesFragmentCategory(String category) {
        super(CATEGORY);
        this.category = category;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.category = savedInstanceState.getString("category", category);
        }

        refresh = new Handler(Looper.getMainLooper());
        new Handler().postDelayed(() -> textBtnState(enableTextButton()), 100);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("category", category);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            this.category = savedInstanceState.getString("category", category);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onCreateView(View v) {

        switch (category){
            case "video":
                pathNavigator.tvPath.setText(R.string.video);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else uri = MediaStore.Files.getContentUri("external");

                break;
            case "audio":
                pathNavigator.tvPath.setText(R.string.audio);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else uri = MediaStore.Files.getContentUri("external");

                break;
            case "picture":
                pathNavigator.tvPath.setText(R.string.pictures);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else uri = MediaStore.Files.getContentUri("external");

                break;
            case "downloads":
                pathNavigator.tvPath.setText(R.string.downloads);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    uri = MediaStore.Downloads.getContentUri("external");
                    // uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                } else uri = MediaStore.Files.getContentUri("external");
                break;
            case "zip":
                pathNavigator.tvPath.setText(R.string.compressed);
                uri = MediaStore.Files.getContentUri("external");
                break;
            case "apk":
                pathNavigator.tvPath.setText(R.string.installations);
                uri = MediaStore.Files.getContentUri("external");
                break;
        }
        binding.folderName.setText(pathNavigator.tvPath.getText());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                category.equals("zip") || category.equals("apk")) type = category;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void refresh() {
        if (multiSelected) instance.eventListener.onMultiSelectedChange(false);
        refresh.post(() -> {
            ArrayList<JFile> remove = new ArrayList<>();
            for (JFile jFile : objects)
                if (!jFile.exists())remove.add(jFile);
            jFileAdapter.jFileList.removeAll(remove);

            binding.rvFiles.post(() -> jFileAdapter.notifyDataSetChanged());
            binding.emptyFolder.post(() -> binding.emptyFolder.setVisibility
                    (jFileAdapter.jFileList.isEmpty() ? View.VISIBLE : View.GONE));
        });
    }

    @Override
    @SuppressLint("NotifyDataSetChanged")
    public void loadList() {

        objects = new ArrayList<>();

        new AsyncTask(() -> setAdapter(type, uri), () -> {
                    toolbar.loading.setVisibility(View.VISIBLE);
                    jFileAdapter = new JFileAdapter(objects, CATEGORY_VIEW_TYPE, CATEGORY);
                    setListeners();
                    setListListener();
                    binding.rvFiles.setAdapter(jFileAdapter);

                    toolbar.loading.setVisibility(View.GONE);
                    binding.emptyFolder.setVisibility(objects.isEmpty() ? View.VISIBLE : View.GONE);
                    bool = true;
                }).execute();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter(String type, Uri uri) {

        String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " ASC";
        ContentResolver contentResolver = instance.getContentResolver();
        @SuppressLint("Recycle")
        Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder);
        Work work = getWork(type);

        if (cursor != null && cursor.moveToLast()) {
            do {
                @SuppressLint("Range")
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATA));
                JFile jFile = new JFile(String.valueOf(Uri.parse(data)), getActivity());
                work.run(jFile);
            } while (cursor.moveToPrevious());
        }
    }

    private @NonNull Work getWork(String type) {
        if (type == null) {
            return new Work() {
                @Override
                public void run(JFile jFile) {
                    if(jFile.length() != 0) objects.add(jFile);
                }
            };
        } else if (type.equals("zip")) {
            return new Work() {
                @Override
                public void run(JFile jFile) {
                    if (jFile.getType() == ARCHIVE && jFile.length() != 0) objects.add(jFile);
                }
            };
        } else {
            return new Work() {
                @Override
                public void run(JFile jFile) {
                    if (jFile.getNameTLC().endsWith("." + type.toLowerCase()) && jFile.isFile()
                            && jFile.length() != 0) objects.add(jFile);
                }
            };
        }
    }

    public boolean notVisible() {
        return !Statics.isVisible(TAG_CATEGORY);
    }

    static abstract class Work {
        abstract void run(JFile jFile);
    }

}
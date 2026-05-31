package com.example.files.activities;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.BYTE;
import static com.example.files.Statics.KB;
import static com.example.files.Statics.MB;
import static com.example.files.activities.StorageAnalyzer.Type.ALL;
import static com.example.files.activities.StorageAnalyzer.Type.APK;
import static com.example.files.activities.StorageAnalyzer.Type.AUDIO;
import static com.example.files.activities.StorageAnalyzer.Type.IMAGE;
import static com.example.files.activities.StorageAnalyzer.Type.OTHER;
import static com.example.files.activities.StorageAnalyzer.Type.SYSTEM;
import static com.example.files.activities.StorageAnalyzer.Type.VIDEO;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.files.R;
import com.example.files.utils.AsyncTask;
import com.example.files.view.AnalyzerItem;
import com.example.files.models.AnalyzerCategory;
import com.example.files.models.JFile;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.util.Objects;

public class StorageAnalyzer extends BaseActivity {

    int typeSize = 0;
    public enum Type {ALL, OTHER, SYSTEM, IMAGE, AUDIO, VIDEO, APK};
    long length = 0;

    AnalyzerCategory all = new AnalyzerCategory(ALL);
    AnalyzerCategory system = new AnalyzerCategory(SYSTEM);
    AnalyzerCategory images = new AnalyzerCategory(IMAGE);
    AnalyzerCategory audio = new AnalyzerCategory(AUDIO);
    AnalyzerCategory video = new AnalyzerCategory(VIDEO);
    AnalyzerCategory apks = new AnalyzerCategory(APK);
    AnalyzerCategory other = new AnalyzerCategory(OTHER);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_analyzer);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);

        MaterialToolbar toolbar = findViewById(R.id.analyzer_toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        new AsyncTask(() -> {
            setAdapter(0);
//            countSize(new File("system/"));
        }, () -> {

            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());

            int totalInt = totalInt(stat.getTotalBytes());

            AnalyzerItem all = new AnalyzerItem(this, "All", this.all, totalInt, typeSize);

            long tempAll = this.all.getSize();

            double availBlocks = stat.getAvailableBlocksLong();
            double blockSize = stat.getBlockSizeLong();
            long free_memory = (long) availBlocks * (long) blockSize;
            long total = stat.getTotalBytes() - free_memory;

            this.all.setSize(total - tempAll);

            all.setPresents(all.integerFileSize(this.all.getSize()));

            totalInt = totalInt(this.all.getSize());

            AnalyzerItem system = new AnalyzerItem(this, "System", this.system, totalInt, typeSize);
            AnalyzerItem images = new AnalyzerItem(this, "Images", this.images, totalInt, typeSize);
            AnalyzerItem audio = new AnalyzerItem(this, "Audio", this.audio, totalInt, typeSize);
            AnalyzerItem video = new AnalyzerItem(this, "Video", this.video, totalInt, typeSize);
            AnalyzerItem apk = new AnalyzerItem(this, "Apk", this.apks, totalInt, typeSize);
            AnalyzerItem other = new AnalyzerItem(this, "Other", this.other, totalInt, typeSize);

            int currentInt = totalInt(total - tempAll);
            other.setPresents(currentInt, total - tempAll);

            LinearLayout analyzerList = findViewById(R.id.ll_list);
            analyzerList.addView(all);
            analyzerList.addView(system);
            analyzerList.addView(images);
            analyzerList.addView(audio);
            analyzerList.addView(video);
            analyzerList.addView(apk);
            analyzerList.addView(other);
        }).execute();
//        setAdapter(0);
//        setAdapter(1);



    }

    public void setAdapter(int s) {

        ContentResolver contentResolver = instance.getContentResolver();

        Uri uri = MediaStore.Files.getContentUri(s==0 ? "external" : "internal");

        String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " ASC";
        @SuppressLint("Recycle")
        Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder);
        if (cursor != null && cursor.moveToLast()) {
            do {
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATA));

                JFile jFile = new JFile(new File(String.valueOf(Uri.parse(data))), instance);
                all.add(jFile);
                switch (jFile.getType()) {
                    case IMAGE:
                        images.add(jFile);
                        break;
                    case AUDIO:
                        audio.add(jFile);
                        break;
                    case VIDEO:
                        video.add(jFile);
                        break;
                    case APK:
                        apks.add(jFile);
                        break;
                    default:
                        other.add(jFile);
                }

            } while (cursor.moveToPrevious());
        }
    }

    public void countSize(File file){
        if (file.canRead())
            if (file.listFiles() != null)
                for (File child : Objects.requireNonNull(file.listFiles())) {
                    if (child.isDirectory()) {
                        countSize(child);
                    } else {
                        JFile jFile = new JFile(child, this);
                        all.add(jFile);
                        system.add(jFile);
                    }
                }
    }

    public int totalInt(long total) {
        int totalInt = 0;
        try {
            typeSize = BYTE;
            totalInt = Math.toIntExact(total);
        } catch (Exception e) {
            try {
                typeSize = KB;
                total = total / 1024;
                totalInt = Math.toIntExact(total);
            } catch (Exception f) {
                typeSize = MB;
                total = total / 1024;
                totalInt = Math.toIntExact(total);
            }
        }
        return totalInt;
    }

}
package com.example.files.models;

import static com.example.files.MainActivity.closeAllFragments;
import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.TAG_FOLDER;
import static com.example.files.Statics.dpToPixels;
import static com.example.files.Statics.openFile;
import static com.example.files.Statics.openFolder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.documentfile.provider.DocumentFile;

import com.example.files.JFileAdapter;
import com.example.files.R;
import com.example.files.utils.FileIcon;
import com.example.files.utils.PathFormatter;
import com.example.files.presentation.files_explorer.FilesFragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.File;

@SuppressLint("ViewConstructor")
public class StorageItem extends LinearLayout {
    int id;
    File file;
    DocumentFile documentFile;
    String storageName;
    boolean isInternal, isShortcut;
    int ten_dp = dpToPixels(10f);

    public StorageItem(int id, File file, boolean isInternal, Context context) { // Storages
        super(context);
        this.id = id;
        this.file = file;
        this.isInternal = isInternal;
        if (isInternal) this.storageName = context.getString(R.string.internal_storage);
        else this.storageName = context.getString(R.string.external_storage, String.valueOf(id-1));
        inflate(context, R.layout.item_storage, this);
        initView();
    }

    public StorageItem(int id, File file, Context context) { // Favorites
        super(context);
        this.id = id;
        this.file = file;
        this.isShortcut = true;
        this.storageName = file.getName();
        inflate(context, R.layout.item_file_card, this);
        initView();
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    public void initView() {
        ((TextView) findViewById(R.id.file_name)).setText(storageName);
        try {
            if (isShortcut) ((TextView) findViewById(R.id.file_info)).setText(
                    new PathFormatter(getContext()).format(file.getPath()));
            else ((TextView) findViewById(R.id.file_info)).setText(getFreeSpace(file));
        } catch (Exception ignored) {}
        if (isInternal) {
            hideDivider();
            ((ImageView) findViewById(R.id.type)).setImageDrawable(getContext().getDrawable(R.drawable.phone));
        } else if (!isShortcut){
            ((ImageView) findViewById(R.id.type)).setImageDrawable(getContext().getDrawable(R.drawable.sdcard));
            findViewById(R.id.type).setClipToOutline(true);
        } else setFavoriteIcon(new JFile(file, instance));
        if (!getContext().getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)
        && getContext().getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL)
        && !isShortcut) findViewById(R.id.type).setPadding(ten_dp, ten_dp/2, ten_dp,ten_dp/2);
        ((TextView) findViewById(R.id.file_name)).setTextSize(20);
        findViewById(R.id.item).setOnClickListener(v -> {
            if (isShortcut) {
                if (getFile().isDirectory()) openFolder(getFile());
                else openFile(new JFile(getFile(), instance), getContext());
            }
            else {
                if (instance.fragmentInLayout()) closeAllFragments();
                instance.loadFragment(instance.newFragment(file), TAG_FOLDER);
            }
        });
        if (isShortcut)
            findViewById(R.id.item).setOnLongClickListener(v -> {
                instance.loadFragment(FilesFragment.newInstance("FAVORITES", null, null), "favorites");
                return true;
            });
    }

    public void setFavoriteIcon(JFile jFile) {
        ViewHolder viewHolder = new ViewHolder(this);
        FileIcon.setIcon(viewHolder, JFileAdapter.ViewType.ROW, jFile, instance);
        viewHolder.size.setVisibility(GONE);
    }

    @SuppressLint("SetTextI18n")
    public void updateView () {
        try {
            if (!isShortcut) ((TextView) findViewById(R.id.file_info)).setText(getFreeSpace(file));
        } catch (Exception ignored) {}
    }

    public String getFreeSpace(File file){
        StatFs stat = new StatFs(file.getPath());
        double availBlocks = stat.getAvailableBlocksLong();
        double blockSize = stat.getBlockSizeLong();
        long free_memory = (long)availBlocks * (long)blockSize;
        long total_memory = stat.getTotalBytes();

        int free = Math.toIntExact(free_memory/1000000000);
        int total = Math.toIntExact(total_memory/1000000000);
        double precent = ((double) total) / 100;

        total = (int) (total / precent);
        free = (int) (free / precent);

        int progress = total-free;

        ((LinearProgressIndicator) findViewById(R.id.capacity)).setProgress(progress);

        Log.d("##### setProgress #####", "t:" + total +", f:" + free + ", p:" + precent + " | t-f="+(total-free));

        return (free_memory/1000000000)+ " "+ getContext().getString(R.string.gb)+" / " +
                (total_memory/1000000000)+" "+ getContext().getString(R.string.gb);
    }

    public File getFile() {
        return file;
    }

    public DocumentFile getDocumentFile() {
        return documentFile;
    }

    public void setDocumentFile(DocumentFile documentFile) {
        this.documentFile = documentFile;
    }

    public boolean isShortcut() {
        return isShortcut;
    }

    public void setShortcut(boolean isShortcut) {
        this.isShortcut = isShortcut;
    }

    public int getItemId() {
        return id;
    }

    public void hideDivider() {
        findViewById(R.id.divider).setVisibility(View.GONE);
    }
}

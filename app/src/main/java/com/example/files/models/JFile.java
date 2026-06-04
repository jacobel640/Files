package com.example.files.models;

import static com.example.files.Statics.isRootFile;
import static com.example.files.Statics.showFileSize;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.icu.text.Collator;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Formatter;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.example.files.R;
import com.example.files.Statics;
import com.example.files.listeners.OnSizeLoadReady;
import com.example.files.utils.FileIcon;
import com.example.files.utils.JFileExecutor;
import com.example.files.utils.PathFormatter;
import com.example.files.listeners.OnIconLoadReady;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class JFile extends File implements Comparable<File> {
    public enum Type {FOLDER, IMAGE, VIDEO, AUDIO, APK, ARCHIVE, DOCUMENT, SHORTCUT, OTHER}

    public String id;
    Context context;
    CharSequence info = "";
    String path;
    boolean selected;
    Type type;
    int position;
    private OnSizeLoadReady sizeLoadListener;
    private volatile boolean sizeLoading;
    private volatile long size = -1;
    private long count = 0;
    private OnIconLoadReady iconLoadListener;
    long lastChecked;
    private volatile Object cachedIcon;
    private volatile boolean iconLoading;

    public JFile(String path, Context context) {
        super(path);
        this.context = context;
        this.type = FileIcon.types(getExtension().toLowerCase(), isDirectory());
    }

    public JFile(String id, String path, Context context) {
        super(path);
        this.id = id;
        this.context = context;
        this.type = FileIcon.types(getExtension().toLowerCase(), isDirectory());
    }

    public JFile(File file, Context context) {
        super(file.getPath());
        this.context = context;
        this.type = FileIcon.types(getExtension().toLowerCase(), isDirectory());
    }

    public Context getContext() {
        return context;
    }

    public JFile(@NonNull String pathname) {
        super(pathname);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public DocumentFile fromFile() {
        return DocumentFile.fromFile(this);
    }

    public DocumentFile getDocumentFileOrig() {
        Uri uri = FileProvider.getUriForFile(context, "com.example.files", this);
        return DocumentFile.fromSingleUri(context, uri);
    }

    public DocumentFile getDocumentFile() {
        String uri = new PathFormatter(context).externalFilePathWoName(FileProvider.getUriForFile(context, "com.example.files", this).toString());
        return DocumentFile.fromSingleUri(context, Uri.parse(uri));
    }

    public DocumentFile getDocumentTree() {
        String uri = new PathFormatter(context).externalFilePathWoName(FileProvider.getUriForFile(context, "com.example.files", this).toString());
        return DocumentFile.fromTreeUri(context, Uri.parse(uri));
    }

    public DocumentFile getDocumentTreeSec() {
        Uri uri = Uri.parse(new PathFormatter(context).externalFolderPathWoName(this.getPath()));
        return DocumentFile.fromTreeUri(context, uri);
    }

    public Uri getUri() {
        return getDocumentFile().getUri(); // return uri granted by user stock in the same
        // chosen directory by SAF
    }

    public boolean isDocumentFile() {
        return Statics.isDocumentFile(this) && !isRootFile(this);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public JFile[] listJFiles() {
        File[] files = listFiles();
        if (files != null) {
            JFile[] jFiles = new JFile[files.length];
            for (int i = 0; i < files.length; i++) {
                jFiles[i] = new JFile(files[i], context);
            }
            return jFiles;
        } else return null;
    }

    public CharSequence getInfo() {
        return DateFormat.format("HH:mm dd/MM/yyyy", new Date(lastModified()));
    }

    public String getStringDate() {
        return DateUtils.getRelativeTimeSpanString(lastModified(), Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getStringSize() {
        if (isDirectory()) {
            if (showFileSize) {
                if (isSizeReady()) {
                    return Formatter.formatFileSize(context, getSize());
                } else {
                    loadSizeIfNeeded();
                    return context.getString(R.string.loading);
                }
            }
            return getCountItems();
        } else return Formatter.formatFileSize(context, getSize());
    }

    public String getCountItems() { //TODO create method to return count of items in folder more faster
        if (isDirectory()) {
            if (count == 0) this.count = countFiles();
            return context.getString(R.string.items, String.valueOf(count));
        } else return Formatter.formatFileSize(context, length());
    }

    public int countFiles() {
        return nullSize(list());
    }

    public static int nullSize(Object[] list) {
        if (list == null) return -1;
        else return list.length;
    }

    public long getSize() {
        if (isFile()) return length();
        else if (showFileSize) return size;
        else return nullSize(listJFiles());
    }

    public boolean isSizeReady() {
        if (!isDirectory()) return true;

        if (lastChecked == 0) return false;
        return !new Date(lastModified()).after(new Date(lastChecked));
    }

    public void loadSizeIfNeeded() {
        if (!isDirectory()) {
            size = length();
            return;
        }

        if (sizeLoading || isSizeReady()) return;

        sizeLoading = true;

        JFileExecutor.execute(() -> {
            long result = calculateFolderSize(this);

            size = result;
            lastChecked = System.currentTimeMillis();
            sizeLoading = false;

            if (sizeLoadListener != null) {
                sizeLoadListener.onSizeReady(result);
            }
        });
    }

    private long calculateFolderSize(File dir) {
        long total = 0;

        File[] files = dir.listFiles();
        if (files == null) return 0;

        for (File f : files) {
            if (f.isDirectory()) {
                total += calculateFolderSize(f);
            } else {
                total += f.length();
            }
        }
        return total;
    }

    public void setSizeLoadListener(OnSizeLoadReady sizeLoadListener) {
        this.sizeLoadListener = sizeLoadListener;
    }

    public Type getType() {
        return type;
    }

    public Object getCachedIcon() {
        return cachedIcon;
    }

    public void setCachedIcon(Object icon) {
        this.cachedIcon = icon;
    }

    public boolean isIconReady() {
        return cachedIcon != null;
    }

    public void loadIconIfNeeded() {
        if (cachedIcon != null || iconLoading) return;

        iconLoading = true;

        JFileExecutor.execute(() -> {
            Object icon = loadIconInternal();
            cachedIcon = icon;
            iconLoading = false;

            if (iconLoadListener != null) {
                iconLoadListener.onIconReady(icon);
            }
        });
    }

    public void setIconReadyListener(OnIconLoadReady evtListener) {
        this.iconLoadListener = evtListener;
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "StaticFieldLeak"})
    public Object loadIconInternal() {
        if (isDirectory()) {
            return R.drawable.folder;
        } else switch (getExtension().toLowerCase()) {
            case "aac":
            case "amr":
            case "flac":
            case "mp3":
            case "m4a":
            case "ogg":
            case "opus":
            case "wma":
            case "wav":
                try (MediaMetadataRetriever mmr = new MediaMetadataRetriever()) {
                    mmr.setDataSource(getPath());
                    byte[] art = mmr.getEmbeddedPicture();
                    if (art != null) return art;
                } catch (Exception ignored) {
                }
                return R.drawable.ctg_audio;
            case "apk":
                try {
                    PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(getPath(), PackageManager.GET_ACTIVITIES);
                    if (packageInfo != null) {
                        ApplicationInfo appInfo = packageInfo.applicationInfo;
                        assert appInfo != null;
                        appInfo.sourceDir = getPath();
                        appInfo.publicSourceDir = getPath();
                        return appInfo.loadIcon(context.getPackageManager());
                    }
                } catch (Exception ignored) {
                }
                return R.drawable.ext_apk;
            // photo
            case "cr2":
            case "dng":
            case "heic":
            case "jpg":
            case "jpeg":
            case "png":
            case "raw":
            case "webp":
            case "ico":
                // video
            case "3gpp":
            case "avi":
            case "gif":
            case "mkv":
            case "mov":
            case "mp4":
                return Uri.fromFile(this);
            case "7z":
            case "7zip":
            case "apks":
            case "apkm":
            case "xapk":
            case "gz":
            case "jar":
            case "rar":
            case "zip":
                return R.drawable.ctg_archive;
            case "docx":
            case "doc":
                return R.drawable.ext_word;
            case "xls":
            case "xlsx":
                return R.drawable.ext_excel;
            case "pptx":
                return R.drawable.ext_powerpoint;
            case "pdf":
                return R.drawable.ext_pdf;
            case "txt":
                return R.drawable.ext_txt;
            case "html":
                return backgroundDrawable();
            default:
                return R.drawable.file;
			/*
			case "txt":
			case "lrc":
			case "jar":
			case "tar":
			case "exe":
			case "html":
			 */
        }
    }

    Drawable backgroundDrawable() {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadii(new float[]{50, 50, 50, 50, 50, 50, 50, 50});
        shape.setColor(Color.BLUE);
        return shape;
    }

    public String getExtension() {
        if (isDirectory()) return "folder";
        String fileName = getName();
        return fileName.substring(fileName.lastIndexOf(".") + 1);

    }

    public String getNameTLC() {
        return getName().toLowerCase();
    }

    @NonNull
    @Override
    public String toString() {
        return path;
    }

    @Override
    public int compareTo(File jFile) {
        Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.PRIMARY);
        return collator.compare(this.getName(), jFile.getName());
    }

}

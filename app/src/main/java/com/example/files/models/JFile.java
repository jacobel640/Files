package com.example.files.models;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.isRootFile;
import static com.example.files.Statics.showFileSize;
import static com.example.files.utils.FileIcon.types;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import com.example.files.utils.AsyncTask;
import com.example.files.utils.FileIcon;
import com.example.files.utils.IconLoader;
import com.example.files.utils.PathFormatter;
import com.example.files.listeners.OnDelayLoadReady;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JFile extends File implements Comparable<File> {
    public enum Type {FOLDER, IMAGE, VIDEO, AUDIO, APK, ARCHIVE, DOCUMENT, SHORTCUT, OTHER}

    Activity activity;
    CharSequence info = "";
    String path;
    String extension = "";
    public String id;
    Object icon;
    Type type;
    long size = 0, count = 0;
    int position;
    boolean selected, start, sizeDone;
    private OnDelayLoadReady eventListener;
    long lastChecked;
    private static final ExecutorService ICON_EXECUTOR =
            Executors.newFixedThreadPool(2);
    private volatile Object cachedIcon;
    private volatile boolean iconLoading;
    //private OnFileSize sizeListener;

    public JFile(String path, Activity activity) {
        super(path);
        this.activity = activity;
        listeners();
        this.type = FileIcon.types(getExtension().toLowerCase(), isDirectory());
    }

    public JFile(String id, String path, Activity activity) {
        super(path);
        this.id = id;
        this.activity = activity;
        listeners();
        this.type = FileIcon.types(getExtension().toLowerCase(), isDirectory());
    }

    public JFile(File file, Activity activity) {
        super(file.getPath());
        this.activity = activity;
        listeners();
        this.type = FileIcon.types(getExtension().toLowerCase(), isDirectory());
    }

    public Activity getActivity() {
        return activity;
    }

    private void listeners() {
        this.eventListener = new OnDelayLoadReady() {
            @Override
            public void onIconReady(Object object) {

            }

            @Override
            public void onSizeReady(long size) {

            }
        };
    }

    public JFile(@NonNull String pathname) {
        super(pathname);
    }


    /*public void addFileSizeListener(OnFileSize szListener)
    {
        this.sizeListener = szListener;
    }*/
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
        Uri uri = FileProvider.getUriForFile(activity, "com.example.files", this);
        return DocumentFile.fromSingleUri(activity, uri);
    }

    public DocumentFile getDocumentFile() {
        String uri = new PathFormatter(activity).externalFilePathWoName(
                FileProvider.getUriForFile(activity, "com.example.files", this).toString());
        return DocumentFile.fromSingleUri(activity, Uri.parse(uri));
    }

    public DocumentFile getDocumentTree() {
        String uri = new PathFormatter(activity).externalFilePathWoName(
                FileProvider.getUriForFile(activity, "com.example.files", this).toString());
        return DocumentFile.fromTreeUri(activity, Uri.parse(uri));
    }

    public DocumentFile getDocumentTreeSec() {
        Uri uri = Uri.parse(new PathFormatter(activity).externalFolderPathWoName(this.getPath()));
        return DocumentFile.fromTreeUri(activity, uri);
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
                jFiles[i] = new JFile(files[i], activity);
            }
            return jFiles;
        } else return null;
    }

    public CharSequence getInfo() {
        return DateFormat.format("HH:mm dd/MM/yyyy",
                new Date(lastModified()));
    }

    public String getStringDate() {
        return DateUtils.getRelativeTimeSpanString(lastModified(),
                Calendar.getInstance().getTimeInMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE).toString();
    }


    //	public long lastModified() {
//		return file.lastModified();
//	}
    public void setInfo(String info) {
        this.info = info;
    }

    public String getStringSize() {
        if (isDirectory()) {
            if (showFileSize) {
                return Formatter.formatFileSize(activity, getSize());
            }
            return getCountItems();
        } else return Formatter.formatFileSize(activity, getSize());
    }

    public String getCountItems() { //TODO create method to return count of items in folder more faster
        if (isDirectory()) {
            if (count == 0) this.count = countFiles();
            return activity.getString(R.string.items, String.valueOf(count));
        } else return Formatter.formatFileSize(activity, length());
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

    public void triggerSizeLoading(Runnable done) {
        this.size = 0;
        new AsyncTask(() -> fileSize(this),
                () -> {
                    sizeDone = true;
                    lastChecked = new Date().getTime();
                    done.run();
                }).execute();
    }

    public void fileSize(File file) {
        if (file.canRead())
            if (file.listFiles() != null) {
                for (File f : Objects.requireNonNull(file.listFiles())) {
                    if (f.isDirectory()) fileSize(f);
                    else this.size += f.length();
                }
            }
        eventListener.onSizeReady(this.size);
    }

    public boolean isSizeDone() {
        if (isDirectory() && lastChecked != 0) {
            if (new Date(lastModified()).after(new Date(lastChecked))) sizeDone = false;
        }
        return sizeDone;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setExtension(String extantion) {
        this.extension = extantion;
    }

    public String getExtension() {
        if (isDirectory()) return "folder";
        String fileName = getName();
        return fileName.substring(fileName.lastIndexOf(".") + 1);

    }

    public Type getType() {
        return type;
    }

    public Object getCachedIcon() {
        return cachedIcon;
    }

    public boolean isIconReady() {
        return cachedIcon != null;
    }

    public void loadIconIfNeeded() {
        if (cachedIcon != null || iconLoading) return;

        iconLoading = true;

        IconLoader.execute(() -> {
            Object icon = loadIconInternal();
            cachedIcon = icon;
            iconLoading = false;

            if (eventListener != null) {
                eventListener.onIconReady(icon);
            }
        });
    }

    public void addIconReadyListener(OnDelayLoadReady evtListener) {
        this.eventListener = evtListener;
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "StaticFieldLeak"})
    public Object loadIconInternal() {
        if (isDirectory()) {
            return activity.getDrawable(R.drawable.folder);
        } else
            switch (getExtension().toLowerCase()) {
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
                    return activity.getDrawable(R.drawable.ctg_audio);
                case "apk":
                    try {
                        PackageInfo packageInfo = activity.getPackageManager()
                                .getPackageArchiveInfo(getPath(), PackageManager.GET_ACTIVITIES);
                        if (packageInfo != null) {
                            ApplicationInfo appInfo = packageInfo.applicationInfo;
                            assert appInfo != null;
                            appInfo.sourceDir = getPath();
                            appInfo.publicSourceDir = getPath();
                            return appInfo.loadIcon(activity.getPackageManager());
                        }
                    } catch (Exception ignored) {
                    }
                    return activity.getDrawable(R.drawable.ext_apk);
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
                    return activity.getDrawable(R.drawable.ctg_archive);
                case "docx":
                case "doc":
                    return activity.getDrawable(R.drawable.ext_word);
                case "xls":
                case "xlsx":
                    return activity.getDrawable(R.drawable.ext_excel);
                case "pptx":
                    return activity.getDrawable(R.drawable.ext_powerpoint);
                case "pdf":
                    return activity.getDrawable(R.drawable.ext_pdf);
                case "txt":
                    return activity.getDrawable(R.drawable.ext_txt);
                case "html":
                    return backgroundDrawable();
                default:
//				this.icon = backgroundDrawable(getExtension());
//				this.icon = backgroundDrawable(Color.DKGRAY);
                    return activity.getDrawable(R.drawable.file);
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
//        view.setBackground(shape);
        return shape;
    }

    public Drawable getAppIcon(String packageName) throws PackageManager.NameNotFoundException {
        return activity.getPackageManager().getApplicationIcon(packageName);
    }

    public byte[] getSongCover(String path) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        //reduceQuality(BitmapFactory.decodeByteArray(data, 0, data.length));
        return mmr.getEmbeddedPicture();
    }

    public static void updateFile(File old, File current) {
        instance.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(current)));
        instance.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(old)));
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

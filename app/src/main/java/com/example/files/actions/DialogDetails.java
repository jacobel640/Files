package com.example.files.actions;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.currentFragment;
import static com.example.files.Statics.folder;
import static com.example.files.Statics.openFolder;
import static com.example.files.Statics.selectedJFiles;
import static com.example.files.models.JFile.Type.APK;
import static com.example.files.models.JFile.Type.AUDIO;
import static com.example.files.models.JFile.Type.IMAGE;
import static com.example.files.models.JFile.Type.OTHER;
import static com.example.files.models.JFile.Type.VIDEO;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.VelocityTrackerCompat;
import androidx.exifinterface.media.ExifInterface;

import com.example.files.R;
import com.example.files.view.DialogItem;
import com.example.files.view.ImageDescription;
import com.example.files.view.Note;
import com.example.files.utils.PathFormatter;
import com.example.files.models.JFile;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.stream.IntStream;

public class DialogDetails extends BottomSheetDialog {

    boolean close = false, category;
    File parent;
    long length;
    int dirs = 0, files = 0;
    final int NAME = 0, CHOSEN = 1, SIZE = 2, CONTAINS = 3, LAST_EDIT = 4, PATH = 5;
    TextView dtClose, dtTitle;
    LinearLayout content, dialog;
    ArrayList<DialogItem> items;
    float defaultY;

    Handler realTime;

    Activity activity;

    PathFormatter pathFormatter;

    public DialogDetails(Activity activity, boolean category) {
        super(activity);
        this.activity = activity;
        this.category = category;
        this.pathFormatter = new PathFormatter(activity);
        this.parent = folder;

        realTime = new Handler();

        if (!selectedJFiles.isEmpty()) {
            try {
                details();
            } catch (Exception e) {
                new Note(activity, "Error", e.getLocalizedMessage(), Arrays.toString(e.getStackTrace())).show();
            }
        }
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n", "DefaultLocale", "ClickableViewAccessibility"})
    public void details() {

        setContentView(R.layout.dialog_details);

        dialog = findViewById(R.id.dialog_layout);
        dtTitle = findViewById(R.id.dt_title);
        content = findViewById(R.id.content);
        assert content != null;
        defaultY = content.getY();

        items = new ArrayList<>();

        items.add(new DialogItem(activity, activity.getString(R.string.name)));
        items.add(new DialogItem(activity, activity.getString(R.string.chosen)));
        items.add(new DialogItem(activity, activity.getString(R.string.size)));
        items.add(new DialogItem(activity, activity.getString(R.string.contains)));
        items.add(new DialogItem(activity, activity.getString(R.string.last_edited)));
        items.add(new DialogItem(activity, activity.getString(R.string.path)));

        dtClose = findViewById(R.id.dt_close);
        assert dtClose != null;
        dtClose.setClipToOutline(true);

        dtClose.setOnClickListener(view -> dismiss());

        setOnDismissListener(dialog1 -> close = true);

        // -------------- Details --------------
//        new Handler().post(this::setTheDetails);
        setTheDetails();

        show();
    }

    private void setTheDetails() {
        if (selectedJFiles.size() == 1) {
            if (selectedJFiles.get(0).isDirectory()) {
                items.get(NAME).setContent(selectedJFiles.get(0).getName());
                items.get(LAST_EDIT).setContent(new Date(selectedJFiles.get(0)
                        .lastModified()).toLocaleString());
                items.get(PATH).setContent(selectedJFiles.get(0).isDocumentFile() ?
                        selectedJFiles.get(0).getDocumentTreeSec().getUri().toString() :
                        pathFormatter.format(selectedJFiles.get(0).getPath()));

                items.get(SIZE).showProgress();
                items.get(CONTAINS).showProgress();
                dirs = 0;
                files = 0;
                length = 0;
                new Thread(() -> {
                    numFiles(selectedJFiles.get(0));
                    instance.runOnUiThread(() ->  {
                        items.get(CONTAINS).setContent(files + " " + activity.getString(R.string.files) +
                                ", " + dirs + " " + activity.getString(R.string.folders));
                        items.get(SIZE).setContent(Formatter.formatFileSize(activity, length));
                        items.get(CONTAINS).hideProgress();
                        items.get(SIZE).hideProgress();
                    });
                }).start();

                new Thread(() -> {
                    while (items.get(CONTAINS).isProgressVisible() ||
                            items.get(SIZE).isProgressVisible()){
                        try {
                            Thread.sleep(10);
                            items.get(CONTAINS).setContent((files) + " " + activity.getString(R.string.files)
                                    + ", " + (dirs) + " " + activity.getString(R.string.folders));
                            items.get(SIZE).setContent(Formatter.formatFileSize(activity, length));
                        } catch (InterruptedException ignored) {}
                    }

                }).start();

                items.get(CHOSEN).toShow(false);
                for (DialogItem item : items)
                    if (item.show()) content.addView(item);

            } else {
                File file = selectedJFiles.get(0);
                ImageDescription imageDescription = new ImageDescription(activity, selectedJFiles.get(0), file.getName());
//                setOnTouchListener(imageDescription);
                if (selectedJFiles.get(0).getType() != OTHER) content.addView(imageDescription);
                else items.get(NAME).setContent(file.getName());
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                // use one of overloaded setDataSource() functions to set your data source
                // retriever.setDataSource(activity, Uri.fromFile(file));

                items.get(LAST_EDIT).setContent(new Date(selectedJFiles.get(0).lastModified()).toString());
                items.get(PATH).setContent(selectedJFiles.get(0).isDocumentFile() ?
                        selectedJFiles.get(0).getDocumentTreeSec().getUri().toString() :
                        pathFormatter.format(selectedJFiles.get(0).getPath()));

                DateFormat.format("HH:mm MM/dd/yyyy", // never used
                        new Date(selectedJFiles.get(0).lastModified()));
                items.get(SIZE).setContent(selectedJFiles.get(0).getStringSize());

                if (category) {
                    items.get(PATH).getTvContent().setTextColor(activity.getColor(R.color.app_theme));
                    items.get(PATH).setOnClickListener(v ->
                            items.get(PATH).showPopupView(activity.getString(R.string.confirm_move_to_location),
                            activity.getString(R.string.move_to_location),
                                    () -> {
                                        dismiss();
                                        openFolder(file.getParentFile());
                                        currentFragment.select(file.getPath());
                                    }));
                }

                if (selectedJFiles.get(0).getType() == IMAGE) {
                    try {
                        ExifInterface exif = new ExifInterface(file.getPath());
                        String height = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                        String width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                        addDialogItem(activity.getString(R.string.resolution), height+"x"+width);

                    } catch (IOException ignored) {}
                }

                if (selectedJFiles.get(0).getType() == VIDEO) {
                    retriever.setDataSource(activity, Uri.fromFile(file));
                    long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                    String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                    String bits = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        addDialogItem(activity.getString(R.string.duration), getTime(duration));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        String frames = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT);
                        addDialogItem("Frames", frames); // needs fix
                    }
                    addDialogItem("bitrate", bits); // needs fix
                    if (width != null) addDialogItem(activity.getString(R.string.resolution), height + "x" + width);
                }

                if (selectedJFiles.get(0).getType() == AUDIO) {
                    retriever.setDataSource(activity, Uri.fromFile(file));
                    String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                    String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    addDialogItem(activity.getString(R.string.title), title);
                    addDialogItem(activity.getString(R.string.artist), artist);
                    addDialogItem(activity.getString(R.string.album), album);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        addDialogItem(activity.getString(R.string.duration), getTime(duration));

                }

                if (selectedJFiles.get(0).getType() == APK) {
                    PackageInfo pai = activity.getPackageManager().getPackageArchiveInfo(file.getPath(), PackageManager.GET_META_DATA);
                    if (pai != null) {
                        int minSdkVersion = pai.applicationInfo.minSdkVersion;
                        int targetSdkVersion = pai.applicationInfo.targetSdkVersion;
                        String versionName = pai.versionName;
                        String packageName = pai.packageName;
                        addDialogItem("package Name", packageName);
                        addDialogItem("min Sdk Version", String.valueOf(minSdkVersion));
                        addDialogItem("target Sdk Version", String.valueOf(targetSdkVersion));
                        addDialogItem("version", versionName);
                    }
                }

                try { retriever.release(); }
                catch (IOException ignored) {}

                if (selectedJFiles.get(0).getType() != OTHER) items.get(NAME).toShow(false);

                items.get(CHOSEN).toShow(false);
                items.get(CONTAINS).toShow(false);

                for (DialogItem item : items) if (item.show()) content.addView(item);
            }

        } else {
            close = false;
            items.get(SIZE).showProgress();
            if (!IntStream.range(0, selectedJFiles.size()).allMatch(i -> selectedJFiles.get(i).isFile())) {
                items.get(CONTAINS).showProgress();
                dirs = 0;
                files = 0;
                length = 0;
                new Thread(() -> {
                    items.get(CHOSEN).setContent(countItems(activity));
                    for (JFile jFile : selectedJFiles) {
                        if(jFile.isDirectory()){
                            dirs++;
                            numFiles(jFile);
                        } else {
                            files++;
                            length += jFile.length();
                        }
                        if (close) break;
                    }
                    instance.runOnUiThread(() -> {
                        items.get(CONTAINS).setContent((files) + " " + activity.getString(R.string.files)
                                + ", " + + (dirs) + " " + activity.getString(R.string.folders));
                        items.get(SIZE).setContent(Formatter.formatFileSize(activity, length));
                        items.get(CONTAINS).hideProgress();
                        items.get(SIZE).hideProgress();
                    });
                }).start();

            } else {
                items.get(CONTAINS).toShow(false);
                new Thread(() -> {
                    length = 0;
                    items.get(CHOSEN).setContent(countItems(activity));
                    for (JFile jFile : selectedJFiles) {
                        length+=jFile.length();

                        if (close) break;
                    }
                    instance.runOnUiThread(() -> {
                        items.get(SIZE).setContent(Formatter.formatFileSize(activity, length));
                        items.get(SIZE).hideProgress();
                    });
                }).start();
            }

            new Thread(() -> {
                while (items.get(CONTAINS).isProgressVisible() ||
                        items.get(SIZE).isProgressVisible()) {
                    try {
                        Thread.sleep(10);

                        if (close) break;

                        if (items.get(CONTAINS).show()) {
                            items.get(CONTAINS).setContent((files) + " " + activity.getString(R.string.files)
                                    + ", " + + (dirs) + " " + activity.getString(R.string.folders));
                        }
                        items.get(SIZE).setContent(Formatter.formatFileSize(activity, length));
                    } catch (InterruptedException ignored) {}
                }

            }).start();

            if (!category) {
                items.get(PATH).setContent(pathFormatter.format(Objects.requireNonNull(selectedJFiles.get(0)
                        .getParentFile()).getPath()));
            } else items.get(PATH).toShow(false);

            items.get(NAME).toShow(false);
            items.get(LAST_EDIT).toShow(false);
            for (DialogItem item : items)
                if (item.show()) content.addView(item);
        }

    }

    public void addDialogItem(String title, String content) {
        DialogItem dialogItem = new DialogItem(activity, title);
        dialogItem.setContent(content);
        if (!dialogItem.getContent().trim().isEmpty()) this.content.addView(dialogItem);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("DefaultLocale")
    public String getTime(long time) {
        Duration duration = Duration.ofMillis(time);
        long hours = duration.toHours();
        duration = duration.minusHours(hours);
        long minutes = duration.toMinutes();
        duration = duration.minusMinutes(minutes);
        long seconds = duration.getSeconds();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void numFiles(File file) {
        if (file.canRead() && file.listFiles() != null) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                if (child.isDirectory()){
                    dirs++;
                    numFiles(child);
                } else {
                    files++;
                    length += child.length();
                }
            }
        };
    }

    public static String countItems(Context context){
        if(selectedJFiles.size() > 1)
            if(IntStream.range(0, selectedJFiles.size()).allMatch(i -> selectedJFiles.get(i).isDirectory()))
                return selectedJFiles.size() + " " + context.getString(R.string.folders);
            else if(IntStream.range(0, selectedJFiles.size()).noneMatch(i -> selectedJFiles.get(i).isDirectory()))
                return selectedJFiles.size() + " " + context.getString(R.string.files);
            else return context.getString(R.string.items, String.valueOf(selectedJFiles.size()));
        else if (selectedJFiles.size() == 1) {
            if (selectedJFiles.get(0).isDirectory())
                return context.getString(R.string.one_folder);
            else return context.getString(R.string.one_file);
        } else return "";
    }

}

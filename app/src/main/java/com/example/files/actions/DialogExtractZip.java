package com.example.files.actions;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.isDocumentFile;

import android.annotation.SuppressLint;
import android.os.Build;
import android.provider.DocumentsContract;
import android.text.format.Formatter;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.example.files.R;
import com.example.files.models.JFile;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;

public class DialogExtractZip extends DialogBase {

    public DialogExtractZip(ArrayList<JFile> mjFiles) {
        super(mjFiles, ActionType.Extract,
                R.string.copying, R.string.copy_here, R.string.copy_action_finished,
                R.string.copy_action_failed, R.drawable.action_copy);
    }

    @Override
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void action() {
        operationPs();
        for (JFile jFile : jFiles){
            if (canceled) break;
            if(jFile.isDirectory()){
                copyFolder(jFile, parentTarget);
                updateFile(jFile, new File(parentTarget.getPath(), jFile.getName()));
            } else copyFile(jFile, parentTarget);
        }
    }

    public void copyFolder(File source, File dest) {

        File destination = new File(dest.getPath() + "/" + source.getName());
        if (destination.mkdir() || destination.exists()) {
            for (File file : Objects.requireNonNull(source.listFiles())) {
                if (canceled) break;
                if (file.isDirectory()) copyFolder(file, destination);
                else copyFile(file, destination);
            }
        }

    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void copyFile(File source, File dest) {

        File currentDestination = new File(dest.getPath() + "/" + source.getName());

        position++;
        activity.runOnUiThread(() -> {
            binding.progressText.setText(position + " / " + sum);
            binding.progressCurrent.setText(source.getName());
        });
        if (currentDestination.exists()) {
            if (!rememberChoice) {
                fileConflictDialog(source.getPath(), currentDestination.getPath());
                while (wait) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}
                }
            }
            switch (fileChoice) {
                case RENAME:
                    rename(source, currentDestination);
                    break;
                case REPLACE:
                    operationCount(currentDestination);
                    copyFileUsingStream(source, currentDestination);
                    updateFile(source, currentDestination);
                    break;
                case SKIP:
                    operationCountSkip(currentDestination);
                    break;
            }
            wait = true;
        } else {
            operationCount(currentDestination);
            if (isDocumentFile(currentDestination)) {
                try {
                    DocumentsContract.copyDocument(activity.getContentResolver(),
                            new JFile(source, activity).getUri(),
                            new JFile(currentDestination, activity).getUri());
                } catch (Exception e) {
                    copyFileUsingStream(source, currentDestination);
                }
            } else copyFileUsingStream(source, currentDestination);
        }
        current_operation = null;

    }

    private void copyFileUsingStream(File source, File dest) {
        try (InputStream is = new FileInputStream(source);
             OutputStream os = new FileOutputStream(dest)) {

            byte[] buffer = new byte[20480];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
                if (canceled) break;
            }
        } catch (IOException e) {
            cancel(Result.FAILED);
        }
    }

    private void rename(File source, File dest) {
        // save the original source to Files.copy()
        File oldSource = source;
        // temporary save the file before changing name
        source = new File(Objects.requireNonNull(activity.getExternalFilesDir("temp")).getPath() + "/" + oldSource.getName());
        operationCount(source);
        copyFileUsingStream(oldSource, source);
        current_operation = null;
        // ------------ causing a long to be -file length ------------
        // option 2: causing the process dialog to recreate
        // saving source file before changing the name
        File oldName = source;
        // shot thing a bit when saving separately
        String fullName = source.getName();
        String name = fullName;
        String ext = "";
        if (fullName.contains(".")) {
            name = fullName.substring(0, fullName.lastIndexOf("."));
            ext = fullName.substring(fullName.lastIndexOf("."));
        }
        // source with the new name
        source = new File(Objects.requireNonNull(source.getParentFile())
                .getPath(), name + " (" + 1 + ")" + ext);
        // avoid using brain  a couple of times
        String destPath = Objects.requireNonNull(dest.getParentFile()).getPath() + "/";
        // saving the destination file temporary to check if exist
        dest = new File(destPath + source.getName());
        // for loop running as many times as the files in the destination folder to
        // get a name that doesn't exist
        int times = Objects.requireNonNull(Objects.requireNonNull(dest.getParentFile()).list()).length;
        for (int i = 1; i < times; i++) {
            if (dest.exists()) {
                source = new File(Objects.requireNonNull(source.getParentFile())
                        .getPath(), name + " (" + i + ")" + ext);
                // again to check if exist
                dest = new File(destPath + source.getName());
            } else break;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.move(oldName.toPath(), oldName.toPath().resolveSibling(source.getName()));
            } //TODO rename operation for SDK 24-26
        } catch (IOException ignored) {}

        move(source, dest);
        updateFile(oldSource, dest); // ...update
    }

    public void move(File source, File dest) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.move(source.toPath(), dest.toPath());
            } else FileUtils.moveFile(source, dest);
        } catch (IOException ignored) {}
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void operationCount(File file) {
        current_operation = file;
        boolean animated = (file.length() > 5000);
        new Thread(() -> {
            int previous = 0 ,current;
            long  prev = 0;
            while (file == current_operation && operating) {
                try {
                    Thread.sleep(100);
                    // if (file != current_operation) break;
                    long fileSize = file.length();
                    if (fileSize <= 0L) continue; // solve 'rename' problem
                    counted += fileSize - prev;
                    prev = fileSize;
                    current = integerFileSize(fileSize); // integerFileSize(actionProgress.getProgress());
                    setProgress(getProgress() + (current - previous), animated);
                    activity.runOnUiThread(() -> binding.progressPresent.setText(String.format("%.1f", (double) 100 /
                            getMax() * getProgress()) + "%"));
                    previous = current;
                    Log.d("##### operationCount() #####", "progress = " + getProgress()
                            + "\nprevious = " + previous + "\ncurrrnt = " + current +
                            "\nfileSize = " + fileSize + " (" + file.length() + ")");
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void operationCountSkip(File file) {
        int current = integerFileSize(file.length()); // integerFileSize(actionProgress.getProgress());
        setProgress(getProgress() + current, false);
        activity.runOnUiThread(() -> binding.progressPresent.setText(String.format("%.1f", (double) 100 /
                getMax() * getProgress()) + "%"));
    }

    @SuppressLint("SetTextI18n")
    public void operationPs() {
        new Thread(() -> {
            long previous = 0 ,current;
            while (operating) {
                try {
                    Thread.sleep(500);
                    current = counted; // integerFileSize(actionProgress.getProgress());
                    long ps = current - previous;
                    binding.progressPs.setText("S/" + Formatter.formatFileSize(instance, ps*2));
                    if (current_operation!=null && inBackground) {
                        builder.setContentText(current_operation.getName())
                                .setProgress(getMax(), getProgress(), false);
                        if (!canceled) notifyNotification();
                    } else if (!inBackground) NotificationManagerCompat.from(instance).cancel(notificationID);
                    previous = current;
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }
}

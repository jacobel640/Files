package com.example.files.actions;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.isDocumentFile;
import static com.example.files.actions.DialogCreateNew.getFolderName;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.text.format.Formatter;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.example.files.R;
import com.example.files.models.JFile;
import com.example.files.view.Note;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;

public class DialogCopy extends DialogBase {

    public DialogCopy(ArrayList<JFile> mjFiles) {
        super(mjFiles, ActionType.Copy,
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

        if (destination.exists()) {
            if (!rememberChoiceFolder) {
                folderConflictDialog(destination.getPath());

                while (wait) {
                    try { Thread.sleep(1000); }
                    catch (InterruptedException ignored) {}
                }
                wait = true;
            }
            switch (folderChoice) {
                case RENAME:
                    destination = getFolderName(destination);
                    break;
                case MERGE:
                    // Do Nothing, let the files to merge
                    break;
                case SKIP:
                    operationFolderCountSkip(destination);
                    return;
            }
        }
        
        if (!destination.mkdirs() && !destination.exists()) {
            cancel(Result.FAILED);
            return;
        }

        for (File file : Objects.requireNonNull(source.listFiles())) {
            if (canceled) break;
            if (file.isDirectory()) copyFolder(file, destination);
            else copyFile(file, destination);
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
                    try { Thread.sleep(1000); }
                    catch (InterruptedException ignored) {}
                }
                wait = true;
            }
            switch (fileChoice) {
                case RENAME:
                    try { rename(source, currentDestination); }
                    catch (IOException ignored) {}
                    return;
                case REPLACE:
                    // do nothing - default to replace...
                    break;
                case SKIP:
                    operationCountSkip(currentDestination);
                    return;
            }
        }

        operationCount(currentDestination);
        copyFileUsingStream(source, currentDestination);
        updateFile(source, currentDestination);
    }

    private void copyFileUsingStream(File source, File dest) {
        try (InputStream is = InputStream(source);
             OutputStream os = OutputStream(dest)) {

            byte[] buffer = new byte[20480];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
                if (canceled) break;
            }
        } catch (IOException e) {
            if (isDocumentFile(dest)) {
                try {
                    copyFileToExternalStorage(source, new File(dest.getPath()+"/"+source.getName()));//copyDocumentFile(source, dest);
                } catch (IOException ignored) {}
                Log.d("##### isDocumentFile #####", "true");
            }
            else cancel(Result.FAILED);
        }
    }

    private InputStream InputStream(File file) throws IOException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return Files.newInputStream(file.toPath());
        } else return new FileInputStream(file);
    }

    private OutputStream OutputStream(File file) throws IOException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return Files.newOutputStream(file.toPath());
        } else return new FileOutputStream(file);
    }

    private void copyDocumentFile(File source, File dest) {
        try {
            DocumentsContract.copyDocument(activity.getContentResolver(),
                    new JFile(source, activity).getUri(),
                    new JFile(dest, activity).getUri());
        } catch (Exception e) {
            cancel(Result.FAILED);
        }
    }

    public static void copyFileToExternalStorage(File sourceFile, File destinationFile) throws IOException {
        FileChannel sourceChannel = new FileInputStream(sourceFile).getChannel();
        FileChannel destinationChannel = new FileOutputStream(destinationFile).getChannel();
        destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());

        sourceChannel.close();
        destinationChannel.close();
    }

    private void rename(File source, File dest) throws IOException {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.move(oldName.toPath(), oldName.toPath().resolveSibling(source.getName()));
        } //TODO rename operation for SDK 24-26

        moveFile(source, dest);
        updateFile(oldSource, dest); // ...update
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
                    if (prev != 0) opDone += fileSize - prev;
                    prev = fileSize;
                    current = integerFileSize(fileSize); // integerFileSize(actionProgress.getProgress());
                    setProgress(getProgress() + (current - previous), animated);
                    activity.runOnUiThread(() -> binding.progressPresent.setText(String.format("%.1f", (double) 100 /
                            getMax() * getProgress()) + "%"));
                    previous = current;
                    Log.d("##### left out of #####", (counted - opSize) + " / " + opSize);
//                    Log.d("##### operationCount() #####", "progress = " + getProgress()
//                            + "\nprevious = " + previous + "\ncurrrnt = " + current +
//                            "\nfileSize = " + fileSize + " (" + file.length() + ")");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static long folderSize(File parent) {
        long length = 0;
        if (parent.listFiles() != null) {
            for (File file : Objects.requireNonNull(parent.listFiles())) {
                if (file.isDirectory()) length += folderSize(file);
                else length += file.length();
            }
        }
        return length;
    }

    @SuppressLint("SetTextI18n")
    public void operationPs() {
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            long previous = 0, current;

            @Override
            public void run() {
                if (operating) {
                    current = counted;
                    long ps = current - previous;
                    binding.progressPs.setText("S/" + Formatter.formatFileSize(instance, ps * 2));
                    if (current_operation != null && inBackground) {
                        builder.setContentText(current_operation.getName())
                                .setProgress(getMax(), getProgress(), false);
                        if (!canceled) notifyNotification();
                    } else if (!inBackground) {
                        NotificationManagerCompat.from(instance).cancel(notificationID);
                    }
                    previous = current;

                    // Schedule the next run after 500 milliseconds
                    handler.postDelayed(this, 500);
                }
            }
        };

        handler.post(runnable);
    }

}
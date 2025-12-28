package com.example.files.actions;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.folder;
import static com.example.files.actions.DialogCreateNew.getFolderName;

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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Objects;

public class DialogMove extends DialogBase {

    public DialogMove(ArrayList<JFile> mjFiles) {
        super(mjFiles, ActionType.Move,
                R.string.moving, R.string.move_here, R.string.move_action_finished,
                R.string.move_action_failed, R.drawable.action_move);
    }

    @Override
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void action() {
        operationPs();
        for (JFile jFile : jFiles) {
            if (canceled) break;
            if (jFile.isDirectory()) moveFolder(jFile, getTarget(parentTarget, jFile));
            else moveFile(jFile, getTarget(parentTarget, jFile), null);
        }
    }

    private void moveFolder(File source, File target) {

        Log.d("##### moveFolder #####", target.getPath());

        if (target.exists()) {
            if (!rememberChoiceFolder) {
                folderConflictDialog(target.getPath());
                while (wait) {
                    try { Thread.sleep(1000); }
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
                wait = true;
            }
            switch (folderChoice) {
                case RENAME:
                    target = getFolderName(target);
                    break;
                case MERGE:
                    // Do Nothing, let the files to merge
                    break;
                case SKIP:
                    operationFolderCountSkip(target);
                    return;
            }
        }

        if (!target.mkdirs() && !target.exists()) {
            cancel(Result.FAILED);
            return;
        }

        if (source.listFiles() != null || source.listFiles().length != 0) {

            for (File file : source.listFiles()) {
                if (canceled) break;
                if (file.isDirectory()) moveFolder(file, getTarget(target, file));
                else moveFile(file, getTarget(target, file), null);
            }
        }

        if (source.listFiles().length == 0) source.delete();
    }

    @SuppressLint("SetTextI18n")
    private void moveFile(File source, File target, Object ignored) {
        Log.d("##### moveFile #####", target.getPath());
        try {
            position++;
            activity.runOnUiThread(() -> {
                binding.progressText.setText(position + " / " + sum);
                binding.progressCurrent.setText(source.getName());
            });

            if (target.exists()) {
                if (!rememberChoice) {
                    fileConflictDialog(source.getPath(), target.getPath());
                    while (wait) {
                        try { Thread.sleep(1000); }
                        catch (InterruptedException e) { e.printStackTrace(); }
                    }
                    wait = true;
                }
                switch (fileChoice) {
                    case RENAME:
                        rename(source, target);
                        return;
                    case REPLACE:
                        // do nothing default to replace
                        break;
                    case SKIP:
                        operationCountSkip(target);
                        return;
                }
            }

            operationCount(target);
            moveFile(source, target);

            updateFile(source, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getTarget(File target, File source) {
        return new File(target.getPath() + "/" + source.getName());
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
                    counted += file.length() - prev;
                    prev = file.length();
                    current = integerFileSize(file.length()); // integerFileSize(actionProgress.getProgress());
                    setProgress(getProgress() + (current - previous), animated);
                    activity.runOnUiThread(() -> binding.progressPresent.setText(String.format("%.1f", (double) 100 /
                            getMax() * getProgress()) + "%"));
                    previous = current;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @SuppressLint("SetTextI18n")
    public void actionOld() {
        operationPs();
        for (JFile jFile : jFiles) {
            if (canceled) break;
            try {
                position++;
                operationCount(new File(parentTarget.getPath() + "/" + jFile.getName()));
                activity.runOnUiThread(() -> {
                    binding.progressText.setText(position + " / " + sum);
                    binding.progressCurrent.setText(jFile.getName());
                });
                File dest = new File(parentTarget.getPath() + "/" + jFile.getName());
                if (new JFile(parentTarget, activity).isDocumentFile())
                    try {
                        DocumentsContract.moveDocument(activity.getContentResolver(),
                                jFile.getDocumentFile().getUri(),
                                Objects.requireNonNull(jFile.getDocumentFile().getParentFile()).getUri(),
                                new JFile(parentTarget, activity).getDocumentFile().getUri());
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Files.move(jFile.toPath(), dest.toPath());
                        } else FileUtils.moveFile(jFile, dest);

                    }
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // TODO conflict handler (like in copy dialog)
                    Files.move(jFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else FileUtils.moveFile(jFile, dest);

                updateFile(jFile, new File(folder.getPath(), jFile.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void rename(File source, File dest) throws IOException {
        // save the original source to Files.copy()
        File oldSource = source;
        // temporary save the file before changing name
        source = new File(activity.getExternalFilesDir("temp").getPath() + "/" + oldSource.getName());
        operationCount(source);
        moveFile(oldSource, source);
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

}


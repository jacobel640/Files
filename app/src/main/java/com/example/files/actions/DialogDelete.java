package com.example.files.actions;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.app.NotificationManagerCompat;

import com.example.files.models.JFile;
import com.example.files.R;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.files.MainActivity.instance;

public class DialogDelete extends DialogBase {

    public DialogDelete(ArrayList<JFile> mjFiles) {
        super(mjFiles, ActionType.Delete,
                R.string.deleting, R.string.delete_confirm, R.string.delete_action_finished,
                R.string.move_action_failed, R.drawable.action_delete);
    }

    @Override
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void action () {
        operationPs();
        AtomicBoolean deleted = new AtomicBoolean(false);
        for(JFile jFile : jFiles) {
            if (canceled) break;
            activity.runOnUiThread(() -> binding.progressCurrent.setText(jFile.getName()));
            current_operation = jFile;
            if (jFile.isDirectory() && jFile.listJFiles() != null) deleteFolder(jFile);
            else position++;
            if (jFile.isDocumentFile()) {
                //deleted.set(jFile.getDocumentFile().delete());
                try {
                    DocumentsContract.deleteDocument(activity.getContentResolver(), jFile.getDocumentFile().getUri());
                    deleted.set(true);
                    instance.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, jFile.getDocumentFile().getUri()));
                } catch (Exception e) {
                    deleted.set(jFile.delete());
                }
            } else {
                deleted.set(jFile.delete());
                instance.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(jFile)));
            }

            operationCount(position, sum);
            builder.setContentText(jFile.getName())
                    .setProgress(getMax(), getProgress(), false);
            if (!operating) notifyNotification();
        }

    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void deleteFolder(JFile folder){
        AtomicBoolean deleted = new AtomicBoolean(false);
        for (JFile file : Objects.requireNonNull(folder.listJFiles())) {
            if (canceled) break;
            if (file.isDirectory() && file.listJFiles() != null) deleteFolder(file);
            else position++;

            operationCount(position, sum);
            builder.setContentText(file.getName())
                    .setProgress(getMax(), getProgress(), false);
            if (!operating) notifyNotification();
            instance.runOnUiThread(() -> binding.progressCurrent.setText(file.getName()));
            current_operation = file;
            if (file.isDocumentFile()) {
                //deleted.set(jFile.getDocumentFile().delete());
                try {
                    DocumentsContract.deleteDocument(activity.getContentResolver(), file.getDocumentFile().getUri());
                    deleted.set(true);
                    instance.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, file.getDocumentFile().getUri()));
                } catch (FileNotFoundException e) {
                    deleted.set(file.delete());
                }
            }
            else deleted.set(file.delete());
            instance.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void operationCount(int position, int sum) {
        instance.runOnUiThread(() -> {
            setProgress(position, false);
            binding.progressText.setText(position + " / " + sum);
            binding.progressPresent.setText(String.format("%.1f", (double) 100 /
                    sum * position) + "%");
        });
    }

    @SuppressLint("SetTextI18n")
    public void operationPs() {
        new Thread(() -> {
            while (operating) {
                try {
                    Thread.sleep(500);
                    if (current_operation!=null && inBackground) {
                        builder.setContentText(current_operation.getName())
                                .setProgress(getMax(), getProgress(), false);
                        if (!canceled) notifyNotification();
                    } else if (!inBackground) NotificationManagerCompat.from(instance).cancel(notificationID);
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    @Override
    @SuppressLint("UseCompatLoadingForDrawables")
    public void showDeleteConfirmDialog() {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_delete);
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).setWindowAnimations(R.style.DialogAnimation);
        Objects.requireNonNull(dialog.getWindow()).
                setBackgroundDrawable(activity.getDrawable(R.color.transparent));

        TextView delTitle = dialog.findViewById(R.id.del_title);
        TextView delCancel = dialog.findViewById(R.id.del_cancel);
        TextView delete = dialog.findViewById(R.id.del_delete);

        delTitle.setText(activity.getString(R.string.delete_confirm, countItems(activity, jFiles.size())));
        delCancel.setClipToOutline(true);
        delete.setClipToOutline(true);

        delCancel.setOnClickListener(view -> dialog.dismiss());
        delete.setOnClickListener(view -> {
            dialog.dismiss();
            startAction(null);
        });

        dialog.setOnDismissListener(dialog1 -> instance.eventListener.onMultiSelectedChange(false));

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.BOTTOM + Gravity.END);
        dialog.show();
    }

}

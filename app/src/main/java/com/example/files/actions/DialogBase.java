package com.example.files.actions;

import static com.example.files.MainActivity.actionBarVisibility;
import static com.example.files.MainActivity.cleanCache;
import static com.example.files.MainActivity.instance;
import static com.example.files.MainActivity.textBtnState;
import static com.example.files.Statics.OPS_CHANNEL_ID;
import static com.example.files.Statics.TAG_ZIPPED;
import static com.example.files.Statics.actionBar;
import static com.example.files.Statics.actions;
import static com.example.files.Statics.copyMode;
import static com.example.files.Statics.currentFragment;
import static com.example.files.Statics.folder;
import static com.example.files.utils.MainActivityUtils.Storages.storageItems;
import static com.example.files.actions.DialogBase.ActionType.Copy;
import static com.example.files.actions.DialogBase.ActionType.Delete;
import static com.example.files.actions.DialogBase.ActionType.Move;
import static com.example.files.actions.DialogBase.Alternative.MERGE;
import static com.example.files.actions.DialogBase.Alternative.RENAME;
import static com.example.files.actions.DialogBase.Alternative.REPLACE;
import static com.example.files.actions.DialogBase.Alternative.SKIP;
import static com.example.files.actions.DialogCopy.folderSize;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.fragment.app.Fragment;

import com.example.files.BuildConfig;
import com.example.files.MainActivity;
import com.example.files.R;
import com.example.files.listeners.OnActivityStateChange;
import com.example.files.utils.AsyncTask;
import com.example.files.utils.PathFormatter;
import com.example.files.view.DialogProgress;
import com.example.files.view.Note;
import com.example.files.listeners.ActionEvent;
import com.example.files.models.JFile;
import com.google.android.material.chip.Chip;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

public abstract class DialogBase extends DialogProgress {

    private ActionEvent actionEventListener;
    protected long HUNDRED_MB = 104857600, countSize, counted, opSize, opDone = 0;
    protected int notificationID, dialogID, sum = 0, position = 0; // TODO dialog notification ID in a different value

    protected enum SizeType {BYTE, KB, MB}

    protected enum ActionType {Copy, Move, Delete, Extract}

    protected enum Alternative {RENAME, REPLACE, SKIP, MERGE}

    public enum Result {SUCCESS, FAILED, CANCELED, INSUFFICIENT_SPACE}

    protected File parentTarget, current_operation;
    protected ActionType type;
    protected SizeType typeSize;
    protected Alternative fileChoice, folderChoice;

    protected Activity activity;
    protected ArrayList<JFile> jFiles;
    protected boolean canceled, inBackground,
            wait = true, rememberChoice, rememberChoiceFolder;

    private NotificationManagerCompat notificationManager;
    protected NotificationCompat.Builder builder;

    public boolean operating;
    public int resTitle;
    protected int resAction;
    protected int resActionFinished;
    protected int resActionFailed;
    protected int resActionDrawable;

    @SuppressLint("SetTextI18n")
    public DialogBase(ArrayList<JFile> jFiles, ActionType type,
                      int resTitle, int resAction, int resActionFinished,
                      int resActionFailed, int resActionDrawable) {
        super(instance);

        this.activity = instance;
        this.jFiles = new ArrayList<>();
        this.jFiles.addAll(jFiles);
        this.dialogID = actions.size();
        this.notificationID = IdGenerator();
        Chip chipID = findViewById(R.id.action_id);
        chipID.setText("n-id:" + notificationID);
        chipID.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

        setType(type);
        setDialogResources(resTitle, resAction, resActionFinished, resActionFailed, resActionDrawable);
        setupNotification(); // after initializing resources

        actionEventListener = (result) -> {};

        instance.addActivityStateChangeListener(new OnActivityStateChange() {
            @Override
            public void onResume() {
                if (operating) show();
            }

            @Override
            public void onPause() {
                hide();
            }
        });
    }

    private int IdGenerator() {
        return checkDuplicateID(new Random().nextInt(999) + 100);
    }

    public int checkDuplicateID(int tempID) {
        for (DialogBase action : actions) {
            if (this.dialogID == action.dialogID) continue;
            if (tempID == action.notificationID) {
                tempID = checkDuplicateID(++tempID);
                break;
            }
        }
        return tempID;
    }

    private void setType(ActionType type) {
        this.type = type;
        this.showPerSecond(this.type != Delete);
    }

    private void setDialogResources(int resTitle, int resAction, int resActionFinished,
                                    int resActionFailed, int resActionDrawable) {
        this.resTitle = resTitle;
        this.resAction = resAction;
        this.resActionFinished = resActionFinished;
        this.resActionFailed = resActionFailed;
        this.resActionDrawable = resActionDrawable;
        this.binding.progressTitle.setText(resTitle);
    }

    private void setupNotification() {
        this.notificationManager = NotificationManagerCompat.from(instance);
        this.builder = new NotificationCompat.Builder(instance, OPS_CHANNEL_ID);
        this.builder.setContentTitle(activity.getString(resTitle))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setSmallIcon(resActionDrawable)
                .setContentText("")
                .setSilent(true);

        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(instance, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(instance);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        this.builder.setContentIntent(resultPendingIntent);
    }

    public void reshow() {
        if (operating) {
            showActionProgress();
            inBackground = false;
        } else new Note(activity, "", activity.getString(resActionFinished), "").show();
    }

    public void preAction() {
        if (jFiles.isEmpty()) return;
//        else OPERATING = type;

        if (type == Delete) {
            showDeleteConfirmDialog();
            return;
        }

        if (type == Copy || type == Move)
            ((TextView) actionBar.findViewById(R.id.copy_here)).setText(activity.getText(resAction));

        textBtnState(currentFragment.isFilesType());
        ((TextView) actionBar.findViewById(R.id.items)).setText(countItems(activity, jFiles.size()));
        actionBar.findViewById(R.id.copy_dialog).setVisibility(View.VISIBLE);
        copyMode = true;
        instance.eventListener.onMultiSelectedChange(false);
    }

    protected abstract void action();

    public void startAction(File destination) {

        if (destination != null) this.parentTarget = destination;

        new AsyncTask(() -> {
            actionBarVisibility(View.GONE); // "Copy Here" button
            showActionProgress();

            canceled = false;
            copyMode = false;
            operating = true;
            instance.eventListener.onRefreshActionsList(); // need to append after operating is true
//            OPERATING = 0;

            countSize = 0;
            sum = 0;
            position = 0;
            counted = 0;
        }, () -> {
            if (Looper.myLooper() == null) Looper.prepare();
            for (JFile jFile : jFiles)
                if (jFile.isDirectory()) sum += numFiles(jFile)[1];
                else sum++;

            if (type != Delete) {
                for (JFile jFile : jFiles) {
                    countSize += fileSize(jFile);
                }
                counted = countSize;
                opSize = countSize;
                try {
                    typeSize = SizeType.BYTE;
                    start(Math.toIntExact(countSize));
                } catch (Exception e) {
                    try {
                        typeSize = SizeType.KB;
                        countSize = countSize / 1024;
                        start(Math.toIntExact(countSize));
                    } catch (Exception f) {
                        typeSize = SizeType.MB;
                        countSize = countSize / 1024;
                        start(Math.toIntExact(countSize));
                    }
                }

                if (opSize > (getFreeSpace() - HUNDRED_MB)/* leave about 100 MB spare*/) { // int value 102400
                    cancel(Result.INSUFFICIENT_SPACE);
                    return;
                }
                Log.d("##### INSUFFICIENT_SPACE #####", "\ncountSize:" + opSize + "\nfreeSpace:" + getFreeSpace());
            } else start(sum);
            binding.progressCancel.post(() ->
                    binding.progressCancel.setOnClickListener(v -> cancel(Result.CANCELED)));
            setOnDismissListener(dialog1 -> inBackground = true); // should verify if operating so you wouldn't get notification on the end
            binding.proceedBackground.setOnClickListener(v -> {
                hide();
                inBackground = true;
            });
            if (inBackground) {
                builder.setProgress(getMax(), 0, false);
                notifyNotification();
            }

            action();
        }, () -> {
            if (canceled) return;
            cancel(Result.SUCCESS);
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected void cancel(Result result) {
        Log.d("##### cancel() #####", "start-method");

        canceled = true;
        operating = false;
        inBackground = !isShowing();
        actionEventListener.onActionFinished(result); //instance.eventListener.onRefreshActionsList();
        actionProgressDismiss();

        switch (result) {
            case INSUFFICIENT_SPACE: // not enough space
                dismiss();
                if (inBackground) {
                    builder.setContentTitle(activity.getString(R.string.copy_action_failed));
                    builder.setContentText(activity.getString(R.string.insufficient_space_alert,
                                    Formatter.formatFileSize(activity, counted),
                                    Formatter.formatFileSize(activity, getFreeSpace() - HUNDRED_MB),
                                    Formatter.formatFileSize(activity, counted - (getFreeSpace() - HUNDRED_MB))) + "\n" +
                                    activity.getString(R.string.insufficient_space_hint))
                            .setProgress(0, 0, false)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setSmallIcon(R.drawable.error);
                }
                insufficientSpacePrompt();
                Log.d("##### cancel().result #####", String.valueOf(result));
                break;
            case SUCCESS: // successfully copied
                if (inBackground) {
                    builder.setContentText(activity.getString(resActionFinished))
                            .setProgress(0, 0, false)
                            .setSmallIcon(R.drawable.check_circle);
                } else NotificationManagerCompat.from(instance).cancel(notificationID);
                if (!zippedExist()) cleanCache(activity);
                Log.d("##### cancel().result #####", String.valueOf(result));
                break;
            case CANCELED: // canceled in purpose
                Log.d("##### cancel().result #####", String.valueOf(result));
                break;
            case FAILED: // some failure
                if (inBackground) {
                    builder.setContentText(activity.getString(resActionFailed))
                            .setProgress(0, 0, false)
                            .setSmallIcon(R.drawable.error);
                }
                Log.d("##### cancel().result #####", String.valueOf(result));
                break;
        }
        if (inBackground) notifyNotification(); // for all the switch cases...

        actions.remove(this);
        instance.eventListener.onRefresh();
        Log.d("##### cancel() #####", "end-method");
    }

    private void insufficientSpacePrompt() {
        // have to run inside runOnUiThread to be able to show dialog inside a thread
        activity.runOnUiThread(() ->
                new Note(activity, activity.getString(R.string.insufficient_space_title),
                        activity.getString(R.string.insufficient_space_alert,
                                Formatter.formatFileSize(activity, counted),
                                Formatter.formatFileSize(activity, getFreeSpace() - HUNDRED_MB),
                                Formatter.formatFileSize(activity, counted - (getFreeSpace() - HUNDRED_MB))),
                        activity.getString(R.string.insufficient_space_hint)).show());
    }

    // don't clean cache if a zip file currently open in fragment in the stack
    protected boolean zippedExist() {
        for (Fragment fragment :
                instance.getSupportFragmentManager().getFragments()) {
            if (fragment.getTag() != null && fragment.getTag().equals(TAG_ZIPPED))
                return true;
        }
        return false;
    }

    protected String countItems(Context context, int jFilesSize) {
        if (jFilesSize > 1)
            if (IntStream.range(0, jFilesSize).allMatch(i -> jFiles.get(i).isDirectory()))
                return jFilesSize + " " + context.getString(R.string.folders);
            else if (IntStream.range(0, jFilesSize).noneMatch(i -> jFiles.get(i).isDirectory()))
                return jFilesSize + " " + context.getString(R.string.files);
            else return context.getString(R.string.items, String.valueOf(jFilesSize));
        else if (jFilesSize == 1) {
            if (jFiles.get(0).isDirectory())
                return context.getString(R.string.one_folder);
            else return context.getString(R.string.one_file);
        } else return "";
    }

    protected int integerFileSize(long fileSize) {
        switch (typeSize) {
            case BYTE:
                return Math.toIntExact(fileSize);
            case KB:
                fileSize = fileSize / 1024;
                return Math.toIntExact(fileSize);
            default:
                fileSize = fileSize / 1024;
                return Math.toIntExact(fileSize);
        }
    }

    private static int[] numFiles(File file) {
        int f = 0, d = 0;
        int[] arr;// = {0,0};
        if (file.listFiles() == null) return new int[]{0, 0};
        for (File s : Objects.requireNonNull(file.listFiles())) {
            if (s.isDirectory() && Objects.requireNonNull(file.listFiles()).length != 0) {
                d++;
                arr = numFiles(s);
                d += arr[0];
                f += arr[1];
            } else f++;
        }
        return new int[]{d, f};
    }

    private long fileSize(File file) {
        long currentLength = 0;
        if (file.isDirectory()) {
            if (file.listFiles() == null) return 0;
            for (File f : Objects.requireNonNull(file.listFiles())) {
                if (f.isDirectory()) {
                    currentLength += fileSize(f);
                } else currentLength += f.length();
            }
        } else currentLength += file.length();
        return currentLength;
    }

    private long getFreeSpace() {
        StatFs stat = new StatFs(storageItems.get(0).getFile().getPath());
        double availBlocks = stat.getAvailableBlocksLong();
        double blockSize = stat.getBlockSizeLong();
        long free_memory = (long) availBlocks * (long) blockSize;

        for (DialogBase action : actions) {
            if (action.dialogID == dialogID) continue;
            if (action.type == ActionType.Copy)
                free_memory -= opSize - (counted - opSize); // TODO recalculate
        }

        return free_memory;
    }

    protected void updateFile(File old, File current) {
        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(current)));
        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(old)));
    }

    public boolean checkConflicts() {
        if (!operating) {
            String[] dest = folder.getPath().split("/");
            for (JFile jFile : jFiles) {
                String[] target = jFile.getPath().split("/");
                if (target.length <= dest.length) {
                    boolean isSub = false;
                    for (int i = 0; i < target.length; i++) {
                        isSub = target[i].equals(dest[i]);
                    }
                    if (isSub) return true;
                }
            }
        }
        return false;
    }

    protected void showDeleteConfirmDialog() {
        // Overriding in DialogDelete.class
    }

    public int getDialogID() {
        return dialogID;
    }

    public void notifyNotification() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e("##### notifyNotification #####", "need permission request");
            return;
        }
        notificationManager.notify(notificationID, builder.build());
    }

    public void addEventListener(ActionEvent actionEventListener) {
        this.actionEventListener = actionEventListener;
    }

//    public void addEventListener(ActionEvent actionEventListener) {
//        this.actionEventListeners.add(actionEventListener);
//    }
//
//    private void notifyActionEventListeners() {
//        for (ActionEvent listener : actionEventListeners) {
//            listener.onActionFinished();
//        }
//        actionEventListeners.clear();
//    }

    protected void moveFile(File source, File target) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else FileUtils.moveFile(source, target);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    protected void operationCountSkip(File file) {
        int current = integerFileSize(file.length()); // integerFileSize(actionProgress.getProgress());
        setProgress(getProgress() + current, false);
        activity.runOnUiThread(() -> binding.progressPresent.setText(String.format("%.1f", (double) 100 /
                getMax() * getProgress()) + "%"));
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    protected void operationFolderCountSkip(File parent) {
        int current = integerFileSize(folderSize(parent));
        setProgress(getProgress() + current, false);
        activity.runOnUiThread(() -> binding.progressPresent.setText(String.format("%.1f", (double) 100 /
                getMax() * getProgress()) + "%"));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    protected void fileConflictDialog(String file1, String file2) {
        // TODO send notification - alert the user about conflict
        activity.runOnUiThread(() -> {
            Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.dialog_action_conflict);
            dialog.setCancelable(false);
            Objects.requireNonNull(dialog.getWindow()).setWindowAnimations(R.style.DialogAnimation);
            Objects.requireNonNull(dialog.getWindow()).
                    setBackgroundDrawable(activity.getDrawable(R.color.transparent));

            TextView fileName1 = dialog.findViewById(R.id.file_name_1);
            TextView fileName2 = dialog.findViewById(R.id.file_name_2);
            TextView rename = dialog.findViewById(R.id.rename);
            TextView replace = dialog.findViewById(R.id.replace);
            TextView skip = dialog.findViewById(R.id.skip);
            CheckBox sameForAll = dialog.findViewById(R.id.same_for_all);
            sameForAll.setOnCheckedChangeListener((buttonView, isChecked) -> rememberChoice = isChecked);

//        title.setText(activity.getString(R.string.delete_confirm, countItems(activity, jFiles)));
            rename.setClipToOutline(true);
            replace.setClipToOutline(true);
            skip.setClipToOutline(true);

            fileName1.setText(new PathFormatter(activity).format(file1));
            fileName2.setText(new PathFormatter(activity).format(file2));

            rename.setOnClickListener(view -> {
                fileChoice = RENAME;
                dialog.dismiss();
            });
            replace.setOnClickListener(view -> {
                fileChoice = REPLACE;
                dialog.dismiss();
            });
            skip.setOnClickListener(view -> {
                fileChoice = SKIP;
                dialog.dismiss();
            });
            dialog.setOnDismissListener(dialog1 -> wait = false);

            Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.show();
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    protected void folderConflictDialog(String folder) {
        // TODO send notification - alert the user about conflict
        activity.runOnUiThread(() -> {
            Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.dialog_action_folder_conflict);
            dialog.setCancelable(false);
            Objects.requireNonNull(dialog.getWindow()).setWindowAnimations(R.style.DialogAnimation);
            Objects.requireNonNull(dialog.getWindow()).
                    setBackgroundDrawable(activity.getDrawable(R.color.transparent));

            TextView folderPath = dialog.findViewById(R.id.folder_name);
            TextView rename = dialog.findViewById(R.id.rename);
            TextView merge = dialog.findViewById(R.id.merge);
            TextView skip = dialog.findViewById(R.id.skip);
            CheckBox sameForAll = dialog.findViewById(R.id.same_for_all);
            sameForAll.setOnCheckedChangeListener((buttonView, isChecked) -> rememberChoiceFolder = isChecked);

//        title.setText(activity.getString(R.string.delete_confirm, countItems(activity, jFiles)));
            rename.setClipToOutline(true);
            merge.setClipToOutline(true);
            skip.setClipToOutline(true);

            folderPath.setText(new PathFormatter(activity).format(folder));

            rename.setOnClickListener(view -> {
                folderChoice = RENAME;
                dialog.dismiss();
            });
            merge.setOnClickListener(view -> {
                folderChoice = MERGE;
                dialog.dismiss();
            });
            skip.setOnClickListener(view -> {
                folderChoice = SKIP;
                dialog.dismiss();
            });
            dialog.setOnDismissListener(dialog1 -> wait = false);

            Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.show();
        });
    }

}

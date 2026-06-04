package com.example.files.actions;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.actionBar;
import static com.example.files.Statics.folder;
import static com.example.files.actions.DialogRename.setFilters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.view.Note;
import com.example.files.models.JFile;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class DialogCreateNew extends Dialog {

    TextView cancel ,create;
    TextInputEditText textInput;
    TextInputLayout textLayout;
    MaterialButtonToggleGroup createSelection;
    TextView dialogTitle;

    Activity activity;

    boolean isExternalStorage;

    public DialogCreateNew(Activity activity, boolean isExternalStorage) {
        super(activity);
        this.activity = activity;
        this.isExternalStorage = isExternalStorage;
        show();
        //this.isExternalStorage = Environment.isExternalStorageRemovable(folder);
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_create_new);
        setCancelable(true);
        getWindow().setWindowAnimations(R.style.DialogAnimation);
        getWindow().setBackgroundDrawable(activity.getDrawable(R.color.transparent));
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.BOTTOM);

        setOnDismissListener(dialogInterface -> {
            hideKeyboard(activity);
            if (actionBar.findViewById(R.id.copy_dialog).getVisibility() == View.VISIBLE) {
                instance.eventListener.onMultiSelectedChange(false);
            } else actionBar.setVisibility(View.GONE);
            instance.eventListener.onRefresh();
            // else not refreshing and can't see the new one...
        });

        dialogTitle = findViewById(R.id.dtv_title);
        textLayout = findViewById(R.id.text_layout);
        textInput = findViewById(R.id.text_input);
        createSelection = findViewById(R.id.create_selection);
        cancel = findViewById(R.id.btn_cancel);
        create = findViewById(R.id.btn_create);
        setFilters(textInput);

        String fileName = instance.getString(R.string.new_file) + ".txt";
        String folderName = instance.getString(R.string.new_folder);

        dialogTitle.setText(R.string.create_folder);

        File newFolder = getFolderName(new File(folder.getPath() + "/" + folderName));
        textInput.setText(newFolder.getName());
        textInput.requestFocus();

        showKeyboard(activity);

        textInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                create.setEnabled(s.length() != 0);
                textLayout.setErrorEnabled(false);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        createSelection.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            Log.d("##### create file #####", "" + (checkedId == R.id.create_file));
            dialogTitle.setText(checkedId == R.id.create_file ? R.string.create_file : R.string.create_folder);
            textLayout.setHint(checkedId == R.id.create_file ? R.string.new_file : R.string.new_folder);
            textInput.setHint(checkedId == R.id.create_file ? R.string.new_file : R.string.new_folder);
            textInput.setText(checkedId == R.id.create_file ? getFileName(fileName) : getFolderName(newFolder).getName());
            textInput.clearFocus();
            textInput.requestFocus();
        });

        cancel.setOnClickListener(view -> dismiss());
        create.setOnClickListener(view -> createNew());

    }

    void createNew() {
        AtomicBoolean created = new AtomicBoolean(false);
//        JFile pickedDir = new JFile(folder, activity);
        JFile newJFile;
        if (createSelection.getCheckedButtonId() == R.id.create_file) {
            newJFile = new JFile(folder.getPath() + "/" + Objects.requireNonNull(textInput.getText()).toString().trim()+".txt", activity);
            if (!newJFile.exists()) {
                try {
                    if (newJFile.isDocumentFile()) {
                        Uri parentUri = new JFile(folder, activity).getDocumentFile().getUri();
                        DocumentsContract.createDocument(activity.getContentResolver(),
                                parentUri, "txt", newJFile.getName());
                        Uri uri = newJFile.getUri();
                        @SuppressLint("Recycle")
                        OutputStream outStream = activity.getContentResolver().openOutputStream(uri);
                        outStream.write("קובץ טקסט חדש\n".getBytes());
                    } else {
                        created.set(newJFile.createNewFile());
                        FileWriter out = new FileWriter(newJFile);
                        out.write("קובץ טקסט חדש\n");
                        out.close();
                    }
                    dismiss();
                } catch (IOException e) {
                    e.printStackTrace();
                    new Note(activity, e.getMessage()).show();
                }
            } else textInput.setError(activity.getString(R.string.error_existed_name_conflict));
        } else {
            newJFile = new JFile(folder.getAbsolutePath() + "/"
                    + Objects.requireNonNull(textInput.getText()).toString().trim(), activity);
            if (!newJFile.exists()) {
                created.set(newJFile.mkdirs());
                if (!created.get()) new JFile(folder, activity).getDocumentTreeSec()
                        .createDirectory(newJFile.getName());
                dismiss();
            } else textInput.setError(activity.getString(R.string.error_existed_name_conflict));

        }
        instance.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(newJFile)));
    }

    public static boolean createNewFile(JFile jFile) {
        android.content.Context activity = jFile.getContext();
        AtomicBoolean created = new AtomicBoolean(false);
        try {
            if (jFile.isDocumentFile()) {
                Uri parentUri = new JFile(folder, activity).getDocumentFile().getUri();
                DocumentsContract.createDocument(activity.getContentResolver(),
                        parentUri, "txt", jFile.getName());
            } else {
                created.set(jFile.createNewFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return created.get();
    }

    public static void hideKeyboard(Context context) {
        try {
            ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if ((((Activity) context).getCurrentFocus() != null) && (((Activity) context).getCurrentFocus().getWindowToken() != null)) {
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showKeyboard(Context context) {
        ((InputMethodManager) (context).getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static File getFolderName(File folder) {
        File parent = new File(Objects.requireNonNull(folder.getParentFile()).getPath());
        File tempDir = folder;

        if (tempDir.exists())
            for (int i = 0; i < Objects.requireNonNull(parent.list()).length; i++) {
                if (tempDir.exists())
                    tempDir = new File(folder + " (" + (i + 1) + ")");
                else break;
            }
        return tempDir;
    }

    String getFileName(String fileName) {
        String name = fileName.substring(0, fileName.lastIndexOf("."));
        String ext = fileName.substring(fileName.lastIndexOf("."));
        File tempFile = new File(folder.getPath() + "/" + name + ext);
        if (tempFile.exists())
            for (int i = 0; i < Objects.requireNonNull(folder.list()).length; i++) {
                if (tempFile.exists())
                    tempFile = new File(Objects.requireNonNull(tempFile.getParentFile())
                            .getPath() + "/" + name + " (" + (i + 1) + ")" + ext);
                else break;
            }
        return tempFile.getName().substring(0, tempFile.getName().
                lastIndexOf("."));
    }

}

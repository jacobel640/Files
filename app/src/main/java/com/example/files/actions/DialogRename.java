package com.example.files.actions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.models.JFile;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static com.example.files.MainActivity.instance;
import static com.example.files.MainActivity.setTextButtonState;
import static com.example.files.Statics.favorites;
import static com.example.files.Statics.selectedJFiles;

public class DialogRename {

    Dialog dialog;
    Button cancel ,rename;
    TextInputLayout textLayout;
    TextInputEditText textInput;
    MaterialButtonToggleGroup options;
    boolean includeExt;
    TextView dialogTitle;

    Activity activity;
    File file;
    int favoritesID = -1;

    public DialogRename(Activity activity) {

        this.activity = activity;
        this.file = selectedJFiles.get(0);
        rename();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void rename() {

        favoritesID = favorites.IDFromPath(file.getPath());
        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_rename);
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).setWindowAnimations(R.style.DialogAnimation);
        Objects.requireNonNull(dialog.getWindow()).
                setBackgroundDrawable(activity.getDrawable(R.color.transparent));
        dialog.setOnDismissListener(dialogInterface -> hideKeyboard(activity));

        dialogTitle = dialog.findViewById(R.id.dtv_title);
        textLayout = dialog.findViewById(R.id.text_layout);
        textInput = dialog.findViewById(R.id.text_input);
        options = dialog.findViewById(R.id.rename_options);
        cancel = dialog.findViewById(R.id.btn_cancel);
        rename = dialog.findViewById(R.id.btn_create);
        setTextButtonState(rename, false);
        setFilters(textInput);

        String name = file.getName();
        String ext = "";
        if (file.isDirectory() || !file.getName().contains("."))
            dialog.findViewById(R.id.include_ext).setVisibility(View.GONE);
        else {
            name = file.getName().substring(0, file.getName().
                    lastIndexOf("."));
            ext = file.getName().substring(file.getName().
                    lastIndexOf("."));//, file.getName().length());
        }
        textInput.setText(name);
        textInput.requestFocus();
        showKeyboard(activity);
        String finalName = name;
        String finalExt = ext;
        textInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

                boolean sameName = s.toString().trim().equals(
                        includeExt ? finalName + finalExt : finalName);
                boolean enabled = (s.toString().trim().length() != 0 && !sameName);
                setTextButtonState(rename, enabled);
                textLayout.setErrorEnabled(false);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        options.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            String text = Objects.requireNonNull(textInput.getText()).toString().trim();

            if (checkedId == R.id.include_ext) {
                includeExt = isChecked;
                textInput.setText(isChecked ? text + finalExt : text.substring(0, text.lastIndexOf(finalExt)));
            } else if (checkedId == R.id.full_name) {
                textInput.setSingleLine(!isChecked);
                textInput.setText(text);
            }

            textInput.clearFocus();
            textInput.requestFocus();
        });

        cancel.setOnClickListener(view -> dialog.dismiss());
        rename.setOnClickListener(view -> { //File to = new File(file.getPath(), String.valueOf(fName.getText()));
            File newName;
            if (file.isDirectory() || includeExt)
                newName = new File(Objects.requireNonNull(file.getParentFile()).getPath(), Objects.requireNonNull(textInput.getText()).toString().trim());
            else newName = new File(Objects.requireNonNull(file.getParentFile()).getPath(), Objects.requireNonNull(textInput.getText()).toString().trim() + finalExt);
            if (!newName.exists()) { // file.renameTo(to);
                if (isDocumentFile(newName)) {
                    try {
                        DocumentsContract.renameDocument(activity.getContentResolver(),
                                new JFile(file, activity).getUri(),
                                newName.getName());
                        instance.eventListener.onRefresh();
                        instance.eventListener.onMultiSelectedChange(false);
                        dialog.dismiss();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        textInput.setError(e.getMessage());
                    }
                }
                else try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Files.move(file.toPath(), file.toPath().resolveSibling(newName.getName()));
                    } //TODO move operation for SDK 24-26
                    updateFile(file, newName);
                    if (favoritesID != -1) {
                        Log.d("##### rename.confirmClick #####", "is favorites ID: " + favoritesID);
                        favorites.update(favoritesID, file.getPath());
                    }
                    instance.eventListener.onRefresh();
                    instance.eventListener.onMultiSelectedChange(false);
                    dialog.dismiss();
                } catch (IOException e) {
                    e.printStackTrace();
                    textInput.setError(e.getMessage());
                    rename.setEnabled(false);
                }
                //objects.get();
            } else {
                textInput.setError(activity.getString(R.string.error_existed_name_conflict));
            }
        });

        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
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

    public void updateFile(File old, File current) {
        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(current)));
        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(old)));
    }

    @SuppressLint("SdCardPath")
    private boolean isDocumentFile(File file) {
        if (!file.getPath().startsWith("/storage/emulated"))
            return !file.getPath().startsWith("/sdcard");
        return false;
    }

    public static void setFilters(TextInputEditText editText) {
        String restrictedCharacters = "*?<>\"|";

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                boolean keepOriginal = true;
                StringBuilder sb = new StringBuilder(end - start);
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (isCharAllowed(c)) // put your condition here
                        sb.append(c);
                    else
                        keepOriginal = false;
                }
                if (keepOriginal)
                    return null;
                else {
                    if (source instanceof Spanned) {
                        SpannableString sp = new SpannableString(sb);
                        TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                        return sp;
                    } else {
                        return sb;
                    }
                }
            }

            private boolean isCharAllowed(char c) {
                if (Character.isLetterOrDigit(c) || Character.isSpaceChar(c)) {
                    return true;
                } else if (Character.isISOControl(c)) return false; else {
                    for (Character character : restrictedCharacters.toCharArray()) {
                        if (character.equals(c)) return false;
                    }
                }
                return true;
            }
        };
        editText.setFilters(new InputFilter[] { filter });

    }

}


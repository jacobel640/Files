package com.example.files.view;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.files.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

@SuppressLint("ViewConstructor")
public class DialogItem extends LinearLayout {
    TextView tvTitle, tvContent;
    ProgressBar pbLoading;
    int itemId;
    boolean show;

    @SuppressLint({"InflateParams", "UseCompatLoadingForDrawables", "StringFormatInvalid"})
    public DialogItem(Context context, String title) {
        super(context);
        inflate(context, R.layout.item_dialog_details, this);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(title);
        tvContent = findViewById(R.id.tv_content);
        tvTitle.setClipToOutline(true);
        tvContent.setClipToOutline(true);
        pbLoading = findViewById(R.id.pb_loading);
        this.show = true;

        setOnLongClickListener(v -> {
            showPopupView(context.getString(R.string.confirm_copy_to_clipboard, title),
                    context.getString(R.string.copy_action), () -> {
                        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Copied", tvContent.getText());
                        cm.setPrimaryClip(clip);
                        Snackbar.make(this, context.getString(R.string.copied_to_clipboard),
                                BaseTransientBottomBar.LENGTH_SHORT).show();
                    });
            return true;
        });
    }

    public String getTitle() {
        return tvContent.getText().toString();
    }

    public String getContent() {
        return tvContent.getText().toString();
    }

    public TextView getTvContent() {
        return tvContent;
    }

    public void setContent(String content) {
        this.post(() -> tvContent.setText(content));
    }

    public boolean show() {
        return show;
    }

    public void toShow(boolean show) {
        this.show = show;
    }

    public int getItemId() {
        return itemId;
    }

    public void showProgress() {
        pbLoading.setVisibility(VISIBLE);
    }

    public void hideProgress() {
        pbLoading.setVisibility(GONE);
    }

    public boolean isProgressVisible() {
        return pbLoading.getVisibility() == VISIBLE;
    }

    @SuppressLint("UseCompatLoadingForDrawables, InflateParams")
    public void showPopupView(String message, String buttonText, Runnable runnable) {
        Context context = this.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        PopupWindow pw = new PopupWindow(inflater.inflate(R.layout.popup_view_text_and_button, null), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
        View parent = pw.getContentView();
        TextView text = parent.findViewById(R.id.message);
        TextView btnCopy = parent.findViewById(R.id.btn_copy);
        btnCopy.setClipToOutline(true);
        btnCopy.setText(buttonText);
        text.setText(message);
        pw.showAsDropDown(this, 0, -this.getHeight(), Gravity.BOTTOM);
        btnCopy.setOnClickListener(v -> {
            runnable.run();
            pw.dismiss();
        });
        // dismiss after 3 seconds
        new Handler().postDelayed(pw::dismiss, 3000);
    }
}

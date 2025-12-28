package com.example.files.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.files.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class SelfUpdateSheet extends BottomSheetDialog {

    Button cancelBtn;
    Button confirmBtn;
    TextView changelogTitleTv, changelogTv;
    @SuppressLint("MissingInflatedId")
    public SelfUpdateSheet(@NonNull Context context) {
        super(context);
        setContentView(R.layout.sheet_self_update);

        cancelBtn = findViewById(R.id.btn_cancel);
        confirmBtn = findViewById(R.id.btn_confirm);
        changelogTitleTv = findViewById(R.id.changelog_title);
        changelogTv = findViewById(R.id.changelog);

        cancelBtn.setOnClickListener(view -> dismiss());
    }

    @SuppressLint("SetTextI18n")
    public SelfUpdateSheet changelog(String versionName, String changelog) {
        changelogTitleTv.setText(getContext().getString(R.string.title_changelog) + ": " + versionName);
        changelogTv.setText(changelog);
        return this;
    }

    public SelfUpdateSheet onConfirmClick(Runnable onClickAction) {
        confirmBtn.setOnClickListener(v -> {
            onClickAction.run();
            dismiss();
        });
        return this;
    }
}

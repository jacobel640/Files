package com.example.files.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.files.R;

public class ProgressDialog extends Dialog {

    public ProgressDialog(@NonNull Context context) {
        super(context);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress);
        setCancelable(false);
        getWindow().setWindowAnimations(R.style.DialogAnimation);
        // Remove Dialog shadow
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setBackgroundDrawable(getContext().getDrawable(R.color.transparent));
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.BOTTOM);
    }
}

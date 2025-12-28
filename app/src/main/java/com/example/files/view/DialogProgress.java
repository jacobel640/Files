package com.example.files.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.files.R;
import com.example.files.databinding.DialogActionProgressBinding;

import java.util.Objects;

public class DialogProgress extends Dialog {

    public DialogActionProgressBinding binding;

    public DialogProgress(Context context) {
        super(context);
        binding = DialogActionProgressBinding.inflate(LayoutInflater.from(context));
        create();
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        Objects.requireNonNull(getWindow()).setWindowAnimations(R.style.DialogAnimation);
        getWindow().setBackgroundDrawable(getContext().getDrawable(R.color.transparent));

        binding.actionProgress.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.actionProgress.setMin(1);
        } //TODO set min for api 24 > 26

        binding.progressPresent.setText(0 + "%");
        binding.progressCancel.setClipToOutline(true);
        binding.proceedBackground.setClipToOutline(true);
        binding.proceedBackground.setOnClickListener(v -> hide());

        Window window = getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.BOTTOM);
    }

    public void start(int max) {

        binding.actionProgress.setIndeterminate(false);
        binding.actionProgress.setProgress(0, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.actionProgress.setMax(binding.actionProgress.getMin() + max);
        } else binding.actionProgress.setMax(max);

        String sum = 0 + " / " + max;

        new Handler(Looper.getMainLooper()).post(() -> {
            binding.progressText.setText(sum);
            show();
        });
    }

    public void actionProgressDismiss() {
        if (isShowing()) dismiss();
    }

    public void showActionProgress() {
        show();
    }

    public void showPerSecond(boolean show) {
        binding.progressPs.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setProgress(int progress, boolean animated) {
        binding.actionProgress.setProgress(progress, animated);
    }

    public int getProgress() {
        return binding.actionProgress.getProgress();
    }

    public int getMax() {
        return binding.actionProgress.getMax();
    }

    public void setFileName(String fileName) {
        binding.progressCurrent.setText(fileName);
    }

}

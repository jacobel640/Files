package com.example.files.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.databinding.DialogPromptBinding;
import com.google.android.material.internal.BaselineLayout;

import java.util.Objects;

public class Note extends Dialog {

    DialogPromptBinding binding;

    public Note(Context context, String title, String content, String hint) {
        super(context);
        initView();

        binding.title.setVisibility(title.isEmpty() ? View.GONE : View.VISIBLE);
        binding.baseline1.setVisibility(title.isEmpty() ? View.GONE : View.VISIBLE);
        binding.hint.setVisibility(hint.isEmpty() ? View.GONE : View.VISIBLE);
        binding.baseline2.setVisibility(hint.isEmpty() ? View.GONE : View.VISIBLE);

        binding.title.setText(title);
        binding.content.setText(content);
        binding.hint.setText(hint);
    }

    public Note(Context context, String content) {
        super(context);
        initView();

        binding.title.setVisibility(View.GONE);
        binding.baseline1.setVisibility(View.GONE);
        binding.hint.setVisibility(View.GONE);
        binding.baseline2.setVisibility(View.GONE);

        binding.content.setText(content);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void initView() {
        binding = DialogPromptBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        setCancelable(true);
        Objects.requireNonNull(getWindow()).setWindowAnimations(R.style.DialogAnimation);
        getWindow().setBackgroundDrawable(getContext().getDrawable(R.color.transparent));
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.BOTTOM);

        binding.close.setClipToOutline(true);
        binding.close.setOnClickListener(view -> dismiss());
    }

}
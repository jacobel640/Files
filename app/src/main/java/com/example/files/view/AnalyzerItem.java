package com.example.files.view;

import static com.example.files.Statics.BYTE;
import static com.example.files.Statics.KB;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.format.Formatter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.models.AnalyzerCategory;

@SuppressLint("ViewConstructor")
public class AnalyzerItem extends LinearLayout {
    TextView tvTitle, tvPresents;
    ProgressBar presents;
    AnalyzerCategory analyzer;
    int typeSize = 0;
    int itemId;

    @SuppressLint({"InflateParams", "UseCompatLoadingForDrawables", "StringFormatInvalid"})
    public AnalyzerItem(Context context, String title, AnalyzerCategory analyzer, int max, int type) {
        super(context);
        inflate(context, R.layout.item_analyzer, this);
        this.analyzer = analyzer;
        this.typeSize = type;
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(title);
        tvPresents = findViewById(R.id.tv_presents);
        tvTitle.setClipToOutline(true);
        tvPresents.setClipToOutline(true);
        presents = findViewById(R.id.presents);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) presents.setMin(0);
        presents.setMax(max);
        setPresents(integerFileSize(analyzer.getSize()));
        setOnClickListener(v -> {});
    }

    public String getTitle() {
        return tvTitle.getText().toString();
    }

    public String getContent() {
        return tvPresents.getText().toString();
    }

    public TextView getTvContent() {
        return tvPresents;
    }

    public void setContent(String content) {
        this.post(() -> tvPresents.setText(content));
    }

    public int getItemId() {
        return itemId;
    }

    public void showProgress() {
        presents.setVisibility(VISIBLE);
    }

    public void hidePresents() {
        presents.setVisibility(GONE);
    }

    public boolean isPresentsVisible() {
        return presents.getVisibility() == VISIBLE;
    }

    @SuppressLint("SetTextI18n")
    public void setPresents(int presents) {
        this.presents.setProgress(presents, true);
        this.tvPresents.setText(Formatter.formatFileSize(getContext(), analyzer.getSize()));
    }

    @SuppressLint("SetTextI18n")
    public void setPresents(int presents, long size) {
        this.presents.setProgress(this.presents.getProgress() + presents, true);
        this.tvPresents.setText(Formatter.formatFileSize(getContext(), analyzer.getSize() + size));
    }

    public int integerFileSize(long fileSize) {
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

}

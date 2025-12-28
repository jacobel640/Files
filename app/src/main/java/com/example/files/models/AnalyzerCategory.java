package com.example.files.models;

import android.app.Activity;
import android.content.Context;
import android.text.format.Formatter;

import com.example.files.activities.StorageAnalyzer;

import java.util.ArrayList;

public class AnalyzerCategory extends ArrayList<JFile> {

    StorageAnalyzer.Type TYPE;
    long size = 0;

    public AnalyzerCategory(StorageAnalyzer.Type type) {
        this.TYPE = type;
    }

    @Override
    public boolean add(JFile jFile) {
        this.size += jFile.getSize();
        return super.add(jFile);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size += size;
    }

    public String getSize(Context context) {
        return Formatter.formatFileSize(context, size);
    }
}

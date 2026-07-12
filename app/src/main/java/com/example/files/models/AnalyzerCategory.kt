package com.example.files.models

import android.content.Context
import android.text.format.Formatter
import com.example.files.activities.StorageAnalyzer

class AnalyzerCategory(val type: StorageAnalyzer.Type) : ArrayList<JFile>() {
    private var mSize: Long = 0

    override fun add(element: JFile): Boolean {
        this.mSize += element.size
        return super.add(element)
    }

    fun setSize(size: Long) {
        this.mSize += size
    }

    fun getSize(): Long {
        return mSize
    }

    fun getSize(context: Context?): String {
        return Formatter.formatFileSize(context, mSize)
    }
}

package com.example.files.listeners;

public interface OnSizeLoadReady {
    void onSizeReady(long size);
    default void onSizeUpdate(long size) {}
}

package com.example.files.models;

import androidx.annotation.NonNull;

import java.io.File;

public class FavoriteItem extends File {

    int id;

    public FavoriteItem(@NonNull String pathname, int id) {
        super(pathname);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

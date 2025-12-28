package com.example.files.utils.MainActivityUtils;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.dpToPixels;
import static com.example.files.Statics.favorites;
import static com.example.files.Statics.folder;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.files.database.DBHelper;
import com.example.files.R;
import com.example.files.models.FavoriteItem;
import com.example.files.models.StorageItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Storages {

    File interSd;
    List<File> extSd;
    ArrayList<View> pathList;
    public static ArrayList<StorageItem> storageItems;

    private final LinearLayout lvStorage;
    TextView favoriteSeparator;

    public Storages(LinearLayout lvStorage) {
        this.lvStorage = lvStorage;
        this.lvStorage.setClipToOutline(true);
    }

    public Storages setStorage() {

        storageItems = new ArrayList<>();
        pathList = new ArrayList<>();

        interSd = new File("/storage/emulated/0");
        if (!interSd.exists()) interSd = new File("/mnt/sdcard");
        StorageItem storageItem = new StorageItem(0, interSd, true, instance);
        lvStorage.addView(storageItem);
        storageItems.add(storageItem);
        folder = new File(""); // to prevent null exception in onBackPressed()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager();

        extSd = Arrays.asList(instance.getExternalFilesDirs(null));
        Collections.reverse(extSd);

        int i = 1;

        for (File storage : extSd) {
            if (Environment.isExternalStorageRemovable(storage)) {
                File file = new File(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull
                        (storage.getParentFile()).getParentFile()).getParentFile()).getParentFile()).getPath());

                StorageItem storageItem1 = new StorageItem(++i, file, false, instance);
                lvStorage.addView(storageItem1);
                storageItems.add(storageItem1);
            }
        }

        favorites = new DBHelper(instance);

        refreshStorage();
//        File shortcut = new File(interSd.getPath() + "/Music");
//        lvStorage.addView(new StorageItem(++i, shortcut, this));
        return this;
    }

    @SuppressLint("ResourceType")
    public void refreshStorage() {

        for (StorageItem storageItem : storageItems) {
            storageItem.updateView();
        }

        extSd = Arrays.asList(instance.getExternalFilesDirs(null));
        Collections.reverse(extSd);
        int i = 1;
        for (File storage : extSd) {
            if (Environment.isExternalStorageRemovable(storage)) {
                File file = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull
                        (storage.getParentFile()).getParentFile()).getParentFile()).getParentFile();
                assert file != null;
                StorageItem storageItem = new StorageItem(++i, file, false, instance);
                if (storageItem.getItemId() > storageItems.size()) {
                    lvStorage.addView(storageItem);
                    storageItems.add(storageItem);
                } else if (storageItem.getItemId() < storageItems.size()) {
                    lvStorage.removeView(lvStorage.getChildAt(lvStorage.getChildCount()-1));
                    storageItems.remove(storageItems.size()-1);
                }
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            StorageManager manager = instance.getSystemService(StorageManager.class);
            List<StorageVolume> storageVolumes = manager.getStorageVolumes();

            for (StorageVolume volume : storageVolumes) {
                if (volume.getDirectory().getPath().equals(storageItems.get(0).getFile().getPath())) continue;
                StorageItem storageItem = new StorageItem(++i, volume.getDirectory(), false, instance);
                if (storageItem.getItemId() > storageItems.size()) {
                    lvStorage.addView(storageItem);
                    storageItems.add(storageItem);
                } else if (storageItem.getItemId() < storageItems.size()) {
                    lvStorage.removeView(lvStorage.getChildAt(lvStorage.getChildCount() - 1));
                    storageItems.remove(storageItems.size() - 1);
                }
            }
        }

//        setOrRefreshFavorites();
    }

    public void setOrRefreshFavorites() {
        // refresh favorites
//        TextView tv = new TextView(this);
//        tv.setText("favorites");
//        lvStorage.addView(tv);

        if (favoriteSeparator == null) {
            favoriteSeparator = new TextView(instance);
            favoriteSeparator.setText(R.string.favorites);
            favoriteSeparator.setTextSize(15);
            favoriteSeparator.setPadding(dpToPixels(15), dpToPixels(15),
                    dpToPixels(15), dpToPixels(15));
            favoriteSeparator.setBackgroundColor(instance.getColor(R.color.background));
//            favoriteSeparator.setTextColor(instance.getColor(R.color.on_primary_container));
//            favoriteSeparator.setBackgroundColor(instance.getColor(R.color.primary_container));
            lvStorage.addView(favoriteSeparator);
        }
        favoriteSeparator.setVisibility(favorites.getAllPaths().size() == 0 ?
                View.GONE : View.VISIBLE);

        int id = 0;
        int favId = 0;
        ArrayList<Integer> removeIndexes = new ArrayList<>();
        for (StorageItem item : storageItems) {
            if (item.isShortcut()) {
                if (favId == 0) item.hideDivider();
                boolean exist = false;
                for (FavoriteItem fav : favorites.getAllPaths()) {
                    if (item.getFile().getPath().equals(fav.getPath())) exist = true;
                }
                if (!exist) {
                    removeIndexes.add(id);
                    lvStorage.removeView(item);
                }
                if (!item.getFile().exists()) {
                    removeIndexes.add(id);
                    lvStorage.removeView(item);
                    favorites.deletePath(DBHelper.idFromPath(item.getFile().getPath()));
                }
                favId++;
            }
            id++;
        }

        for (int index : removeIndexes) {
            try {
                storageItems.remove(index);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        favId = 0;
        int index = storageItems.get(storageItems.size()-1).getItemId();
        for (FavoriteItem fav : favorites.getAllPaths()) {
            boolean exist = false;
            for (StorageItem item : storageItems)
                if (item.isShortcut()) {
                    if (item.getFile().getPath().equals(fav.getPath())) exist = true;
                }
            if (!exist) {
                File shortcut = new File(fav.getPath());
                StorageItem storageItem = new StorageItem(index++, shortcut, instance);
                if (favId == 0) storageItem.hideDivider();
                storageItems.add(storageItem);
                lvStorage.addView(storageItems.get(storageItems.size()-1));
            }
            favId++;
        }
    }

}

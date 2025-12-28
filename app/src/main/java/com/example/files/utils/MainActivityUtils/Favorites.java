package com.example.files.utils.MainActivityUtils;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.favorites;
import static com.example.files.Statics.showFavorites;

import android.view.View;
import android.widget.LinearLayout;

import com.example.files.database.DBHelper;
import com.example.files.R;
import com.example.files.models.FavoriteItem;
import com.example.files.models.StorageItem;

import java.io.File;
import java.util.ArrayList;

public class Favorites {

    public ArrayList<StorageItem> favoriteItems;
    private final LinearLayout favoriteSection, lvFavorites;

    public Favorites(LinearLayout favoriteSection) {
        this.favoriteSection = favoriteSection;
        this.lvFavorites = favoriteSection.findViewById(R.id.lvFavorites);
        this.lvFavorites.setClipToOutline(true);
        this.favoriteItems = new ArrayList<>();
    }

    public Favorites setOrRefreshFavorites() {

        if (showFavorites && favorites.numberOfRows() !=0) favoriteSection.setVisibility(View.VISIBLE);
        else {
            favoriteSection.setVisibility(View.GONE);
            return this;
        }

        int id = 0;
        int favId = 0;
        ArrayList<Integer> removeIndexes = new ArrayList<>();
        for (StorageItem item : favoriteItems) {
            if (item.isShortcut()) {
                if (favId == 0) item.hideDivider();
                boolean exist = false;
                for (FavoriteItem fav : favorites.getAllPaths()) {
                    if (item.getFile().getPath().equals(fav.getPath())) exist = true;
                }
                if (!exist) {
                    removeIndexes.add(id);
                    lvFavorites.removeView(item);
                }
                if (!item.getFile().exists()) {
                    removeIndexes.add(id);
                    lvFavorites.removeView(item);
                    favorites.deletePath(DBHelper.idFromPath(item.getFile().getPath()));
                }
                favId++;
            }
            id++;
        }

        for (int index : removeIndexes) {
            try {
                favoriteItems.remove(index);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        favId = 0;
        int index = 0;
        for (FavoriteItem fav : favorites.getAllPaths()) {
            boolean exist = false;
            for (StorageItem item : favoriteItems)
                if (item.isShortcut()) {
                    if (item.getFile().getPath().equals(fav.getPath())) exist = true;
                }
            if (!exist) {
                File shortcut = new File(fav.getPath());
                StorageItem storageItem = new StorageItem(index++, shortcut, instance);
                if (favId == 0) storageItem.hideDivider();
                favoriteItems.add(storageItem);
                lvFavorites.addView(favoriteItems.get(favoriteItems.size()-1));
            }
            favId++;
        }

        return this;
    }

    public void refreshFavorites() {

    }
}

package com.example.files.fragments;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.currentFragment;
import static com.example.files.Statics.folder;
import static com.example.files.Statics.isRootFile;
import static com.example.files.Statics.openFolder;
import static com.example.files.utils.MainActivityUtils.Storages.storageItems;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.view.DropdownDialog;
import com.example.files.actions.DialogSort;
import com.example.files.adapters.PathAdapter;
import com.example.files.models.JFile;
import com.example.files.models.StorageItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ToolbarPathList {

    public static void setPathList(LinearLayout list, HorizontalScrollView scroller, TextView tvPath, File parent, boolean zipDir) {

        int start = 3;
        String[] arr = parent.getPath().split("/");
        if (isRootFile(folder)) start = 1;
        else if (arr.length > 2)
            for (int i = 1; i < storageItems.size(); i++) {
                if (storageItems.get(i).getFile().getName().equals(arr[2])) {
                    start = 2;
                    break;
                }
            }

        if (zipDir) start = 11;
//        if (zipDir) {
//            int zipPosition = 0;
//            start = 11;
//            for (String part : arr) {
//                if (part.endsWith(".zip")) break;
//                else zipPosition++;
//            }
//        }
//        new Note(requireActivity(), "", storageItems.get(1).getName(), arr[2]).show();
        for (int i = start; i < arr.length - 1; i++) {
            //if (i == 1) if (arr[i].equals("mnt") || arr[i].equals("storage")
            //      || arr[i].equals("emulated") || arr[i].equals("")) continue;
            LayoutInflater secInflater = (LayoutInflater) instance.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View view = secInflater.inflate(R.layout.item_path, null);
            TextView btnPath = view.findViewById(R.id.btnPath);
            btnPath.setClipToOutline(true);
            int finalI = i;
            PathAdapter adapter;
            ArrayList<JFile> folders = new ArrayList<>();
            Log.d("##### getPath #####", getParents(parent, arr.length, finalI + 1) + "/");
            File[] parentChildren = new File(getParents(parent, arr.length, finalI + 1) + "/").listFiles();
            if (parentChildren!=null)
                for (File file : parentChildren) if (file.isDirectory()) folders.add(new JFile(file, instance));
            new DialogSort().compare(folders, 0, false);
            DropdownDialog dialog = new DropdownDialog(instance, view);
            adapter = new PathAdapter(instance, folders);
            adapter.setPostClickListener(dialog::dismiss);
            DisplayMetrics dm = instance.getResources().getDisplayMetrics();
            int height = dm.heightPixels;
            if (folders.size() > 8) dialog.setHeight(height / 2);
            dialog.setAdapter(adapter);

            if (i == start) {
                if (arr[i].equals(storageItems.get(0).getFile().getName()) || arr[i].equals("0")) btnPath.setText(instance.getString(R.string.internal_storage));
                else {
                    btnPath.setText(arr[i]);
                    for (StorageItem storageItem : storageItems)
                        if (storageItem.getFile().getName().equals(arr[i]))
                            btnPath.setText(instance.getString(R.string.external_storage, String.valueOf(storageItem.getItemId() - 1)));
                }
            } else btnPath.setText(arr[i]);
            list.addView(view);

            scroller.post(() -> scroller.fullScroll(HorizontalScrollView.FOCUS_FORWARD));

//            if (!zipDir || i > zipPosition-1) {
            btnPath.setOnClickListener(view1 -> {
                openFolder(new File(getParents(parent, arr.length, finalI + 1) + "/"));
                currentFragment.select(parent.getPath());
            });


            btnPath.setOnLongClickListener(v -> {
                dialog.show();
                return true;
            });
//            }
        }
        if (arr.length == start+1) {
            if (arr[arr.length - 1].equals("0")) tvPath.setText(instance.getString(R.string.internal_storage));
            else {
                tvPath.setText(arr[arr.length - 1]);
                for (StorageItem storageItem : storageItems)
                    if (storageItem.getFile().getName().equals(arr[arr.length - 1])
                            && !storageItem.isShortcut()) tvPath.setText(instance.getString(R.string.external_storage, String.valueOf(storageItem.getItemId() - 1)));
            }
        } else tvPath.setText(arr[arr.length - 1]);
    }

    public static String getParents(File parent, int sum, int i) {
        if (currentFragment.isArchive) return getZipParents(parent, sum, i);

        while(parent != null){
            sum--; // going backwards, so if you want the parent, you increase the "i", not minimize it.
            parent = parent.getParentFile();
            if (sum == i) {
                if (parent != null) {
                    return parent.getPath();
                } else return null;
            }
        } return null;
    }

    public static String getZipParents(File f, int sum, int i){

        Log.d("##### getParent #####", f.getPath() + ", sum=" + sum + ", i=" + i);
        while(f != null){
            sum--; // going backwards, so if you want the parent, you increase the "i", not minimize it.
            f = f.getParentFile();
            if (sum == i)
                if (sum <= currentFragment.zipPosition) {
                    return getPath(f != null ? f.getPath() : "");
                } else if (f != null) {
                    return f.getPath();
                } else return null;
        } return null;
    }

    public static String getPath(String path) {
        ArrayList<String> parts = new ArrayList<>(Arrays.asList(path.split("/")));
        // "/storage/emulated/0/Android/data/com.example.files/files/zips"
        //      0         1   2     3     4          5            6    7      8      9     10    11     12    13
        // "/storage/emulated/0/Android/data/com.example.files/files/zips/storage/emulated/0/file.zip/folder/inside"

        for (int i = 0; i < 8; i++) {
            parts.remove(4);
        }

        StringBuilder fixPath = new StringBuilder("/");
        for (String part : parts) {
            fixPath.append(part).append("/");
        }

//        Log.d("##### getPath #####", fixPath.toString());

        return fixPath.toString();
    }

}

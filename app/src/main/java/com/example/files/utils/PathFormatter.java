package com.example.files.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.files.R;
import com.example.files.models.StorageItem;

import java.util.ArrayList;

import static com.example.files.utils.MainActivityUtils.Storages.storageItems;
import static com.example.files.Statics.DOC_SLASH;
import static com.example.files.Statics.DOC_SPACE;

public class PathFormatter {

    Context context;

    public PathFormatter(Context context) {
        this.context = context;
    }

    @SuppressLint({"StringFormatMatches", "ResourceType"})
    public String format(String path) {
        String[] preArr = path.split("/");
        ArrayList<String> pathArray = new ArrayList<>();
        for (String s : preArr) {
            if (!s.equals("")) pathArray.add(s);
        }
        StringBuilder pathBuilder = new StringBuilder();
        if (pathArray.get(0).equals("storage")) {
            if (pathArray.get(1).equals("emulated")) {
                for (int i = 2; i < pathArray.size(); i++) {
                    if (i == 2) pathBuilder.append(pathArray.get(i).replace("0",
                            context.getString(R.string.internal_storage)));
                    else pathBuilder.append(pathArray.get(i));
                    if (i != pathArray.size()-1) pathBuilder.append("/");
                }
            } else {
                int storageId = 0;
                for (StorageItem si : storageItems) {
                    if (si.getFile().getPath().split("/")[2].equals(pathArray.get(1)))
                        storageId = si.getItemId() - 1;
                }
                for (int i = 1; i < pathArray.size(); i++) {
                    pathBuilder.append(pathArray.get(i).replace(pathArray.get(1),
                            context.getString(R.string.external_storage, storageId)));
                    if (i != pathArray.size()-1) pathBuilder.append("/");
                }
            }
        }
        if (pathBuilder.toString().length() > 0) return pathBuilder.toString();
        else return path;
    }

    public String externalPath(String name, String path) {

        StringBuilder pathEnd = new StringBuilder();
        pathEnd.append("content://com.android.externalstorage.documents/tree/")
                .append(name).append("%3A/document/").append(name).append("%3A");

        String[] path1 = path.split("/");
        if (path1.length >= 3) {
            pathEnd.append(path1[3]);
        }
        if (path1.length >= 4) {
            for (int i = 4; i < path1.length; i++) {
                pathEnd.append(DOC_SLASH).append(path1[i]);
            }
        }

        return pathEnd.toString().replace(" ", "%20");
    }

    public String externalFilePathWoName(String path) {

        String[] preName = path.substring(path.lastIndexOf("storage/")+8).split("/");
        String name = preName[0];

        String pathEnd = "";
        pathEnd += ("content://com.android.externalstorage.documents/tree/");
        pathEnd += name + "%3A/document/" + name + "%3A";

//        D/PATH: content://com.example.files/root/storage/5A85-D438/app-release-mini.apk
//        D/NAME: 5A85-D438
//        D/PATH END: content://com.android.externalstorage.documents/tree/5A85-D438%3A/document/5A85-D438%3Aapp-release-mini.apk

//        D/PATH: content://com.example.files/files/Spotify%20v8.6.48.792%20Mod-armeabi-v7a-noPictures-v2.apk
//        D/NAME: 5A85-D438
//        D/PATH END: content://com.android.externalstorage.documents/tree/5A85-D438%3A/document/5A85-D438%3ASpotify%20v8.6.48.792%20Mod-armeabi-v7a-noPictures-v2.apk


        // the first item in the path - after the storage name shouldn't get "DSlash"
        if (name.length() < 3) {
            name = "5A85-D438";
            pathEnd = "";
            pathEnd += ("content://com.android.externalstorage.documents/tree/");
            pathEnd += name + "%3A/document/" + name + "%3A";
            Log.d("PATH", path);
            Log.d("NAME", name);

            String path1 = path.substring(path.indexOf("files/") + 3 + name.length()).replaceAll("/", DOC_SLASH);
            pathEnd += path1.replaceAll(" ", DOC_SPACE);

            Log.d("PATH END", pathEnd);

            return pathEnd;
        }

        Log.d("PATH", path);
        Log.d("NAME", name);
        if (!path.endsWith(name.substring(name.length()-3))) {
            String path1 = path.substring(path.indexOf("storage/") + 9 + name.length()).replaceAll("/", DOC_SLASH);
            pathEnd += path1.replaceAll(" ", DOC_SPACE);
        } else {
            String path1 = path.substring(path.indexOf("storage/") + 8 + name.length()).replaceAll("/", DOC_SLASH);
            pathEnd += path1.replaceAll(" ", DOC_SPACE);
        }

        Log.d("PATH END", pathEnd);

        return pathEnd;
    }

    public String externalFolderPathWoName(String path) {

        String[] preName = path.substring(path.lastIndexOf("storage/")+8).split("/");
        String name = preName[0];

        String pathEnd = "";
        pathEnd += ("content://com.android.externalstorage.documents/tree/");
        pathEnd += name + "%3A";

        // the first item i the path - after the storage name shouldn't get "DSlash"
        Log.d("##### PathFormatter.externalFolderPathWoName #####", name);
        if (!path.endsWith(name.substring(name.length()-3))) {
            String path1 = path.substring(path.indexOf("storage/") + 9 + name.length()).replaceAll("/", DOC_SLASH);
            pathEnd += path1.replaceAll(" ", DOC_SPACE);
        } else {
            String path1 = path.substring(path.indexOf("storage/") + 8 + name.length()).replaceAll("/", DOC_SLASH);
            pathEnd += path1.replaceAll(" ", DOC_SPACE);
        }

        return pathEnd;
    }

}

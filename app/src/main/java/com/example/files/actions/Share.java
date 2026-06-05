package com.example.files.actions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.core.content.FileProvider;

import com.example.files.models.JFile;
import com.example.files.R;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.IntStream;

import static android.content.Intent.EXTRA_STREAM;
import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.multiSelected;
import static com.example.files.Statics.selectedJFiles;

public class Share {

    Activity activity;

    public Share(Activity activity) {

        this.activity = activity;

        share();
    }

    public void share(){
        if(multiSelected && !selectedJFiles.isEmpty()){
            Intent share = new Intent();
            if(selectedJFiles.size()==1){
                File file = selectedJFiles.get(0);
                Uri uri = FileProvider.getUriForFile(activity,
                        activity.getPackageName(), file);
                MimeTypeMap mimeType = MimeTypeMap.getSingleton();
                String type = mimeType.getMimeTypeFromExtension(file.getName().
                        substring(file.getName().lastIndexOf(".") + 1).toLowerCase());
                //Toast.makeText(getContext(), type, Toast.LENGTH_SHORT).show();
                if (type == null) type = "*/*";
                share.setAction(Intent.ACTION_SEND); //Change if needed
                share.setType(type);
                share.putExtra(EXTRA_STREAM, uri);
            } else {
                share.setType("*/*");
                share.setAction(Intent.ACTION_SEND_MULTIPLE);
                ArrayList<Uri> uris = new ArrayList<>();

                for (JFile jFile : selectedJFiles) {
                    uris.add(FileProvider.getUriForFile(activity,
                            activity.getPackageName(), jFile));
                }
                share.putParcelableArrayListExtra(EXTRA_STREAM, uris);
            }
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(Intent.createChooser(share, countItems(activity)));
        } else /*TODO check if list != 0*/ instance.eventListener.onMultiSelectedChange(true);
    }

    public static String countItems(Context context){
        if(selectedJFiles.size() > 1)
            if(IntStream.range(0, selectedJFiles.size()).allMatch(i -> selectedJFiles.get(i).isDirectory()))
                return selectedJFiles.size() + " " + context.getString(R.string.folders);
            else if(IntStream.range(0, selectedJFiles.size()).noneMatch(i -> selectedJFiles.get(i).isDirectory()))
                return selectedJFiles.size() + " " + context.getString(R.string.files);
            else return context.getString(R.string.items, String.valueOf(selectedJFiles.size()));
        else if (selectedJFiles.size() == 1) {
            if (selectedJFiles.get(0).isDirectory())
                return context.getString(R.string.one_folder);
            else return context.getString(R.string.one_file);
        } else return "";
    }

}

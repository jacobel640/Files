package com.example.files.utils.MainActivityUtils;

import static com.example.files.MainActivity.actionBarVisibility;
import static com.example.files.Statics.OpenCategory;
import static com.example.files.Statics.OpenSearch;
import static com.example.files.Statics.multiSelected;
import static com.example.files.Statics.openFolder;
import static com.example.files.Statics.openRecent;
import static com.example.files.Statics.openZipFile;
import static com.example.files.Statics.prepareAction;
import static com.example.files.Statics.selectedJFiles;
import static com.example.files.utils.MainActivityUtils.Storages.storageItems;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;

import com.example.files.MainActivity;
import com.example.files.view.Note;
import com.example.files.actions.DialogCopy;
import com.example.files.actions.DialogDetails;
import com.example.files.models.JFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AppShortcuts {

    MainActivity mainActivity;

    public void shortcut(Bundle savedInstanceState, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        String shortcut = null;
        String send = null;

        Intent receivedIntent = mainActivity.getIntent();
        String receivedAction = receivedIntent.getAction();
//        String receivedType = receivedIntent.getType();

        if (savedInstanceState == null) {
            Bundle extras = receivedIntent.getExtras();
            if (extras != null) {
                shortcut = extras.getString("shortcut");
                send = extras.getString("send");
            }

        } else {
            shortcut = (String) savedInstanceState.getSerializable("shortcut");
            send = (String) savedInstanceState.getSerializable("send");
        }

        if (shortcut != null) {
            if (shortcut.equals("search")) OpenSearch("search");
            else if (shortcut.equals("recent")) openRecent();
            return;
        }

        if (send != null) {
            sendAction(send);
            return;
        }

        if (receivedAction != null) {
            switch (receivedAction) {
                case Intent.ACTION_OPEN_DOCUMENT:
                    new Note(mainActivity, receivedAction).show();
                    return;
                case "android.intent.action.VIEW_DOWNLOADS":
                    OpenCategory("downloads");
                    return;
                case "android.intent.action.VIEW": // receivedAction = "android.intent.action.VIEW"
                    if (receivedIntent.getData() == null) break;
                    try {
                        File file = getFile(mainActivity, receivedIntent.getData());
//                        new Note(mainActivity, "path", file.getPath(), receivedAction).show();
                        openZipFile(new JFile(file.getPath(), mainActivity));
                        Log.d("##### AppShortcus.shortcut() #####", "success: " + file.getPath());
                    } catch (IOException e) {
                        Log.d("##### AppShortcus.shortcut() #####", "fail: " +  receivedIntent.getData());
                        e.printStackTrace();
                    }
            }
        }
    }

    public void sendAction(String send) {
        Uri fileUri = Uri.parse(send);
        if (fileUri != null) // TODO this is not working
            try {
                String path = "sdcard/";
                try {
                    path = getFile(mainActivity, fileUri).getPath();
                } catch (IOException e) {
                    e.printStackTrace();
                    if (send.contains("content:")) {
                        path = getRealPathFromURI(Uri.parse(send));
                    } else if (send.contains("file:")) {
                        path = Uri.parse(send).getPath();
                    }
                }
                File file = new File(path);
                openFolder(new File(storageItems.get(0).getFile().getPath())); // TODO find parent folder
                new Handler().postDelayed(() -> {
                    selectedJFiles.add(new JFile(file, mainActivity));
                    actionBarVisibility(View.VISIBLE);
                    prepareAction(new DialogCopy(selectedJFiles));
                    new DialogDetails(mainActivity, false);
                }, 1000);
            } catch (Exception e) {
                if (multiSelected) mainActivity.eventListener.onMultiSelectedChange(false);
                new Note(mainActivity, "הקובץ לא נמצא", send, "").show();
            }
    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = mainActivity.getContentResolver().query(contentUri, proj, null, null,
                    null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
//            new Note(this,"","hi","").show();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static File getFile(Context context, Uri uri) throws IOException {
        File destinationFilename = new File(context.getFilesDir().getPath() + File.separatorChar + queryName(context, uri));
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.printStackTrace();
        }
        return destinationFilename;
    }

    public static void createFileFromStream(InputStream ins, File destination) {
        try (OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static String queryName(Context context, Uri uri) {
        Cursor returnCursor =
                context.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    public JFile fromUri(Uri uri) {
        String dataString = uri.toString();
        String pattern = "storage_root/storage/emulated";
        String path;
        if (!dataString.contains(pattern)) {
            pattern = "com.example.files/files";
            if (dataString.contains(pattern))
                path = dataString.substring(dataString.indexOf(pattern))
                        .replace(pattern, "sdcard");
            else return null;
        } else path = dataString.substring(dataString.indexOf(pattern))
                .replace(pattern, "").replace("0", "sdcard");
        return new JFile(path, mainActivity);
    }

}

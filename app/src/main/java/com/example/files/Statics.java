package com.example.files;

import static android.content.ContentValues.TAG;
import static android.content.Intent.ACTION_VIEW_PERMISSION_USAGE;
import static android.content.Intent.EXTRA_ALLOW_MULTIPLE;
import static com.example.files.MainActivity.instance;
import static com.example.files.models.JFile.Type.SHORTCUT;
import static com.example.files.utils.MainActivityUtils.Storages.storageItems;
import static com.example.files.models.JFile.Type.ARCHIVE;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.files.actions.DialogDelete;
import com.example.files.database.DBHelper;
import com.example.files.view.Note;
import com.example.files.actions.DialogBase;
import com.example.files.presentation.files_explorer.FilesFragment;
import com.example.files.presentation.search.SearchScreen;
import com.example.files.models.JFile;
import com.example.files.models.StorageItem;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;
import me.zhanghai.android.fastscroll.PopupStyles;
import me.zhanghai.android.fastscroll.PopupTextProvider;

public class Statics {

    @SuppressLint("StaticFieldLeak")
    public static View actionBar;
    @SuppressLint("StaticFieldLeak")
    public static int shortAnimationDuration;
    @SuppressLint("StaticFieldLeak")
    public static LinearLayout move, copy, details, share, delete;
    public static CoordinatorLayout mainLayout;
    public static ArrayList<JFile> selectedJFiles;
    public static boolean multiSelected = false, copyMode = false, showHiddenFiles, showFileSize,
            isSingleLine, showRecent, showCategories, showFavorites;
    public static int sort, order;
    public static File folder, tempFolder;
    public static String highlightFile = null;
    public static final String DOC_SLASH = "%2F", DOC_SPACE = "%20";
    public static final String OPS_CHANNEL_ID = "operations_channel";
    public static final String TAG_FOLDER = "folder", TAG_CATEGORY = "category", TAG_SEARCH = "search",
    TAG_RECENT = "recent", TAG_ZIPPED = "zipped";
//    public static int OPERATING = 0; // got the feeling it's useless
    public static JFileAdapter.ViewType FOLDER_VIEW_TYPE = JFileAdapter.ViewType.ROW,
            CATEGORY_VIEW_TYPE = JFileAdapter.ViewType.ROW;
    public static final int BYTE = 10, KB = 10240, MB = 20480;
    public static final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 4010;
    @SuppressLint("StaticFieldLeak")
    public static FilesFragment currentFragment;
    @SuppressLint("StaticFieldLeak")
    public static SearchScreen searchFragment;
    public enum FragmentType { FILES, CATEGORY, MAIN, RECENT, FAVORITES, ARCHIVE, SEARCH }
    // operations
    @SuppressLint("StaticFieldLeak")
    public static ArrayList<DialogBase> actions;
    public static void prepareAction(DialogBase action) {
        actions.add(action);
        action.preAction();
    }

    public static String getPackageName() {
        return BuildConfig.APPLICATION_ID;
    }
    public static void startCurrentAction() {
        actions.get(actions.size() - 1).startAction(folder);
    }
    public static void removeCurrentAction() {
        actions.remove(actions.size()-1);
    }
    public static DBHelper favorites;

    public static void openFile(JFile jFile, Context context) {
        if (jFile.getType() == ARCHIVE) {
            openZipFile(jFile);
            return;
        }
        else if (jFile.getType() == SHORTCUT) {
            openShortcut(jFile);
            return;
        }
        Uri uri;
        if (jFile.isDocumentFile()) uri = jFile.getDocumentFile().getUri();
        else uri = FileProvider.getUriForFile(context, getPackageName(), jFile);
        MimeTypeMap mimeType = MimeTypeMap.getSingleton();
        String type = mimeType.getMimeTypeFromExtension(jFile.getName().substring(jFile.getName().lastIndexOf(".") + 1).toLowerCase());
        //Toast.makeText(context, type, Toast.LENGTH_SHORT).show();
        if (type == null) type = "*/*";
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setDataAndType(uri, type);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            view.putExtra(ACTION_VIEW_PERMISSION_USAGE, true);
        view.putExtra(EXTRA_ALLOW_MULTIPLE, true);
        view.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(view);
        } catch (ActivityNotFoundException e){
            new Note(instance, e.getMessage()).show();
        }
        //context.startActivity(Intent.createChooser(share, file.getName()));
    }

    private static void openShortcut(JFile jFile) { // TODO Open Shortcut file
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                File file = new File(Files.readAllLines(Paths.get(jFile.getPath())).toString());
                if (file.exists()) openFolder(file);
                else Snackbar.make(mainLayout, "Source not found, Delete Shortcut?", Snackbar.LENGTH_SHORT)
                        .setAction(R.string.delete, view -> {
                            ArrayList<JFile> temp = new ArrayList<>();
                            temp.add(jFile);
                            prepareAction(new DialogDelete(temp));
                        }).show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static void openFileWith(JFile jFile, Context context) {
        instance.eventListener.onMultiSelectedChange(false);
        Uri uri;
        if (jFile.isDocumentFile()) uri = jFile.getDocumentFile().getUri();
        else uri = FileProvider.getUriForFile(context, getPackageName(), jFile);
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setDataAndType(uri, "*/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            view.putExtra(ACTION_VIEW_PERMISSION_USAGE, true);
        view.putExtra(EXTRA_ALLOW_MULTIPLE, true);
        view.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(view);
        } catch (ActivityNotFoundException e) {
            new Note(instance, e.getMessage()).show();
        }
    }

    public static void openZipFile(JFile jFile) {
        if (jFile.getType() != ARCHIVE) return;
        if (multiSelected) instance.eventListener.onMultiSelectedChange(false);
        instance.loadFragment(instance.newZippedFragment(jFile), TAG_ZIPPED);

    }

    public static void openFolder(File file, String highlight) {
        highlightFile = highlight;
        openFolder(file);
    }

    public static void openFolder(File file) {
        tempFolder = file;
        if (multiSelected) instance.eventListener.onMultiSelectedChange(false);
        if (!instance.permissionGranted()) {
            instance.requestStoragePermissions();
            return;
        }
        boolean isVisible = isVisible(TAG_FOLDER);
        if (!isVisible || !folder.getPath().equals(file.getPath())) { // TODO make sure this condition don't ruing anything
            if(!canRead(file) && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                // TODO find another way to grant SAF permissions (need a way to specify a folder)
                takeCardUriPermission(file);
            } else if (!isAndroidR(file)) instance.loadFragment(instance.newFragment(file), TAG_FOLDER);
        }
//        Log.d("##### OPEN FOLDER #####", "multiSelected=" + multiSelected +
//                "\nff.isVisible=" + ff.isVisible() + "\nfolder.getPath().equals(file.getPath())="
//                + folder.getPath().equals(file.getPath()) + "\n!canRead(file)=" + !canRead(file)
//                + "\nBuild.VERSION.SDK_INT < Build.VERSION_CODES.R=" +
//                (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) + "\n!isAndroidR(file)=" + !isAndroidQ(file));
    }

    public static void OpenSearch(String category) {
        instance.loadFragment(instance.newSFragment(category, null), TAG_SEARCH);
    }

    public static void OpenSearch(String category, ArrayList<JFile> jFiles) {
        instance.loadFragment(instance.newSFragment(category, jFiles), TAG_SEARCH);
    }

    public static void OpenCategory(String category) {
        Statics.sort = 2;
        Statics.order = 1;
        MainActivity.editor.putInt("SORT", 2).apply();
        MainActivity.editor.putInt("ORDER", 1).apply();
        instance.loadFragment(instance.newDFragment(category), TAG_CATEGORY);
    }

    public static void openRecent() {
        Statics.sort = 2;
        Statics.order = 1;
        MainActivity.editor.putInt("SORT", 2).apply();
        MainActivity.editor.putInt("ORDER", 1).apply();
        instance.loadFragment(instance.newRFragment(), TAG_RECENT);
    }

    public static void takeCardUriPermission(File sdCardRoot) {
        StorageManager sm = (StorageManager) instance.getSystemService(Context.STORAGE_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            Intent intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
            //String startDir = "Android";
            //String startDir = "Download"; // Not choosable on an Android 11 device
            //String startDir = "DCIM";
            //String startDir = "DCIM/Camera";  // replace "/", "%2F"
            //String startDir = "DCIM%2FCamera";
            // String startDir = "Documents";
            String startDir = sdCardRoot.getPath()
                    .replace("/storage/emulated/0/", "");

            Uri uri = intent.getParcelableExtra("android.provider.extra.INITIAL_URI");

            assert uri != null;
            String scheme = uri.toString();

            Log.d(TAG, "INITIAL_URI scheme: " + scheme);

            scheme = scheme.replace("/root/", "/document/");

            startDir = startDir.replace("/", "%2F");

            scheme += "%3A" + startDir;

            uri = Uri.parse(scheme);

            intent.putExtra("android.provider.extra.INITIAL_URI", uri);

            Log.d("##### uriPermissions #####", "uri: " + uri.toString());

            instance.startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);

            return;
        }

        File sdCard = new File(sdCardRoot.getPath());
        StorageManager storageManager = (StorageManager) instance.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume storageVolume = storageManager.getStorageVolume(sdCard);
        Intent intent/*;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            intent = storageManager.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
        } else intent*/ = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//        storageVolume.createAccessIntent(null)

        try {
            instance.startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "takeCardUriPermission: " + e.getMessage());
        }
    }

    public static Uri getUriPermission() {
        List<UriPermission> persistedUriPermissions = instance.getContentResolver().getPersistedUriPermissions();
        if (!persistedUriPermissions.isEmpty()) {
            UriPermission uriPermission = persistedUriPermissions.get(0);
            return uriPermission.getUri();
        }
        return null;
    }

    @SuppressLint("SdCardPath")
    public static boolean isDocumentFile(File file) {
        if (!file.getPath().startsWith("/storage/emulated"))
            return !file.getPath().startsWith("/sdcard");
        return false;
    }

    public static boolean canRead(File file) {
//        if (isDocumentFile(file) && !isRootFile(file))
        if (isExtSDCardRootDir(file))
        return new JFile(file, instance).getDocumentTreeSec().canRead();
        else return file.canRead();
    }

    @SuppressLint("SdCardPath")
    public static boolean isRootFile(File file) {
        while (file.getParentFile() != null) {
            if (file.getPath().startsWith("/storage/emulated") ||
                    file.getPath().startsWith("/sdcard") ||
                    isExternalSDCardDir(file)) return false;
            file = file.getParentFile();
        }
        return file.getPath().equals("/");
    }

    @SuppressLint("SdCardPath")
    private static boolean isExternalSDCardDir(File file) {
        if (file.getPath().startsWith("/storage/emulated") ||
                file.getPath().startsWith("/sdcard")) return false;
        for (StorageItem si : storageItems) {
            if (file.getPath().contains(si.getFile().getPath())) return true;
        }
        return false;
    }

    @SuppressLint("SdCardPath")
    public static boolean isExtSDCardRootDir(File file) {
        if (file.getPath().startsWith("/storage/emulated") ||
                file.getPath().startsWith("/sdcard")) return false;
        for (StorageItem si : storageItems) {
            if (file.getPath().endsWith(si.getFile().getName())) return true;
        }
        return false;
    }

    @SuppressLint("SdCardPath")
    public static boolean isAndroidR(File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (file.getPath().equals("/storage/emulated/0/Android/data") ||
                    file.getPath().equals("/sdcard/Android/data")) {
                if (!new JFile(file, instance).getDocumentTreeSec().canRead()) {
                    // TODO same as above
                    tempFolder = file;
                    takeCardUriPermission(file);
                } else return false;
                return true;
            }
        }
        return false;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void setFastScrollBar(RecyclerView recyclerView, PopupTextProvider provider) {
        Context context = recyclerView.getContext();
        FastScrollerBuilder fastScroller = new FastScrollerBuilder(recyclerView);
        fastScroller.setPopupStyle(textView -> {
                    PopupStyles.MD2.accept(textView);
                textView.setBackgroundTintMode(PorterDuff.Mode.LIGHTEN);
                textView.setTextColor(context.getColor(R.color.app_theme));
            })
            .setPopupTextProvider(provider)
            .setThumbDrawable(Objects.requireNonNull(context.getDrawable(R.drawable.scroll_bar_thumb)))
            .setTrackDrawable(Objects.requireNonNull(context.getDrawable(R.drawable.scroll_bar_track)))
            .build();
    }

    public static int dpToPixels(float dip) {
        Resources r = instance.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
    }

    @SuppressLint("DiscouragedApi")
    public static boolean hasNavigationBar() {
        Resources resources = instance.getResources();
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && resources.getBoolean(id);
    }

    public static boolean isVisible(String tag) {
        int stackCount = instance.getSupportFragmentManager().getBackStackEntryCount();
        if (stackCount <= 0) return false;
        return Objects.equals(instance.getSupportFragmentManager().getFragments().get(stackCount-1).getTag(), tag);
    }
}

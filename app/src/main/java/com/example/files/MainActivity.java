package com.example.files;

import static com.example.files.Statics.OPS_CHANNEL_ID;
import static com.example.files.Statics.OpenSearch;
import static com.example.files.Statics.REQUEST_CODE_OPEN_DOCUMENT_TREE;
import static com.example.files.Statics.TAG_FOLDER;
import static com.example.files.Statics.actionBar;
import static com.example.files.Statics.actions;
import static com.example.files.Statics.copyMode;
import static com.example.files.Statics.currentFragment;
import static com.example.files.Statics.dpToPixels;
import static com.example.files.Statics.folder;
import static com.example.files.Statics.hasNavigationBar;
import static com.example.files.Statics.isSingleLine;
import static com.example.files.Statics.isVisible;
import static com.example.files.Statics.mainLayout;
import static com.example.files.Statics.multiSelected;
import static com.example.files.Statics.openFolder;
import static com.example.files.Statics.order;
import static com.example.files.Statics.prepareAction;
import static com.example.files.Statics.removeCurrentAction;
import static com.example.files.Statics.searchFragment;
import static com.example.files.Statics.selectedJFiles;
import static com.example.files.Statics.shortAnimationDuration;
import static com.example.files.Statics.showFileSize;
import static com.example.files.Statics.showHiddenFiles;
import static com.example.files.Statics.showRecent;
import static com.example.files.Statics.showCategories;
import static com.example.files.Statics.showFavorites;
import static com.example.files.Statics.sort;
import static com.example.files.Statics.startCurrentAction;
import static com.example.files.Statics.tempFolder;
import static com.example.files.utils.Animations.hide;
import static com.example.files.utils.Animations.show;
import static com.example.files.utils.Animations.show;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.WindowCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.example.files.activities.BaseActivity;
import com.example.files.database.StoragesUri;
import com.example.files.listeners.OnActivityStateChange;
import com.example.files.utils.MainActivityUtils.AppShortcuts;
import com.example.files.utils.MainActivityUtils.Categories;
import com.example.files.utils.MainActivityUtils.Favorites;
import com.example.files.utils.MainActivityUtils.RecentFiles;
import com.example.files.utils.MainActivityUtils.Storages;
import com.example.files.view.Note;
import com.example.files.actions.DialogCopy;
import com.example.files.actions.DialogDelete;
import com.example.files.actions.DialogDetails;
import com.example.files.actions.DialogMove;
import com.example.files.actions.Share;
import com.example.files.activities.SettingsActivity;
import com.example.files.activities.StorageAnalyzer;
import com.example.files.presentation.files_explorer.FilesFragment;
import com.example.files.presentation.search.SearchScreen;
import com.example.files.listeners.OnMultiSelectedChange;
import com.example.files.models.JFile;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;


import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends BaseActivity {

    boolean activityCreated;
    public static SharedPreferences sp;
    public static SharedPreferences.Editor editor;
    SharedPreferences.OnSharedPreferenceChangeListener spListener;
    @SuppressLint("StaticFieldLeak")
    public static MainActivity instance;
    ImageButton search;
    public OnMultiSelectedChange eventListener = new OnMultiSelectedChange() {
        @Override
        public void onMultiSelectedChange(boolean multiSelected) { }
        @Override
        public void onRefresh() { }
        @Override
        public void onRefreshActionsList() { } };

    Storages storages;
    Favorites favorites;
    RecentFiles recent;
    Categories categories;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint({"UseCompatLoadingForDrawables", "CommitPrefEdits", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EdgeToEdge.enable(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        instance = this;
        cleanCache(this);
        hideKeyboard(this);
        
        androidx.compose.ui.platform.ComposeView activeTasksComposeView = findViewById(R.id.active_tasks_compose_view);
        com.example.files.components.ActiveTasksOverlayKt.bindActiveTasksOverlay(activeTasksComposeView);

        if (currentFragment == null) statics(); // --------------------------------- //

        if (!permissionGranted()) requestStoragePermissions();

        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            // request for the permission
            new MaterialAlertDialogBuilder(this)
                    .setTitle("External Storage Manager")
                    .setMessage("External Storage Manager Permission is not granted!")
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        startActivity(intent);
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        }

        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
        } catch (Throwable ignored) {}
        Glide.get(this.getApplicationContext()).setMemoryCategory(MemoryCategory.HIGH);

        // set notifications channel
        // https://developer.android.com/training/notify-user/channels#CreateChannel
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence name = getString(R.string.operations_progress); // The user-visible name of the channel in App Info > Notifications.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(OPS_CHANNEL_ID, name, importance);
        notificationManager.createNotificationChannel(mChannel);

        mainLayout = findViewById(R.id.mainLayout);
        findViewById(R.id.nScrollView).setClipToOutline(true);
        bottomActionBar();

        selectedJFiles = new ArrayList<>();

        InitAndRegisterPreferences();

        search = findViewById(R.id.search);
        search.setClipToOutline(true);
        search.setOnClickListener(view -> OpenSearch("search"));

        Toolbar toolbar;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getColor(R.color.transparent));
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setOnClickListener(v1 -> {
            if (!actions.isEmpty())
                actions.get(actions.size() - 1).reshow();
        });

        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
            }
            if (item.getItemId() == R.id.action_search) {
                OpenSearch("search");
            }
            if (item.getItemId() == R.id.action_analyzer) {
                Intent analyzer = new Intent(this, StorageAnalyzer.class);
                startActivity(analyzer);
            }
            return false;
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                homeScreenRefresh();
                folder = new File("");
            }
        });

        storages = new Storages(findViewById(R.id.lvStorage)).setStorage();

        favorites = new Favorites(findViewById(R.id.favorites_section)).setOrRefreshFavorites();

        recent = new RecentFiles(findViewById(R.id.recent_section)).recent();

        categories = new Categories(findViewById(R.id.categories_section)).setOrRefreshCategories();

        new AppShortcuts().shortcut(savedInstanceState, this);

        onBackPressedCallback();
    }

    private void InitAndRegisterPreferences() {

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        showHiddenFiles = sp.getBoolean("SHOW_HIDDEN_FILES", false);
        showFileSize = sp.getBoolean("SHOW_FILE_SIZE", false);
        isSingleLine = !sp.getBoolean("SHOW_FULL_FILE_NAME", false);
        showRecent = sp.getBoolean("SHOW_RECENT", true);
        showCategories = sp.getBoolean("SHOW_CATEGORIES", true);
        showFavorites = sp.getBoolean("SHOW_FAVORITES", true);
        sort = sp.getInt("SORT", 0);
        order = sp.getInt("ORDER", 0);
        sp.getString("category", "name");

        spListener = (prefs, key) -> {
            showHiddenFiles = sp.getBoolean("SHOW_HIDDEN_FILES", false);
            showFileSize = sp.getBoolean("SHOW_FILE_SIZE", false);
            isSingleLine = !sp.getBoolean("SHOW_FULL_FILE_NAME", false);
            showRecent = sp.getBoolean("SHOW_RECENT", false);
            showCategories = sp.getBoolean("SHOW_CATEGORIES", false);
            showFavorites = sp.getBoolean("SHOW_FAVORITES", false);
            sort = sp.getInt("SORT", 0);
            order = sp.getInt("ORDER", 0);
        };
        sp.registerOnSharedPreferenceChangeListener(spListener);
        editor = sp.edit();

    }

    public void addMultiSelectedChangeListener(OnMultiSelectedChange evtListener) {
        this.eventListener = evtListener;
    }

    // TODO take care...
    @SuppressLint("WrongConstant")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (resultCode != RESULT_OK) return;
        else folder = new File("");

        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT_TREE && resultData != null) {

            Uri uri = resultData.getData();

            grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);

            final int takeFlags = resultData.getFlags() & (Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);

            assert uri != null;
            getContentResolver().takePersistableUriPermission(uri, takeFlags);

            DocumentFile file = DocumentFile.fromTreeUri(this, uri);
            if (file != null) {
                StoragesUri storagesUri = new StoragesUri(this);
                storagesUri.addStorage(file.getName(), file.getUri().getPath(), uri.toString());
                storagesUri.close();
            }

        } else return;

        if (tempFolder != null) openFolder(tempFolder);
    }

    public boolean permissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return Environment.isExternalStorageManager();
        else return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestStoragePermissions() {
        java.util.List<String> permissions = new java.util.ArrayList<>();
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permission Granted");
            }// else new Note(MainActivity.this, getString(R.string.permission_denied)).show();
        } else new Note(MainActivity.this, "hey JE, it's unknown request code").show();
    }

    public Fragment newFragment(File file) {
        folder = file;
        return currentFragment = new FilesFragment();
    }

    public Fragment newZippedFragment(File file) {
        folder = file;
        return currentFragment = com.example.files.presentation.files_explorer.FilesFragment.newInstance("ZIPPED", null, file.getPath());
    }

    public Fragment newSFragment(String category, ArrayList<JFile> jFiles) {
        if (jFiles != null) return searchFragment = new SearchScreen(category, jFiles);
        return searchFragment = new SearchScreen(category);
    }

    public Fragment newDFragment(String category) {
        return currentFragment = com.example.files.presentation.files_explorer.FilesFragment.newInstance("CATEGORY", category, null);
    }
    public Fragment newRFragment() {
        return currentFragment = com.example.files.presentation.files_explorer.FilesFragment.newInstance("RECENT", null, null);
    }

    public void loadFragment(Fragment fragment, String tag) {
        // create a FragmentManager
        FragmentManager fm = getSupportFragmentManager();
        // create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction fragmentTransaction = fm.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        // replace the FrameLayout with new Fragment
        fragmentTransaction.add(R.id.frameLayout, fragment, tag);
//        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.addToBackStack("");

        fragmentTransaction.commit(); // save the changes
//        fm.executePendingTransactions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        new Handler().post(() -> {

            if (!getResources().getConfiguration()
                    .isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)) {
                recent.recentScanner();
                categories.refreshCategories(true);
            }
            if (currentFragment != null) currentFragment.refreshGrid();
        });

        Log.e("On Config Change","Yes");
    }

    @Override
    protected void onNightModeChanged(int mode) {
        super.onNightModeChanged(mode);

//        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public static void bottomActionBar(){
        actionBar = instance.findViewById(R.id.bottom_action_bar);
        actionBar.setVisibility(View.GONE);

        int ten_dp = dpToPixels(10);
        int twenty_dp = dpToPixels(20);

        if (hasNavigationBar()) actionBar.setPadding(ten_dp, twenty_dp, ten_dp, twenty_dp);
        else actionBar.setPadding(ten_dp, ten_dp, ten_dp, ten_dp);

        Log.d("##### bottomActionBar().hasNavBar #####", String.valueOf(hasNavigationBar()));

        actionBar.getTouchables().forEach(view -> view.setClipToOutline(true));

        actionBar.findViewById(R.id.delete).setOnClickListener(v ->
                prepareAction(new DialogDelete(selectedJFiles)));

        actionBar.findViewById(R.id.move).setOnClickListener(v ->
                prepareAction(new DialogMove(selectedJFiles)));

        actionBar.findViewById(R.id.copy).setOnClickListener(v ->
                prepareAction(new DialogCopy(selectedJFiles)));

        actionBar.findViewById(R.id.copy_here).setOnClickListener(v -> {
            if (!copyMode) return;
            startCurrentAction();
        });
        actionBar.findViewById(R.id.cancel).setOnClickListener(v -> {
            removeCurrentAction();
            actionBarVisibility(View.GONE);
            copyMode = false;
//            OPERATING = 0;
            cleanCache(instance);
        });
        actionBar.findViewById(R.id.details).setOnClickListener(v ->
                new DialogDetails(instance, !isVisible(TAG_FOLDER)));

        actionBar.findViewById(R.id.share).setOnClickListener(v -> new Share(instance));
    }

    public static void actionBarVisibility(int visibility) {

        new Handler().post(() -> {
            if (visibility == View.VISIBLE) show(actionBar, () -> currentFragment.refreshRecyclerPadding(true));
            if (visibility == View.GONE) hide(actionBar, () -> {
                actionBar.setVisibility(View.GONE);
                actionBar.findViewById(R.id.copy_dialog).setVisibility(View.GONE);
                currentFragment.refreshRecyclerPadding(false);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityListener.onResume();

        sp.registerOnSharedPreferenceChangeListener(spListener);

        if (activityCreated) homeScreenRefresh();
        activityCreated = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (activityCreated) onResume();
        else super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityListener.onPause();
    }

    public OnActivityStateChange activityListener = new OnActivityStateChange() {
        @Override
        public void onResume() { }
        @Override
        public void onPause() { } };

    public void addActivityStateChangeListener(OnActivityStateChange evtListener) {
        this.activityListener = evtListener;
    }

    private void homeScreenRefresh() {

        if (recent!=null && permissionGranted()) recent.recentScanner();
        storages.refreshStorage();
        favorites.setOrRefreshFavorites();
        categories.refreshCategories(false);
    }

    public void onBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (multiSelected) eventListener.onMultiSelectedChange(false); // when passing to ff using details path button
                else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    hideKeyboard(MainActivity.this);
                    remove();
                    getOnBackPressedDispatcher().onBackPressed();
                    new Handler().postDelayed(() -> {
                        try {
                            getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getBackStackEntryCount()-1).onResume();
                            if (isVisible(TAG_FOLDER)) currentFragment.animate();
                        } catch (IndexOutOfBoundsException ignored) {}

                    }, 10);
                } else finish();
                new Handler().postDelayed(() -> textBtnState(enableTextButton()), 100);
                onBackPressedCallback();
            }
        });

    }

    public static void textBtnState(boolean enabled) {
        Log.d("##### MainActivity.textBtnState #####", "");
        setTextButtonState(actionBar.findViewById(R.id.copy_here), enabled);
    }

    public static boolean enableTextButton() {
        if (!currentFragment.isTypeFiles()) return false;
        Log.d("##### MainActivity.enableTextButton #####",
                "\n!currentFragment.notVisible() = " + !currentFragment.notVisible() +
                "\ncurrentFragment.isFilesType() = " + currentFragment.isFilesType() + "\ncopyMode = " + copyMode);
        if (!currentFragment.notVisible() && currentFragment.isFilesType() && copyMode) {
            return !selfDirectory();
        } else return false;
    }

    public static boolean selfDirectory() {
        return actions.get(actions.size()-1).checkConflicts();
    }

    public static void setTextButtonState(Button textView, boolean enabled) {
        textView.setEnabled(enabled);
    }

    public static void hideKeyboard(Context context) {
        Activity activity = ((Activity) context);
        try {
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if ((activity.getCurrentFocus() != null) && (activity.getCurrentFocus().getWindowToken() != null)) {
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            Log.d(activity.getClass().getName(), "hideKeyboard: " + e.getMessage());
        }
    }

    public static void showKeyboard(Context context) {
        ((InputMethodManager) (context).getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        copyMode = false;
        hideKeyboard(this);
        editor.putBoolean("SHOW_FILE_SIZE", false).commit();
    }

    public static void cleanCache(Activity activity) {
        for (File file : Objects.requireNonNull(new File(Objects.requireNonNull(activity.getExternalFilesDir("temp")).getPath()).listFiles())) deleteFile(file);
        for (File file : Objects.requireNonNull(new File(Objects.requireNonNull(activity.getExternalFilesDir("zips")).getPath()).listFiles())) deleteFile(file);
    }
    
    private static void deleteFile(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteFile(child);
            }
        }
        fileOrDirectory.delete();
    }

    public boolean fragmentInLayout(){
        return currentFragment.isVisible() || getSupportFragmentManager().getBackStackEntryCount() > 0;
    }

    public static void closeAllFragments(){
        if (multiSelected) instance.eventListener.onMultiSelectedChange(false);
        for (Fragment ignored : instance.getSupportFragmentManager().getFragments())
            instance.getSupportFragmentManager().popBackStack();
    }

    private void statics() {
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        currentFragment = new FilesFragment();
        searchFragment = new SearchScreen();
        actions = new ArrayList<>();
    }

}
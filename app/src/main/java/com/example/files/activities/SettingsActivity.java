package com.example.files.activities;

import static com.example.files.Statics.currentFragment;
import static com.example.files.Statics.openFile;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.WindowCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.files.BuildConfig;
import com.example.files.R;
import com.example.files.databinding.ActivitySettingsBinding;
import com.example.files.models.JFile;
import com.example.files.models.SelfUpdate;
import com.example.files.utils.DynamicColorUtils;
import com.example.files.utils.MainActivityUtils.Storages;
import com.example.files.utils.ThemeManager;
import com.example.files.view.SelfUpdateSheet;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingsActivity extends BaseActivity {

    DownloadManager downloadManager;
    ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        binding.settingsToolbar.setNavigationOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentFragment != null && currentFragment.isVisible()) currentFragment.applySettings();

                remove();  // Remove this callback to let the system handle the back press
                getOnBackPressedDispatcher().onBackPressed();
            }
        });


    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        final String UPDATE_URL = "https://gitlab.com/jacobel640/sample/-/raw/main/files_update.json";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference preferenceMap = findPreference("update_check");

            if (preferenceMap != null) {
                preferenceMap.setOnPreferenceClickListener(arg0 -> {
                    checkForUpdate();
                    return true;
                });
            }

            preferenceMap = findPreference("theme_picker");

            if (preferenceMap != null) {
                preferenceMap.setOnPreferenceClickListener(arg0 -> {
                    openThemePicker(requireContext());
                    return true;
                });
            }
        }

        private void openThemePicker(Context context) {
            ColorPickerDialogBuilder
                    .with(context)
                    .setTitle(context.getString(R.string.choose_theme_color))
                    .initialColor(ThemeManager.getThemeColor(context))
                    .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                    .density(20)
                    .setPositiveButton(context.getString(R.string.apply_theme_color), (dialog, selectedColor, allColors) -> {
                        DynamicColorUtils.applyDynamicTheme(context, selectedColor);
                        Snackbar.make(((SettingsActivity) requireActivity()).binding.getRoot(), R.string.aplying_dynamic_theme_message, BaseTransientBottomBar.LENGTH_SHORT).show();
                        new Handler().postDelayed(() -> restartApp(context),1000);
                    })
                    .setNegativeButton(context.getString(R.string.reset_default_theme_color), (dialog, which) -> {
                        DynamicColorUtils.applyDynamicTheme(context, R.color.app_theme);
                        new Handler().postDelayed(() -> restartApp(context),1000);
                    })
//                    .setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                    .build()
                    .show();
        }

        public void checkForUpdate() {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(UPDATE_URL).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String body = response.body().string();
                    printLog(body);
                    Gson gson = new GsonBuilder().create();
                    SelfUpdate selfUpdate = gson.fromJson(body, SelfUpdate.class);
                    requireActivity().runOnUiThread(() -> {
                        if (selfUpdate.versionCode > BuildConfig.VERSION_CODE) showSelfUpdateSheet(selfUpdate);
                        else Snackbar.make(requireView(), "You Have the latest version Installed on your device.\nDo you want to download update anyway?",
                                            Snackbar.LENGTH_LONG).setAction("YES", view -> showSelfUpdateSheet(selfUpdate)).show();
//                        toastMessage("SelfUpdate: " + selfUpdate.versionCode + ", current: " + BuildConfig.VERSION_CODE);
                    });
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() -> toastMessage("Failed to get update details!"));
                }
            });
//            new SelfUpdateSheet(requireContext()).show();

        }

        public static void restartApp(Context context) {
            PackageManager packageManager = context.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
            assert intent != null;
            ComponentName componentName = intent.getComponent();
            Intent mainIntent = Intent.makeRestartActivityTask(componentName);
            // Required for API 34 and later
            // Ref: https://developer.android.com/about/versions/14/behavior-changes-14#safer-intents
            mainIntent.setPackage(context.getPackageName());
            context.startActivity(mainIntent);
            Runtime.getRuntime().exit(0);
        }

        public void showSelfUpdateSheet(SelfUpdate selfUpdate) {
            new SelfUpdateSheet(requireContext())
                    .changelog(selfUpdate.versionName, selfUpdate.changelog)
                    .onConfirmClick(() ->  downloadUpdate(selfUpdate))
                    .show();
        }

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        private void downloadUpdate(SelfUpdate selfUpdate) {
            String fileName = "Files-v" + selfUpdate.versionName + ".apk";
            final JFile updateFile = new JFile(Storages.storageItems.get(0).getFile() + "/Download/Files/" + fileName);
            if (updateFile.exists()) { boolean ignored = updateFile.delete(); }

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(selfUpdate.filesBuild));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setTitle(requireContext().getString(R.string.title_files_self_update));
            request.setDescription(requireContext().getString(R.string.desc_downloading_new_version));
            request.setAllowedOverRoaming(false);
            request.setDestinationUri(Uri.fromFile(updateFile));

            DownloadManager downloadManager = ((SettingsActivity) requireActivity()).downloadManager;
            long downloadID = downloadManager.enqueue(request);

            Toast.makeText(requireContext(), requireContext().getString(R.string.downloading), Toast.LENGTH_SHORT).show();

            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    openFile(updateFile, requireContext());
                }
            };
            requireActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }

        public void printLog(String log) {
            Log.d("##### updateCheck #####", log);
        }

        public void toastMessage(String message) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }

//        static class SelfUpdate {
//
//            @SerializedName("version_name")
//            String versionName = "";
//
//            @SerializedName("version_code")
//            int versionCode = 0;
//
//            @SerializedName("files_build")
//            String filesBuild = "";
//
//            @SerializedName("changelog")
//            String changelog = "";
//
//        }
    }
    
}
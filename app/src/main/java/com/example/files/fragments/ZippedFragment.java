package com.example.files.fragments;

import static com.example.files.MainActivity.enableTextButton;
import static com.example.files.MainActivity.instance;
import static com.example.files.MainActivity.textBtnState;
import static com.example.files.Statics.FOLDER_VIEW_TYPE;
import static com.example.files.Statics.TAG_ZIPPED;
import static com.example.files.Statics.copyMode;
import static com.example.files.Statics.folder;
import static com.example.files.Statics.showFileSize;
import static com.example.files.Statics.showHiddenFiles;
import static com.example.files.Statics.sort;
import static com.example.files.fragments.FragmentBase.FragmentType.FILES;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.files.JFileAdapter;
import com.example.files.R;
import com.example.files.Statics;
import com.example.files.actions.DialogSort;
import com.example.files.models.JFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZippedFragment extends FragmentBase {

    public ZippedFragment() {
        super(FragmentType.ARCHIVE);// Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        objects = new ArrayList<>();
        refresh = new Handler(Looper.getMainLooper());
        parent = folder;
        zip = parent;
    }

    @Override
    void onCreateView(View v) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void unzip(String zipFilePath, String destDir) {

        //Open the file
        try(ZipFile file = new ZipFile(zipFilePath))
        {
            FileSystem fileSystem = FileSystems.getDefault();
            //Get file entries
            Enumeration<? extends ZipEntry> entries = file.entries();

            //We will unzip files in this folder - destDir
            String uncompressedDirectory = destDir + "/";
//            Files.createDirectory(fileSystem.getPath(uncompressedDirectory));

            //Iterate over entries
            while (entries.hasMoreElements())
            {
                if (canceled) break;
                ZipEntry entry = entries.nextElement();
                //If directory then create a new directory in uncompressed folder
                if (entry.isDirectory())
                {
                    System.out.println("Creating Directory:" + uncompressedDirectory + entry.getName());
                    Files.createDirectories(fileSystem.getPath(uncompressedDirectory + entry.getName()));
                }
                //Else create the file
                else
                {
                    InputStream is = file.getInputStream(entry);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    String uncompressedFileName = uncompressedDirectory + entry.getName();
                    Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
                    Files.createFile(uncompressedFilePath);
                    FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName);
                    byte[] buffer = new byte[20480];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        if (canceled) break;
                        fileOutput.write(buffer, 0, length);
                    }
                    fileOutput.close();
                    System.out.println("Written :" + entry.getName());
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void loadList() {
        String tempFolder = instance.getExternalFilesDir("zips" + "/" + parent.getPath()).getPath();
        parentJFile = new JFile(parent, instance);
        new com.example.files.utils.AsyncTask(() -> {
            objects.clear();
        }, () -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                unzip(parentJFile.getPath(), tempFolder);
            } // TODO else
        }, () -> {
            parent = new File(tempFolder);
            folder = parent;
            parentJFile = new JFile(tempFolder, requireActivity());
            if (parentJFile.countFiles() > 0)
                for(JFile jFile : parentJFile.listJFiles()) {
                    if(showHiddenFiles) objects.add(jFile);
                    else if(!jFile.isHidden()) objects.add(jFile);
                    else if (!bool) sumLoad++; //because of the "bool" he will check only the first time for hidden files
                }
            requireActivity().runOnUiThread(() -> {
                jFileAdapter = new JFileAdapter(objects, FOLDER_VIEW_TYPE, FILES);
                binding.emptyFolder.setVisibility(objects.size() == 0 ? View.VISIBLE : View.GONE);
                setListeners();
                binding.rvFiles.setAdapter(jFileAdapter);
                new DialogSort().sortAndNotify(jFileAdapter);
                if (showFileSize) triggerSizeCounting();
                if (selected) highlightSelectedItem(false);
                LayoutAnimationController layoutAnimation = AnimationUtils
                        .loadLayoutAnimation(instance, R.anim.layout_anim_ltr);
                binding.rvLayout.setLayoutAnimation(layoutAnimation);
                Log.d("##### onCreate() #####", "create");
            });
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        // if (multiSelected) instance.eventListener.onMultiSelectedChange();
        new Handler().post(() -> new Thread(() -> {
            Looper.prepare();
            // remove not existed files and hidden files according to the settings
            ArrayList<JFile> temp = new ArrayList<>(jFileAdapter.jFileList);
            ArrayList<JFile> remove = new ArrayList<>();
            for (JFile jFile : temp) {
                if (!jFile.exists()) {
                    if (showHiddenFiles || !jFile.getName().startsWith("."))
                        remove.add(jFile);
                } else if (!showHiddenFiles && jFile.getName().startsWith("."))
                    remove.add(jFile);
            }
            temp.removeAll(remove);
            // scanning and adding new files
            for (File file : Objects.requireNonNull(parent.listFiles())) {
                boolean contains = false;
                if (!showHiddenFiles) if (file.getName().startsWith(".")) continue;
                for (JFile jFile : temp) {
                    if (file.getPath().equals(jFile.getPath())) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) temp.add(new JFile(file, requireActivity()));
            }
            binding.rvFiles.post(() -> {
                jFileAdapter.jFileList = temp;
                binding.emptyFolder.setVisibility(jFileAdapter.jFileList.isEmpty() ? View.VISIBLE : View.GONE);
                if (showFileSize) {
                    triggerSizeCounting();
                    if (sort != 1) new DialogSort().sortAndNotify(jFileAdapter);
                } else new DialogSort().sortAndNotify(jFileAdapter);
                jFileAdapter.notifyDataSetChanged();
                Log.d("##### refresh() #####", "rvfiles.post");
            });
        }).start());
    }

    @Override
    public void onResume() {
        super.onResume();
        folder = parent;
        if (!parent.exists()) {
            new Handler().post(() -> instance.onBackPressed());
            return;
        }
        setListListener();
        new Handler().post(this::refreshActionsList);
        new Handler().postDelayed(() -> textBtnState(enableTextButton()),100);
        if (fragmentPosition == requireActivity().getSupportFragmentManager().getBackStackEntryCount()) {
            //new Note(requireActivity(), "hi " + currentFragment).show();
            if (bool) {
                setListeners();
                refresh();
            }

        }
        bool = true;
        Log.d("##### onResume() #####", "resume");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        folder = parent;
        textBtnState(enableTextButton());
        //for(JFile jFile : objects)if(!jFile.file.exists())objects.remove(jFile);
        new Handler().postDelayed(() -> {
//            ff = this;
            if (!folder.exists()) instance.onBackPressed();
        },100);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        canceled = true;
        if (!copyMode) for (File child : Objects.requireNonNull(instance.getExternalFilesDir("zips").listFiles())) {
            delete(child);
        }
    }

    public static void delete(File file) {
        if (file != null) {
            if (file.listFiles() != null) {
                for (File sub : Objects.requireNonNull(file.listFiles())) {
                    if (sub.isDirectory()) delete(sub);
                    sub.delete();
                }
                file.delete();
            }
        }
    }

    @Override
    public boolean notVisible() {
        return !Statics.isVisible(TAG_ZIPPED);
        // TODO check if is same fragment by compering stack count with current place on stack
    }
}
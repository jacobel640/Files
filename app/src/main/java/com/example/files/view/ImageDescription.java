package com.example.files.view;

import static android.content.Intent.ACTION_VIEW_PERMISSION_USAGE;
import static android.content.Intent.EXTRA_ALLOW_MULTIPLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.example.files.R;
import com.example.files.models.JFile;

import java.io.IOException;

@SuppressLint("ViewConstructor")
public class ImageDescription extends LinearLayout {

    Activity activity;
    JFile jFile;
    ImageView ivDescription, ivType, ivFrame;
    TextView tvTitle;
    boolean imageExist;
    int itemId;
    boolean show;

    @SuppressLint("InflateParams")
    public ImageDescription(Activity activity, JFile jfile, String title) {
        super(activity);
        inflate(activity, R.layout.image_description, this);
        this.activity = activity;
        this.jFile = jfile;
        this.show = true;

        tvTitle = findViewById(R.id.tv_title);
        ivType = findViewById(R.id.type);
        ivFrame = findViewById(R.id.frame);
        tvTitle.setText(title);
        tvTitle.setClipToOutline(true);

        ivDescription = findViewById(R.id.image_description);
        ivDescription.setClipToOutline(true);
        ivDescription.setCropToPadding(true);

        new Handler().post(() -> {
            setImage(jfile);
            setType(jfile.getType());
        });

        setOnClickListener(v -> openFiles());
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setImage(JFile file) {
        switch (file.getType()) {
            case AUDIO:
                ivDescription.setImageDrawable(activity.getDrawable(R.drawable.ctg_audio));
                Bitmap bitmap = null;
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(file.getPath());
                    byte[] bytes = mmr.getEmbeddedPicture();
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                } catch (Exception ignored){
                }
                if (bitmap != null) {
                    ivDescription.setImageBitmap(bitmap);
                    imageExist = true;
                }
                break;
            case APK:
                ivDescription.setImageDrawable(activity.getDrawable(R.drawable.ext_apk));
                Drawable drawable = null;
                try {
                    drawable = getAppIcon(activity.getPackageManager().getPackageArchiveInfo(
                            file.getPath(), PackageManager.GET_ACTIVITIES).packageName);
                } catch (Exception e) {
                    try {
                        PackageInfo packageInfo = activity.getPackageManager()
                                .getPackageArchiveInfo(file.getPath(), PackageManager.GET_ACTIVITIES);
                        ApplicationInfo appInfo = packageInfo.applicationInfo;
                        appInfo.sourceDir = file.getPath();
                        appInfo.publicSourceDir = file.getPath();
                        drawable = appInfo.loadIcon(activity.getPackageManager());
                    } catch (Exception ignored) {
                        ivDescription.setImageDrawable(activity.getDrawable(R.drawable.ext_apk));
                    }
                }
                if (drawable != null) {
                    ivDescription.setImageDrawable(drawable);
                    imageExist = true;
                }
                break;
                //new loadApkImage().execute();
                // photo
            case IMAGE:
                ivDescription.setImageDrawable(activity.getDrawable(R.drawable.ctg_photo));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ivDescription.setImageBitmap(ThumbnailUtils.createImageThumbnail(file.getAbsolutePath(),
                            MediaStore.Images.Thumbnails.FULL_SCREEN_KIND));
                } else
                    try {
                    ivDescription.setImageBitmap(MediaStore.Images.Media
                            .getBitmap(activity.getContentResolver(), Uri.fromFile(file)));
                } catch (IOException e) {
                    ivDescription.setImageURI(file.getDocumentFile().getUri());
                }
                imageExist = true;
                break;
                // video
            case VIDEO:
                ivDescription.setImageDrawable(activity.getDrawable(R.drawable.ctg_video));
                Bitmap bitmap1 = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(),
                        MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                if (bitmap1 != null) {
                    ivDescription.setImageBitmap(bitmap1);
                    imageExist = true;
                }
                else imageExist = true;
                break;
            case ARCHIVE:
                ivDescription.setImageDrawable(activity.getDrawable(R.drawable.ctg_archive));
                break;
            default:
                ivDescription.setImageDrawable(activity.getDrawable(R.drawable.file));
                break;
			/*
			case "pdf":
			case "txt":
			case "docx":
			case "pptx":
			case "xlsx":
			case "lrc":
			case "jar":
			case "tar":
			case "exe":
			case "html":
			 */
        }
    }

    public Drawable getAppIcon(String packageName) throws PackageManager.NameNotFoundException {
        return activity.getPackageManager().getApplicationIcon(packageName);
    }

    public boolean show() {
        return show;
    }

    public void toShow(boolean show) {
        this.show = show;
    }

    public int getItemId() {
        return itemId;
    }

    public void openFiles() {
        Uri uri;
        if (jFile.isDocumentFile()) uri = jFile.getDocumentFile().getUri();
        else uri = FileProvider.getUriForFile(activity, "com.example.files", jFile);
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
            activity.startActivity(view);
        } catch (ActivityNotFoundException e){
            new Note(activity, e.getMessage()).show();
        }
        //context.startActivity(Intent.createChooser(share, file.getName()));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void setType(JFile.Type type) {
        if (imageExist) {
            ivType.setVisibility(View.VISIBLE);
            ivFrame.setVisibility(View.VISIBLE);
            switch (type) {
                case VIDEO:
                    ivType.setImageDrawable(activity.getDrawable(R.drawable.play));
                    break;
                case IMAGE:
                    ivType.setImageDrawable(activity.getDrawable(R.drawable.ctg_photo));
                    break;
                case AUDIO:
                    ivType.setImageDrawable(activity.getDrawable(R.drawable.ctg_audio));
                    ivDescription.setBackground(activity.getDrawable(R.drawable.round));
                    ivFrame.setVisibility(View.GONE);
                    break;
                default:
                    ivType.setVisibility(View.GONE);
                    ivFrame.setVisibility(View.GONE);
            }
        }
    }
}

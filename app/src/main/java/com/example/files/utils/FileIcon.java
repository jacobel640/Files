package com.example.files.utils;

import static com.example.files.MainActivity.instance;
import static com.example.files.Statics.dpToPixels;
import static com.example.files.models.JFile.Type.FOLDER;
import static com.example.files.models.JFile.Type.APK;
import static com.example.files.models.JFile.Type.ARCHIVE;
import static com.example.files.models.JFile.Type.AUDIO;
import static com.example.files.models.JFile.Type.DOCUMENT;
import static com.example.files.models.JFile.Type.IMAGE;
import static com.example.files.models.JFile.Type.OTHER;
import static com.example.files.models.JFile.Type.SHORTCUT;
import static com.example.files.models.JFile.Type.VIDEO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.files.JFileAdapter;
import com.example.files.R;
import com.example.files.models.JFile;
import com.example.files.models.ViewHolder;

public class FileIcon {

    public static void setIcon(ViewHolder holder, JFileAdapter.ViewType viewType, JFile jFile, Context context) {
        setIcon(holder.iconView, holder.image, holder.icon, holder.indicator, holder.ext, viewType, jFile, context);
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    public static void setIcon(View iconView, ImageView image, ImageView icon, ImageView indicator,
                               TextView ext, JFileAdapter.ViewType viewType, JFile jFile, Context context) {
        Drawable drawable = getTypeDrawable(jFile.getType(), context);

        ext.setVisibility(View.GONE);
        image.setVisibility(View.GONE);
        iconView.setForeground(null);

        if(viewType == JFileAdapter.ViewType.GRID)
            iconView.setForeground(context.getDrawable(R.drawable.frame));

        if (isImageType(jFile.getType())) {
            indicator.setImageDrawable(drawable);
            image.setVisibility(View.VISIBLE);
        }

        if (jFile.isIconReady()) {
            FileIcon.setImage(iconView, image, icon, indicator,
                    jFile.getCachedIcon(), context, isImageType(jFile.getType()));
        } else {
            jFile.loadIconIfNeeded();
        }
        // haltura...

    }

    public static boolean isImageType(JFile.Type type) {
        return type == IMAGE || type == VIDEO || type == AUDIO;
    }

    public static void setImage(View iconView, ImageView image, ImageView icon, ImageView indicator,
                                Object object, Context context, boolean imageType) {
        icon.setVisibility(View.VISIBLE);
        indicator.setVisibility(View.GONE);
        ImageView imageView = imageType ? image : icon;

        if (object instanceof Drawable) {
            icon.setImageDrawable((Drawable) object);
            Log.d("setImage", "object instanceof Drawable");
            return;
        }

        Glide.with(imageView)
                .load(object)
                .override(128, 128)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                        return false;
                    }

                    @SuppressLint("UseCompatLoadingForDrawables")
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (imageType) {
                            indicator.setVisibility(View.VISIBLE);
                            icon.setVisibility(View.GONE);
                            iconView.setForeground(context.getDrawable(R.drawable.frame));
                        }
                        return false;
                    }
                })
                .into(imageView);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getTypeDrawable(JFile.Type type, Context context) {
        switch (type) {
            case FOLDER:
                return context.getDrawable(R.drawable.folder);
            case AUDIO:
                return context.getDrawable(R.drawable.ctg_audio);
            case IMAGE:
                return context.getDrawable(R.drawable.ctg_photo);
            case VIDEO:
                return context.getDrawable(R.drawable.ctg_video);
            case APK:
                return context.getDrawable(R.drawable.ext_apk);
            case ARCHIVE:
                return context.getDrawable(R.drawable.ctg_archive);
            default:
                return context.getDrawable(R.drawable.file);
        }
    }

    public static void setImage(ImageView imageView, Drawable placeHolder, Object object) {
        Glide.with(imageView)
                .load(object)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(placeHolder)
                .error(placeHolder)
                .dontAnimate()
                .into(imageView);
    }

    public static void setImage(ViewHolder imageView, Drawable placeHolder, Object object) {
        Glide.with(imageView.image)
                .load(object)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(placeHolder)
                .error(placeHolder)
                .dontAnimate()
                .into(imageView.image);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getPlaceholder(JFile.Type type, JFileAdapter.ViewType viewType, ViewHolder holder, Context context) {
        int three_dp = dpToPixels(3f);
        boolean isVideo = false;
        ImageView jIcon = holder.image;
        ImageView jIndicator = holder.indicator;
        holder.ext.setVisibility(View.GONE);
        Drawable drawable;
        switch (type) {
            case FOLDER:
                drawable = context.getDrawable(R.drawable.folder);
//                if (isNormalScreen)
                jIcon.setPadding(three_dp, three_dp, three_dp, three_dp);
                break;
            case AUDIO:
                drawable = context.getDrawable(R.drawable.ctg_audio);
//                if (isNormalScreen)
                jIcon.setPadding(0, 0, 0, 0);
                break;
            case IMAGE:
                drawable = context.getDrawable(R.drawable.ctg_photo);
//                if (isNormalScreen)
                jIcon.setPadding(0, 0, 0, 0);
                break;
            case VIDEO:
                isVideo = true;
                drawable = context.getDrawable(R.drawable.ctg_video);
//                if (isNormalScreen)
                jIcon.setPadding(0, 0, 0, 0);
                break;
            case APK:
                drawable = context.getDrawable(R.drawable.ext_apk);
//                if (isNormalScreen)
                jIcon.setPadding(three_dp, three_dp, three_dp, three_dp);
                break;
            case ARCHIVE:
                drawable = context.getDrawable(R.drawable.ctg_archive);
//                if (isNormalScreen)
                jIcon.setPadding(three_dp, three_dp, three_dp, three_dp);
                break;
            case DOCUMENT:
                drawable = context.getDrawable(R.drawable.type_office); // needs change
//                if (isNormalScreen)
                jIcon.setPadding(three_dp, three_dp, three_dp, three_dp);
                break;
                // TODO icon for shortcut file
//            case SHORTCUT:
//                drawable = context.getDrawable(R.drawable.file);
//                jIcon.setPadding(three_dp, three_dp, three_dp, three_dp);
            default:
//                holder.jExt.setVisibility(View.VISIBLE);
                drawable = context.getDrawable(R.drawable.file);
                jIcon.setPadding(three_dp, three_dp, three_dp, three_dp);
                break;
        }

        if (isVideo){
            jIcon.setCropToPadding(true);
            holder.iconView.setForeground(context.getDrawable(R.drawable.frame));
            jIndicator.setVisibility(View.VISIBLE);
        } else if (viewType == JFileAdapter.ViewType.GRID) {
            jIcon.setCropToPadding(true);
            holder.iconView.setForeground(context.getDrawable(R.drawable.frame));
            jIndicator.setVisibility(View.GONE);
        } else {
            jIcon.setCropToPadding(false);
            holder.iconView.setForeground(null);
            jIndicator.setVisibility(View.GONE);
        }

        return drawable;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getPlaceHolder(JFile.Type type, Context context) {
        Drawable drawable;
        switch (type) {
            case FOLDER:
                drawable = context.getDrawable(R.drawable.folder);
                break;
            case AUDIO:
                drawable = context.getDrawable(R.drawable.ctg_audio);
                break;
            case IMAGE:
                drawable = context.getDrawable(R.drawable.ctg_photo);
                break;
            case VIDEO:
                drawable = context.getDrawable(R.drawable.ctg_video);
                break;
            case APK:
                drawable = context.getDrawable(R.drawable.ext_apk);
                break;
            case ARCHIVE:
                drawable = context.getDrawable(R.drawable.ctg_archive);
                break;
//            case DOCUMENT:
//                drawable = context.getDrawable(R.drawable.type_office); // needs change
//                break;
            default:
//                holder.jExt.setVisibility(View.VISIBLE);
                drawable = context.getDrawable(R.drawable.file);
                break;
        }

        return drawable;

    }

    static Drawable backgroundDrawable(int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadii(new float[] { 50, 50, 50, 50, 50, 50, 50, 50 });
        shape.setColor(color);
//        view.setBackground(shape);
        return shape;
    }

    @SuppressLint("DefaultLocale")
    Drawable backgroundDrawable(String ext) {
        int color = 0;
        for (byte b : ext.getBytes()) color+=b;
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadii(new float[] { 8, 8, 8, 8, 0, 0, 0, 0 });
        shape.setColor(Integer.parseInt(String.format("%06d", color)));
//        view.setBackground(shape);
        return shape;
    }

    private boolean isNormalScreen() {

        DisplayMetrics dm = new DisplayMetrics();
        instance.getWindowManager().getDefaultDisplay().getMetrics(dm);
        double x = Math.pow(dm.widthPixels/dm.xdpi,2);
        double y = Math.pow(dm.heightPixels/dm.ydpi,2);
        double screenInches = Math.sqrt(x+y);
        screenInches = (double) Math.round(screenInches * 10) / 10;

        return screenInches > 3;
    }

    public static JFile.Type types(String extensionTLC, boolean isDirectory) {
        if (isDirectory) return FOLDER;
        else
            switch (extensionTLC) {
                // photo
                case "cr2":
                case "dng":
                case "heic":
                case "jpg":
                case "jpeg":
                case "png":
                case "raw":
                case "webp":
                case "ico":
                    return IMAGE;
                // audio
                case "aac":
                case "amr":
                case "flac":
                case "mp3":
                case "m4a":
                case "ogg":
                case "opus":
                case "wma":
                case "wav":
                    return AUDIO;
                // video
                case "3gpp":
                case "avi":
                case "gif":
                case "mkv":
                case "mov":
                case "mp4":
                    return VIDEO;
                case "apk":
                    return APK;
                // archives
                case "7z":
                case "7zip":
                case "apks":
                case "apkm":
                case "xapk":
                case "gz":
                case "jar":
                case "rar":
                case "zip":
                    return ARCHIVE;
                // documents
                case "txt":
                case "pdf":
                case "doc":
                case "docx":
                case "xls":
                case "xlsx":
                    return DOCUMENT;
                case "lnk":
                    return SHORTCUT;
//                    // backup files
//                case "bak": // restore file
//                case "bkup":
//                case "backup":
//                    // image archive
//                case "img":
//                case "ext4":
//                    // executable files
//                case "exe":
//                case "sh":
//                case "bat":
//                case "batch":
//                    // driver installer (windows)
//                case "ini":
//                case "inf":
//                    // program part
//                case "dll":
//                    // coding txt files
//                case "xml":
//                case "java":
//                case "class":
//                    // smali archive
//                case "dex":
//                    // logging txt file
//                case "log":
//                    // system configuration file
//                case "prop":
//                    // code/program description file (like github etc.)
//                case "md":
//                    // temporary held files
//                case "temp":
//                case "tmp":
//                case "thumbnails":
//                    // android folder media hider
//                case "nommedia":
//                    // playlist file
//                case "m3u":
//                    // unknowns
//                case "sys":
//                case "cat":
//                case "vdex":
//                case "odex":
//                        return 0;
                default:
                    return OTHER;
            }
    }
}

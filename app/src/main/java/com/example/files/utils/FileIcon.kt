package com.example.files.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.files.MainActivity.instance
import com.example.files.JFileAdapter
import com.example.files.R
import com.example.files.Statics.dpToPixels
import com.example.files.models.JFile
import com.example.files.models.ViewHolder
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object FileIcon {

    @JvmStatic
    fun setIcon(holder: ViewHolder, viewType: JFileAdapter.ViewType, jFile: JFile, context: Context) {
        setIcon(
            holder.iconView, holder.image, holder.icon, holder.indicator, holder.ext,
            viewType, jFile, context
        )
    }

    @JvmStatic
    @SuppressLint("UseCompatLoadingForDrawables")
    fun setIcon(
        iconView: View, image: ImageView, icon: ImageView, indicator: ImageView,
        ext: TextView, viewType: JFileAdapter.ViewType, jFile: JFile, context: Context
    ) {
        val drawable = getTypeDrawable(jFile.type, context)

        ext.visibility = View.GONE
        image.visibility = View.GONE
        iconView.foreground = null

        if (viewType == JFileAdapter.ViewType.GRID)
            iconView.foreground = ContextCompat.getDrawable(context, R.drawable.frame)

        if (isImageType(jFile.type)) {
            indicator.setImageDrawable(drawable)
            image.visibility = View.VISIBLE
        }

        if (jFile.isIconReady) {
            setImage(
                iconView, image, icon, indicator,
                jFile.cachedIcon, context, isImageType(jFile.type)
            )
        } else {
            jFile.loadIconIfNeeded()
        }
    }

    @JvmStatic
    fun isImageType(type: JFile.Type): Boolean {
        return type == JFile.Type.IMAGE || type == JFile.Type.VIDEO || type == JFile.Type.AUDIO
    }

    @JvmStatic
    fun setImage(
        iconView: View, image: ImageView, icon: ImageView, indicator: ImageView,
        `object`: Any?, context: Context, imageType: Boolean
    ) {
        icon.visibility = View.VISIBLE
        indicator.visibility = View.GONE
        val imageView = if (imageType) image else icon

        if (`object` is Drawable) {
            icon.setImageDrawable(`object`)
            return
        }

        Glide.with(imageView)
            .load(`object`)
            .override(128, 128)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                @SuppressLint("UseCompatLoadingForDrawables")
                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    if (imageType) {
                        indicator.visibility = View.VISIBLE
                        icon.visibility = View.GONE
                        iconView.foreground = ContextCompat.getDrawable(context, R.drawable.frame)
                    }
                    return false
                }
            })
            .into(imageView)
    }

    @JvmStatic
    @SuppressLint("UseCompatLoadingForDrawables")
    fun getTypeDrawable(type: JFile.Type, context: Context): Drawable? {
        return when (type) {
            JFile.Type.FOLDER -> ContextCompat.getDrawable(context, R.drawable.folder)
            JFile.Type.AUDIO -> ContextCompat.getDrawable(context, R.drawable.ctg_audio)
            JFile.Type.IMAGE -> ContextCompat.getDrawable(context, R.drawable.ctg_photo)
            JFile.Type.VIDEO -> ContextCompat.getDrawable(context, R.drawable.ctg_video)
            JFile.Type.APK -> ContextCompat.getDrawable(context, R.drawable.ext_apk)
            JFile.Type.ARCHIVE -> ContextCompat.getDrawable(context, R.drawable.ctg_archive)
            else -> ContextCompat.getDrawable(context, R.drawable.file)
        }
    }

    @JvmStatic
    fun setImage(imageView: ImageView, placeHolder: Drawable?, `object`: Any?) {
        Glide.with(imageView)
            .load(`object`)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(placeHolder)
            .error(placeHolder)
            .dontAnimate()
            .into(imageView)
    }

    @JvmStatic
    fun setImage(imageView: ViewHolder, placeHolder: Drawable?, `object`: Any?) {
        Glide.with(imageView.image)
            .load(`object`)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(placeHolder)
            .error(placeHolder)
            .dontAnimate()
            .into(imageView.image)
    }

    @JvmStatic
    @SuppressLint("UseCompatLoadingForDrawables")
    fun getPlaceholder(
        type: JFile.Type,
        viewType: JFileAdapter.ViewType,
        holder: ViewHolder,
        context: Context
    ): Drawable? {
        val threeDp = dpToPixels(3f)
        var isVideo = false
        val jIcon = holder.image
        val jIndicator = holder.indicator
        holder.ext.visibility = View.GONE
        val drawable: Drawable?
        
        when (type) {
            JFile.Type.FOLDER -> {
                drawable = ContextCompat.getDrawable(context, R.drawable.folder)
                jIcon.setPadding(threeDp, threeDp, threeDp, threeDp)
            }
            JFile.Type.AUDIO -> {
                drawable = ContextCompat.getDrawable(context, R.drawable.ctg_audio)
                jIcon.setPadding(0, 0, 0, 0)
            }
            JFile.Type.IMAGE -> {
                drawable = ContextCompat.getDrawable(context, R.drawable.ctg_photo)
                jIcon.setPadding(0, 0, 0, 0)
            }
            JFile.Type.VIDEO -> {
                isVideo = true
                drawable = ContextCompat.getDrawable(context, R.drawable.ctg_video)
                jIcon.setPadding(0, 0, 0, 0)
            }
            JFile.Type.APK -> {
                drawable = ContextCompat.getDrawable(context, R.drawable.ext_apk)
                jIcon.setPadding(threeDp, threeDp, threeDp, threeDp)
            }
            JFile.Type.ARCHIVE -> {
                drawable = ContextCompat.getDrawable(context, R.drawable.ctg_archive)
                jIcon.setPadding(threeDp, threeDp, threeDp, threeDp)
            }
            JFile.Type.DOCUMENT -> {
                drawable = ContextCompat.getDrawable(context, R.drawable.type_office)
                jIcon.setPadding(threeDp, threeDp, threeDp, threeDp)
            }
            else -> {
                drawable = ContextCompat.getDrawable(context, R.drawable.file)
                jIcon.setPadding(threeDp, threeDp, threeDp, threeDp)
            }
        }

        if (isVideo) {
            jIcon.cropToPadding = true
            holder.iconView.foreground = ContextCompat.getDrawable(context, R.drawable.frame)
            jIndicator.visibility = View.VISIBLE
        } else if (viewType == JFileAdapter.ViewType.GRID) {
            jIcon.cropToPadding = true
            holder.iconView.foreground = ContextCompat.getDrawable(context, R.drawable.frame)
            jIndicator.visibility = View.GONE
        } else {
            jIcon.cropToPadding = false
            holder.iconView.foreground = null
            jIndicator.visibility = View.GONE
        }

        return drawable
    }

    @JvmStatic
    @SuppressLint("UseCompatLoadingForDrawables")
    fun getPlaceHolder(type: JFile.Type, context: Context): Drawable? {
        return when (type) {
            JFile.Type.FOLDER -> ContextCompat.getDrawable(context, R.drawable.folder)
            JFile.Type.AUDIO -> ContextCompat.getDrawable(context, R.drawable.ctg_audio)
            JFile.Type.IMAGE -> ContextCompat.getDrawable(context, R.drawable.ctg_photo)
            JFile.Type.VIDEO -> ContextCompat.getDrawable(context, R.drawable.ctg_video)
            JFile.Type.APK -> ContextCompat.getDrawable(context, R.drawable.ext_apk)
            JFile.Type.ARCHIVE -> ContextCompat.getDrawable(context, R.drawable.ctg_archive)
            else -> ContextCompat.getDrawable(context, R.drawable.file)
        }
    }

    @JvmStatic
    fun backgroundDrawable(color: Int): Drawable {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadii = floatArrayOf(50f, 50f, 50f, 50f, 50f, 50f, 50f, 50f)
        shape.setColor(color)
        return shape
    }

    @JvmStatic
    fun types(extensionTLC: String, isDirectory: Boolean): JFile.Type {
        if (isDirectory) return JFile.Type.FOLDER
        return when (extensionTLC) {
            "cr2", "dng", "heic", "jpg", "jpeg", "png", "raw", "webp", "ico" -> JFile.Type.IMAGE
            "aac", "amr", "flac", "mp3", "m4a", "ogg", "opus", "wma", "wav" -> JFile.Type.AUDIO
            "3gpp", "avi", "gif", "mkv", "mov", "mp4" -> JFile.Type.VIDEO
            "apk" -> JFile.Type.APK
            "7z", "7zip", "apks", "apkm", "xapk", "gz", "jar", "rar", "zip" -> JFile.Type.ARCHIVE
            "txt", "pdf", "doc", "docx", "xls", "xlsx" -> JFile.Type.DOCUMENT
            "lnk" -> JFile.Type.SHORTCUT
            else -> JFile.Type.OTHER
        }
    }
}

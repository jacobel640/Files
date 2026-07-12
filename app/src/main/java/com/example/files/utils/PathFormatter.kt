package com.example.files.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.files.R
import com.example.files.Statics.DOC_SLASH
import com.example.files.Statics.DOC_SPACE
import com.example.files.utils.MainActivityUtils.Storages.storageItems

class PathFormatter(private val context: Context) {

    @SuppressLint("StringFormatMatches", "ResourceType")
    fun format(path: String): String {
        val preArr = path.split("/")
        val pathArray = ArrayList<String>()
        for (s in preArr) {
            if (s.isNotEmpty()) pathArray.add(s)
        }
        if (pathArray.isEmpty()) return path
        val pathBuilder = StringBuilder()
        if (pathArray[0] == "storage" && pathArray.size > 1) {
            if (pathArray[1] == "emulated") {
                for (i in 2 until pathArray.size) {
                    if (i == 2) pathBuilder.append(
                        pathArray[i].replace(
                            "0",
                            context.getString(R.string.internal_storage)
                        )
                    )
                    else pathBuilder.append(pathArray[i])
                    if (i != pathArray.size - 1) pathBuilder.append("/")
                }
            } else {
                var storageId = 0
                for (si in storageItems) {
                    if (si.file.path.split("/")[2] == pathArray[1]) storageId = si.itemId - 1
                }
                for (i in 1 until pathArray.size) {
                    pathBuilder.append(
                        pathArray[i].replace(
                            pathArray[1],
                            context.getString(R.string.external_storage, storageId.toString())
                        )
                    )
                    if (i != pathArray.size - 1) pathBuilder.append("/")
                }
            }
        }
        return if (pathBuilder.toString().isNotEmpty()) pathBuilder.toString() else path
    }

    fun externalPath(name: String, path: String): String {
        val pathEnd = java.lang.StringBuilder()
        pathEnd.append("content://com.android.externalstorage.documents/tree/")
            .append(name).append("%3A/document/").append(name).append("%3A")

        val path1 = path.split("/")
        if (path1.size >= 3) {
            pathEnd.append(path1[3])
        }
        if (path1.size >= 4) {
            for (i in 4 until path1.size) {
                pathEnd.append(DOC_SLASH).append(path1[i])
            }
        }

        return pathEnd.toString().replace(" ", "%20")
    }

    fun externalFilePathWoName(path: String): String {
        val preName = path.substring(path.lastIndexOf("storage/") + 8).split("/")
        var name = preName[0]

        var pathEnd = ""
        pathEnd += "content://com.android.externalstorage.documents/tree/"
        pathEnd += "$name%3A/document/$name%3A"

        // the first item in the path - after the storage name shouldn't get "DSlash"
        if (name.length < 3) {
            name = "5A85-D438"
            pathEnd = ""
            pathEnd += "content://com.android.externalstorage.documents/tree/"
            pathEnd += "$name%3A/document/$name%3A"
            Log.d("PATH", path)
            Log.d("NAME", name)

            val path1 = path.substring(path.indexOf("files/") + 3 + name.length).replace("/", DOC_SLASH)
            pathEnd += path1.replace(" ", DOC_SPACE)

            Log.d("PATH END", pathEnd)

            return pathEnd
        }

        Log.d("PATH", path)
        Log.d("NAME", name)
        if (!path.endsWith(name.substring(name.length - 3))) {
            val path1 = path.substring(path.indexOf("storage/") + 9 + name.length).replace("/", DOC_SLASH)
            pathEnd += path1.replace(" ", DOC_SPACE)
        } else {
            val path1 = path.substring(path.indexOf("storage/") + 8 + name.length).replace("/", DOC_SLASH)
            pathEnd += path1.replace(" ", DOC_SPACE)
        }

        Log.d("PATH END", pathEnd)

        return pathEnd
    }

    fun externalFolderPathWoName(path: String): String {
        val preName = path.substring(path.lastIndexOf("storage/") + 8).split("/")
        val name = preName[0]

        var pathEnd = ""
        pathEnd += "content://com.android.externalstorage.documents/tree/"
        pathEnd += "$name%3A"

        // the first item i the path - after the storage name shouldn't get "DSlash"
        Log.d("##### PathFormatter.externalFolderPathWoName #####", name)
        if (!path.endsWith(name.substring(name.length - 3))) {
            val path1 = path.substring(path.indexOf("storage/") + 9 + name.length).replace("/", DOC_SLASH)
            pathEnd += path1.replace(" ", DOC_SPACE)
        } else {
            val path1 = path.substring(path.indexOf("storage/") + 8 + name.length).replace("/", DOC_SLASH)
            pathEnd += path1.replace(" ", DOC_SPACE)
        }

        return pathEnd
    }
}

package com.example.files.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.files.models.JFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FileRepository @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun queryMediaStore(uri: Uri, isRecent: Boolean = false): List<JFile> = withContext(Dispatchers.IO) {
        val result = mutableListOf<JFile>()
        val contentResolver: ContentResolver = context.contentResolver
        val sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " ASC"
        
        val cursor = contentResolver.query(uri, null, null, null, sortOrder)
        cursor?.use {
            if (it.moveToLast()) {
                var count = 0
                do {
                    val dataIndex = it.getColumnIndex(MediaStore.Downloads.DATA)
                    if (dataIndex >= 0) {
                        val data = it.getString(dataIndex)
                        val file = File(data)
                        if (isRecent) {
                            if (filterRecent(file)) {
                                result.add(JFile(file, context))
                                count++
                            }
                        } else {
                            result.add(JFile(file, context))
                        }
                    }
                } while (it.moveToPrevious() && (!isRecent || count <= 300))
            }
        }
        result
    }

    suspend fun getRecentFiles(): List<JFile> = withContext(Dispatchers.IO) {
        val external = queryMediaStore(MediaStore.Files.getContentUri("external"), isRecent = true)
        val internal = queryMediaStore(MediaStore.Files.getContentUri("internal"), isRecent = true)
        external + internal
    }

    suspend fun getAllSearchFiles(): List<JFile> = withContext(Dispatchers.IO) {
        val external = queryMediaStore(MediaStore.Files.getContentUri("external"), isRecent = false)
        val internal = queryMediaStore(MediaStore.Files.getContentUri("internal"), isRecent = false)
        external + internal
    }

    suspend fun iterateFolder(parent: File): List<JFile> = withContext(Dispatchers.IO) {
        val result = mutableListOf<JFile>()
        val files = parent.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    result.addAll(iterateFolder(file))
                }
                result.add(JFile(file, context))
            }
        }
        result
    }

    private fun filterRecent(file: File): Boolean {
        if (file.isDirectory || file.length() == 0L) return false
        val path = file.absolutePath
        if (path.startsWith("//Android")) return false
        
        val parentPath = file.parentFile?.path ?: ""
        if (parentPath.endsWith("WhatsApp/Databases") || parentPath.endsWith("WhatsApp/Backups")) return false
        
        val ext = file.name.substringAfterLast('.', "").lowercase()
        return isRecentExt(ext)
    }

    private fun isRecentExt(ext: String): Boolean {
        return when (ext) {
            "", "m3u", "log", "bak", "bkup", "backup", "crypt1", "crypt12" -> false
            else -> true
        }
    }
}

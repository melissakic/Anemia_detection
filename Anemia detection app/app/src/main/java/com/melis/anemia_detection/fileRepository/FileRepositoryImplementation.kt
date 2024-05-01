package com.melis.anemia_detection.fileRepository

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.InputStream

class FileRepositoryImplementation : FileRepositoryInterface {

    override fun getFileFromUri(context: Context, uri: Uri): File? {
        val filePath: String = getRealPathFromUri(context, uri) ?: return null
        return File(filePath)
    }

    override fun getRealPathFromUri(context: Context, uri: Uri): String? {
        var realPath: String? = null

        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                    realPath = it.getString(columnIndex)
                }
            }
        } else if (uri.scheme == ContentResolver.SCHEME_FILE) {
            realPath = uri.path
        }

        return realPath
    }

    override fun getInputStreamFromUri(context: Context, uri: Uri): InputStream? {
        return context.contentResolver.openInputStream(uri)
    }

}
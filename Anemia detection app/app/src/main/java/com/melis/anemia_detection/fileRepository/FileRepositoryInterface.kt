package com.melis.anemia_detection.fileRepository

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream

interface FileRepositoryInterface {

    fun getFileFromUri(context: Context, uri: Uri): File?

    fun getRealPathFromUri(context: Context, uri: Uri): String?

    fun getInputStreamFromUri(context: Context, uri: Uri): InputStream?
}
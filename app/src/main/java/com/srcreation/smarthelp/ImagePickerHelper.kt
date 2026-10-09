package com.srcreation.smarthelp

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImagePickerHelper {

    /**
     * Copies a picked Image Uri (from Photo Picker / Gallery) into private app internal storage (/files/profile_images/).
     * Returns permanent file URI string ("file:///data/user/0/com.srcreation.smarthelp/files/profile_images/prefix_xxx.jpg").
     * Internal files NEVER expire, NEVER require runtime SAF permissions, and NEVER fail across device reboots or app restarts!
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri, fileNamePrefix: String): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val directory = File(context.filesDir, "profile_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val destinationFile = File(directory, "${fileNamePrefix}_profile_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(destinationFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destinationFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

package com.srcreation.smarthelp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object ReportShareHelper {

    fun sharePdfUri(context: Context, contentUri: Uri, targetApp: String = "ALL") {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Smart Help - Doctor Consultation Health Summary")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is my Doctor Consultation Health Summary generated from Smart Help App."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            when (targetApp.uppercase()) {
                "WHATSAPP" -> {
                    val whatsappPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
                    var launched = false
                    for (pkg in whatsappPackages) {
                        try {
                            val appIntent = Intent(shareIntent).apply { setPackage(pkg) }
                            context.startActivity(appIntent)
                            launched = true
                            break
                        } catch (_: Exception) {}
                    }
                    if (!launched) {
                        Toast.makeText(context, "WhatsApp not found. Opening Share menu...", Toast.LENGTH_SHORT).show()
                        context.startActivity(Intent.createChooser(shareIntent, "Share Report via..."))
                    }
                }
                "EMAIL" -> {
                    val emailIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        putExtra(Intent.EXTRA_SUBJECT, "Doctor Consultation Summary - Smart Help")
                        putExtra(Intent.EXTRA_TEXT, "Please find attached my medical health summary.")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(emailIntent, "Send Email..."))
                }
                else -> {
                    context.startActivity(Intent.createChooser(shareIntent, "Share Report via..."))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Sharing failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sharePdfFile(context: Context, file: File, targetApp: String = "ALL") {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            sharePdfUri(context, contentUri, targetApp)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Sharing failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveFileToDownloads(context: Context, sourceFile: File, fileName: String): Boolean {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/SmartHelp")
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { os ->
                    sourceFile.inputStream().use { isStream ->
                        isStream.copyTo(os)
                    }
                }
                Toast.makeText(context, "Report saved to Downloads/SmartHelp 📂", Toast.LENGTH_LONG).show()
                true
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}

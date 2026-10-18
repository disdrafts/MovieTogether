package com.example.movietogether.data.local

import android.content.Context
import android.net.Uri
import java.io.File

class AvatarStorage(private val context: Context) {

    private val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    private val key = "avatar_path"

    fun saveFromUri(uri: Uri): String {
        val input = context.contentResolver.openInputStream(uri)
            ?: error("Cannot open image")

        val file = File(context.filesDir, "avatar.jpg")
        file.outputStream().use { output ->
            input.use { it.copyTo(output) }
        }

        prefs.edit().putString(key, file.absolutePath).apply()
        return file.absolutePath
    }

    fun getPath(): String? = prefs.getString(key, null)

    fun clear() {
        getPath()?.let { File(it).delete() }
        prefs.edit().remove(key).apply()
    }
}
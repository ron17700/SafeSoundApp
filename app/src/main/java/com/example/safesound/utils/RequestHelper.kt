package com.example.safesound.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.safesound.network.NetworkModule
import com.squareup.picasso.Picasso
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream

object RequestHelper {
    fun String.toRequestBody(): RequestBody =
        RequestBody.create(MediaType.parse("text/plain"), this)

    fun imageUriToMultiPart(context: Context, imageUri: Uri, fileNamePrefix: String = "image"): MultipartBody.Part {
        val bitmap: Bitmap?
        if (imageUri.toString().startsWith("uploads/") ||
            imageUri.toString().startsWith("default-files")) {
            val fullUrl = NetworkModule.BASE_URL + imageUri
            bitmap = Picasso.get().load(fullUrl).get()
        } else {
            bitmap = Picasso.get().load(imageUri).get()
        }
        val tempFile = saveBitmapToTempFile(bitmap, fileNamePrefix, context.cacheDir)
        val requestBody = RequestBody.create(MediaType.parse("image/*"), tempFile)
        return MultipartBody.Part.createFormData("file", tempFile.name, requestBody)
    }

    private fun saveBitmapToTempFile(bitmap: Bitmap, fileNamePrefix: String, cacheDir: File): File {
        val tempFile = File.createTempFile(fileNamePrefix, ".jpg", cacheDir)
        FileOutputStream(tempFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        }
        return tempFile
    }
}
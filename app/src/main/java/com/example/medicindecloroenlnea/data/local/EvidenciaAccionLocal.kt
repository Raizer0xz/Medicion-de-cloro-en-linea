package com.example.medicindecloroenlnea.data.local

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.IOException
import java.util.UUID

internal object EvidenciaAccionLocal {
    fun guardar(context: Context, uri: Uri): String {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException("No se pudo leer la imagen seleccionada.")
        return crearArchivo(context).also { file ->
            input.use { source -> file.outputStream().use { output -> source.copyTo(output) } }
        }.absolutePath
    }

    fun guardar(context: Context, bitmap: Bitmap): String {
        val file = crearArchivo(context)
        val guardada = file.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
        if (!guardada) throw IOException("No se pudo guardar la fotografía.")
        return file.absolutePath
    }

    private fun crearArchivo(context: Context): File {
        val directory = File(context.filesDir, "acciones")
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("No se pudo crear el directorio de evidencias.")
        }
        return File(directory, "${UUID.randomUUID()}.img")
    }
}

package com.agroberriesmx.reclutadores.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Clase auxiliar para manejar la copia de archivos de imágenes desde una Uri (cámara/galería)
 * al almacenamiento interno privado y seguro de la aplicación.
 */
class ImageFileManager(private val context: Context) {

    private val DIRECTORY_NAME = "candidatos_docs"
    private val TAG = "ImageFileManager"

    /**
     * Copia el contenido de una Uri a un archivo local en el almacenamiento interno de la app.
     * @param sourceUri La Uri de la imagen (ej: del resultado de la cámara).
     * @param fileNamePrefix Un prefijo para nombrar el archivo (ej: nombre del reclutador).
     * @return La ruta absoluta del archivo guardado, o null si falla.
     */
    fun saveImageToInternalStorage(sourceUri: Uri, fileNamePrefix: String): String? {
        // 1. Crear el directorio de destino
        val storageDir = File(context.filesDir, DIRECTORY_NAME)
        if (!storageDir.exists()) {
            storageDir.mkdirs() // Crear si no existe
        }

        // 2. Definir el nombre del archivo final (Ej: JUAN_PEREZ_17012025.jpg)
        val timeStamp = System.currentTimeMillis()
        val extension = context.contentResolver.getType(sourceUri)?.substringAfter('/') ?: "jpg"
        val cleanPrefix = fileNamePrefix.replace("[^a-zA-Z0-9]".toRegex(), "_") // Limpiar el nombre
        val fileName = "${cleanPrefix}_${timeStamp}.$extension"
        val destinationFile = File(storageDir, fileName)

        try {
            // 3. Abrir streams y copiar
            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(destinationFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            // 4. Devolver la ruta absoluta
            return destinationFile.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar la imagen: ${e.message}", e)
            return null
        }
    }
}
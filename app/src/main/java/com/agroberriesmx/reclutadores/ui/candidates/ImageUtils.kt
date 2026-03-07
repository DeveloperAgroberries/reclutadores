package com.agroberriesmx.reclutadores.ui.candidates // Asegúrate que el package coincida con tu carpeta

import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import java.net.URL
import java.util.concurrent.Executors

// Esta función debe ir fuera de cualquier clase (Top-level function)
fun descargarImagen(url: String, imageView: ImageView) {
    val executor = Executors.newSingleThreadExecutor()
    val handler = Handler(Looper.getMainLooper())

    executor.execute {
        try {
            // Especificamos URL(url) explícitamente para evitar errores de inferencia
            val imageURL = URL(url)
            val `in` = imageURL.openStream()
            val bitmap = BitmapFactory.decodeStream(`in`)

            handler.post {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Opcional: poner una imagen de error si falla la descarga
            handler.post {
                imageView.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        }
    }
}
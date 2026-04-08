package com.agroberriesmx.reclutadores.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.agroberriesmx.reclutadores.R
import com.agroberriesmx.reclutadores.databinding.FragmentAboutBinding
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // CARGAR EL GIF DE FONDO
        // Asegúrate que tu archivo se llame "fondo_animado.gif" o cambia el nombre abajo
        Glide.with(this)
            .asGif()
            .load(R.raw.agrobackground) // <--- Cambia esto al nombre de tu GIF
            .into(binding.gifBackgroundAbout)

        // Configuración de clics
        binding.tvLinkPrivacyPolicy.setOnClickListener {
            val url = "https://agroberries.mx/agroweb/PrivacyPolicy/index.html"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)

            // Es una buena práctica verificar si hay una app que pueda abrir el link
            try {
                startActivity(intent)
            } catch (e: Exception) {
                // En caso de que no haya navegador (muy raro en Android), puedes mostrar un mensaje
                android.widget.Toast.makeText(context, "No se pudo abrir el enlace", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
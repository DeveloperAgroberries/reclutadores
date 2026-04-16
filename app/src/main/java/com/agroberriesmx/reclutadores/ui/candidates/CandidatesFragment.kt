package com.agroberriesmx.reclutadores.ui.candidates

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.agroberriesmx.reclutadores.R
import com.agroberriesmx.reclutadores.data.network.response.CandidateRemote
import com.agroberriesmx.reclutadores.databinding.FragmentCandidatesBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CandidatesFragment : Fragment() {

    private var _binding: FragmentCandidatesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CandidatesViewModel by viewModels()

    // 1. Actualizamos el adapter para que escuche el click y abra el modal
    private val adapter = CandidatesAdapter(emptyList()) { candidato ->
        showCandidateDetail(candidato)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCandidatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🕵️ EXTRAEMOS DIRECTAMENTE DE LA CAJA (Sin ViewModel, sin Hilt)
        val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val usuarioLogueado = prefs.getString("last_user_id", "")?.trim()?.uppercase() ?: ""

        // 🚨 ESTE LOG DEBE SALIR SI EL FRAGMENTO SE ABRE
        //android.util.Log.e("PRUEBA_MAESTRA", "USUARIO ENCONTRADO: [$usuarioLogueado]")

        val vips = listOf("VARELLANO", "RDIMAS", "AOROZCO")

        if (!vips.contains(usuarioLogueado)) {
            Toast.makeText(requireContext(), "Acceso denegado para: $usuarioLogueado", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }

        // --- SI ERES RDIMAS, EL CÓDIGO SIGUE AQUÍ ---
        try {
            setupRecyclerView()
            setupObservers()

            // 1. FORZAR ESTADO INICIAL LIMPIO
            binding.etReclutador.apply {
                setText("", false)
                clearFocus()
                // Lo deshabilitamos momentáneamente para que no den click
                // mientras el ProgressBar está cargando la lista
                isEnabled = false
                keyListener = null // Bloquea el teclado físico/virtual
            }

            // 2. CONFIGURAR EL CLICK
            binding.etReclutador.setOnClickListener {
                (it as? AutoCompleteTextView)?.showDropDown()
            }

            // 3. LIMPIAR ERROR AL ESCRIBIR/SELECCIONAR
            // Si antes marcaste error por no seleccionar nada, esto lo quita al elegir
            binding.etReclutador.setOnItemClickListener { _, _, _, _ ->
                binding.tilSearch.error = null
            }

            // 4. DISPARAR CARGA DE DATOS
            viewModel.initData(requireContext())

            // 5. BOTÓN DE BÚSQUEDA
            binding.btnSearch.setOnClickListener {
                val seleccion = binding.etReclutador.text.toString().trim()
                if (seleccion.isNotEmpty()) {
                    binding.tilSearch.error = null // Limpiamos error si existía
                    viewModel.fetchCandidates(seleccion, requireContext())
                } else {
                    binding.tilSearch.error = "Selecciona un reclutador"
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PRUEBA_MAESTRA", "Error en el setup: ${e.message}")
        }
    }

    // 2. Nueva función para mostrar el Modal (BottomSheet)
    private fun showCandidateDetail(item: CandidateRemote) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_candidate_detail, null)

        // 1. Vinculación de Vistas (Asegúrate de que estos IDs existan en tu XML)
        val ivFoto = view.findViewById<ImageView>(R.id.ivDetalleFoto)
        val tvStatusPago = view.findViewById<TextView>(R.id.tvDetalleStatusPago)
        val tvNombre = view.findViewById<TextView>(R.id.tvDetalleNombre)
        val tvCurp = view.findViewById<TextView>(R.id.tvDetalleCurp)
        val tvRfc = view.findViewById<TextView>(R.id.tvDetalleRfc)
        val tvNss = view.findViewById<TextView>(R.id.tvDetalleNss)
        val tvBanco = view.findViewById<TextView>(R.id.tvDetalleBanco)
        val tvActa = view.findViewById<TextView>(R.id.tvDetalleActa) // ID nuevo en XML
        val tvSf = view.findViewById<TextView>(R.id.tvDetalleSf)     // ID nuevo en XML
        val btnClose = view.findViewById<Button>(R.id.btnClose)

        // 2. Función de utilidad para pintar Estatus (Verde/Rojo)
        fun configurarDocumento(textView: TextView, valor: String?) {
            if (valor?.trim()?.uppercase() == "SI") {
                textView.text = "ENTREGADO"
                textView.setTextColor(android.graphics.Color.parseColor("#2E7D32")) // Verde Esmeralda
            } else {
                textView.text = "FALTANTE"
                textView.setTextColor(android.graphics.Color.parseColor("#C62828")) // Rojo Alerta
            }
        }

        // 3. Llenado de Datos Generales
        tvNombre.text = item.vNomcandidato ?: "SIN NOMBRE"

        // 💰 Estatus de Pago
        if (item.cPagado == "1") {
            tvStatusPago.text = "ESTADO: LIQUIDADO"
            tvStatusPago.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        } else {
            tvStatusPago.text = "ESTADO: PAGO PENDIENTE"
            tvStatusPago.setTextColor(android.graphics.Color.parseColor("#F44336"))
        }

        // 📝 Aplicar lógica de documentos a todos los campos nuevos
        configurarDocumento(tvCurp, item.cCurp)
        configurarDocumento(tvRfc, item.cRfc)
        configurarDocumento(tvNss, item.cNss)
        configurarDocumento(tvBanco, item.cBanco)
        configurarDocumento(tvActa, item.cActa)
        configurarDocumento(tvSf, item.cSf)

        // 🖼️ Cargar Foto con Glide de forma segura y con URL dinámica
        if (!item.vInedoc.isNullOrEmpty()) {

            // 1. Definimos la IP (puedes usar la constante que definimos en el Adapter o ponerla aquí)
            val BASE_URL_FOTOS = "http://192.168.50.120:5011/"
            val PROD_URL_FOTOS = "http://54.165.41.23:5053/"

            // 2. Construimos la URL: si no empieza con http, le pegamos la IP
            val urlBase = if (item.vInedoc.startsWith("http")) "" else PROD_URL_FOTOS

            // 3. Reemplazamos espacios por %20 para que Glide no falle
            val urlFinal = (urlBase + item.vInedoc).replace(" ", "%20")

            com.bumptech.glide.Glide.with(requireContext())
                .load(urlFinal)
                .placeholder(android.R.drawable.ic_menu_gallery) // Ocupa uno del sistema mientras carga
                .error(android.R.drawable.stat_notify_error)    // Si la URL falla muestra error
                .into(ivFoto)
        } else {
            // Si no hay ruta, ponemos una imagen por defecto
            ivFoto.setImageResource(android.R.drawable.ic_menu_gallery)
        }
        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.setContentView(view)
        dialog.show()
    }
    private fun setupRecyclerView() {
        binding.rvCandidates.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCandidates.adapter = adapter
    }

    private fun setupObservers() {
        // Dentro de setupObservers
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reclutadores.collect { lista ->
                if (lista.isNotEmpty()) {
                    val displayList = lista.map { "${it.cCodigoOrg} - ${it.vNombreOrg}" }
                    val autoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, displayList)

                    binding.etReclutador.apply {
                        setAdapter(autoAdapter)

                        // 🚀 ESTO ES LO QUE TE FALTA:
                        // Al poner el adapter, Android selecciona el primero por defecto.
                        // Con esto le decimos: "Ponlo vacío y NO filtres nada".
                        setText("", false)
                    }
                }
            }
        }

        /*viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reclutadorNombre.collect { nombre ->
                if (nombre.isNotEmpty()) {
                    binding.etReclutador.setText(nombre, false)
                }
            }
        }*/

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.candidates.collect { lista ->
                adapter.updateList(lista)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { cargando ->
                binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { mensaje ->
                mensaje?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
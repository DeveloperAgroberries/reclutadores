package com.agroberriesmx.reclutadores.ui.menu

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController // 💡 Importación para la navegación
import com.agroberriesmx.reclutadores.R // Asegúrate de que este import exista
import com.agroberriesmx.reclutadores.databinding.FragmentMenuBinding
import com.google.android.material.textfield.TextInputEditText
import androidx.navigation.NavOptions // Añadir esta importación
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.fragment.app.viewModels

@AndroidEntryPoint // 👈 ¡ESTO ES VITAL PARA HILT!
class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    // Inyectamos el ViewModel
    private val viewModel: MenuViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View { // Cambié View? a View
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Empezar a observar la lista
        setupObservers()

        // 2. Cargar datos (puedes pasar el token de las SharedPreferences)
        val token = "" // O el token que tengas guardado
        viewModel.fetchReclutadores(token)

        val prefs = requireActivity().getSharedPreferences("RecruiterPrefs", Context.MODE_PRIVATE)
        val isActive = prefs.getBoolean("IS_RECRUITER_SESSION_ACTIVE", false)

        if (isActive) {
            // 1. Leer los datos guardados
            val nReclutador = prefs.getString("nReclutador", null)
            val lOrigen = prefs.getString("lOrigen", null)
            val nPersonas = prefs.getString("nPersonas", null)

            // 2. Volver a crear el Bundle con los datos guardados
            val bundle = Bundle().apply {
                putString("reclutadorName", nReclutador)
                putString("lugarOrigen", lOrigen)
                putString("numPersonas", nPersonas)
            }

            // 3. Navegar con el Bundle
            if (nReclutador != null && lOrigen != null) {
                // Usamos un try/catch en caso de que la navegación falle por otro motivo
                try {
                    // Navegamos al registro de candidatos con el Bundle
                    findNavController().navigate(R.id.registerCandidatesFragment, bundle)

                } catch (e: Exception) {
                    // Si falla la navegación, desactivamos la bandera para forzar el inicio limpio
                    prefs.edit().putBoolean("IS_RECRUITER_SESSION_ACTIVE", false).apply()
                    Toast.makeText(context, "Error al continuar sesión, por favor, inicie de nuevo.", Toast.LENGTH_LONG).show()
                    initListeners()
                }
            } else {
                // Si los datos guardados son nulos (falló el guardado), desactivamos la bandera
                prefs.edit().putBoolean("IS_RECRUITER_SESSION_ACTIVE", false).apply()
                initListeners()
            }

        } else {
            // La sesión no está activa, mostramos el formulario normalmente
            initListeners()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reclutadores.collect { lista ->
                    // 🔍 LOG DE CONTROL
                    android.util.Log.d("MENU_DEBUG", "Tamaño de lista recibida: ${lista.size}")

                    if (lista.isNotEmpty()) {
                        val displayList = lista.map { "${it.cCodigoOrg} - ${it.vNombreOrg}" }
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, displayList)

                        (binding.etReclutador as? AutoCompleteTextView)?.apply {
                            setAdapter(adapter)
                            threshold = 0 // Para que muestre todo desde el inicio
                        }
                    } else {
                        android.util.Log.w("MENU_DEBUG", "La lista llegó VACÍA del ViewModel")
                    }
                }
            }
        }
    }

    private fun initListeners() {
        binding.btnAction.setOnClickListener {
            // 2. Al dar clic, validar el formulario
            if (isFormValid()) {
                // 3. Si es válido, navegar
                navigateToRegisterCandidates()
            }
        }
    }

    private fun isFormValid(): Boolean {
        var isValid = true

        // Limpiar errores primero
        binding.tilReclutador.error = null
        binding.tilLugar.error = null
        binding.tilPersonas.error = null

        if (binding.etReclutador.text.isNullOrBlank()) {
            binding.tilReclutador.error = "Seleccione un reclutador"
            isValid = false
        }
        if (binding.etLugar.text.isNullOrBlank()) {
            binding.tilLugar.error = "Este campo es obligatorio"
            isValid = false
        }
        // Validación de campo obligatorio
        if (binding.etPersonas.text.isNullOrBlank()) {
            binding.tilPersonas.error = "Este campo es obligatorio"
            isValid = false
        } else {
            // Validación de valor mayor a cero
            val numPersonas = binding.etPersonas.text.toString().toIntOrNull() ?: 0
            if (numPersonas <= 0) {
                binding.tilPersonas.error = "El número debe ser mayor a 0"
                isValid = false
            }
        }

        return isValid
    }

    // Función auxiliar para obtener el TextInputLayout padre
    private fun getTextInputLayout(editText: TextInputEditText) = when (editText.id) {
        binding.etReclutador.id -> binding.tilReclutador
        binding.etLugar.id -> binding.tilLugar
        binding.etPersonas.id -> binding.tilPersonas
        else -> null
    }

    private fun navigateToRegisterCandidates() {

        // 1. Obtener los valores (ya validados)
        val reclutadorName = binding.etReclutador.text.toString().trim()
        val lugarOrigen = binding.etLugar.text.toString().trim()
        val numPersonas = binding.etPersonas.text.toString().trim()
        // 2. Crear la acción de navegación con los argumentos
        /*try {
            // Navegación directa al ID del fragmento de destino
            // DEBES CONFIRMAR QUE R.id.registerCandidatesFragment ES EL ID CORRECTO
            findNavController().navigate(R.id.registerCandidatesFragment)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al navegar: Verifique el ID en el NavGraph.", Toast.LENGTH_LONG).show()
        }*/
        // 2. Crear la acción de navegación con los argumentos usando Safe Args.
        // ⭐⭐ SOLUCIÓN SENCILLA: CREAR UN BUNDLE DE ARGUMENTOS ⭐⭐
        val bundle = Bundle().apply {
            putString("reclutadorName", reclutadorName)
            putString("lugarOrigen", lugarOrigen)
            putString("numPersonas", numPersonas)
        }

        val prefs = requireActivity().getSharedPreferences("RecruiterPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            // ⭐⭐ GUARDAMOS TAMBIÉN LOS DATOS DEL RECLUTADOR ⭐⭐
            putBoolean("IS_RECRUITER_SESSION_ACTIVE", true)
            putString("nReclutador", reclutadorName)
            putString("lOrigen", lugarOrigen)
            putString("nPersonas", numPersonas)
            apply()
        }

        try {
            // 2. Crear los NavOptions para modificar la pila de navegación
            val navOptions = NavOptions.Builder()
                // ⭐⭐ ESTA LÍNEA ES CLAVE: ELIMINA menuFragment2 DE LA PILA ⭐⭐
                .setPopUpTo(R.id.menuFragment2, true)
                .build()

            // 3. Navegar al registro usando el Bundle y las NavOptions
            findNavController().navigate(R.id.registerCandidatesFragment, bundle, navOptions)

        } catch (e: Exception) {
            Toast.makeText(context, "Error al navegar: Verifique el ID en el NavGraph.", Toast.LENGTH_LONG).show()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
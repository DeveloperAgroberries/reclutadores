package com.agroberriesmx.reclutadores.ui.synchronize

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agroberriesmx.reclutadores.databinding.FragmentSynchronizeBinding
// ⭐ IMPORTACIONES NECESARIAS ⭐
import android.util.Log // Para mensajes de depuración
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.agroberriesmx.reclutadores.data.local.DatabaseHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SynchronizeFragment : Fragment() {

    private val syncViewModel by viewModels<SynchronizeViewModel>()

    private var _binding: FragmentSynchronizeBinding? = null

    private val binding get() = _binding!!
    private lateinit var sessionPrefs: SharedPreferences

    companion object{
        private const val SESSION_PREFERENCES_KEY = "session_prefs"
        private const val PRIVATE_ACCESS_TOKEN_KEY = "access_token"
    }

    // ⭐ 1. Declarar el DBHelper ⭐
    @Inject
    lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSynchronizeBinding.inflate(inflater, container, false)

        // ⭐ 2. Inicializar el DBHelper ⭐
        dbHelper = DatabaseHelper(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ⭐ 3. Llamar a la función que cargará el conteo ⭐
        updatePendingCount()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            observeViewModel()
            initListeners()
        }
    }

    private fun observeViewModel() {
        syncViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SynchronizeState.Loading -> {
                    binding.pbs.visibility = View.VISIBLE
                }

                is SynchronizeState.UploadSuccess -> {
                    binding.pbs.visibility = View.GONE
                    Toast.makeText(context, "${state.message}", Toast.LENGTH_LONG).show()
                    syncViewModel.clearState()
                }

                is SynchronizeState.Error -> {
                    binding.pbs.visibility = View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    syncViewModel.clearState()
                }

                else -> {
                    binding.pbs.visibility = View.GONE
                }
            }
        }

        syncViewModel.pendingRecords.observe(viewLifecycleOwner) { records ->
            val pendingCount = records.size
            binding.tvPending.text = if (pendingCount > 0) {
                pendingCount.toString()
            } else {
                "0"
            }
        }

        //RICARDO DIMAS
        syncViewModel.pendingRecords.observe(viewLifecycleOwner) { records ->
            val pendingCount = records.size
            binding.tvPending.text = if (pendingCount > 0) {
                pendingCount.toString()
            } else {
                "0"
            }
        }
    }

    // ⭐ 4. Función que obtiene el conteo de la DB y actualiza la UI ⭐
    private fun updatePendingCount() {
        // Obtenemos la lista de candidatos que tienen isSynced = 0
        val pendingList = dbHelper.getUnsyncedCandidatos()
        val count = pendingList.size

        // Actualizar el TextView 'tvPending'
        binding.tvPending.text = count.toString()

        Log.d("SyncFragment", "Total de registros pendientes: $count")

        // Opcional: Deshabilitar el CardView si no hay datos.
        binding.cvUpload.isEnabled = count > 0
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListeners() {
        //Ricardo Dimas - Rondines 23/06/2025
        binding.cvUpload.setOnClickListener {
            binding.cvUpload.isEnabled = false
            uploadData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadData() {
        lifecycleScope.launch {
            try {
                val response = getToken()
                if(response != null){
                    syncViewModel.upload()
                } else {
                    Toast.makeText(context, "No puedes enviar tus datos, cierra sesion y vuelve a intentarlo", Toast.LENGTH_LONG).show()
                }
            } finally {
                binding.cvUpload.isEnabled = true
            }
        }
    }

    private fun getToken(): String? {
        sessionPrefs = requireActivity().getSharedPreferences(
            SESSION_PREFERENCES_KEY, MODE_PRIVATE
        )
        return sessionPrefs.getString(PRIVATE_ACCESS_TOKEN_KEY, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
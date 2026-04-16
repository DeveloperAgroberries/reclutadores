package com.agroberriesmx.reclutadores.ui.registerCandidate

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope // ⭐ Importación necesaria
import androidx.navigation.NavOptions
import com.agroberriesmx.reclutadores.databinding.FragmentRegisterCandidatesBinding
import com.agroberriesmx.reclutadores.utils.ImageFileManager // ⭐ Asegúrate que esta ruta y clase existan
import kotlinx.coroutines.Dispatchers // ⭐ Importación necesaria
import kotlinx.coroutines.launch // ⭐ Importación necesaria
import kotlinx.coroutines.withContext // ⭐ Importación necesaria
import java.io.File
// ⭐⭐ IMPORTACIONES REQUERIDAS PARA NAVEGACIÓN Y RECURSOS ⭐⭐
import com.agroberriesmx.reclutadores.R // 1. Para R.id.menuFragment2
import androidx.navigation.fragment.findNavController // 2. Para findNavController()
import com.agroberriesmx.reclutadores.data.local.DatabaseHelper
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import java.util.Calendar
import java.util.TimeZone
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager

@AndroidEntryPoint
class RegisterCandidatesFragment : Fragment() {

    private var _binding: FragmentRegisterCandidatesBinding? = null
    private val binding get() = _binding!!

    // ... (Variables de clase) ...
    private lateinit var nReclutador: String
    private lateinit var lOrigen: String
    private lateinit var nPersonas: String
    private var imagePathForDb: String? = null
    private var latestImageUri: Uri? = null

    @Inject
    lateinit var dbHelper: DatabaseHelper

    //contador candidatos registrados
    private var registeredCount: Int = 0

    private fun updateCountDisplay() {
        binding.tvCandidatesCount.text =
            "Candidatos Registrados: $registeredCount / $nPersonas"
    }

    // 1... (cameraLauncher y galleryLauncher) ...
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            latestImageUri?.let { uri ->
                startCropActivity(uri) // ⭐⭐⭐ CAMBIO AQUÍ ⭐⭐⭐
            }
        } else {
            Toast.makeText(context, "Captura cancelada.", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. RegisterCandidatesFragment.kt
    private fun startCropActivity(uri: Uri) {
        val cropIntent = Intent("com.android.camera.action.CROP").apply {
            // Se establece la URI de la imagen recién tomada
            setDataAndType(uri, "image/*")

            // Se otorgan permisos temporales al proveedor de archivos
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            // Propiedades de recorte
            putExtra("crop", "true")
            putExtra("aspectX", 3) // Ratio 3:2 (típico de documentos)
            putExtra("aspectY", 2)
            putExtra("outputX", 600) // Tamaño de salida
            putExtra("outputY", 400)

            // Indica dónde debe guardarse la imagen recortada (usamos la misma URI temporal)
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
            putExtra("return-data", false) // No devolver un Bitmap gigante, sino la URI
            putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        }

        try {
            cropLauncher.launch(cropIntent)
        } catch (e: Exception) {
            // Si el dispositivo no tiene una aplicación de recorte, volvemos al flujo original.
            Toast.makeText(context, "Error: No se encontró herramienta de recorte. Usando la imagen completa.", Toast.LENGTH_LONG).show()
            handleImageUri(uri)
        }
    }

    // 3. Lanzador para RECORTAR la imagen
    private val cropLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // La URI de la imagen recortada se recibe en los datos del intent
            val croppedUri: Uri? = result.data?.data
            croppedUri?.let { uri ->
                handleImageUri(uri) // Usamos la URI recortada para guardarla
            }
        } else {
            Toast.makeText(context, "Recorte cancelado.", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            handleImageUri(it)
        } ?: Toast.makeText(context, "Selección cancelada.", Toast.LENGTH_SHORT).show()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterCandidatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { args ->
            nReclutador = args.getString("reclutadorName", "")
            lOrigen = args.getString("lugarOrigen", "")
            nPersonas = args.getString("numPersonas", "")
        }

        Log.d("Registro", "Datos recibidos: $nReclutador, $lOrigen, $nPersonas")

        // ⭐⭐⭐ CAMBIO CLAVE: INICIALIZAR registeredCount DESDE LA DB ⭐⭐⭐
        // Usamos el dbHelper para obtener el conteo de registros existentes para esta sesión.
        registeredCount = dbHelper.getRegisteredCountForSession(nReclutador, lOrigen)

        // Aseguramos que el contador no exceda la meta, aunque no debería pasar.
        val requiredCount = nPersonas.toIntOrNull() ?: 0
        if (registeredCount > requiredCount) {
            registeredCount = requiredCount
        }

        // ⭐⭐ MOSTRAR EL CONTEO INICIAL ⭐⭐
        updateCountDisplay()
        // ---------------------------------

        initListeners() // ⭐ Asegúrate de llamar a los listeners aquí
    }

    private fun initListeners() {
        binding.btnUploadPhoto.setOnClickListener {
            showImageSourceDialog()
        }
        binding.btnRegisterCandidate.setOnClickListener {
            registerCandidate()
        }
        // ⭐⭐ NUEVO LISTENER ⭐⭐
        binding.btnFinishRecords.setOnClickListener {
            // 1. Obtener los valores (Asegúrate que nPersonas es un String, lo convertiremos a Int)
            val requiredCount = nPersonas.toIntOrNull() ?: 0 // Si falla la conversión, asumimos 0

            // 2. ⭐⭐⭐ REALIZAR LA VALIDACIÓN ⭐⭐⭐
            if (registeredCount < requiredCount) {
                // Validación fallida: Faltan registros
                val missingCount = requiredCount - registeredCount
                Toast.makeText(
                    context,
                    "⚠️ Aún faltan $missingCount candidatos por registrar (Objetivo: $requiredCount).",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener // Detiene el proceso y no finaliza la sesión
            }

            // 3. Validación Exitosa: Los conteos coinciden
            Toast.makeText(context, "¡Objetivo de $requiredCount candidatos alcanzado! Finalizando sesión.", Toast.LENGTH_SHORT).show()

            // 4. Desactivar la Bandera de Sesión
            val prefs = requireActivity().getSharedPreferences("RecruiterPrefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("IS_RECRUITER_SESSION_ACTIVE", false).apply()

            // 5. Navegar de vuelta al menú (o al inicio)
            //findNavController().popBackStack(R.id.menuFragment2, true)
            try {
                // Usamos navigate() con NavOptions para ir al destino de inicio
                // y eliminar todos los fragmentos hasta ese punto (incluyendo el MenuFragment y este).

                val navOptions = NavOptions.Builder()
                    // ⭐ Reemplaza R.id.id_de_tu_fragmento_de_inicio por el ID real de tu pantalla Home ⭐
                    // (Ejemplo: R.id.homeFragment o R.id.mainMenuFragment)
                    .setPopUpTo(R.id.menuFragment2, true)
                    .build()

                // Navegamos al fragmento de inicio, eliminando la pila de la sesión.
                findNavController().navigate(R.id.menuFragment2, null, navOptions)

            } catch (e: Exception) {
                // Fallback: Si la navegación por ID directo falla, intentamos una limpieza de pila más genérica.
                Log.e("NavError", "Error al navegar al inicio: ${e.message}")
                Toast.makeText(context, "Error en navegación final. Reiniciando.", Toast.LENGTH_LONG).show()

                // Asumiendo que R.id.menuFragment2 es la raíz temporal, volvemos allí y confiamos en el MenuFragment para manejar la bandera 'false'.
                findNavController().popBackStack(R.id.menuFragment2, false)
            }
        }
    }

    private fun showImageSourceDialog() {
        //val options = arrayOf("Tomar Foto (Cámara)", "Seleccionar de Galería")
        val options = arrayOf("Tomar Foto (Cámara)")
        AlertDialog.Builder(requireContext())
            .setTitle("Documento INE")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> takePhoto()
                    //1 -> galleryLauncher.launch("image/*")
                }
                dialog.dismiss()
            }
            .show()
    }

    // RegisterCandidatesFragment.kt
    // ⭐ NUEVO: Lanzador para solicitar el permiso de cámara
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Si el usuario aceptó el permiso ahora, ejecutamos la lógica original de la foto
            proceedToTakePhoto()
        } else {
            Toast.makeText(requireContext(), "Se requiere permiso de cámara para tomar la foto del INE", Toast.LENGTH_LONG).show()
        }
    }

    // ⭐ MODIFICADO: Esta es la función que llamas desde el diálogo
    private fun takePhoto() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            // Ya tenemos permiso, vamos directo a la cámara
            proceedToTakePhoto()
        } else {
            // No tenemos permiso, lo solicitamos al usuario
            requestCameraPermissionLauncher.launch(permission)
        }
    }

    // ⭐ NUEVA: Aquí movimos tu lógica original de creación de archivos
    private fun proceedToTakePhoto() {
        val context = requireContext()
        val timeStamp = System.currentTimeMillis()
        val tempFileName = "temp_ine_photo_$timeStamp.jpg"
        val file = File(context.filesDir, tempFileName)

        // Generamos la nueva URI
        latestImageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // Lanzar la cámara
        latestImageUri?.let {
            cameraLauncher.launch(it)
        }
    }

    private fun handleImageUri(uri: Uri) {

        // ⭐⭐ CORRECCIÓN: Usar el ID real de tu XML (ivCandidatePhoto) ⭐⭐
        binding.ivCandidatePhoto.setImageURI(uri)

        // LÓGICA DE ALMACENAMIENTO DE ARCHIVOS
        val imageFileManager = ImageFileManager(requireContext())
        val fileBaseName = nReclutador

        // Ejecutar en una coroutine porque implica operaciones de I/O (disco)
        lifecycleScope.launch(Dispatchers.IO) {
            val path = imageFileManager.saveImageToInternalStorage(uri, fileBaseName)

            withContext(Dispatchers.Main) {
                if (path != null) {
                    imagePathForDb = path
                    Toast.makeText(context, "✅ Foto INE lista. Ruta: $path", Toast.LENGTH_LONG).show()
                    binding.root.requestFocus()
                    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                    imm?.hideSoftInputFromWindow(binding.etCandidateName.windowToken, 0)
                } else {
                    Toast.makeText(context, "Error al guardar la foto.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ... (registerCandidate y onDestroyView) ...

    private fun registerCandidate() {
        // 1. Validar la ruta de la foto
        val finalImagePath = imagePathForDb

        if (finalImagePath.isNullOrEmpty()) {
            Toast.makeText(context, "Debe tomar o seleccionar la foto del INE.", Toast.LENGTH_LONG).show()
            return // Detiene el proceso si no hay foto
        }

        // 2. Obtener datos del formulario y CheckBoxes
        val candidateName = binding.etCandidateName.text.toString().trim()
        if (candidateName.isEmpty()) {
            Toast.makeText(context, "El nombre del candidato es obligatorio.", Toast.LENGTH_LONG).show()
            return
        }

        // Mapeo de CheckBoxes a "SI" o "NO" (Asegúrate de usar los IDs correctos de tu XML)
        val dCurp = if (binding.cbCurp.isChecked) "SI" else "NO"
        val dRFC = if (binding.cbRfc.isChecked) "SI" else "NO"
        val dActa = if (binding.cbActa.isChecked) "SI" else "NO"
        val dNSS = if (binding.cbNss.isChecked) "SI" else "NO"

        // ⭐⭐ AQUÍ ASUMO LOS NOMBRES DE TUS CHECKBOXES para cBanco y cSF ⭐⭐
        // Si tus IDs son cbCbanco y cbCfiscal, úsalos:
        val cBanco = if (binding.cbCbanco.isChecked) "SI" else "NO"
        val cSF = if (binding.cbCfiscal.isChecked) "SI" else "NO"

        // 1. Obtener la hora actual
        val mexicoTimeZone = TimeZone.getTimeZone("America/Mexico_City")
        val calendar = Calendar.getInstance(mexicoTimeZone)
        // 1.1. Obtener la hora actual usando la zona horaria de México
        val localTimeInMexico = calendar.time
        // 2. Definir el formateador de salida con el patrón exacto, pero SIN la 'Z'
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        // 3. CRÍTICO: Forzar al formateador a usar la hora de MÉXICO para que imprima 14:29.
        // NO USAR UTC.
        isoFormatter.timeZone = mexicoTimeZone
        // 4. Formatear la hora
        val dFechaRegistro = isoFormatter.format(localTimeInMexico)
        // ---------------------------------------------------------------------

        // 3. ⭐⭐ LLAMADA AL DBHelper PARA INSERTAR ⭐⭐
        // La función insertCandidato devuelve el ID del nuevo registro (Long) o -1 si falla.
        val insertionId = dbHelper.insertCandidato(
            nReclutador = nReclutador,
            lOrigen = lOrigen,
            nCandidato = candidateName,
            ineDocPath = finalImagePath,
            dCurp = dCurp,
            dRFC = dRFC,
            dActa = dActa,
            dNSS = dNSS,
            cBanco = cBanco,
            cSF = cSF,
            dFechaRegistro = dFechaRegistro  + "Z"
        )

        // 4. Procesar el resultado de la inserción
        if (insertionId > 0) {
            // Inserción exitosa

            // 5. Aumentar el contador
            registeredCount++

            // 6. Actualizar la UI
            updateCountDisplay()

            // 7. Mostrar mensaje de éxito
            Toast.makeText(context, "Candidato registrado (ID: $insertionId). Listo para el siguiente.", Toast.LENGTH_SHORT).show()

            // 8. Limpiar el formulario para el siguiente candidato
            clearCandidateForm()
        } else {
            // Inserción fallida
            Toast.makeText(context, "❌ Error al guardar el candidato en la base de datos.", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearCandidateForm() {
        // 1. Limpiar campos de texto (Nombre del Candidato)
        binding.etCandidateName.setText("")

        // 2. Limpiar CheckBoxes (Documentación)
        binding.cbCurp.isChecked = false
        binding.cbRfc.isChecked = false
        binding.cbActa.isChecked = false
        binding.cbNss.isChecked = false
        binding.cbCbanco.isChecked = false
        binding.cbCfiscal.isChecked = false


        // 3. Limpiar la previsualización de la foto y las variables de ruta
        binding.ivCandidatePhoto.setImageURI(null) // Esto borra la imagen de la vista
        imagePathForDb = null // Crucial: Elimina la ruta de la base de datos para el siguiente candidato
        latestImageUri = null // Opcional: Limpia la Uri temporal

        // 4. Mover el foco al primer campo de texto (opcional)
        binding.etCandidateName.requestFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
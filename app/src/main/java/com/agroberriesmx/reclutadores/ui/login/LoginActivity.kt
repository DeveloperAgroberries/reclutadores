package com.agroberriesmx.reclutadores.ui.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.agroberriesmx.reclutadores.R
import com.agroberriesmx.reclutadores.databinding.ActivityLoginBinding
import com.agroberriesmx.reclutadores.ui.home.MainActivity
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject // Necesario si usas la inyección en constructor (aunque no en Activity)

// 1. ANOTACIÓN CRÍTICA: Permite la inyección de Hilt en esta Activity
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    // 2. INYECTAR ViewModel: Usamos viewModels() para obtener el ViewModel inyectado por Hilt
    private val loginViewModel: LoginViewModel by viewModels()

    private lateinit var binding: ActivityLoginBinding
    private var currentUser: String? = null

    // 3. Constantes para SharedPreferences, necesarias para guardar token y usuario.
    companion object {
        private const val SESSION_PREFERENCES_KEY = "session_prefs"
        private const val PRIVATE_ACCESS_TOKEN_KEY = "access_token"
        private lateinit var sessionPrefs: SharedPreferences
        private const val REMIND_USERNAME_KEY = "Username"
        private const val REMIND_PASSWORD_KEY = "Password"
        private const val LOGGED_IN_KEY = "logged_in" // Asegúrate de que esta clave exista aquí
        private const val PERSISTENT_PREFERENCES_KEY = "persistent_prefs"
        private lateinit var persistentPrefs: SharedPreferences


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- AGREGA ESTO PARA FORZAR EL MARGEN ---
        // Esto asegura que el sistema NO ignore las barras de estado y navegación
        WindowCompat.setDecorFitsSystemWindows(window, true)

        initUI()
        // 4. Iniciar la observación del ViewModel
        observeViewModel()

        Glide.with(this)
            .asGif()
            .load(R.drawable.login_agroberries) // También puedes usar una URL: "https://ejemplo.com/fondo.gif"
            .into(binding.gifBackground)
    }

    private fun initUI() {
        // Carga el estado inicial (ej. usuario guardado) si aplica
        initListeners()
        loadUserDataIfExists()
    }

    private fun initListeners() {
        binding.btnLogin.setOnClickListener {
            val user = binding.etUser.text.toString().uppercase().trim()
            val password = binding.etPassword.text.toString().trim()
            currentUser = user // Guarda el usuario en la variable de clase

            if (user != "" || password != "") {
                lifecycleScope.launch {
                    if(binding.cbReminder.isChecked){
                        remindUser(user,password)
                    }else{
                        clearUserData()
                    }
                    loginViewModel.login(user, password, "1", "")
                }
            } else {
                Toast.makeText(
                    this,
                    "El usuario o la contraseña estan vacios, vuelve a intentarlo, por favor.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun observeViewModel() {
        loginViewModel.state.observe(this, Observer { state ->
            when (state) {
                is LoginState.Waiting -> {
                    binding.pb.visibility = View.GONE
                }

                is LoginState.Loading -> {
                    binding.pb.visibility = View.VISIBLE
                    binding.etUser.isEnabled = false
                    binding.etPassword.isEnabled = false
                    binding.btnLogin.isEnabled = false
                }

                is LoginState.Success -> {
                    binding.pb.visibility = View.GONE
                    binding.etUser.isEnabled = true // Habilitar campos y botón al finalizar carga
                    binding.etPassword.isEnabled = true
                    binding.btnLogin.isEnabled = true

                    if(state.isLocal){
                        Toast.makeText(applicationContext, "Inicio de sesión: ¡Acceso offline!", Toast.LENGTH_LONG).show()
                        saveUserCode(binding.etUser.text.toString().trim())
                        navigateToMainActivity()
                    }else{
                        val token = state.success?.token // Suponiendo que 'state' tiene un campo 'token'

                        if (token != null) {
                            Toast.makeText(applicationContext, "Inicio de sesión: ¡Acceso online!", Toast.LENGTH_LONG).show()
                            saveToken(token) // Guarda el token del servidor
                            saveUserCode(binding.etUser.text.toString().trim())
                            lifecycleScope.launch {
                                try {
                                    //synchronizeCatalogs() // Intenta sincronizar porque hay conexión
                                    navigateToMainActivity()
                                } catch (e: java.lang.Exception) {
                                    Toast.makeText(applicationContext, "Error en la sincronización: ${e.message}", Toast.LENGTH_LONG).show()
                                    // A pesar del error de sincronización, el usuario ya inició sesión
                                    navigateToMainActivity()
                                }
                            }
                        } else {
                            // Esto no debería ocurrir si el ViewModel maneja correctamente 'isLocal'
                            Toast.makeText(applicationContext, "Error: Token nulo en acceso online exitoso.", Toast.LENGTH_LONG).show()
                            // Considera qué hacer aquí: ¿volver a login, mostrar error crítico?
                            navigateToMainActivity() // Por ahora, navega para no bloquear al usuario
                        }
                        // Asegúrate de que el usuario no sea nulo antes de guardar
                        /*currentUser?.let { userCode ->
                            saveUserCode(userCode)
                        }

                        saveToken(token)
                        lifecycleScope.launch {
                            synchronizeCatalogs()
                        }
                        navigateToMainActivity()*/
                    }
                }

                is LoginState.Error -> {
                    binding.pb.visibility = View.GONE
                    binding.etUser.isEnabled = true
                    binding.etPassword.isEnabled = true
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "${state.message}", Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        })

        loginViewModel.state.observe(this, Observer { state ->
            when (state) {
                is LoginState.Waiting -> {
                    binding.pb.visibility = View.GONE
                }

                is LoginState.Loading -> {
                    binding.pb.visibility = View.VISIBLE
                    binding.etUser.isEnabled = false
                    binding.etPassword.isEnabled = false
                    binding.btnLogin.isEnabled = false
                }

                /*is LoginState.Success -> {
                    binding.pb.visibility = View.GONE
                    val token = state.success.token // Suponiendo que 'state' tiene un campo 'token'
                    saveToken(token)
                    lifecycleScope.launch {
                        synchronizeCatalogs()
                    }
                    navigateToMainActivity()
                }*/

                is LoginState.Error -> {
                    binding.pb.visibility = View.GONE
                    binding.etUser.isEnabled = true
                    binding.etPassword.isEnabled = true
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "${state.message}", Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        })
    }

    private fun navigateToMainActivity() {
        sessionPrefs = getSharedPreferences(SESSION_PREFERENCES_KEY, MODE_PRIVATE)

        with(sessionPrefs.edit()) {
            putBoolean(LOGGED_IN_KEY, true)
            apply()
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.etUser.isEnabled = enabled
        binding.etPassword.isEnabled = enabled
        binding.btnLogin.isEnabled = enabled
        binding.cbReminder.isEnabled = enabled
    }

    // Funciones auxiliares para manejo de datos persistentes (SharedPreferences)
    private fun saveToken(token: String) {
        getSharedPreferences(SESSION_PREFERENCES_KEY, Context.MODE_PRIVATE)
            .edit()
            .putString(PRIVATE_ACCESS_TOKEN_KEY, token)
            .apply()
    }

    private fun remindUser(usr: String, pwd: String) {
        persistentPrefs = getSharedPreferences(PERSISTENT_PREFERENCES_KEY, MODE_PRIVATE)
        with(persistentPrefs.edit()){
            putString(REMIND_USERNAME_KEY, usr)
            putString(REMIND_PASSWORD_KEY, pwd)
            apply()
        }
    }

    private fun loadUserDataIfExists() {
        persistentPrefs = getSharedPreferences(PERSISTENT_PREFERENCES_KEY, MODE_PRIVATE)
        val savedUsername = persistentPrefs.getString(REMIND_USERNAME_KEY, null)
        val savedPassword = persistentPrefs.getString(REMIND_PASSWORD_KEY, null)

        // Si se han guardado los datos, colócalos en los campos de texto correspondientes
        if (savedUsername != null && savedPassword != null) {
            binding.etUser.setText(savedUsername)
            binding.etPassword.setText(savedPassword)
            binding.cbReminder.isChecked = true // Marca el checkbox si se encontraron datos
        }
    }

    private fun clearUserData() {
        persistentPrefs = getSharedPreferences(PERSISTENT_PREFERENCES_KEY, MODE_PRIVATE)
        with(persistentPrefs.edit()) {
            remove(REMIND_USERNAME_KEY)
            remove(REMIND_PASSWORD_KEY)
            apply()
        }
    }

    // MODIFICACIÓN: Haz esta función síncrona
    private fun setLoggedInState(isLoggedIn: Boolean) {
        getSharedPreferences(SESSION_PREFERENCES_KEY, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(LOGGED_IN_KEY, isLoggedIn)
            .commit() // ⬅️ CAMBIO: Usa .commit() para que sea SÍNCRONO y bloqueante
    }

    private fun saveUserCode(userCode: String) {
        sessionPrefs = getSharedPreferences(SESSION_PREFERENCES_KEY, MODE_PRIVATE)
        with(sessionPrefs.edit()) {
            putString("cCodigoUsu", userCode)
            apply()
        }
    }
}
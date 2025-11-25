package com.agroberriesmx.reclutadores.ui.home

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.agroberriesmx.reclutadores.R
import com.agroberriesmx.reclutadores.databinding.ActivityMainBinding
import com.agroberriesmx.reclutadores.ui.login.LoginActivity
import com.agroberriesmx.reclutadores.ui.base.BaseActivity
import com.agroberriesmx.reclutadores.ui.privacypolicy.PrivacyPolicyActivity
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log
import androidx.navigation.NavOptions
import androidx.navigation.ui.navigateUp

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var persistentPrefs: SharedPreferences
    private lateinit var sessionPrefs: SharedPreferences

    companion object {
        private const val PERSISTENT_PREFERENCES_KEY = "persistent_prefs"
        private const val SESSION_PREFERENCES_KEY = "session_prefs"
        private const val POLICIES_SHOWN_KEY = "policies_shown"
        private const val LOGGED_IN_KEY = "logged_in"
        // 💡 AÑADIR esta constante (debe ser la misma que en LoginActivity)
        private const val PRIVATE_ACCESS_TOKEN_KEY = "access_token"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    private fun initUI() {
        appRun()
        initListener()
        initNavigation()
    }

    private fun appRun() {
        persistentPrefs = getSharedPreferences(PERSISTENT_PREFERENCES_KEY, MODE_PRIVATE)
        sessionPrefs = getSharedPreferences(SESSION_PREFERENCES_KEY, MODE_PRIVATE)
        val policiesShown = persistentPrefs.getBoolean(POLICIES_SHOWN_KEY, false)

        // 🛑 CAMBIAR AQUÍ: Verificar el token en lugar del booleano LOGGED_IN_KEY
        val token = sessionPrefs.getString(PRIVATE_ACCESS_TOKEN_KEY, null)
        //ESTA ES LA VARIABLE O EL RESULTADO QUE NO DEJABA ENTRAR AL MODO OFFLINE
        //val isLoggedIn = !token.isNullOrEmpty()
        val isLoggedIn = sessionPrefs.getBoolean(LOGGED_IN_KEY, false) // El valor por defecto es 'false'

        //val loggedIn = sessionPrefs.getBoolean(LOGGED_IN_KEY, true)
        Log.d("MainActivityMessage", "Policies shown: $policiesShown, Logged in: $isLoggedIn")
        when {
            !policiesShown -> {
                val intent = Intent(this, PrivacyPolicyActivity::class.java)
                startActivity(intent)
                finish() // Cierra esta MainActivity para no dejarla en la pila.
            }
            !isLoggedIn -> {
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        // Navega al fragmento de login usando el NavController
        //navController.navigate(R.id.menuFragment2)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun initListener() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackFunction()
            }
        })
    }

    private fun initNavigation() {
        setSupportActionBar(binding.toolbar)

        val navHost =
            supportFragmentManager.findFragmentById(binding.fragmentMainView.id) as NavHostFragment
        navController = navHost.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.menuFragment2,
                R.id.candidatesFragment,
                R.id.registerCandidatesFragment,
                R.id.synchronizeFragment,
            ), binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navViewDrawer.setupWithNavController(navController)

        binding.navViewDrawer.setNavigationItemSelectedListener { menuItem ->
            // 1. Verificar si el destino es el menú de inicio
            if (menuItem.itemId == R.id.menuFragment2) {

                // 2. Definir NavOptions para asegurar que el fragmento se muestre
                val navOptions = NavOptions.Builder()
                    // Si ya estás en esta pantalla, evita crear otra instancia
                    .setLaunchSingleTop(true)
                    // Limpia todo el back stack hasta llegar a 'menuFragment2'
                    // (Esto asegura que el botón 'Atrás' no te saque de la app al hacer clic)
                    .setPopUpTo(R.id.menuFragment2, false) // false = no saca el destino actual
                    .build()

                // 3. Ejecutar la navegación con las opciones
                navController.navigate(R.id.menuFragment2, null, navOptions)

                // 4. Cerrar el drawer y confirmar el manejo
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true

            } else if (menuItem.itemId == R.id.logout) {
                showExitConfirmationData()
                true
            } else {
                // Para todos los demás destinos, usamos el comportamiento normal de NavigationUI
                val navigated = NavigationUI.onNavDestinationSelected(menuItem, navController)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                navigated
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun onBackFunction() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            showExitConfirmationData()
        }
    }

    private fun showExitConfirmationData() {
        AlertDialog.Builder(this)
            .setMessage("Quieres salir de la aplicacion?")
            .setCancelable(false)
            .setPositiveButton("Si") { dialog, _ ->
                dialog.dismiss()
                handleLogout()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun handleLogout() {
        sessionPrefs = getSharedPreferences(SESSION_PREFERENCES_KEY, MODE_PRIVATE)
        val editor = sessionPrefs.edit()
        editor.clear()
        editor.apply()

        navigateToLogin()
    }

    /*private fun initNavigation() {
        val navHost: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_main_view) as NavHostFragment
        navController = navHost.navController

        setSupportActionBar(binding.toolbar)

        // Asumimos que los destinos de nivel superior son el menú, la lista, sincronizar y about
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.menuFragment2,
                R.id.synchronizeFragment,
                R.id.candidatesFragment,
                R.id.registerCandidatesFragment
            ), binding.drawerLayout
        )

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        binding.navViewDrawer.setupWithNavController(navController)

        binding.navViewDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logout -> {
                    showExitConfirmationData()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                    true
                }
            }
        }
    }*/
}

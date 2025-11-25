package com.agroberriesmx.reclutadores.ui.base

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.agroberriesmx.reclutadores.ui.login.LoginActivity


open class BaseActivity : AppCompatActivity() {
    companion object {
        private const val PRIVATE_ACCESS_TOKEN_KEY = "access_token"
        private const val SESSION_PREFERENCES_KEY = "session_prefs"
        private const val LOGGED_IN_KEY = "logged_in"
    }

    private val timeoutInMillis: Long = 5 * 60 * 1000
    private val handler = Handler(Looper.getMainLooper())
    private val logoutRunnable = Runnable { navigateToLogin() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetTimeout()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetTimeout()
    }

    private fun resetTimeout() {
        handler.removeCallbacks(logoutRunnable)
        handler.postDelayed(logoutRunnable, timeoutInMillis)
    }

    private fun navigateToLogin() {
        val sessionPrefs = getSharedPreferences(SESSION_PREFERENCES_KEY, MODE_PRIVATE)
        with(sessionPrefs.edit()) {
            remove(PRIVATE_ACCESS_TOKEN_KEY)
            putBoolean(LOGGED_IN_KEY, false)
            apply()
        }

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        resetTimeout()
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(logoutRunnable)
    }
}
package com.dsy1105.therionlabsapp.datos

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Acceso local de demostración; para producción se necesita autenticación de servidor.
object AccesoAdmin {
    private const val PREFS = "configuracion_admin"

    suspend fun ingresar(context: Context, usuario: String, clave: String): Boolean = withContext(Dispatchers.Default) {
        if (usuario.trim() != "admin") return@withContext false
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val hash = prefs.getString("hash", null)
        val salt = prefs.getString("salt", null)
        if (hash == null || salt == null) clave == "admin123"
        else SeguridadClave.esCorrecta(clave, hash, salt)
    }

    suspend fun cambiarClave(context: Context, claveActual: String, nueva: String): Boolean = withContext(Dispatchers.Default) {
        if (!ingresar(context, "admin", claveActual)) return@withContext false
        val (hash, salt) = SeguridadClave.crear(nueva)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString("hash", hash).putString("salt", salt).apply()
        true
    }

    fun avisosActivos(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("avisos", true)
    }

    fun guardarAvisos(context: Context, activo: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("avisos", activo).apply()
    }
}

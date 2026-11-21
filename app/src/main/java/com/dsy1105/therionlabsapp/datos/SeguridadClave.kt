package com.dsy1105.therionlabsapp.datos

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// En Room guardamos un resultado protegido, no la contrasena original.
// Es un inicio de sesion local para la demostracion; no hay servidor de usuarios.
object SeguridadClave {
    private const val ITERACIONES = 210000

    private fun calcularHash(clave: String, salt: ByteArray): ByteArray {
        val datos = PBEKeySpec(clave.toCharArray(), salt, ITERACIONES, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(datos).encoded
        } finally {
            datos.clearPassword()
        }
    }

    fun crear(clave: String): Pair<String, String> {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val hash = calcularHash(clave, salt)
        return Pair(
            Base64.encodeToString(hash, Base64.NO_WRAP),
            Base64.encodeToString(salt, Base64.NO_WRAP)
        )
    }

    fun esCorrecta(clave: String, hashGuardado: String, saltGuardado: String): Boolean {
        val salt = Base64.decode(saltGuardado, Base64.NO_WRAP)
        val hashEsperado = Base64.decode(hashGuardado, Base64.NO_WRAP)
        val hashIngresado = calcularHash(clave, salt)
        return MessageDigest.isEqual(hashEsperado, hashIngresado)
    }
}

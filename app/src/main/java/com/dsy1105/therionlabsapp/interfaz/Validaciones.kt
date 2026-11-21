package com.dsy1105.therionlabsapp.interfaz

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Funciones pequenas y faciles de reutilizar en las pantallas.
object Validaciones {

    fun nombreValido(nombre: String): Boolean {
        return nombre.trim().length >= 3 && nombre.any { it.isLetter() } && !nombre.any { it.isDigit() }
    }

    fun correoValido(correo: String): Boolean {
        val correoLimpio = correo.trim()
        val formato = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")
        if (!formato.matches(correoLimpio) || correoLimpio.contains("..")) return false
        val partes = correoLimpio.split("@")
        val nombre = partes[0]
        val dominios = partes[1].split(".")
        if (nombre.startsWith(".") || nombre.endsWith(".")) return false
        return dominios.all { it.isNotEmpty() && it.first().isLetterOrDigit() && it.last().isLetterOrDigit() }
    }

    fun claveValida(clave: String): Boolean {
        return clave.length >= 8 && clave.any { it.isLetter() } && clave.any { it.isDigit() }
    }

    // Acepta RUT con o sin puntos, y comprueba su digito verificador.
    fun rutValido(rut: String): Boolean {
        val limpio = rut.replace(".", "").replace("-", "").trim().uppercase()
        if (!limpio.matches(Regex("[0-9]{7,8}[0-9K]"))) return false

        val numero = limpio.dropLast(1)
        val digito = limpio.last().toString()
        var suma = 0
        var multiplicador = 2
        for (i in numero.length - 1 downTo 0) {
            suma += (numero[i] - '0') * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        val resultado = 11 - (suma % 11)
        val esperado = when (resultado) {
            11 -> "0"
            10 -> "K"
            else -> resultado.toString()
        }
        return digito == esperado
    }

    fun fechaValida(fecha: String): Boolean {
        if (!fecha.matches(Regex("[0-9]{2}/[0-9]{2}/[0-9]{4}"))) return false
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CL"))
        formato.isLenient = false
        val fechaIngresada = try { formato.parse(fecha) } catch (e: Exception) { null }
        return fechaIngresada != null && !fechaIngresada.after(Date())
    }

    fun telefonoValido(telefono: String): Boolean {
        val digitos = telefono.filter { it.isDigit() }
        val numero = if (digitos.startsWith("56") && digitos.length == 11) digitos.drop(2) else digitos
        return numero.matches(Regex("9[0-9]{8}"))
    }

    fun direccionValida(direccion: String): Boolean {
        return direccion.trim().length >= 5
    }
}

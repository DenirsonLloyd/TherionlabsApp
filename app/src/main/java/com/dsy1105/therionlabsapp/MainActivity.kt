package com.dsy1105.therionlabsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import com.dsy1105.therionlabsapp.datos.BaseDatosApp
import com.dsy1105.therionlabsapp.datos.ProspectoRepositorio
import com.dsy1105.therionlabsapp.interfaz.*
import com.dsy1105.therionlabsapp.interfazAdmin.InterfazAdmin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = BaseDatosApp.obtenerBaseDatos(applicationContext)
        val repositorio = ProspectoRepositorio(db.prospectoDao())
        val viewModel = ProspecctoViewModel(repositorio)

        setContent {
            MaterialTheme {
                Surface {
                    var pantallaActual by remember { mutableStateOf("login") }

                    when (pantallaActual) {
                        // 1. Pantalla de Login con validación de cuentas registradas y botón "Iniciar"
                        "login" -> PantallaLogin(
                            onLoginExitoso = { esAdmin, _ ->
                                if (esAdmin) {
                                    pantallaActual = "admin" // Panel principal del Administrador
                                } else {
                                    pantallaActual = "inicio" // Flujo de Captador en terreno
                                }
                            }
                        )

                        // 2. Menú Principal del Captador (Con opción de cerrar sesión / volver al login)
                        "inicio" -> PantallaInicio(
                            onIrAFormulario = { pantallaActual = "formulario" },
                            onIrAEstado = { pantallaActual = "formulario" },
                            onVolverLogin = { pantallaActual = "login" }
                        )

                        // 3. Formulario de Captación de Prospectos (Con opción de volver al inicio)
                        "formulario" -> PantallaFormulario(
                            viewModel = viewModel,
                            onRegistroCompletado = { pantallaActual = "inicio" },
                            onVolver = { pantallaActual = "inicio" }
                        )

                        // 4. Pantalla de Registros Locales / Admin (Con opción de volver al login)
                        "registros_admin" -> PantallaRegistro(
                            viewModel = viewModel,
                            onNuevoRegistro = { pantallaActual = "formulario" },
                            onVolver = { pantallaActual = "admin" }
                        )

                        // 5. Panel Administrativo General / Métricas (Con opción de cerrar sesión)
                        "admin" -> InterfazAdmin(
                            viewModel = viewModel,
                            onCerrarSesion = { pantallaActual = "login" },
                            onIrARegistros = { pantallaActual = "registros_admin" }
                        )
                    }
                }
            }
        }
    }
}
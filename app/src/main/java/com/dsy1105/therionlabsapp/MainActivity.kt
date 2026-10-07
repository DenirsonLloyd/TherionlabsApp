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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = BaseDatosApp.obtenerBaseDatos(applicationContext)
        val repositorio = ProspectoRepositorio(db.prospectoDao())
        val viewModel = ProspecctoViewModel(repositorio)

        setContent {
            // Reemplazamos TherionLabsAppTheme por MaterialTheme directo
            MaterialTheme {
                Surface {
                    var pantallaActual by remember { mutableStateOf("inicio") }

                    when (pantallaActual) {
                        "inicio" -> PantallaInicio(
                            onIrAFormulario = { pantallaActual = "formulario" },
                            onIrAEstado = { pantallaActual = "registro" }
                        )
                        "formulario" -> PantallaFormulario(
                            viewModel = viewModel,
                            onRegistroCompletado = { pantallaActual = "registro" }
                        )
                        "registro" -> PantallaRegistro(
                            viewModel = viewModel,
                            onNuevoRegistro = { pantallaActual = "formulario" }
                        )
                    }
                }
            }
        }
    }
}
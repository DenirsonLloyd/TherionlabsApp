package com.dsy1105.therionlabsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dsy1105.therionlabsapp.ui.theme.TherionlabsAppTheme
import androidx.compose.material3.Surface
import com.dsy1105.therionlabsapp.datos.BaseDatosApp
import com.dsy1105.therionlabsapp.datos.ProspectoRepositorio
import com.dsy1105.therionlabsapp.datos.UsuarioRepositorio
import com.dsy1105.therionlabsapp.interfaz.UsuarioViewModel
import com.dsy1105.therionlabsapp.interfaz.ProspecctoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = BaseDatosApp.obtenerBaseDatos(applicationContext)
        val repositorio = ProspectoRepositorio(db.prospectoDao())
        val viewModel = ProspecctoViewModel(repositorio)
        val usuarioViewModel = UsuarioViewModel(UsuarioRepositorio(db.usuarioDao()))

        setContent {
            TherionlabsAppTheme {
                Surface {
                    AplicacionUniversidad(viewModel, usuarioViewModel)
                }
            }
        }
    }
}

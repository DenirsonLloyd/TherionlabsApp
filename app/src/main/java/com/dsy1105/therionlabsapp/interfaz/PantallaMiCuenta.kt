package com.dsy1105.therionlabsapp.interfaz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dsy1105.therionlabsapp.ui.theme.DuocFondo

@Composable
fun PantallaMiCuenta(
    vm: UsuarioViewModel,
    onVolver: () -> Unit,
    onCerrarSesion: () -> Unit,
    onActualizarDatos: (String, String) -> Unit
) {
    val usuario = vm.usuarioActual ?: return
    var nombre by remember(usuario.id) { mutableStateOf(usuario.nombre) }
    var correo by remember(usuario.id) { mutableStateOf(usuario.correo) }
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().background(DuocFondo).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
        Text("Mi cuenta", style = MaterialTheme.typography.headlineSmall)
        Text("Puedes actualizar tu nombre, correo o contraseña. Los datos quedan en este dispositivo.")
        OutlinedTextField(nombre, { nombre = it; vm.mensajePerfil = "" }, label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(correo, { correo = it; vm.mensajePerfil = "" }, label = { Text("Correo electrónico") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = { vm.actualizarPerfil(nombre, correo, onActualizarDatos) },
            enabled = !vm.guardandoPerfil,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar datos") }
        if (vm.mensajePerfil.isNotEmpty()) Text(vm.mensajePerfil, color = MaterialTheme.colorScheme.primary)

        HorizontalDivider()
        Text("Cambiar contraseña", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(actual, { actual = it }, label = { Text("Contraseña actual") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(nueva, { nueva = it }, label = { Text("Nueva contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(confirmar, { confirmar = it }, label = { Text("Repetir nueva contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Text("Mínimo 8 caracteres, letras y números.", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { vm.cambiarClave(actual, nueva, confirmar); actual = ""; nueva = ""; confirmar = "" }, enabled = !vm.guardandoPerfil, modifier = Modifier.fillMaxWidth()) {
            Text("Cambiar contraseña")
        }
        if (vm.mensajeClave.isNotEmpty()) Text(vm.mensajeClave, color = MaterialTheme.colorScheme.primary)
        HorizontalDivider()
        OutlinedButton(onClick = { vm.cerrarSesion(); onCerrarSesion() }, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
    }
}

package com.dsy1105.therionlabsapp.interfaz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsy1105.therionlabsapp.ui.theme.DuocAmarillo
import com.dsy1105.therionlabsapp.ui.theme.DuocNegro
import com.dsy1105.therionlabsapp.ui.theme.DuocFondo

private val azul = DuocAmarillo
private val azulOscuro = DuocNegro
private val fondo = DuocFondo

@Composable
fun PantallaRegistroUsuario(
    vm: UsuarioViewModel,
    onVolver: () -> Unit,
    onIngresar: () -> Unit,
    onRegistrado: () -> Unit
) {
    var mostrarClave by remember { mutableStateOf(false) }
    var mostrarConfirmar by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(fondo).verticalScroll(rememberScrollState()).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(onClick = onVolver) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
        }
        Text("Crear cuenta", color = azulOscuro, fontWeight = FontWeight.Bold, fontSize = 25.sp)
        Text("Regístrate para iniciar tu postulación.", color = Color.DarkGray)

        OutlinedTextField(
            value = vm.nombre,
            onValueChange = { vm.nombre = it; vm.errorNombre = "" },
            label = { Text("Nombre completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            trailingIcon = { if (vm.errorNombre.isNotEmpty()) Icon(Icons.Default.Error, contentDescription = "Error") },
            isError = vm.errorNombre.isNotEmpty(),
            supportingText = { if (vm.errorNombre.isNotEmpty()) Text(vm.errorNombre) },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        OutlinedTextField(
            value = vm.correo,
            onValueChange = { vm.correo = it; vm.errorCorreo = "" },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            trailingIcon = { if (vm.errorCorreo.isNotEmpty()) Icon(Icons.Default.Error, contentDescription = "Error") },
            isError = vm.errorCorreo.isNotEmpty(),
            supportingText = { if (vm.errorCorreo.isNotEmpty()) Text(vm.errorCorreo) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        OutlinedTextField(
            value = vm.clave,
            onValueChange = { vm.clave = it; vm.errorClave = "" },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { mostrarClave = !mostrarClave }) {
                    Icon(if (mostrarClave) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (mostrarClave) "Ocultar contraseña" else "Mostrar contraseña")
                }
            },
            visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
            isError = vm.errorClave.isNotEmpty(),
            supportingText = {
                if (vm.errorClave.isNotEmpty()) Text(vm.errorClave)
                else Text("Mínimo 8 caracteres, con letras y números.")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        OutlinedTextField(
            value = vm.confirmarClave,
            onValueChange = { vm.confirmarClave = it; vm.errorConfirmar = "" },
            label = { Text("Confirmar contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { mostrarConfirmar = !mostrarConfirmar }) {
                    Icon(if (mostrarConfirmar) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (mostrarConfirmar) "Ocultar contraseña" else "Mostrar contraseña")
                }
            },
            visualTransformation = if (mostrarConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
            isError = vm.errorConfirmar.isNotEmpty(),
            supportingText = { if (vm.errorConfirmar.isNotEmpty()) Text(vm.errorConfirmar) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        if (vm.errorGeneral.isNotEmpty()) Text(vm.errorGeneral, color = Color.Red, fontSize = 12.sp)

        Button(
            onClick = { vm.registrarUsuario(onRegistrado) },
            enabled = !vm.cargando,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = azul, contentColor = DuocNegro)
        ) { Text(if (vm.cargando) "Guardando..." else "Crear cuenta") }
        TextButton(onClick = onIngresar, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
        Text("Las cuentas de esta versión se guardan solamente en este dispositivo.", fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun PantallaIngresoUsuario(
    vm: UsuarioViewModel,
    onVolver: () -> Unit,
    onCrearCuenta: () -> Unit,
    onAdmin: () -> Unit,
    onIngresado: () -> Unit
) {
    var mostrarClave by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(fondo).verticalScroll(rememberScrollState()).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconButton(onClick = onVolver) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
        }
        Spacer(Modifier.height(24.dp))
        Text("Iniciar sesión", color = azulOscuro, fontWeight = FontWeight.Bold, fontSize = 25.sp)
        Text("Ingresa con tu cuenta de postulante.", color = Color.DarkGray)
        OutlinedTextField(
            value = vm.correoLogin,
            onValueChange = { vm.correoLogin = it; vm.errorCorreoLogin = ""; vm.errorLogin = "" },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            isError = vm.errorCorreoLogin.isNotEmpty(),
            supportingText = { if (vm.errorCorreoLogin.isNotEmpty()) Text(vm.errorCorreoLogin) },
            trailingIcon = { if (vm.errorCorreoLogin.isNotEmpty()) Icon(Icons.Default.Error, contentDescription = "Error") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        OutlinedTextField(
            value = vm.claveLogin,
            onValueChange = { vm.claveLogin = it; vm.errorClaveLogin = ""; vm.errorLogin = "" },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { mostrarClave = !mostrarClave }) {
                    Icon(if (mostrarClave) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (mostrarClave) "Ocultar contraseña" else "Mostrar contraseña")
                }
            },
            visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
            isError = vm.errorClaveLogin.isNotEmpty(),
            supportingText = { if (vm.errorClaveLogin.isNotEmpty()) Text(vm.errorClaveLogin) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        if (vm.errorLogin.isNotEmpty()) Text(vm.errorLogin, color = Color.Red, fontSize = 12.sp)

        Button(
            onClick = { vm.iniciarSesion(onIngresado) },
            enabled = !vm.cargando,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = azul, contentColor = DuocNegro)
        ) { Text(if (vm.cargando) "Verificando..." else "Iniciar sesión") }
        TextButton(onClick = onCrearCuenta, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("¿No tienes cuenta? Regístrate")
        }
        TextButton(onClick = onAdmin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Ingresar como administrador")
        }
    }
}

package com.dsy1105.therionlabsapp.interfaz

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormulario(
    viewModel: ProspecctoViewModel,
    onRegistroCompletado: () -> Unit,
    onVolver: () -> Unit // Parámetro para regresar a la pantalla anterior
) {
    var pasoActual by remember { mutableStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (pasoActual == 1) "Nuevo Prospecto (1/2)" else "Nuevo Prospecto (2/2)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (pasoActual == 2) {
                            pasoActual = 1 // Si está en el paso 2, vuelve al paso 1 del formulario
                        } else {
                            onVolver() // Si está en el paso 1, sale de la pantalla hacia el menú anterior
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF1A1C1E)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (pasoActual == 1) {
                // Paso 1: Datos Personales
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("REGISTRO DE PROSPECTO", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = viewModel.nombre,
                        onValueChange = { viewModel.nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = viewModel.correo,
                        onValueChange = { viewModel.correo = it },
                        label = { Text("Correo electrónico") },
                        isError = viewModel.errorCorreo,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (viewModel.errorCorreo) {
                        Text("Error: Formato de correo inválido", color = Color.Red,
                            style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedTextField(
                        value = viewModel.telefono,
                        onValueChange = { viewModel.telefono = it },
                        label = { Text("Teléfono de contacto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = { pasoActual = 2 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    Text("SIGUIENTE: PREFERENCIAS", fontWeight = FontWeight.Bold)
                }
            } else {
                // Paso 2: Preferencias Académicas
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("PREFERENCIAS ACADÉMICAS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = viewModel.nivelEducativo,
                        onValueChange = { viewModel.nivelEducativo = it },
                        label = { Text("Nivel Educativo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = viewModel.areaInteres,
                        onValueChange = { viewModel.areaInteres = it },
                        label = { Text("Área de Interés") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = viewModel.cursoInteres,
                        onValueChange = { viewModel.cursoInteres = it },
                        label = { Text("Curso de Interés") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Modalidad:", fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = viewModel.modalidad == "Presencial",
                            onClick = { viewModel.modalidad = "Presencial" }
                        )
                        Text("Presencial")
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = viewModel.modalidad == "Online",
                            onClick = { viewModel.modalidad = "Online" }
                        )
                        Text("Online")
                    }
                }

                Button(
                    onClick = {
                        viewModel.guardarProspecto {
                            onRegistroCompletado()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("CONFIRMAR Y GUARDAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
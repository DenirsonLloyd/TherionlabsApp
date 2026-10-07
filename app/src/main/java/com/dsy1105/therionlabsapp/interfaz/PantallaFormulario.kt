package com.dsy1105.therionlabsapp.interfaz

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PantallaFormulario(
    viewModel: ProspecctoViewModel,
    onRegistroCompletado: () -> Unit
) {
    var pasoActual by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (pasoActual == 1) {
            // Paso 1: Datos Personales
            Column {
                Text("REGISTRO DE PROSPECTO", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("GUARDAR REGISTRO LOCAL")
            }
        } else {
            // Paso 2: Preferencias Académicas
            Column {
                Text("PREFERENCIAS ACADÉMICAS", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(8.dp))
                Text("Modalidad:")
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CONFIRMAR REGISTRO")
            }
        }
    }
}
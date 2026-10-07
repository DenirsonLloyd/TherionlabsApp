package com.dsy1105.therionlabsapp.interfaz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PantallaRegistro(
    viewModel: ProspecctoViewModel,
    onNuevoRegistro: () -> Unit
) {
    var tabSeleccionada by remember { mutableIntStateOf(0) }
    val pendientes by viewModel.pendientes.collectAsState()
    val sincronizados by viewModel.sincronizados.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("✓ REGISTRO GUARDADO EN EL DISPOSITIVO", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("El registro se guardó localmente y se enviará automáticamente al detectar red.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = tabSeleccionada) {
            Tab(selected = tabSeleccionada == 0, onClick = { tabSeleccionada = 0 }) {
                Text("Pendientes (${pendientes.size})", modifier = Modifier.padding(12.dp))
            }
            Tab(selected = tabSeleccionada == 1, onClick = { tabSeleccionada = 1 }) {
                Text("Sincronizados (${sincronizados.size})", modifier = Modifier.padding(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            val lista = if (tabSeleccionada == 0) pendientes else sincronizados
            items(lista) { prospecto ->
                TarjetaProspecto(prospecto = prospecto)
            }
        }

        Button(
            onClick = onNuevoRegistro,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("NUEVO REGISTRO")
        }
    }
}
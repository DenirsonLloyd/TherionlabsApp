package com.dsy1105.therionlabsapp.interfaz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    onIrAFormulario: () -> Unit,
    onIrAEstado: () -> Unit,
    onVolverLogin: () -> Unit // Botón para regresar al login
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Captador", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolverLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cerrar sesión / Volver",
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
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "THERIONLABS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF3F51B5)
            )
            Text(
                text = "Gestión de Captación en Terreno",
                fontSize = 13.sp,
                color = Color(0xFF74777F)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onIrAFormulario,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Registro de Prospecto", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
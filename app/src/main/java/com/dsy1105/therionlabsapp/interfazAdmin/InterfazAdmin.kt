package com.dsy1105.therionlabsapp.interfazAdmin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsy1105.therionlabsapp.datos.ProspectoEntidad
import com.dsy1105.therionlabsapp.interfaz.ProspecctoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterfazAdmin(
    viewModel: ProspecctoViewModel,
    onCerrarSesion: () -> Unit,
    onIrARegistros: () -> Unit
) {
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }
    val prospectos by viewModel.listaProspectos.collectAsState()
    var textoBusqueda by remember { mutableStateOf("") }

    val listaFiltrada = prospectos.filter {
        it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                it.correo.contains(textoBusqueda, ignoreCase = true) ||
                it.cursoInteres.contains(textoBusqueda, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Barra Superior
        TopAppBar(
            title = {
                Text(
                    text = "PANEL ADMINISTRATIVO",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1C1E)
                )
            },
            navigationIcon = {
                IconButton(onClick = onCerrarSesion) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Cerrar Sesión",
                        tint = Color(0xFF1A1C1E)
                    )
                }
            },
            actions = {
                TextButton(onClick = onIrARegistros) {
                    Text("Registros", color = Color(0xFF3F51B5), fontWeight = FontWeight.Bold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        // Pestañas
        TabRow(
            selectedTabIndex = pestanaSeleccionada,
            containerColor = Color.White,
            contentColor = Color(0xFF3F51B5)
        ) {
            Tab(
                selected = pestanaSeleccionada == 0,
                onClick = { pestanaSeleccionada = 0 },
                text = { Text("Listado Maestro (${prospectos.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = pestanaSeleccionada == 1,
                onClick = { pestanaSeleccionada = 1 },
                text = { Text("Métricas y Resumen", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (pestanaSeleccionada == 0) {
            // VISTA 1: LISTADO MAESTRO
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    placeholder = { Text("Buscar prospecto...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF3F51B5)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (listaFiltrada.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No se encontraron registros.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(listaFiltrada) { prospecto ->
                            TarjetaAdminConCambioEstado(
                                prospecto = prospecto,
                                onCambiarEstado = { nuevoEstado ->
                                    viewModel.actualizarEstadoSincronizacion(prospecto.id, nuevoEstado)
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // VISTA 2: PANEL DE MÉTRICAS Y GRÁFICOS
            val total = prospectos.size
            val pendientesCount = prospectos.count { !it.esSincronizado }
            val sincronizadosCount = prospectos.count { it.esSincronizado }

            // Agrupaciones para métricas
            val porModalidad = prospectos.groupBy { it.modalidad.ifBlank { "No especificada" } }
            val porArea = prospectos.groupBy { it.areaInteres.ifBlank { "General" } }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta 1: Resumen General con Barra Visual
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Resumen General", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1C1E))
                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Prospectos:", color = Color.Gray)
                            Text("$total", fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pendientes de Sincronización:", color = Color(0xFFE65100))
                            Text("$pendientesCount", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sincronizados:", color = Color(0xFF2E7D32))
                            Text("$sincronizadosCount", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Progreso de Sincronización", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)

                        val porcentajeSinc = if (total > 0) sincronizadosCount.toFloat() / total else 0f
                        BarraProgresoGrafica(
                            progreso = porcentajeSinc,
                            colorBarra = Color(0xFF2E7D32),
                            colorFondo = Color(0xFFFFF3E0)
                        )
                    }
                }

                // Tarjeta 2: Gráfico por Modalidad
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Distribución por Modalidad", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1C1E))
                        HorizontalDivider()

                        if (total == 0) {
                            Text("Sin datos suficientes", fontSize = 13.sp, color = Color.Gray)
                        } else {
                            porModalidad.forEach { (modalidad, lista) ->
                                ElementoGraficoBarra(
                                    etiqueta = modalidad,
                                    cantidad = lista.size,
                                    total = total,
                                    colorBarra = Color(0xFF3F51B5)
                                )
                            }
                        }
                    }
                }

                // Tarjeta 3: Gráfico por Área de Interés
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Distribución por Área de Interés", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1C1E))
                        HorizontalDivider()

                        if (total == 0) {
                            Text("Sin datos suficientes", fontSize = 13.sp, color = Color.Gray)
                        } else {
                            porArea.forEach { (area, lista) ->
                                ElementoGraficoBarra(
                                    etiqueta = area,
                                    cantidad = lista.size,
                                    total = total,
                                    colorBarra = Color(0xFF009688)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun BarraProgresoGrafica(progreso: Float, colorBarra: Color, colorFondo: Color) {
    val progresoAnimado by animateFloatAsState(targetValue = progreso, label = "progreso")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colorFondo)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progresoAnimado)
                .clip(RoundedCornerShape(6.dp))
                .background(colorBarra)
        )
    }
}

@Composable
fun ElementoGraficoBarra(
    etiqueta: String,
    cantidad: Int,
    total: Int,
    colorBarra: Color
) {
    val proporcion = if (total > 0) cantidad.toFloat() / total else 0f
    val porcentajeInt = (proporcion * 100).toInt()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(etiqueta, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1C1E))
            Text("$cantidad ($porcentajeInt%)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
        BarraProgresoGrafica(
            progreso = proporcion,
            colorBarra = colorBarra,
            colorFondo = Color(0xFFE8EAF6)
        )
    }
}

@Composable
fun TarjetaAdminConCambioEstado(
    prospecto: ProspectoEntidad,
    onCambiarEstado: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prospecto.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A1C1E)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (prospecto.esSincronizado) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    onClick = { onCambiarEstado(!prospecto.esSincronizado) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Cambiar estado",
                            modifier = Modifier.size(12.dp),
                            tint = if (prospecto.esSincronizado) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                        Text(
                            text = if (prospecto.esSincronizado) "Sincronizado" else "Pendiente",
                            color = if (prospecto.esSincronizado) Color(0xFF2E7D32) else Color(0xFFE65100),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "${prospecto.correo} • ${prospecto.telefono}",
                fontSize = 13.sp,
                color = Color(0xFF74777F)
            )

            Text(
                text = "Interés: ${prospecto.cursoInteres} (${prospecto.modalidad})",
                fontSize = 13.sp,
                color = Color(0xFF3F51B5),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
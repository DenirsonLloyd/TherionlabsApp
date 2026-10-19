package com.dsy1105.therionlabsapp.interfazAdmin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsy1105.therionlabsapp.datos.ProspectoEntidad
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AzulAdmin = Color(0xFF168DFF)
private val FondoTarjeta = Color(0xFFF0F0F0)
private val Tinta = Color(0xFF17212B)

@Composable
fun PanelAdministrativo(
    prospectos: List<ProspectoEntidad>,
    onAbrirListado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = prospectos.size
    val pendientes = prospectos.count { !it.esSincronizado }
    val sincronizados = total - pendientes
    val porDia = prospectos.groupBy {
        SimpleDateFormat("dd/MM", Locale("es", "CL")).format(Date(it.fechaCreacion))
    }.entries.sortedBy { it.key }.takeLast(7)

    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFFF8FAFC)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("PERFIL ADMINISTRATIVO", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Tinta)
        Text("SISTEMA DE GESTIÓN CENTRAL · THERIONLABS", fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold, color = Color.DarkGray)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            IndicadorAdmin("Total de prospectos", total.toString(), Modifier.weight(1f))
            IndicadorAdmin("Capturas pendientes", pendientes.toString(), Modifier.weight(1f))
            IndicadorAdmin("Sincronizados", sincronizados.toString(), Modifier.weight(1f))
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Card(
                modifier = Modifier.weight(1.25f).height(230.dp),
                colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB9C0C6))
            ) {
                Column(Modifier.fillMaxSize().padding(14.dp)) {
                    Text("Capturas por día", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    if (porDia.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Aún no hay capturas registradas", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    } else {
                        Canvas(Modifier.fillMaxSize().padding(top = 8.dp, bottom = 18.dp)) {
                            val maxValue = (porDia.maxOfOrNull { it.value.size } ?: 1).coerceAtLeast(1)
                            val slot = size.width / (porDia.size * 1.5f)
                            porDia.forEachIndexed { index, entry ->
                                val h = (entry.value.size.toFloat() / maxValue) * (size.height - 8f)
                                drawRect(
                                    color = AzulAdmin,
                                    topLeft = Offset(index * slot * 1.5f + slot * .25f, size.height - h),
                                    size = Size(slot * .65f, h)
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.weight(1f).height(230.dp),
                colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB9C0C6))
            ) {
                Column(Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Estado de envíos", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Canvas(Modifier.size(132.dp)) {
                        val sum = total.coerceAtLeast(1).toFloat()
                        val pendienteSweep = 360f * pendientes / sum
                        drawArc(Color(0xFFBFC5CB), 0f, 360f, true)
                        drawArc(AzulAdmin, -90f, pendienteSweep, true)
                        drawCircle(Color.White, radius = size.minDimension * .27f)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Pendientes: $pendientes  ·  Enviados: $sincronizados", fontSize = 11.sp,
                        textAlign = TextAlign.Center)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD8DDE2))
        ) {
            Row(
                Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Listado maestro de prospectos", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Consulta los registros almacenados en el dispositivo.", color = Color.Gray, fontSize = 13.sp)
                }
                Button(onClick = onAbrirListado, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD5E9FF))) {
                    Text("ABRIR LISTADO", color = Tinta)
                }
            }
        }
    }
}

@Composable
private fun IndicadorAdmin(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB9C0C6))
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.Center) {
            Text(titulo, fontSize = 12.sp, color = Color.DarkGray)
            Spacer(Modifier.height(8.dp))
            Text(valor, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = AzulAdmin)
        }
    }
}
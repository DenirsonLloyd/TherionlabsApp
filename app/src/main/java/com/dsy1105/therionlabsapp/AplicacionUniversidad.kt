package com.dsy1105.therionlabsapp

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.dsy1105.therionlabsapp.datos.AccesoAdmin
import com.dsy1105.therionlabsapp.datos.FuncionesDocumento
import com.dsy1105.therionlabsapp.interfaz.FotoPostulante
import com.dsy1105.therionlabsapp.interfaz.VistaPreviaFoto
import com.dsy1105.therionlabsapp.interfaz.PantallaMiCuenta
import com.dsy1105.therionlabsapp.interfaz.Validaciones
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsy1105.therionlabsapp.datos.ProspectoEntidad
import com.dsy1105.therionlabsapp.interfaz.ProspecctoViewModel
import com.dsy1105.therionlabsapp.interfaz.UsuarioViewModel
import com.dsy1105.therionlabsapp.interfaz.PantallaRegistroUsuario
import com.dsy1105.therionlabsapp.interfaz.PantallaIngresoUsuario
import com.dsy1105.therionlabsapp.interfaz.PantallaPanelEstudiante
import com.dsy1105.therionlabsapp.interfaz.PantallaFeriados
import com.dsy1105.therionlabsapp.interfazAdmin.PantallaServidor
import com.dsy1105.therionlabsapp.ui.theme.DuocAmarillo
import com.dsy1105.therionlabsapp.ui.theme.DuocAmarilloClaro
import com.dsy1105.therionlabsapp.ui.theme.DuocNegro
import com.dsy1105.therionlabsapp.ui.theme.DuocFondo

private val Azul = DuocNegro
private val AzulOscuro = DuocNegro
private val Fondo = DuocFondo
private val Texto = DuocNegro
private val AzulClaro = DuocAmarilloClaro

@Composable
fun AplicacionUniversidad(vm: ProspecctoViewModel, usuarioVm: UsuarioViewModel) {
    var pantalla by remember { mutableStateOf("inicio") }
    var carrera by remember { mutableStateOf("Ingeniería en Informática") }
    var adminTab by remember { mutableStateOf("Alumnos") }
    var alumnoDetalle by remember { mutableStateOf<ProspectoEntidad?>(null) }
    val alumnos by vm.listaProspectos.collectAsState()

    Crossfade(targetState = pantalla, animationSpec = tween(220), label = "Cambio de pantalla") { pantallaActual ->
    when (pantallaActual) {
        "inicio" -> InicioAlumno(
            onIniciar = { pantalla = "registroUsuario" },
            onCuenta = { pantalla = "loginUsuario" }
        )
        "panel" -> {
            val usuario = usuarioVm.usuarioActual
            if (usuario != null) {
                LaunchedEffect(usuario.id, usuario.servidorId) {
                    vm.sincronizarDatos(usuario.id, usuario.servidorId)
                }
                PantallaPanelEstudiante(
                    vm = vm,
                    usuario = usuario,
                    onNuevaPostulacion = {
                        vm.reiniciarFormulario()
                        vm.nombre = usuario.nombre
                        vm.correo = usuario.correo
                        pantalla = "terminos"
                    },
                    onMiCuenta = { pantalla = "cuenta" },
                    onFeriados = { pantalla = "feriados" },
                    onSincronizar = { vm.sincronizarDatos(usuario.id, usuario.servidorId) },
                    onCerrarSesion = {
                        usuarioVm.cerrarSesion()
                        vm.reiniciarFormulario()
                        pantalla = "inicio"
                    }
                )
            }
        }
        "registroUsuario" -> PantallaRegistroUsuario(
            vm = usuarioVm,
            onVolver = { pantalla = "inicio" },
            onIngresar = { pantalla = "loginUsuario" },
            onRegistrado = {
                pantalla = "panel"
            }
        )
        "loginUsuario" -> PantallaIngresoUsuario(
            vm = usuarioVm,
            onVolver = { pantalla = "inicio" },
            onCrearCuenta = { pantalla = "registroUsuario" },
            onAdmin = { pantalla = "loginAdmin" },
            onIngresado = {
                pantalla = "panel"
            }
        )
        "terminos" -> Terminos(
            onBack = { vm.reiniciarFormulario(); pantalla = "panel" },
            onCuenta = { pantalla = "cuenta" },
            onNext = { pantalla = "datos" }
        )
        "feriados" -> PantallaFeriados(onVolver = { pantalla = "panel" })
        "cuenta" -> PantallaMiCuenta(
            vm = usuarioVm,
            onVolver = { pantalla = "panel" },
            onCerrarSesion = { vm.reiniciarFormulario(); pantalla = "inicio" },
            onActualizarDatos = { nombre, correo -> vm.nombre = nombre; vm.correo = correo }
        )
        "datos" -> DatosAlumno(vm, onBack = { pantalla = "terminos" }, onNext = { pantalla = "carreras" })
        "carreras" -> SeleccionCarrera(carrera, onSelect = { carrera = it }, onBack = { pantalla = "datos" }, onNext = { pantalla = "detalle" })
        "detalle" -> DetalleCarrera(carrera, onBack = { pantalla = "carreras" }, onNext = { pantalla = "confirmar" })
        "confirmar" -> Confirmar(vm, carrera, onBack = { pantalla = "detalle" }, onSend = {
            vm.cursoInteres = carrera
            vm.facultad = carreras.firstOrNull { it.first == carrera }?.second ?: ""
            vm.areaInteres = vm.facultad
            if (usuarioVm.usuarioActual == null) {
                pantalla = "loginUsuario"
            } else {
                vm.guardarProspecto(
                    validarDatosCompletos = true,
                    usuarioId = usuarioVm.usuarioActual!!.id,
                    usuarioServidorId = usuarioVm.usuarioActual!!.servidorId,
                    onExito = { pantalla = "panel" },
                    onError = { pantalla = "datos" }
                )
            }
        })
        "loginAdmin" -> LoginAdmin(onBack = { pantalla = "inicio" }, onLogin = { pantalla = "admin" })
        "adminServidor" -> PantallaServidor(vm, onVolver = { pantalla = "admin" })
        "admin" -> AdminShell(
            alumnos = alumnos,
            tab = adminTab,
            onTab = { adminTab = it },
            onLogout = { pantalla = "inicio" },
            onServidor = { pantalla = "adminServidor" },
            onDetail = { alumnoDetalle = it }
        )
    }
    }

    if (alumnoDetalle != null) {
        DetalleAlumnoDialog(
            alumno = alumnoDetalle!!,
            onClose = { alumnoDetalle = null },
            onCambiarEstado = { id, estado ->
                vm.cambiarEstadoPostulacion(id, estado)
                alumnoDetalle = null
            }
        )
    }
}

@Composable
private fun FondoPantalla(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Fondo).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
private fun BarraTitulo(titulo: String, back: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) {
            IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Texto) }
        }
        Text(titulo, color = Texto, fontWeight = FontWeight.Bold, fontSize = 19.sp)
    }
}

@Composable
private fun BotonAzul(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DuocAmarillo, contentColor = DuocNegro)
    ) { Text(texto, fontWeight = FontWeight.Bold) }
}

@Composable
private fun InicioAlumno(onIniciar: () -> Unit, onCuenta: () -> Unit) {
    Box(Modifier.fillMaxSize().background(AzulOscuro)) {
        Column(
            Modifier.fillMaxWidth().align(Alignment.Center).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Icon(Icons.Default.School, null, tint = DuocAmarillo, modifier = Modifier.size(76.dp))
            Text("TherionLabs", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Admisión académica · Proyecto estudiantil", color = Color.White, fontSize = 15.sp)
            Spacer(Modifier.height(130.dp))
            BotonAzul("Comenzar admisión / Crear cuenta", onIniciar)
            OutlinedButton(
                onClick = onCuenta, modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) { Text("Ya tengo cuenta") }
        }
    }
}

@Composable
private fun Terminos(onBack: () -> Unit, onCuenta: () -> Unit, onNext: () -> Unit) {
    var aceptado by remember { mutableStateOf(false) }
    FondoPantalla {
        BarraTitulo("Términos y Condiciones", onBack)
        Spacer(Modifier.height(12.dp))
        Icon(Icons.Default.Security, null, tint = Azul, modifier = Modifier.size(76.dp).align(Alignment.CenterHorizontally))
        Text("Ley N° 19.628", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Texto, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text("Protección de Datos Personales", fontWeight = FontWeight.SemiBold, color = Texto, modifier = Modifier.align(Alignment.CenterHorizontally))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Text(
                "Para continuar con tu proceso de admisión, debes aceptar los términos y condiciones relacionados con el tratamiento de tus datos personales, de acuerdo con la Ley N° 19.628 sobre Protección de la Vida Privada y los principios de seguridad de la información de la Universidad.",
                Modifier.padding(18.dp), color = Texto, lineHeight = 22.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = aceptado, onCheckedChange = { aceptado = it })
            Text("He leído y acepto los términos y condiciones.")
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onCuenta, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Mi cuenta / Cambiar contraseña")
        }
        BotonAzul("Siguiente", onNext, enabled = aceptado)
    }
}

@Composable
private fun DatosAlumno(vm: ProspecctoViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    FondoPantalla {
        BarraTitulo("Datos Personales", onBack)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Paso("1", "Datos", true); Paso("2", "Carrera", false); Paso("3", "Confirmar", false)
        }
        Text("Información personal", color = Texto, fontWeight = FontWeight.Bold)
        Column(
            Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                vm.nombre, { vm.nombre = it; vm.errorNombre = "" },
                label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                isError = vm.errorNombre.isNotEmpty(),
                supportingText = { if (vm.errorNombre.isNotEmpty()) Text(vm.errorNombre) },
                trailingIcon = { if (vm.errorNombre.isNotEmpty()) Icon(Icons.Default.Error, "Error") }
            )
            OutlinedTextField(
                vm.rut, { vm.rut = it; vm.errorRut = "" },
                label = { Text("RUT (12.345.678-5)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                isError = vm.errorRut.isNotEmpty(),
                supportingText = { if (vm.errorRut.isNotEmpty()) Text(vm.errorRut) },
                trailingIcon = { if (vm.errorRut.isNotEmpty()) Icon(Icons.Default.Error, "Error") }
            )
            OutlinedTextField(
                vm.fechaNacimiento, { vm.fechaNacimiento = it; vm.errorFecha = "" },
                label = { Text("Fecha de nacimiento (dd/mm/aaaa)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                isError = vm.errorFecha.isNotEmpty(),
                supportingText = { if (vm.errorFecha.isNotEmpty()) Text(vm.errorFecha) },
                trailingIcon = { if (vm.errorFecha.isNotEmpty()) Icon(Icons.Default.Error, "Error") }
            )
            OutlinedTextField(
                vm.correo, { vm.correo = it; vm.errorEmail = "" },
                label = { Text("Correo electrónico") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                isError = vm.errorEmail.isNotEmpty(),
                supportingText = { if (vm.errorEmail.isNotEmpty()) Text(vm.errorEmail) },
                trailingIcon = { if (vm.errorEmail.isNotEmpty()) Icon(Icons.Default.Error, "Error") }
            )
            OutlinedTextField(
                vm.telefono, { vm.telefono = it; vm.errorTelefono = "" },
                label = { Text("Teléfono celular") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                isError = vm.errorTelefono.isNotEmpty(),
                supportingText = { if (vm.errorTelefono.isNotEmpty()) Text(vm.errorTelefono) },
                trailingIcon = { if (vm.errorTelefono.isNotEmpty()) Icon(Icons.Default.Error, "Error") }
            )
            OutlinedTextField(
                vm.direccion, { vm.direccion = it; vm.errorDireccion = "" },
                label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                isError = vm.errorDireccion.isNotEmpty(),
                supportingText = { if (vm.errorDireccion.isNotEmpty()) Text(vm.errorDireccion) },
                trailingIcon = { if (vm.errorDireccion.isNotEmpty()) Icon(Icons.Default.Error, "Error") }
            )
            HorizontalDivider()
            FotoPostulante(ruta = vm.fotoRuta, onFotoGuardada = { vm.fotoRuta = it })
        }
        BotonAzul("Siguiente", { if (vm.validarDatos()) onNext() })
    }
}

@Composable
private fun Paso(numero: String, texto: String, activo: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(50), color = if (activo) DuocAmarillo else Color(0xFFE7E7E7)) {
            Text(numero, Modifier.padding(horizontal = 12.dp, vertical = 5.dp), color = Texto, fontWeight = FontWeight.Bold)
        }
        Text(texto, fontSize = 11.sp, color = Texto)
    }
}

private val carreras = listOf(
    "Ingeniería en Informática" to "Facultad de Ingeniería",
    "Ingeniería Comercial" to "Facultad de Economía y Negocios",
    "Psicología" to "Facultad de Ciencias Sociales",
    "Derecho" to "Facultad de Derecho",
    "Medicina" to "Facultad de Ciencias de la Salud"
)

@Composable
private fun SeleccionCarrera(seleccion: String, onSelect: (String) -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
    var busqueda by remember { mutableStateOf("") }
    FondoPantalla {
        BarraTitulo("Carreras", onBack)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Paso("1", "Datos", true); Paso("2", "Carrera", true); Paso("3", "Confirmar", false)
        }
        OutlinedTextField(busqueda, { busqueda = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Buscar carrera...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(carreras.filter { it.first.contains(busqueda, true) }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(item.first) },
                    colors = CardDefaults.cardColors(containerColor = if (seleccion == item.first) DuocAmarilloClaro else Color.White),
                    border = if (seleccion == item.first) androidx.compose.foundation.BorderStroke(1.dp, DuocAmarillo) else null
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(10.dp), color = Azul) {
                            Icon(Icons.Default.School, null, tint = Color.White, modifier = Modifier.padding(12.dp).size(25.dp))
                        }
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(item.first, color = Texto, fontWeight = FontWeight.Bold)
                            Text(item.second, color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Azul)
                    }
                }
            }
        }
        BotonAzul("Siguiente", onNext)
    }
}

@Composable
private fun DetalleCarrera(carrera: String, onBack: () -> Unit, onNext: () -> Unit) {
    val facultad = carreras.firstOrNull { it.first == carrera }?.second ?: "Facultad de Ingeniería"
    val context = LocalContext.current
    var mensaje by remember { mutableStateOf("") }
    val guardarPdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            try {
                FuncionesDocumento.guardarMalla(context, uri, carrera, facultad)
                mensaje = "Malla demostrativa guardada como PDF."
            } catch (e: Exception) {
                mensaje = "No se pudo guardar el PDF."
            }
        }
    }
    FondoPantalla {
        BarraTitulo("Detalle de la carrera", onBack)
        Card(colors = CardDefaults.cardColors(containerColor = AzulOscuro), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(22.dp)) {
                Icon(Icons.Default.School, null, tint = Color.White, modifier = Modifier.size(42.dp))
                Text(carrera, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text(facultad, color = Color.White.copy(alpha = .8f))
            }
        }
        Text("¿De qué trata?", color = Texto, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Esta carrera entrega formación profesional y herramientas para desarrollar competencias en su área, resolver problemas y participar en proyectos del mundo laboral.", color = Texto, lineHeight = 21.sp)
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FilaDato(Icons.Default.Work, "Título profesional", carrera)
                FilaDato(Icons.Default.Schedule, "Duración", "10 semestres")
                FilaDato(Icons.Default.LocationOn, "Modalidad", "Presencial")
                FilaDato(Icons.Default.AccountBalance, "Facultad", facultad)
            }
        }
        OutlinedButton(onClick = {
            val nombre = carrera.replace(" ", "_")
            guardarPdf.launch("Malla_referencial_${nombre}.pdf")
        }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Download, null); Spacer(Modifier.width(8.dp)); Text("Guardar malla curricular (ejemplo PDF)")
        }
        Text("Documento demostrativo, no es la malla oficial de una universidad.", fontSize = 11.sp, color = Color.Gray)
        if (mensaje.isNotEmpty()) Text(mensaje, color = Azul, fontSize = 12.sp)
        Spacer(Modifier.weight(1f))
        BotonAzul("Continuar a confirmación", onNext)
    }
}

@Composable
private fun FilaDato(icon: androidx.compose.ui.graphics.vector.ImageVector, titulo: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Azul, modifier = Modifier.size(24.dp))
        Column(Modifier.padding(start = 12.dp)) {
            Text(titulo, color = Color.Gray, fontSize = 11.sp)
            Text(valor, color = Texto, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Confirmar(vm: ProspecctoViewModel, carrera: String, onBack: () -> Unit, onSend: () -> Unit) {
    val facultad = carreras.firstOrNull { it.first == carrera }?.second ?: ""
    FondoPantalla {
        BarraTitulo("Confirmar Postulación", onBack)
        Text("Revisa tus datos antes de enviar", color = Texto, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Datos personales", color = Azul, fontWeight = FontWeight.Bold)
                Text("Nombre: ${vm.nombre}")
                Text("RUT: ${vm.rut}")
                Text("Nacimiento: ${vm.fechaNacimiento}")
                Text("Correo: ${vm.correo}")
                Text("Teléfono: ${vm.telefono}")
                Text("Dirección: ${vm.direccion}")
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(16.dp)) {
                Text("Carrera seleccionada", color = Azul, fontWeight = FontWeight.Bold)
                Text(carrera, color = Texto, fontWeight = FontWeight.Bold)
                Text(facultad, color = Color.Gray, fontSize = 12.sp)
            }
        }
        if (vm.fotoRuta.isNotBlank()) {
            Text("Imagen adjunta", color = Texto, fontWeight = FontWeight.Bold)
            VistaPreviaFoto(vm.fotoRuta)
        }
        Text("Tu postulación quedará asociada a tu cuenta y podrás revisar su estado.", color = Texto, fontSize = 12.sp)
        }
        BotonAzul("Enviar Postulación", onSend)
    }
}

@Composable
private fun LoginAdmin(onBack: () -> Unit, onLogin: () -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val coloresCampos = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Texto,
        unfocusedTextColor = Texto,
        focusedLabelColor = Azul,
        unfocusedLabelColor = Color(0xFF64748B),
        focusedBorderColor = Azul,
        unfocusedBorderColor = Color(0xFFCBD5E1),
        cursorColor = Azul,
        focusedLeadingIconColor = Azul,
        unfocusedLeadingIconColor = Color(0xFF64748B),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )
    Box(Modifier.fillMaxSize().background(Fondo), contentAlignment = Alignment.Center) {
        Card(
            Modifier.fillMaxWidth().padding(22.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(Icons.Default.AdminPanelSettings, null, tint = Azul, modifier = Modifier.size(58.dp))
                Text("TherionLabs", color = AzulOscuro, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("Acceso de administrador", color = Color(0xFF64748B), fontSize = 14.sp)
                OutlinedTextField(usuario, { usuario = it; error = false }, modifier = Modifier.fillMaxWidth(), label = { Text("Usuario") }, leadingIcon = { Icon(Icons.Default.Person, null) }, singleLine = true, colors = coloresCampos)
                OutlinedTextField(clave, { clave = it; error = false }, modifier = Modifier.fillMaxWidth(), label = { Text("Contraseña") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, visualTransformation = PasswordVisualTransformation(), singleLine = true, colors = coloresCampos)
                if (error) Text("Usuario o contraseña incorrectos.", color = Color(0xFFDC2626), fontSize = 12.sp)
                Button(
                    onClick = {
                        scope.launch {
                            cargando = true
                            val correcto = AccesoAdmin.ingresar(context, usuario, clave)
                            cargando = false
                            if (correcto) {
                                if (AccesoAdmin.avisosActivos(context)) Toast.makeText(context, "Bienvenido al panel administrador", Toast.LENGTH_SHORT).show()
                                clave = ""
                                onLogin()
                            } else error = true
                        }
                    },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Azul, contentColor = Color.White)
                ) { Text(if (cargando) "Verificando..." else "Iniciar sesión", fontWeight = FontWeight.Bold) }
                TextButton(onClick = onBack) { Text("Volver", color = Azul) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminShell(
    alumnos: List<ProspectoEntidad>,
    tab: String,
    onTab: (String) -> Unit,
    onLogout: () -> Unit,
    onServidor: () -> Unit,
    onDetail: (ProspectoEntidad) -> Unit
) {
    val secciones = listOf("Inicio", "Alumnos", "Carreras", "Estadísticas", "Configuración")
    val anchoMovil = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 600

    Scaffold(
        containerColor = Fondo,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TherionLabs", color = AzulOscuro, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(tab, color = Color(0xFF64748B), fontSize = 12.sp)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión", tint = Azul)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (anchoMovil) {
                NavigationBar(containerColor = Color.White, tonalElevation = 5.dp) {
                    secciones.forEach { item ->
                        val icono = when (item) {
                            "Inicio" -> Icons.Default.Home
                            "Alumnos" -> Icons.Default.Groups
                            "Carreras" -> Icons.Default.School
                            "Estadísticas" -> Icons.Default.BarChart
                            else -> Icons.Default.Settings
                        }
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { onTab(item) },
                            icon = { Icon(icono, contentDescription = item) },
                            label = { Text(when (item) { "Estadísticas" -> "Estadíst."; "Configuración" -> "Ajustes"; else -> item }, fontSize = 9.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Azul,
                                selectedTextColor = AzulOscuro,
                                indicatorColor = AzulClaro,
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (!anchoMovil) {
                NavigationRail(containerColor = Color.White) {
                    secciones.forEach { item ->
                        val icono = when (item) {
                            "Inicio" -> Icons.Default.Home
                            "Alumnos" -> Icons.Default.Groups
                            "Carreras" -> Icons.Default.School
                            "Estadísticas" -> Icons.Default.BarChart
                            else -> Icons.Default.Settings
                        }
                        NavigationRailItem(
                            selected = tab == item,
                            onClick = { onTab(item) },
                            icon = { Icon(icono, contentDescription = item) },
                            label = { Text(item, fontSize = 10.sp) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
            Column(
                Modifier.weight(1f).fillMaxHeight().padding(horizontal = if (anchoMovil) 14.dp else 22.dp, vertical = 12.dp)
            ) {
                when (tab) {
                    "Inicio" -> InicioAdmin(alumnos)
                    "Alumnos" -> ListaAlumnos(alumnos, onDetail)
                    "Carreras" -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                        items(carreras) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text(item.first, fontWeight = FontWeight.Bold, color = Texto, fontSize = 16.sp)
                                    Spacer(Modifier.height(5.dp))
                                    Text(item.second, fontSize = 13.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                    "Estadísticas" -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                        item { Estadisticas(alumnos) }
                    }
                    else -> Configuracion(onLogout, alumnos, onServidor)
                }
            }
        }
    }
}

@Composable
private fun InicioAdmin(alumnos: List<ProspectoEntidad>) {
    val pendientes = alumnos.count { !it.esSincronizado }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            Text("Resumen general", color = Texto, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Text("Estado actual de la gestión académica", color = Color(0xFF64748B), fontSize = 13.sp)
        }
        item { StatCard("Total de postulaciones", alumnos.size.toString()) }
        item { StatCard("Carreras registradas", carreras.size.toString()) }
        item { StatCard("Registros pendientes", pendientes.toString()) }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Actividad reciente", fontWeight = FontWeight.Bold, color = Texto)
                    Spacer(Modifier.height(6.dp))
                    Text("Hay ${alumnos.size} alumnos registrados en el dispositivo.", color = Color(0xFF64748B), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun StatCard(titulo: String, valor: String) {
    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Groups, null, tint = Azul, modifier = Modifier.size(32.dp))
            Column(Modifier.padding(start = 12.dp)) { Text(titulo, color = Color.Gray, fontSize = 12.sp); Text(valor, color = Texto, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ListaAlumnos(alumnos: List<ProspectoEntidad>, onDetail: (ProspectoEntidad) -> Unit) {
    var buscar by remember { mutableStateOf("") }
    val filtrados = alumnos.filter {
        it.nombre.contains(buscar, true) || it.rut.contains(buscar, true) || it.cursoInteres.contains(buscar, true)
    }
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = buscar,
            onValueChange = { buscar = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar alumno por nombre, RUT o carrera") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Azul) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Azul, cursorColor = Azul, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
        )
        Spacer(Modifier.height(12.dp))
        Text("${filtrados.size} alumno(s)", color = Color(0xFF64748B), fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        if (filtrados.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay alumnos que coincidan con la búsqueda.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(filtrados, key = { it.id }) { alumno ->
                    Card(
                        Modifier.fillMaxWidth().clickable { onDetail(alumno) },
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, null, tint = Azul, modifier = Modifier.size(38.dp))
                            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                                Text(alumno.nombre, color = Texto, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("RUT: ${alumno.rut}", color = Color(0xFF64748B), fontSize = 12.sp)
                                Text(alumno.cursoInteres.ifBlank { "Carrera no especificada" }, color = Color(0xFF64748B), fontSize = 12.sp, maxLines = 2)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Azul)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleAlumnoDialog(
    alumno: ProspectoEntidad,
    onClose: () -> Unit,
    onCambiarEstado: (Int, String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Detalle del Alumno") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(alumno.nombre, fontWeight = FontWeight.Bold, color = Azul)
                Text("RUT: ${alumno.rut}")
                Text("Nacimiento: ${alumno.fechaNacimiento}")
                Text("Correo: ${alumno.correo}")
                Text("Teléfono: ${alumno.telefono}")
                Text("Dirección: ${alumno.direccion}")
                Divider()
                Text("Carrera seleccionada", fontWeight = FontWeight.Bold)
                Text(alumno.cursoInteres)
                Text(alumno.facultad)
                Text("Modalidad: ${alumno.modalidad}")
                HorizontalDivider()
                Text("Estado de la postulación", fontWeight = FontWeight.Bold)
                Text(alumno.estadoPostulacion)
                Box {
                    OutlinedButton(onClick = { expandido = true }) {
                        Text("Cambiar estado")
                    }
                    DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                        listOf("Recibida", "En revisión", "Aceptada", "Rechazada").forEach { estado ->
                            DropdownMenuItem(
                                text = { Text(estado) },
                                onClick = {
                                    expandido = false
                                    onCambiarEstado(alumno.id, estado)
                                }
                            )
                        }
                    }
                }
                if (alumno.fotoRuta.isNotBlank()) {
                    HorizontalDivider()
                    Text("Imagen adjunta", fontWeight = FontWeight.Bold)
                    VistaPreviaFoto(alumno.fotoRuta)
                }
            }
        },
        confirmButton = { Button(onClick = onClose) { Text("Cerrar") } }
    )
}

@Composable
private fun Estadisticas(alumnos: List<ProspectoEntidad>) {
    val conteo = carreras.map { it.first to alumnos.count { alumno -> alumno.cursoInteres == it.first } }
    StatCard("Total de postulaciones", alumnos.size.toString())
    Text("Carreras más solicitadas", color = Texto, fontWeight = FontWeight.Bold)
    conteo.forEach { (nombre, cantidad) ->
        Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(nombre, fontSize = 11.sp, color = Texto); Text(cantidad.toString(), fontSize = 11.sp, color = Texto) }
            val objetivo = if (alumnos.isEmpty()) 0f else cantidad.toFloat() / alumnos.size
            val progreso by animateFloatAsState(targetValue = objetivo, animationSpec = tween(650), label = "Progreso carrera")
            LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth(), color = Azul)
        }
    }
}

@Composable
private fun Configuracion(onLogout: () -> Unit, alumnos: List<ProspectoEntidad>, onServidor: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mostrarPerfil by remember { mutableStateOf(false) }
    var cambiarClave by remember { mutableStateOf(false) }
    var claveActual by remember { mutableStateOf("") }
    var claveNueva by remember { mutableStateOf("") }
    var repetir by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var avisos by remember { mutableStateOf(AccesoAdmin.avisosActivos(context)) }
    var trabajando by remember { mutableStateOf(false) }

    val exportar = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            try {
                FuncionesDocumento.exportarPostulaciones(context, uri, alumnos)
                mensaje = "Respaldo CSV guardado correctamente."
            } catch (e: Exception) {
                mensaje = "No se pudo guardar el respaldo."
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onServidor, modifier = Modifier.fillMaxWidth()) {
            Text("Postulaciones y usuarios en Spring Boot")
        }
        OutlinedButton(onClick = { mostrarPerfil = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Person, null)
            Spacer(Modifier.width(8.dp))
            Text("Perfil del administrador")
        }
        OutlinedButton(onClick = { cambiarClave = true; mensaje = "" }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Lock, null)
            Spacer(Modifier.width(8.dp))
            Text("Cambiar contraseña")
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Aviso de bienvenida", color = Texto, fontWeight = FontWeight.Bold)
                    Text("Mostrar aviso al ingresar como admin", fontSize = 11.sp)
                }
                Switch(checked = avisos, onCheckedChange = { avisos = it; AccesoAdmin.guardarAvisos(context, it) })
            }
        }
        OutlinedButton(onClick = { exportar.launch("postulaciones_therionlabs.csv") }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Download, null)
            Spacer(Modifier.width(8.dp))
            Text("Respaldar postulaciones (CSV)")
        }
        if (mensaje.isNotBlank()) Text(mensaje, color = Azul, fontSize = 12.sp)
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
    }

    if (mostrarPerfil) {
        AlertDialog(
            onDismissRequest = { mostrarPerfil = false },
            title = { Text("Perfil administrador") },
            text = { Text("Usuario: admin\nRol: Administrador local de demostración\nLos datos se almacenan en este dispositivo.") },
            confirmButton = { TextButton(onClick = { mostrarPerfil = false }) { Text("Aceptar") } }
        )
    }

    if (cambiarClave) {
        AlertDialog(
            onDismissRequest = { cambiarClave = false },
            title = { Text("Cambiar contraseña") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(claveActual, { claveActual = it }, label = { Text("Contraseña actual") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                    OutlinedTextField(claveNueva, { claveNueva = it }, label = { Text("Nueva contraseña") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                    OutlinedTextField(repetir, { repetir = it }, label = { Text("Confirmar nueva contraseña") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                    Text("Mínimo 8 caracteres, letras y números.", fontSize = 11.sp)
                    if (mensaje.isNotBlank()) Text(mensaje, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(enabled = !trabajando, onClick = {
                    when {
                        !Validaciones.claveValida(claveNueva) -> mensaje = "La contraseña nueva debe tener 8 caracteres, letras y números."
                        claveNueva == claveActual -> mensaje = "La contraseña nueva debe ser diferente."
                        claveNueva != repetir -> mensaje = "Las contraseñas no coinciden."
                        else -> scope.launch {
                            trabajando = true
                            val correcta = AccesoAdmin.cambiarClave(context, claveActual, claveNueva)
                            trabajando = false
                            if (correcta) {
                                cambiarClave = false
                                claveActual = ""; claveNueva = ""; repetir = ""
                                mensaje = "Contraseña de administrador actualizada."
                            } else mensaje = "La contraseña actual no es correcta."
                        }
                    }
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { cambiarClave = false }) { Text("Cancelar") } }
        )
    }
}

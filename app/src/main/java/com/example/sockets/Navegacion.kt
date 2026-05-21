package com.example.sockets

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kotlinx.coroutines.delay

// --- RUTAS DE NAVEGACIÓN COMPOSABLE (Type-Safe Navigation) ---
@Serializable object RutaCatalogo
@Serializable object RutaCarrito
@Serializable data class RutaVerificacion(val correo: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationContainer(urlServer: String, innerPadding: PaddingValues) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // --- ESTADOS GLOBALES COMPARTIDOS ---
    var listaProductos by remember { mutableStateOf(listOf<Producto>()) }
    var carrito by remember { mutableStateOf(mapOf<Int, Int>()) }
    var correoCliente by remember { mutableStateOf("") }

    var proformaTexto by remember { mutableStateOf<String?>(null) }
    var mostrarDialogoProforma by remember { mutableStateOf(false) }
    var codigoIngresado by remember { mutableStateOf("") }
    var mensajeErrorCodigo by remember { mutableStateOf(false) }

    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    // --- LÓGICA DE CONTROL DE CARGA ---
    var cargandoProductos by remember { mutableStateOf(true) }

    // --- MITIGACIÓN DE SPAM GLOBAL (PERSISTENTE ENTRE PANTALLAS) ---
    var cooldownVerificacion by remember { mutableIntStateOf(0) }

    LaunchedEffect(cooldownVerificacion) {
        if (cooldownVerificacion > 0) {
            delay(1000L)
            cooldownVerificacion -= 1
        }
    }

    // --- INICIALIZACIÓN DEL SOCKET Y CALLBACKS ---
    val socketManager = remember {
        SocketManager(
            onCatalogoRecibido = { productos ->
                mainHandler.post {
                    listaProductos = productos
                    cargandoProductos = false
                }
            },
            onProformaRecibida = { reporte ->
                mainHandler.post {
                    Log.d("SOCKET", "Navegando a catálogo de forma segura en el Main Thread")
                    proformaTexto = reporte
                    mostrarDialogoProforma = true
                    mensajeErrorCodigo = false
                    codigoIngresado = ""
                    cooldownVerificacion = 0 // Liberamos el cooldown ya que la proforma cerró el ciclo de compra

                    carrito = mapOf()

                    navController.navigate(RutaCatalogo) {
                        popUpTo(RutaCatalogo) { inclusive = true }
                    }
                }
            },
            onPedirCodigo = {
                mainHandler.post {
                    navController.navigate(RutaVerificacion(correo = correoCliente))
                }
            },
            onCodigoErroneo = {
                mainHandler.post { mensajeErrorCodigo = true }
            }
        )
    }

    DisposableEffect(Unit) {
        socketManager.conectar(urlServer)

        onDispose {
            Log.d("SOCKET", "Cerrando conexión de forma limpia...")
            socketManager.desconectar()
        }
    }

    // --- DIÁLOGO GLOBAL DE LA PROFORMA ---
    if (mostrarDialogoProforma && proformaTexto != null) {
        Dialog(onDismissRequest = { mostrarDialogoProforma = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.SurfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¡Compra Procesada!",
                        color = colorResource(id = R.color.TextPrimary),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Espacio sutil de separación entre título y subtítulo
                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtítulo informativo secundario (más pequeño y estilizado)
                    Text(
                        text = "Se ha enviado una copia de esta proforma a su correo electrónico.",
                        color = colorResource(id = R.color.TextSecondary), // Usamos el color secundario para atenuarlo
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(colorResource(id = R.color.DarkBackground), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        item {
                            Text(
                                text = proformaTexto!!,
                                color = colorResource(id = R.color.TextPrimary),
                                fontSize = 13.sp,
                                style = androidx.compose.ui.text.TextStyle(lineHeight = 18.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { mostrarDialogoProforma = false },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.AccentColor)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Aceptar", color = colorResource(id = R.color.TextPrimary))
                    }
                }
            }
        }
    }

    // --- CONFIGURACIÓN DEL NAVHOST DE JETPACK NAVIGATION ---
    NavHost(
        navController = navController,
        startDestination = RutaCatalogo,
        modifier = Modifier.fillMaxSize().background(colorResource(id = R.color.DarkBackground))
    ) {
        composable<RutaCatalogo> {
            CatalogoScreenView(
                listaProductos = listaProductos,
                carrito = carrito,
                isLoading = cargandoProductos,
                innerPadding = innerPadding,
                onCarritoChanged = { nuevoCarrito ->
                    carrito = nuevoCarrito
                },
                onNavigateToCarrito = {
                    if (carrito.isEmpty()) {
                        Toast.makeText(context, "El carrito está vacío", Toast.LENGTH_SHORT).show()
                    } else {
                        navController.navigate(RutaCarrito)
                    }
                }
            )
        }

        composable<RutaCarrito> {
            CarritoScreenView(
                listaProductos = listaProductos,
                carrito = carrito,
                correoCliente = correoCliente,
                innerPadding = innerPadding,
                onCarritoChanged = { nuevoCarrito -> carrito = nuevoCarrito },
                onCorreoChanged = { correoCliente = it },
                onBackNavigate = { navController.popBackStack() },
                onGestionarCompra = {
                    // INTERCEPTOR DE SEGURIDAD: Evita solicitudes redundantes si el temporizador está corriendo
                    if (cooldownVerificacion > 0) {
                        Toast.makeText(context, "Por seguridad, espera ${cooldownVerificacion}s antes de otra solicitud.", Toast.LENGTH_SHORT).show()
                        navController.navigate(RutaVerificacion(correo = correoCliente))
                        return@CarritoScreenView
                    }

                    val itemsParaEnviar = carrito.map { ItemCarrito(ProductoId = it.key, Cantidad = it.value) }

                    if (socketManager.estaConectado()) {
                        // Caso Normal: Activamos el candado de 60 segundos y enviamos
                        cooldownVerificacion = 60
                        socketManager.enviarCompra(correoCliente, itemsParaEnviar)
                    } else {
                        Toast.makeText(context, "Restableciendo conexión con el servidor C#...", Toast.LENGTH_SHORT).show()
                        cargandoProductos = true

                        navController.navigate(RutaCatalogo) {
                            popUpTo(RutaCatalogo) { inclusive = true }
                        }

                        Thread {
                            try {
                                socketManager.conectar(urlServer)
                                Thread.sleep(600)

                                mainHandler.post {
                                    if (socketManager.estaConectado()) {
                                        // Caso Recuperado: Fijamos el bloqueo global antes de retransmitir el JSON
                                        cooldownVerificacion = 60
                                        socketManager.enviarCompra(correoCliente, itemsParaEnviar)
                                    } else {
                                        cargandoProductos = false
                                        Toast.makeText(context, "No se pudo recuperar la conexión. Intenta de nuevo.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (_: Exception) {
                                mainHandler.post {
                                    cargandoProductos = false
                                    Toast.makeText(context, "Error de red: Servidor inaccesible.", Toast.LENGTH_LONG).show()
                                }
                            }
                        }.start()
                    }
                }
            )
        }

        composable<RutaVerificacion> { backStackEntry ->
            val argumentos = backStackEntry.toRoute<RutaVerificacion>()

            // Pasamos las propiedades del temporizador global a la vista de verificación
            VerificacionScreenView(
                correoCliente = argumentos.correo,
                codigoIngresado = codigoIngresado,
                mensajeErrorCodigo = mensajeErrorCodigo,
                innerPadding = innerPadding,
                cooldownRestante = cooldownVerificacion,
                onCooldownChanged = { cooldownVerificacion = it },
                onCodigoChanged = { codigoIngresado = it },
                onConfirmarCodigo = {
                    socketManager.enviarVerificacion(argumentos.correo, codigoIngresado)
                },
                onReenviarCodigo = {
                    cooldownVerificacion = 60 // Reiniciamos el bloqueo global al presionar reenvío voluntario
                    val itemsParaEnviar = carrito.map { ItemCarrito(ProductoId = it.key, Cantidad = it.value) }
                    socketManager.enviarCompra(argumentos.correo, itemsParaEnviar)
                    Toast.makeText(context, "Código reenviado a ${argumentos.correo}", Toast.LENGTH_SHORT).show()
                },
                onBackNavigate = { navController.popBackStack() }
            )
        }
    }
}
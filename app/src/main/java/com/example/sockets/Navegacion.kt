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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

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
    var carrito by remember { mutableStateOf(mutableMapOf<Int, Int>()) }
    var correoCliente by remember { mutableStateOf("") }

    var proformaTexto by remember { mutableStateOf<String?>(null) }
    var mostrarDialogoProforma by remember { mutableStateOf(false) }
    var codigoIngresado by remember { mutableStateOf("") }
    var mensajeErrorCodigo by remember { mutableStateOf(false) }

    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    // --- LÓGICA DE CONTROL DE CARGA ---
    // Inicia en true para capturar el tiempo en que el Socket despierta al servidor
    var cargandoProductos by remember { mutableStateOf(true) }

    // --- INICIALIZACIÓN DEL SOCKET Y CALLBACKS ---
    val socketManager = remember {
        SocketManager(
            onCatalogoRecibido = { productos ->
                mainHandler.post {
                    listaProductos = productos
                    cargandoProductos = false // <--- APAGA LA ANIMACIÓN: Los datos llegaron con éxito
                }
            },
            onProformaRecibida = { reporte ->
                mainHandler.post {
                    Log.d("SOCKET", "Navegando a catálogo de forma segura en el Main Thread")
                    proformaTexto = reporte
                    mostrarDialogoProforma = true
                    mensajeErrorCodigo = false
                    codigoIngresado = ""

                    carrito = mutableMapOf()

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

    // Conexión inicial al Socket del servidor C#
    LaunchedEffect(Unit) {
        socketManager.conectar(urlServer)
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
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
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
                        onClick = {
                            mostrarDialogoProforma = false
                        },
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
                isLoading = cargandoProductos, // <--- ENLACE DEL ESTADO: Pasa la bandera de carga a la vista
                innerPadding = innerPadding,
                onCarritoChanged = { carrito = it },
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
                onCarritoChanged = { carrito = it },
                onCorreoChanged = { correoCliente = it },
                onBackNavigate = { navController.popBackStack() },
                onGestionarCompra = {
                    val itemsParaEnviar = carrito.map { ItemCarrito(ProductoId = it.key, Cantidad = it.value) }
                    socketManager.enviarCompra(correoCliente, itemsParaEnviar)
                }
            )
        }

        composable<RutaVerificacion> { backStackEntry ->
            val argumentos = backStackEntry.toRoute<RutaVerificacion>()

            VerificacionScreenView(
                correoCliente = argumentos.correo,
                codigoIngresado = codigoIngresado,
                mensajeErrorCodigo = mensajeErrorCodigo,
                innerPadding = innerPadding,
                onCodigoChanged = { codigoIngresado = it },
                onConfirmarCodigo = {
                    socketManager.enviarVerificacion(argumentos.correo, codigoIngresado)
                },
                onReenviarCodigo = {
                    val itemsParaEnviar = carrito.map { ItemCarrito(ProductoId = it.key, Cantidad = it.value) }
                    socketManager.enviarCompra(argumentos.correo, itemsParaEnviar)
                    Toast.makeText(context, "Código reenviado a ${argumentos.correo}", Toast.LENGTH_SHORT).show()
                },
                onBackNavigate = { navController.popBackStack() }
            )
        }
    }
}
package com.example.sockets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding // <-- NUEVO IMPORT
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreenView(
    listaProductos: List<Producto>,
    carrito: Map<Int, Int>,
    correoCliente: String,
    innerPadding: PaddingValues,
    onCarritoChanged: (Map<Int, Int>) -> Unit,
    onCorreoChanged: (String) -> Unit,
    onBackNavigate: () -> Unit,
    onGestionarCompra: () -> Unit
) {
    val correoValido = remember(correoCliente) { isEmailValid(correoCliente) }

    val costoTotal = remember(carrito, listaProductos) {
        var suma = 0.0
        carrito.forEach { (idProducto, cantidad) ->
            val producto = listaProductos.find { it.Id == idProducto }
            if (producto != null) {
                suma += (producto.Precio * cantidad)
            }
        }
        suma
    }

    // --- EL BOTÓN DE PÁNICO: CÁLCULO MANUAL NATIVO EN PÍXELES ---
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val tecladoAbierto = imeBottom > 0

    // Convertimos los píxeles puros del teclado a unidades DP legibles por Compose
    val paddingTecladoManual = with(density) { imeBottom.toDp() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding())
    ) {
        // --- ENCABEZADO ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackNavigate) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = colorResource(id = R.color.TextPrimary)
                )
            }
            Text(
                text = "Carrito de Compras",
                color = colorResource(id = R.color.TextPrimary),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // --- LISTA DE PRODUCTOS ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val itemsCarrito = listaProductos.filter { carrito.containsKey(it.Id) }

            items(itemsCarrito) { producto ->
                val cantidad = carrito[producto.Id] ?: 0
                val subtotal = (producto.Precio * cantidad)

                Card(
                    colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.SurfaceColor)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = producto.ImagenUrl,
                            contentDescription = producto.Nombre,
                            modifier = Modifier
                                .size(65.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colorResource(id = R.color.DarkBackground)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = producto.Nombre,
                                color = colorResource(id = R.color.TextPrimary),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            Text(
                                text = "$${producto.Precio}",
                                color = colorResource(id = R.color.TextSecondary),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Cantidad: $cantidad",
                                color = colorResource(id = R.color.TextPrimary),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format(java.util.Locale.US, "%.2f", subtotal)}",
                                color = colorResource(id = R.color.AccentColor),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- PANEL DE PAGO INYECTADO ---
        Surface(
            color = colorResource(id = R.color.SurfaceColor),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                // Evitamos que el botón quede oculto bajo la barra de navegación nativa
                .padding(bottom = if (tecladoAbierto) paddingTecladoManual else 0.dp)
                .then(
                    if (!tecladoAbierto) Modifier.navigationBarsPadding()
                    else Modifier
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total a pagar:",
                        color = colorResource(id = R.color.TextPrimary),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$${String.format(java.util.Locale.US, "%.2f", costoTotal)}",
                        color = colorResource(id = R.color.TextPrimary),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedTextField(
                    value = correoCliente,
                    onValueChange = onCorreoChanged,
                    label = { Text("Correo Electrónico", color = colorResource(id = R.color.TextSecondary)) },
                    singleLine = true,
                    isError = correoCliente.isNotEmpty() && !correoValido,
                    supportingText = {
                        if (correoCliente.isNotEmpty() && !correoValido) {
                            Text("Estructura de correo inválida", color = colorResource(id = R.color.ErrorColor))
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = colorResource(id = R.color.TextPrimary),
                        unfocusedTextColor = colorResource(id = R.color.TextPrimary),
                        focusedIndicatorColor = colorResource(id = R.color.AccentColor),
                        unfocusedIndicatorColor = colorResource(id = R.color.TextSecondary),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onGestionarCompra,
                    enabled = correoValido && carrito.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.AccentColor),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Realizar pedido",
                        color = colorResource(id = R.color.TextPrimary),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
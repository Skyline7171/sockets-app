package com.example.sockets

// Importación para soporte de imágenes asíncronas con Coil
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun CatalogoScreenView(
    listaProductos: List<Producto>,
    carrito: Map<Int, Int>,
    innerPadding: PaddingValues,
    onCarritoChanged: (MutableMap<Int, Int>) -> Unit,
    onNavigateToCarrito: () -> Unit
) {
    // --- CONTROL DE ADVERTENCIAS (SNACKBAR) ---
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // --- FILTRADO DE STOCK ---
    // Solo tomamos en cuenta productos que tengan stock disponible mayor a 0
    val productosDisponibles = remember(listaProductos) {
        listaProductos.filter { it.Stock > 0 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // --- ENCABEZADO ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catálogo de Productos",
                    color = colorResource(id = R.color.TextPrimary),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNavigateToCarrito) {
                    BadgedBox(
                        badge = {
                            if (carrito.values.sum() > 0) {
                                Badge(containerColor = colorResource(id = R.color.AccentColor)) {
                                    Text(carrito.values.sum().toString(), color = colorResource(id = R.color.TextPrimary))
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Ver Carrito",
                            tint = colorResource(id = R.color.TextPrimary)
                        )
                    }
                }
            }
            Text(
                text = "Servidor remoto conectado vía Sockets",
                color = colorResource(id = R.color.TextSecondary),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- VALIDACIÓN DE LISTA VACÍA ---
            if (productosDisponibles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay ningún producto disponible",
                        color = colorResource(id = R.color.TextSecondary),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // --- LISTA DE PRODUCTOS ---
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(productosDisponibles) { producto ->
                        val cantidadEnCarrito = carrito[producto.Id] ?: 0

                        Card(
                            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.SurfaceColor)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // SECCIÓN DE IMAGEN (Adaptado para soportar URL)
                                AsyncImage(
                                    model = producto.ImagenUrl, // URL que vendrá desde el C# Server
                                    contentDescription = producto.Nombre,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colorResource(id = R.color.DarkBackground)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                // DETALLES DEL PRODUCTO
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = producto.Nombre,
                                        color = colorResource(id = R.color.TextPrimary),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Precio: $${producto.Precio}",
                                        color = colorResource(id = R.color.TextSecondary),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Disponibles: ${producto.Stock}",
                                        color = colorResource(id = R.color.TextSecondary),
                                        fontSize = 12.sp
                                    )
                                }

                                // CONTROLES DE CANTIDAD
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (cantidadEnCarrito > 0) {
                                                val nuevoCarrito = carrito.toMutableMap()
                                                if (cantidadEnCarrito == 1) nuevoCarrito.remove(producto.Id)
                                                else nuevoCarrito[producto.Id] = cantidadEnCarrito - 1
                                                onCarritoChanged(nuevoCarrito)
                                            }
                                        }
                                    ) {
                                        Text("-", color = colorResource(id = R.color.AccentColor), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        text = cantidadEnCarrito.toString(),
                                        color = colorResource(id = R.color.TextPrimary),
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        fontSize = 16.sp
                                    )

                                    IconButton(
                                        onClick = {
                                            if (cantidadEnCarrito < producto.Stock) {
                                                val nuevoCarrito = carrito.toMutableMap()
                                                nuevoCarrito[producto.Id] = cantidadEnCarrito + 1
                                                onCarritoChanged(nuevoCarrito)
                                            } else {
                                                // ADVERTENCIA DE STOCK: Lanza el mensaje si intenta exceder el límite
                                                coroutineScope.launch {
                                                    snackbarHostState.currentSnackbarData?.dismiss()
                                                    snackbarHostState.showSnackbar(
                                                        message = "No puedes añadir más unidades. ¡Stock máximo alcanzado!",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Text("+", color = colorResource(id = R.color.AccentColor), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- CONTENEDOR DEL SNACKBAR (Flota sobre la UI al fondo de la pantalla) ---
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
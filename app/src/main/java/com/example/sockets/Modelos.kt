package com.example.sockets

data class Producto(
    val Id: Int,
    val Nombre: String,
    val Precio: Double,
    var Stock: Int,
    var ImagenUrl: String
)

data class ItemCarrito(
    val ProductoId: Int,
    val Cantidad: Int
)

// Esta es la estructura exacta que tu servidor en C# espera recibir
data class SolicitudCompra(
    val Accion: String = "COMPRAR",
    val CorreoCliente: String,
    val Items: List<ItemCarrito>
)

// Para mapear la respuesta del catálogo que envía el servidor
data class RespuestaServer(
    val accion: String,
    val productos: List<Producto>? = null,
    val reporte: String? = null
)

data class SolicitudVerificacion(
    val Accion: String = "VERIFICAR_CODIGO",
    val CorreoCliente: String,
    val Codigo: String
)
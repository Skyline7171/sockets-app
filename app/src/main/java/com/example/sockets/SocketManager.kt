package com.example.sockets

import android.util.Log
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class SocketManager(
    private val onCatalogoRecibido: (List<Producto>) -> Unit,
    private val onProformaRecibida: (String) -> Unit,
    private val onPedirCodigo: () -> Unit,       // Avisa a la UI que muestre el campo del código
    private val onCodigoErroneo: () -> Unit     // Avisa a la UI que el código falló
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val gson = Gson()

    fun conectar(url: String) {
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("SOCKET", "¡Conectado exitosamente al servidor!")
            }

            // SOBRECARGA PARA TEXTO PLANO (La que usa C# por defecto)
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("SOCKET", "Mensaje recibido (Texto): $text")
                procesarMensajeJson(text)
            }

            // SOBRECARGA PARA TRAMAS DE BYTES (Protección ante pérdidas de bytes)
            override fun onMessage(webSocket: WebSocket, bytes: okio.ByteString) {
                // Forzamos la decodificación manual a UTF-8 directo de los bytes binarios
                val text = bytes.string(Charsets.UTF_8)
                Log.d("SOCKET", "Mensaje recibido (Bytes forzados a UTF-8): $text")
                procesarMensajeJson(text)
            }

            // Función auxiliar interna para no duplicar el código de parseo
            private fun procesarMensajeJson(jsonText: String) {
                try {
                    val respuesta = gson.fromJson(jsonText, RespuestaServer::class.java)

                    // Evaluamos las acciones que nos manda el backend en C#
                    when (respuesta.accion) {
                        "CATALOGO" -> respuesta.productos?.let { onCatalogoRecibido(it) }
                        "PEDIR_CODIGO" -> onPedirCodigo()
                        "CODIGO_ERRONEO" -> onCodigoErroneo()
                        "PROFORMA" -> respuesta.reporte?.let { onProformaRecibida(it) }
                    }
                } catch (e: Exception) {
                    Log.e("SOCKET", "Error parseando JSON: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("SOCKET", "Fallo en la conexión: ${t.message}")
            }
        })
    }

    fun enviarCompra(correo: String, items: List<ItemCarrito>) {
        val solicitud = SolicitudCompra(CorreoCliente = correo, Items = items)
        val jsonStr = gson.toJson(solicitud)
        webSocket?.send(jsonStr)
    }

    // Despacha el código a C# para su validación
    fun enviarVerificacion(correo: String, codigo: String) {
        val solicitud = SolicitudVerificacion(CorreoCliente = correo, Codigo = codigo)
        val jsonStr = gson.toJson(solicitud)
        webSocket?.send(jsonStr)
        Log.d("SOCKET", "Enviando código para verificar: $jsonStr")
    }

    fun desconectar() {
        webSocket?.close(1000, "App cerrada")
    }
}
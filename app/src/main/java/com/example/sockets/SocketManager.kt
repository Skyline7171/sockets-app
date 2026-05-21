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

    // --- NUEVOS ESTADOS DE CONTROL DE RED ---
    private var isConnected = false
    private var isConnecting = false

    /**
     * Valida si el socket está activo y listo para transmitir datos.
     * Es la función clave que llamará tu NavHost antes de procesar la compra.
     */
    fun estaConectado(): Boolean {
        return webSocket != null && isConnected
    }

    fun conectar(url: String) {
        // Si ya está conectado o en proceso de conexión, evitamos duplicar sockets
        if (isConnected || isConnecting) {
            Log.d("SOCKET", "Conexión omitida: Ya existe un canal activo o en proceso.")
            return
        }

        isConnecting = true
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("SOCKET", "¡Conectado exitosamente al servidor!")
                isConnected = true
                isConnecting = false
            }

            // SOBRECARGA PARA TEXTO PLANO (La que usa C# por defecto)
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("SOCKET", "Mensaje recibido (Texto): $text")
                procesarMensajeJson(text)
            }

            // SOBRECARGA PARA TRAMAS DE BYTES (Protección ante pérdidas de bytes)
            override fun onMessage(webSocket: WebSocket, bytes: okio.ByteString) {
                val text = bytes.string(Charsets.UTF_8)
                Log.d("SOCKET", "Mensaje recibido (Bytes forzados a UTF-8): $text")
                procesarMensajeJson(text)
            }

            private fun procesarMensajeJson(jsonText: String) {
                try {
                    val respuesta = gson.fromJson(jsonText, RespuestaServer::class.java)

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

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("SOCKET", "El servidor está cerrando la conexión... Código: $code")
                marcarDesconectado()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("SOCKET", "Conexión del socket completamente cerrada de forma limpia.")
                marcarDesconectado()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("SOCKET", "Fallo en la conexión: ${t.message}")
                marcarDesconectado()
            }

            // Función interna para resetear las banderas limpiamente en cualquier falla o cierre
            private fun marcarDesconectado() {
                isConnected = false
                isConnecting = false
            }
        })
    }

    fun enviarCompra(correo: String, items: List<ItemCarrito>) {
        val solicitud = SolicitudCompra(CorreoCliente = correo, Items = items)
        val jsonStr = gson.toJson(solicitud)
        webSocket?.send(jsonStr)
    }

    fun enviarVerificacion(correo: String, codigo: String) {
        val solicitud = SolicitudVerificacion(CorreoCliente = correo, Codigo = codigo)
        val jsonStr = gson.toJson(solicitud)
        webSocket?.send(jsonStr)
        Log.d("SOCKET", "Enviando código para verificar: $jsonStr")
    }

    fun desconectar() {
        webSocket?.close(1000, "App cerrada")
        isConnected = false
        isConnecting = false
    }
}
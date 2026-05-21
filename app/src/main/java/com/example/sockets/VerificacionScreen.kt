package com.example.sockets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets // <-- REQUERIDO
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime // <-- REQUERIDO
import androidx.compose.foundation.layout.navigationBarsPadding // <-- NUEVO IMPORT
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificacionScreenView(
    correoCliente: String,
    codigoIngresado: String,
    mensajeErrorCodigo: Boolean,
    innerPadding: PaddingValues,
    cooldownRestante: Int,
    onCooldownChanged: (Int) -> Unit,
    onCodigoChanged: (String) -> Unit,
    onConfirmarCodigo: () -> Unit,
    onReenviarCodigo: () -> Unit,
    onBackNavigate: () -> Unit
) {
    val esBotonHabilitado = cooldownRestante == 0
    val scrollState = rememberScrollState()

    // --- MEDICIÓN MANUAL DEL TECLADO PARA ADAPTACIÓN ANTIFANTASMA ---
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val tecladoAbierto = imeBottom > 0

    // Transformamos los píxeles puros leídos en la unidad de medida de Compose
    val paddingTecladoManual = with(density) { imeBottom.toDp() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Respetamos la barra superior (top), pero dejamos que el bottom se calcule abajo dinámicamente
            .padding(top = innerPadding.calculateTopPadding())
            // Si el teclado está abierto, inyectamos su espacio medido al contenedor principal.
            .padding(bottom = if (tecladoAbierto) paddingTecladoManual else 0.dp)
            // Evitamos que la barra de botones nativa de Android
            // opaque o tape los elementos inferiores cuando el teclado esté guardado.
            .then(
                if (!tecladoAbierto) Modifier.navigationBarsPadding()
                else Modifier
            )
    ) {
        // --- CONTENIDO CENTRAL (Con física de Scroll) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Un pequeño espacio arriba para que el título no choque con el botón de regreso si el teclado empuja todo
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Verificación de Seguridad",
                color = colorResource(id = R.color.TextPrimary),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Ingresa el código enviado a:\n$correoCliente",
                color = colorResource(id = R.color.TextSecondary),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = codigoIngresado,
                onValueChange = onCodigoChanged,
                label = { Text("Código de 4 dígitos", color = colorResource(id = R.color.TextSecondary)) },
                singleLine = true,
                isError = mensajeErrorCodigo,
                supportingText = {
                    if (mensajeErrorCodigo) {
                        Text("El código es incorrecto, intenta de nuevo.", color = colorResource(id = R.color.ErrorColor))
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = onConfirmarCodigo,
                enabled = codigoIngresado.length >= 4,
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.AccentColor)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirmar código", color = colorResource(id = R.color.TextPrimary), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = {
                    if (esBotonHabilitado) {
                        onReenviarCodigo()
                    }
                },
                enabled = esBotonHabilitado,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorResource(id = R.color.AccentColor),
                    disabledContentColor = colorResource(id = R.color.TextSecondary)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = if (esBotonHabilitado) colorResource(id = R.color.AccentColor) else colorResource(id = R.color.TextSecondary),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (esBotonHabilitado) {
                            "¿Reenviar código?"
                        } else {
                            "Reenviar código estará disponible en ${cooldownRestante}s"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Espaciador final para darle holgura al scroll cuando el teclado esté completamente desplegado
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- BOTÓN DE REGRESO (Flota de forma independiente en la esquina superior izquierda) ---
        IconButton(
            onClick = onBackNavigate,
            modifier = Modifier
                .padding(start = 12.dp, top = 12.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Regresar al carrito",
                tint = colorResource(id = R.color.TextPrimary)
            )
        }
    }
}
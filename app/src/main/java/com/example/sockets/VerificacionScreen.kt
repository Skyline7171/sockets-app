package com.example.sockets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onCodigoChanged: (String) -> Unit,
    onConfirmarCodigo: () -> Unit,
    onReenviarCodigo: () -> Unit,
    onBackNavigate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Verificación de Seguridad", color = colorResource(id = R.color.TextPrimary), fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "Ingresa el código enviado a:\n$correoCliente", color = colorResource(id = R.color.TextSecondary), fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 24.dp))

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
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        Button(
            onClick = onConfirmarCodigo,
            enabled = codigoIngresado.length >= 4,
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.AccentColor)),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Confirmar Código", color = colorResource(id = R.color.TextPrimary), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onReenviarCodigo) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = colorResource(id = R.color.AccentColor), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "¿Reenviar código?", color = colorResource(id = R.color.AccentColor), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
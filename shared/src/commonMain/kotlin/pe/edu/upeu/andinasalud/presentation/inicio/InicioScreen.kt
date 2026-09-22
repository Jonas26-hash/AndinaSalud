package pe.edu.upeu.andinasalud.presentation.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.andinasalud.presentation.citas.CitasUiState
import pe.edu.upeu.andinasalud.presentation.components.CitaCard
import pe.edu.upeu.andinasalud.presentation.components.EstadoVacio


@Composable
fun InicioScreen(
    uiState: CitasUiState,
    onReintentar: () -> Unit,
    onVerCitas: () -> Unit,
    onSolicitarCita: () -> Unit,
    modifier: Modifier = Modifier
) {

    when (val fase = uiState.fase) {

        is CitasUiState.Fase.Cargando -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is CitasUiState.Fase.Error -> {
            Box(modifier = modifier.fillMaxSize()) {
                EstadoVacio(
                    icono = Icons.Default.Info,
                    titulo = "No se pudo cargar el inicio",
                    descripcion = fase.mensaje,
                    accion = {
                        OutlinedButton(onClick = onReintentar) {
                            Text("Reintentar")
                        }
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        else -> {
            InicioConContenido(
                uiState = uiState,
                onVerCitas = onVerCitas,
                onSolicitarCita = onSolicitarCita,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun InicioConContenido(
    uiState: CitasUiState,
    onVerCitas: () -> Unit,
    onSolicitarCita: () -> Unit,
    modifier: Modifier = Modifier
) {

    val nombre = uiState.paciente?.nombre
        ?.split(" ")
        ?.firstOrNull()
        .orEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Hola, $nombre",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Aqui estan tus citas en AndinaSalud.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "PRÓXIMA CITA",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        val proxima = uiState.proximaCita
        if (proxima != null) {
            CitaCard(
                cita = proxima,
                onClick = onVerCitas,
                icono = Icons.Default.Event
            )
        } else {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "No tienes citas programadas",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Pide una cita y la veras aqui.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onVerCitas,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null
                    )
                    Text("Ver citas")
                }

                Button(
                    onClick = onSolicitarCita,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Text("Solicitar")
                }
            }
        }
    }
}
package pe.edu.upeu.andinasalud.presentation.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upeu.andinasalud.domain.model.EstadoCita
import pe.edu.upeu.andinasalud.presentation.citas.CitaUi
import pe.edu.upeu.andinasalud.presentation.citas.etiqueta
import pe.edu.upeu.andinasalud.presentation.components.EstadoVacio


@Composable
fun DetalleCitaScreen(
    viewModel: DetalleCitaViewModel,
    onCitaCancelada: () -> Unit,
    modifier: Modifier = Modifier
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.canceladaExitosamente) {
        if (uiState.canceladaExitosamente) onCitaCancelada()
    }

    when (val fase = uiState.fase) {

        is DetalleCitaUiState.Fase.Cargando -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        DetalleCitaUiState.Fase.SinCitas -> {
            Box(modifier = modifier.fillMaxSize()) {
                EstadoVacio(
                    icono = Icons.Default.Info,
                    titulo = "Cita no encontrada",
                    descripcion = "La cita que buscas ya no está disponible.",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        is DetalleCitaUiState.Fase.Error -> {
            Box(modifier = modifier.fillMaxSize()) {
                EstadoVacio(
                    icono = Icons.Default.Info,
                    titulo = "No se pudo cargar la cita",
                    descripcion = fase.mensaje,
                    accion = {
                        OutlinedButton(onClick = viewModel::cargar) {
                            Text("Reintentar")
                        }
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        is DetalleCitaUiState.Fase.ConContenido -> {
            DetalleConContenido(
                cita = fase.cita,
                cancelando = uiState.cancelando,
                errorCancelacion = uiState.errorCancelacion,
                onCancelar = viewModel::solicitarConfirmacionCancelacion,
                modifier = modifier
            )

            if (uiState.mostrandoDialogoCancelacion) {
                AlertDialog(
                    onDismissRequest = viewModel::descartarConfirmacionCancelacion,
                    title = { Text("Cancelar cita") },
                    text = {
                        Text(
                            "${fase.cita.especialidad} del ${fase.cita.fechaTexto} a las " +
                                "${fase.cita.horaTexto}. Esta acción no se puede deshacer."
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = viewModel::cancelar,
                            enabled = !uiState.cancelando
                        ) {
                            Text(if (uiState.cancelando) "Cancelando…" else "Confirmar")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = viewModel::descartarConfirmacionCancelacion) {
                            Text("Volver")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DetalleConContenido(
    cita: CitaUi,
    cancelando: Boolean,
    errorCancelacion: String?,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = cita.especialidad,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    DetalleFila(Icons.Default.Person, "Médico", cita.medicoNombre)
                    DetalleFila(Icons.Default.Place, "Sede", cita.sedeNombre)
                    DetalleFila(Icons.Default.CalendarMonth, "Fecha", cita.fechaTexto)
                    DetalleFila(Icons.Default.Schedule, "Hora", cita.horaTexto)

                    Text(
                        text = "Estado: ${cita.estado.etiqueta()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    when (val estado = cita.estado) {
                        is EstadoCita.Programada -> Text(
                            text = if (estado.recordatorioActivo) {
                                "Recordatorio activo para esta cita."
                            } else {
                                "Esta cita no tiene recordatorio activado."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        is EstadoCita.Atendida -> DetalleFila(
                            Icons.Default.Info,
                            "Indicaciones",
                            estado.indicaciones
                        )

                        is EstadoCita.Cancelada -> DetalleFila(
                            Icons.Default.Info,
                            "Motivo de cancelación",
                            estado.motivo +
                                if (estado.canceladaPorPaciente) " (cancelada por el paciente)" else ""
                        )
                    }
                }
            }
        }

        if (cita.esProgramada) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onCancelar,
                        enabled = !cancelando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(if (cancelando) "Cancelando…" else "Cancelar cita")
                    }

                    if (errorCancelacion != null) {
                        Text(
                            text = errorCancelacion,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleFila(
    icono: ImageVector,
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {

    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
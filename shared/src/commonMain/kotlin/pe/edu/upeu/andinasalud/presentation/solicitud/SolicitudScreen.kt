package pe.edu.upeu.andinasalud.presentation.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upeu.andinasalud.presentation.components.DesplegableOpciones
import pe.edu.upeu.andinasalud.presentation.components.EstadoVacio
import pe.edu.upeu.andinasalud.presentation.components.ValidatedTextField


@Composable
fun SolicitudScreen(
    viewModel: SolicitudViewModel,
    onCitaRegistrada: () -> Unit,
    modifier: Modifier = Modifier
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.registradaExitosamente) {
        if (uiState.registradaExitosamente) onCitaRegistrada()
    }

    when (val fase = uiState.fase) {

        is SolicitudUiState.Fase.Cargando -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is SolicitudUiState.Fase.Error -> {
            Box(modifier = modifier.fillMaxSize()) {
                EstadoVacio(
                    icono = Icons.Default.Info,
                    titulo = "No se pudieron cargar las opciones",
                    descripcion = fase.mensaje,
                    accion = {
                        OutlinedButton(onClick = viewModel::cargarCatalogos) {
                            Text("Reintentar")
                        }
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        is SolicitudUiState.Fase.ListaCargada -> {
            FormularioSolicitud(
                especialidades = fase.especialidades,
                sedes = fase.sedes,
                formulario = uiState.formulario,
                enviando = uiState.enviando,
                onEspecialidadChange = viewModel::onEspecialidadChange,
                onSedeChange = viewModel::onSedeChange,
                onFechaChange = viewModel::onFechaChange,
                onHoraChange = viewModel::onHoraChange,
                onMotivoChange = viewModel::onMotivoChange,
                onSolicitar = viewModel::solicitar,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun FormularioSolicitud(
    especialidades: List<String>,
    sedes: List<String>,
    formulario: FormularioSolicitud,
    enviando: Boolean,
    onEspecialidadChange: (String) -> Unit,
    onSedeChange: (String) -> Unit,
    onFechaChange: (String) -> Unit,
    onHoraChange: (String) -> Unit,
    onMotivoChange: (String) -> Unit,
    onSolicitar: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Completa los datos y presiona solicitar. Tu cita quedará " +
                "registrada en la memoria de esta clínica.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        DesplegableOpciones(
            opciones = especialidades,
            seleccion = formulario.especialidad.takeIf { it.isNotBlank() },
            onSeleccion = onEspecialidadChange,
            label = "Especialidad",
            leadingIcon = Icons.Default.MedicalServices,
            error = formulario.especialidadError
        )

        DesplegableOpciones(
            opciones = sedes,
            seleccion = formulario.sede.takeIf { it.isNotBlank() },
            onSeleccion = onSedeChange,
            label = "Sede",
            leadingIcon = Icons.Default.Place,
            error = formulario.sedeError
        )

        ValidatedTextField(
            value = formulario.fecha,
            onValueChange = onFechaChange,
            label = "Fecha",
            error = formulario.fechaError,
            leadingIcon = Icons.Default.CalendarMonth,
            ayuda = "Formato AAAA-MM-DD, por ejemplo 2026-10-05",
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Ascii
        )

        ValidatedTextField(
            value = formulario.hora,
            onValueChange = onHoraChange,
            label = "Hora",
            error = formulario.horaError,
            leadingIcon = Icons.Default.Schedule,
            ayuda = "Formato HH:MM en hora local, por ejemplo 14:30",
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Ascii
        )

        ValidatedTextField(
            value = formulario.motivo,
            onValueChange = onMotivoChange,
            label = "Motivo",
            error = formulario.motivoError,
            leadingIcon = null,
            ayuda = if (formulario.motivoError == null) {
                "Escribe entre 10 y 200 caracteres"
            } else null,
            singleLine = false,
            minLines = 3
        )

        if (formulario.errorGeneral != null) {
            Text(
                text = formulario.errorGeneral,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = onSolicitar,
            enabled = !enviando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (enviando) "Guardando…" else "Solicitar cita")
        }
    }
}
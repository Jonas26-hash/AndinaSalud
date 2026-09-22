package pe.edu.upeu.andinasalud.presentation.citas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.andinasalud.presentation.components.CitaCard
import pe.edu.upeu.andinasalud.presentation.components.EstadoVacio


@Composable
fun CitasScreen(
    uiState: CitasUiState,
    onBusquedaChange: (String) -> Unit,
    onFiltroChange: (CitasUiState.FiltroCita) -> Unit,
    onCitaClick: (Long) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        OutlinedTextField(
            value = uiState.busqueda,
            onValueChange = onBusquedaChange,
            placeholder = { Text("Buscar por especialidad o médico") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = if (uiState.busqueda.isNotEmpty()) {
                {
                    IconButton(onClick = { onBusquedaChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar búsqueda"
                        )
                    }
                }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        FiltroEstados(
            seleccionado = uiState.filtro,
            onSeleccion = onFiltroChange
        )

        Box(modifier = Modifier.fillMaxSize()) {

            when (val fase = uiState.fase) {

                is CitasUiState.Fase.Cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is CitasUiState.Fase.Error -> {
                    EstadoVacio(
                        icono = Icons.Default.Info,
                        titulo = "No se pudieron cargar las citas",
                        descripcion = fase.mensaje,
                        accion = {
                            OutlinedButton(onClick = onReintentar) {
                                Text("Reintentar")
                            }
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                CitasUiState.Fase.SinCitas -> {
                    EstadoVacio(
                        icono = Icons.Default.Info,
                        titulo = "Aún no tienes citas",
                        descripcion = "Cuando pidas una cita, aparecerá aqui.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is CitasUiState.Fase.ConContenido -> {
                    if (uiState.citasVisibles.isEmpty()) {
                        EstadoVacio(
                            icono = Icons.Default.Search,
                            titulo = "Sin resultados",
                            descripcion = "No hay citas que coincidan con la búsqueda " +
                                "o el filtro seleccionado.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = uiState.citasVisibles,
                                key = { it.id }
                            ) { cita ->
                                CitaCard(
                                    cita = cita,
                                    onClick = { onCitaClick(cita.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun FiltroEstados(
    seleccionado: CitasUiState.FiltroCita,
    onSeleccion: (CitasUiState.FiltroCita) -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        CitasUiState.FiltroCita.entries.forEach { opcion ->
            FilterChip(
                selected = seleccionado == opcion,
                onClick = { onSeleccion(opcion) },
                label = { Text(opcion.nombreVisible()) }
            )
        }
    }
}

private fun CitasUiState.FiltroCita.nombreVisible(): String = when (this) {
    CitasUiState.FiltroCita.Todas -> "Todas"
    CitasUiState.FiltroCita.Programadas -> "Programadas"
    CitasUiState.FiltroCita.Atendidas -> "Atendidas"
    CitasUiState.FiltroCita.Canceladas -> "Canceladas"
}
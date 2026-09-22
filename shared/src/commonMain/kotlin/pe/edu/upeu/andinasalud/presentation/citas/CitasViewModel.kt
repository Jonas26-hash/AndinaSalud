package pe.edu.upeu.andinasalud.presentation.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.andinasalud.domain.model.Cita
import pe.edu.upeu.andinasalud.domain.model.EstadoCita
import pe.edu.upeu.andinasalud.domain.model.Paciente
import pe.edu.upeu.andinasalud.domain.usecase.ObtenerCitasUseCase


class CitasViewModel(
    private val obtenerCitas: ObtenerCitasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CitasUiState())
    val uiState: StateFlow<CitasUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = CitasUiState.Fase.Cargando) }

            runCatching { cargarEnParalelo() }.fold(
                onSuccess = { (paciente, citas) ->
                    if (citas.isEmpty()) {
                        _uiState.update {
                            it.copy(fase = CitasUiState.Fase.SinCitas, paciente = paciente)
                        }
                        recalcularVisibles()
                    } else {
                        val citasUi = citas.map { it.aUi() }
                        _uiState.update {
                            it.copy(
                                fase = CitasUiState.Fase.ConContenido(citasUi),
                                paciente = paciente
                            )
                        }
                        recalcularVisibles()
                    }
                },
                onFailure = { fallo ->
                    _uiState.update {
                        it.copy(
                            fase = CitasUiState.Fase.Error(
                                fallo.message ?: "No se pudieron cargar las citas"
                            )
                        )
                    }
                }
            )
        }
    }

    private suspend fun cargarEnParalelo(): Pair<Paciente, List<Cita>> = coroutineScope {
        val paciente = async { obtenerCitas.paciente() }
        val citas = async { obtenerCitas() }
        paciente.await() to citas.await()
    }

    fun onFiltroChange(filtro: CitasUiState.FiltroCita) {
        _uiState.update { it.copy(filtro = filtro) }
        recalcularVisibles()
    }

    fun onBusquedaChange(busqueda: String) {
        _uiState.update { it.copy(busqueda = busqueda) }
        recalcularVisibles()
    }

    private fun recalcularVisibles() {
        val estado = _uiState.value
        val base = (estado.fase as? CitasUiState.Fase.ConContenido)?.citas.orEmpty()

        val visibles = base
            .filter { cumpleFiltro(it, estado.filtro) }
            .filter { cumpleBusqueda(it, estado.busqueda) }

        _uiState.update {
            it.copy(
                citasVisibles = visibles,


                proximaCita = base.firstOrNull { it.esProgramada }
            )
        }
    }

    private fun cumpleFiltro(cita: CitaUi, filtro: CitasUiState.FiltroCita): Boolean =
        when (filtro) {
            CitasUiState.FiltroCita.Todas -> true
            CitasUiState.FiltroCita.Programadas -> cita.estado is EstadoCita.Programada
            CitasUiState.FiltroCita.Atendidas -> cita.estado is EstadoCita.Atendida
            CitasUiState.FiltroCita.Canceladas -> cita.estado is EstadoCita.Cancelada
        }


    private fun cumpleBusqueda(cita: CitaUi, busqueda: String): Boolean {
        val termino = normalizarParaBusqueda(busqueda)
        if (termino.isEmpty()) return true

        return normalizarParaBusqueda(cita.especialidad).contains(termino) ||
            normalizarParaBusqueda(cita.medicoNombre).contains(termino)
    }
}
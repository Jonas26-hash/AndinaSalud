package pe.edu.upeu.andinasalud.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.andinasalud.domain.usecase.CancelarCitaUseCase
import pe.edu.upeu.andinasalud.domain.usecase.ObtenerCitasUseCase
import pe.edu.upeu.andinasalud.presentation.citas.aUi


class DetalleCitaViewModel(
    private val citaId: Long,
    private val obtenerCitas: ObtenerCitasUseCase,
    private val cancelarCita: CancelarCitaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleCitaUiState())
    val uiState: StateFlow<DetalleCitaUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = DetalleCitaUiState.Fase.Cargando) }

            runCatching { obtenerCitas.porId(citaId) }.fold(
                onSuccess = { cita ->
                    if (cita == null) {
                        _uiState.update { it.copy(fase = DetalleCitaUiState.Fase.SinCitas) }
                    } else {
                        _uiState.update {
                            it.copy(fase = DetalleCitaUiState.Fase.ConContenido(cita.aUi()))
                        }
                    }
                },
                onFailure = { fallo ->
                    _uiState.update {
                        it.copy(
                            fase = DetalleCitaUiState.Fase.Error(
                                fallo.message ?: "No se pudo cargar el detalle de la cita"
                            )
                        )
                    }
                }
            )
        }
    }

    fun solicitarConfirmacionCancelacion() {
        _uiState.update { it.copy(mostrandoDialogoCancelacion = true) }
    }

    fun descartarConfirmacionCancelacion() {
        _uiState.update { it.copy(mostrandoDialogoCancelacion = false) }
    }

    fun cancelar() {
        if (_uiState.value.cancelando) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(cancelando = true, errorCancelacion = null)
            }

            cancelarCita(citaId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            cancelando = false,
                            mostrandoDialogoCancelacion = false,
                            canceladaExitosamente = true
                        )
                    }
                },
                onFailure = { fallo ->
                    _uiState.update {
                        it.copy(
                            cancelando = false,
                            mostrandoDialogoCancelacion = false,
                            errorCancelacion = fallo.message
                                ?: "No se pudo cancelar la cita. Inténtalo de nuevo."
                        )
                    }
                }
            )
        }
    }
}
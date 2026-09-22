package pe.edu.upeu.andinasalud.presentation.solicitud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.andinasalud.domain.usecase.ObtenerCitasUseCase
import pe.edu.upeu.andinasalud.domain.usecase.SolicitarCitaUseCase
import pe.edu.upeu.andinasalud.domain.usecase.SolicitudInvalidaException


class SolicitudViewModel(
    private val obtenerCitas: ObtenerCitasUseCase,
    private val solicitarCita: SolicitarCitaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolicitudUiState())
    val uiState: StateFlow<SolicitudUiState> = _uiState.asStateFlow()

    init {
        cargarCatalogos()
    }

    fun cargarCatalogos() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = SolicitudUiState.Fase.Cargando) }

            runCatching { cargarListas() }.fold(
                onSuccess = { (especialidades, sedes) ->
                    _uiState.update {
                        it.copy(fase = SolicitudUiState.Fase.ListaCargada(especialidades, sedes))
                    }
                },
                onFailure = { fallo ->
                    _uiState.update {
                        it.copy(
                            fase = SolicitudUiState.Fase.Error(
                                fallo.message ?: "No se pudieron cargar las opciones"
                            )
                        )
                    }
                }
            )
        }
    }

    private suspend fun cargarListas(): Pair<List<String>, List<String>> = coroutineScope {
        val especialidades = async { obtenerCitas.especialidades() }
        val sedes = async { obtenerCitas.sedes().map { it.nombre } }
        especialidades.await() to sedes.await()
    }

    fun onEspecialidadChange(especialidad: String) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(especialidad = especialidad, especialidadError = null))
        }
    }

    fun onSedeChange(sede: String) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(sede = sede, sedeError = null))
        }
    }

    fun onFechaChange(fecha: String) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(fecha = fecha, fechaError = null))
        }
    }

    fun onHoraChange(hora: String) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(hora = hora, horaError = null))
        }
    }

    fun onMotivoChange(motivo: String) {
        _uiState.update {
            it.copy(formulario = it.formulario.copy(motivo = motivo, motivoError = null))
        }
    }

    fun solicitar() {
        if (_uiState.value.enviando) return

        val f = _uiState.value.formulario

        _uiState.update { it.copy(enviando = true, formulario = it.formulario.copy(errorGeneral = null)) }

        viewModelScope.launch {

            solicitarCita(
                especialidad = f.especialidad,
                sede = f.sede,
                fecha = f.fecha,
                hora = f.hora,
                motivo = f.motivo
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(enviando = false, registradaExitosamente = true)
                    }
                },
                onFailure = { fallo ->
                    val errores = (fallo as? SolicitudInvalidaException)?.errores
                    _uiState.update {
                        it.copy(
                            enviando = false,
                            formulario = it.formulario.copy(
                                especialidadError = errores?.especialidad,
                                sedeError = errores?.sede,
                                fechaError = errores?.fecha,
                                horaError = errores?.hora,
                                motivoError = errores?.motivo,
                                errorGeneral = if (errores != null) {
                                    errores.general
                                } else {
                                    fallo.message ?: "No se pudo registrar la cita."
                                }
                            )
                        )
                    }
                }
            )
        }
    }
}
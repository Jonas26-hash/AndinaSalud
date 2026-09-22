package pe.edu.upeu.andinasalud.presentation.detalle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import pe.edu.upeu.andinasalud.data.local.CitasSimuladas
import pe.edu.upeu.andinasalud.domain.usecase.CancelarCitaUseCase
import pe.edu.upeu.andinasalud.domain.usecase.ObtenerCitasUseCase
import pe.edu.upeu.andinasalud.fakes.CitasDePrueba
import pe.edu.upeu.andinasalud.fakes.FakeCitaRepository
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


@OptIn(ExperimentalCoroutinesApi::class)
class DetalleCitaViewModelTest {

    @AfterTest
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    private fun viewModelDe(
        id: Long,
        repositorio: FakeCitaRepository
    ): DetalleCitaViewModel = DetalleCitaViewModel(
        citaId = id,
        obtenerCitas = ObtenerCitasUseCase(repositorio),
        cancelarCita = CancelarCitaUseCase(repositorio)
    )

    @Test
    fun `carga el detalle de una cita existente`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val repositorio = FakeCitaRepository(CitasSimuladas.citas(ancla = LocalDate.parse("2026-01-10")))
        val viewModel = viewModelDe(1, repositorio)
        advanceUntilIdle()

        val fase = assertIs<DetalleCitaUiState.Fase.ConContenido>(viewModel.uiState.value.fase)
        assertEquals(1, fase.cita.id)
        assertEquals("Medicina General", fase.cita.especialidad)
        assertTrue(fase.cita.esProgramada)
    }

    @Test
    fun `un id desconocido muestra SinCitas`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val repositorio = FakeCitaRepository(CitasSimuladas.citas(ancla = LocalDate.parse("2026-01-10")))
        val viewModel = viewModelDe(999, repositorio)
        advanceUntilIdle()

        assertEquals(DetalleCitaUiState.Fase.SinCitas, viewModel.uiState.value.fase)
    }

    @Test
    fun `un fallo del repositorio llega como fase de error`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val repositorio = FakeCitaRepository(CitasSimuladas.citas(LocalDate.parse("2026-01-10")))
        repositorio.errorSimulado = RuntimeException("Servidor no disponible")
        val viewModel = viewModelDe(1, repositorio)
        advanceUntilIdle()

        val fase = assertIs<DetalleCitaUiState.Fase.Error>(viewModel.uiState.value.fase)
        assertEquals("Servidor no disponible", fase.mensaje)
    }

    @Test
    fun `la cancelacion pasa por el dialogo y termina con exito`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.programada(1, LocalDate.parse("2100-01-20"), LocalTime(9, 0))
            )
        )
        val viewModel = viewModelDe(1, repositorio)
        advanceUntilIdle()

        viewModel.solicitarConfirmacionCancelacion()
        assertTrue(viewModel.uiState.value.mostrandoDialogoCancelacion)

        viewModel.cancelar()
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertTrue(estado.canceladaExitosamente)
        assertNull(estado.errorCancelacion)
        assertTrue(
            repositorio.registros.first { it.id == 1L }.estado
                is pe.edu.upeu.andinasalud.domain.model.EstadoCita.Cancelada
        )
    }

    @Test
    fun `descartar el dialogo no cancela la cita`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.programada(1, LocalDate.parse("2100-01-20"), LocalTime(9, 0))
            )
        )
        val viewModel = viewModelDe(1, repositorio)
        advanceUntilIdle()

        viewModel.solicitarConfirmacionCancelacion()
        viewModel.descartarConfirmacionCancelacion()

        assertTrue(
            repositorio.registros.first { it.id == 1L }.estado
                is pe.edu.upeu.andinasalud.domain.model.EstadoCita.Programada
        )
    }

    @Test
    fun `cancelar una cita atendida informa el error`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.atendida(1, LocalDate.parse("2026-01-05"))
            )
        )
        val viewModel = viewModelDe(1, repositorio)
        advanceUntilIdle()

        viewModel.solicitarConfirmacionCancelacion()
        viewModel.cancelar()
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertNotNull(estado.errorCancelacion)
        assertTrue(!estado.canceladaExitosamente)
    }
}
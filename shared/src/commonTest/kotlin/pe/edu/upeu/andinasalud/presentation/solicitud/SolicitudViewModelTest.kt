package pe.edu.upeu.andinasalud.presentation.solicitud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.andinasalud.domain.usecase.ObtenerCitasUseCase
import pe.edu.upeu.andinasalud.domain.usecase.SolicitarCitaUseCase
import pe.edu.upeu.andinasalud.fakes.FakeCitaRepository
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


@OptIn(ExperimentalCoroutinesApi::class)
class SolicitudViewModelTest {

    @AfterTest
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    private fun viewModelDe(repositorio: FakeCitaRepository): SolicitudViewModel =
        SolicitudViewModel(
            obtenerCitas = ObtenerCitasUseCase(repositorio),
            solicitarCita = SolicitarCitaUseCase(repositorio)
        )

    private fun llenarFormularioValido(viewModel: SolicitudViewModel) {
        viewModel.onEspecialidadChange("Medicina General")
        viewModel.onSedeChange("Ñaña")
        viewModel.onFechaChange("2100-01-20")
        viewModel.onHoraChange("09:00")
        viewModel.onMotivoChange("Control general anual completo")
    }

    @Test
    fun `carga los catalogos de especialidades y sedes`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = viewModelDe(FakeCitaRepository(emptyList()))
        advanceUntilIdle()

        val fase = assertIs<SolicitudUiState.Fase.ListaCargada>(viewModel.uiState.value.fase)
        assertEquals(5, fase.especialidades.size)
        assertEquals(4, fase.sedes.size)
        assertEquals(listOf("Ñaña", "Chosica", "Chaclacayo", "Santa Anita"), fase.sedes)
    }

    @Test
    fun `al llenar el formulario se limpia el error de cada campo`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = viewModelDe(FakeCitaRepository(emptyList()))
        advanceUntilIdle()

        viewModel.onEspecialidadChange("Medicina General")

        val formulario = viewModel.uiState.value.formulario
        assertEquals("Medicina General", formulario.especialidad)
        assertTrue(formulario.especialidadError == null)
    }

    @Test
    fun `registra la cita cuando el dominio la acepta`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = viewModelDe(FakeCitaRepository(emptyList()))
        advanceUntilIdle()

        llenarFormularioValido(viewModel)
        viewModel.solicitar()
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertTrue(estado.registradaExitosamente)
        assertTrue(!estado.enviando)
    }

    @Test
    fun `una solicitud invalida deja cada error en su campo`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = viewModelDe(FakeCitaRepository(emptyList()))
        advanceUntilIdle()

        viewModel.onEspecialidadChange("Medicina General")
        viewModel.onSedeChange("Ñaña")
        viewModel.onFechaChange("2020-01-01")
        viewModel.onHoraChange("09:00")
        viewModel.onMotivoChange("corto")
        viewModel.solicitar()
        advanceUntilIdle()

        val formulario = viewModel.uiState.value.formulario
        assertNotNull(formulario.fechaError)
        assertNotNull(formulario.motivoError)
        assertTrue(!viewModel.uiState.value.registradaExitosamente)
    }

    @Test
    fun `una fecha mal formateada se senala sin romper el formulario`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = viewModelDe(FakeCitaRepository(emptyList()))
        advanceUntilIdle()

        viewModel.onEspecialidadChange("Medicina General")
        viewModel.onSedeChange("Ñaña")
        viewModel.onFechaChange("20-01-2100")
        viewModel.onHoraChange("09:00")
        viewModel.onMotivoChange("Control general anual completo")
        viewModel.solicitar()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.formulario.fechaError)
        assertTrue(!viewModel.uiState.value.registradaExitosamente)
    }

    @Test
    fun `un fallo general del repositorio llega como errorGeneral`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val repositorio = FakeCitaRepository(emptyList())
        repositorio.errorSimulado = RuntimeException("Servidor caido")
        val viewModel = viewModelDe(repositorio)
        advanceUntilIdle()

        llenarFormularioValido(viewModel)
        viewModel.solicitar()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.formulario.errorGeneral)
        assertTrue(!viewModel.uiState.value.registradaExitosamente)
    }
}
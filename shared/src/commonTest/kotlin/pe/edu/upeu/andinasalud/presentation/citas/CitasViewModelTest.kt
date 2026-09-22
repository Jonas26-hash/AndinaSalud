package pe.edu.upeu.andinasalud.presentation.citas

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import pe.edu.upeu.andinasalud.data.local.CitasSimuladas
import pe.edu.upeu.andinasalud.domain.usecase.ObtenerCitasUseCase
import pe.edu.upeu.andinasalud.fakes.FakeCitaRepository
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


@OptIn(ExperimentalCoroutinesApi::class)
class CitasViewModelTest {

    @AfterTest
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    private fun repositorioConElSeed() =
        FakeCitaRepository(CitasSimuladas.citas(ancla = LocalDate.parse("2026-01-10")))

    private fun cargarViewModel(repositorio: FakeCitaRepository): CitasViewModel =
        CitasViewModel(ObtenerCitasUseCase(repositorio))

    @Test
    fun `arranca en la fase de carga`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = cargarViewModel(repositorioConElSeed())

        assertEquals(CitasUiState.Fase.Cargando, viewModel.uiState.value.fase)
    }

    @Test
    fun `carga las citas del seed y deja visible la lista completa`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = cargarViewModel(repositorioConElSeed())
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        val contenido = assertIs<CitasUiState.Fase.ConContenido>(estado.fase)
        assertEquals(6, contenido.citas.size)
        assertEquals(6, estado.citasVisibles.size)
        assertNotNull(estado.paciente)
        assertEquals("P-0417", estado.paciente?.id)
    }

    @Test
    fun `la proxima cita es la programada mas cercana`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = cargarViewModel(repositorioConElSeed())
        advanceUntilIdle()

        assertEquals(1L, viewModel.uiState.value.proximaCita?.id)
    }

    @Test
    fun `filtra por estado sobre la lista completa`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = cargarViewModel(repositorioConElSeed())
        advanceUntilIdle()

        viewModel.onFiltroChange(CitasUiState.FiltroCita.Programadas)
        assertEquals(listOf(1L, 2L, 3L), viewModel.uiState.value.citasVisibles.map { it.id })

        viewModel.onFiltroChange(CitasUiState.FiltroCita.Atendidas)
        assertEquals(listOf(5L, 4L), viewModel.uiState.value.citasVisibles.map { it.id })

        viewModel.onFiltroChange(CitasUiState.FiltroCita.Canceladas)
        assertEquals(listOf(6L), viewModel.uiState.value.citasVisibles.map { it.id })

        viewModel.onFiltroChange(CitasUiState.FiltroCita.Todas)
        assertEquals(6, viewModel.uiState.value.citasVisibles.size)
    }

    @Test
    fun `la busqueda encuentra por especialidad sin tildes ni mayusculas`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = cargarViewModel(repositorioConElSeed())
        advanceUntilIdle()

        viewModel.onBusquedaChange("odonto")
        assertEquals(listOf(2L), viewModel.uiState.value.citasVisibles.map { it.id })

        viewModel.onBusquedaChange("ODONTOLOGIA")
        assertEquals(listOf(2L), viewModel.uiState.value.citasVisibles.map { it.id })
    }

    @Test
    fun `la busqueda encuentra por nombre de medico`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = cargarViewModel(repositorioConElSeed())
        advanceUntilIdle()

        viewModel.onBusquedaChange("rojas")
        assertEquals(listOf(6L, 1L), viewModel.uiState.value.citasVisibles.map { it.id })

        viewModel.onBusquedaChange("nunez")
        assertEquals(listOf(4L), viewModel.uiState.value.citasVisibles.map { it.id })
    }

    @Test
    fun `una busqueda sin resultados deja la lista vacia`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = cargarViewModel(repositorioConElSeed())
        advanceUntilIdle()

        viewModel.onBusquedaChange("traumatologia")

        assertTrue(viewModel.uiState.value.citasVisibles.isEmpty())
    }

    @Test
    fun `la busqueda se combina con el filtro`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = cargarViewModel(repositorioConElSeed())
        advanceUntilIdle()

        viewModel.onFiltroChange(CitasUiState.FiltroCita.Programadas)
        viewModel.onBusquedaChange("nutricion")

        assertEquals(listOf(3L), viewModel.uiState.value.citasVisibles.map { it.id })
    }

    @Test
    fun `un fallo del repositorio llega como fase de error`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val repositorio = repositorioConElSeed()
        repositorio.errorSimulado = RuntimeException("Servidor no disponible")
        val viewModel = cargarViewModel(repositorio)
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        val error = assertIs<CitasUiState.Fase.Error>(estado.fase)
        assertEquals("Servidor no disponible", error.mensaje)
    }

    @Test
    fun `reintentar tras un fallo recupera el contenido`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val repositorio = repositorioConElSeed()
        repositorio.errorSimulado = RuntimeException("Servidor no disponible")
        val viewModel = cargarViewModel(repositorio)
        advanceUntilIdle()
        assertIs<CitasUiState.Fase.Error>(viewModel.uiState.value.fase)

        repositorio.errorSimulado = null
        viewModel.cargar()
        advanceUntilIdle()

        assertIs<CitasUiState.Fase.ConContenido>(viewModel.uiState.value.fase)
    }
}
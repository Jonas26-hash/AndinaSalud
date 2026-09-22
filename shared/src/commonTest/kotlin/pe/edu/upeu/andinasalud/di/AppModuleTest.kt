package pe.edu.upeu.andinasalud.di

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import pe.edu.upeu.andinasalud.data.repository.CitaRepositoryFake
import pe.edu.upeu.andinasalud.domain.repository.CitaRepository
import pe.edu.upeu.andinasalud.domain.usecase.CancelarCitaUseCase
import pe.edu.upeu.andinasalud.domain.usecase.ObtenerCitasUseCase
import pe.edu.upeu.andinasalud.domain.usecase.SolicitarCitaUseCase
import pe.edu.upeu.andinasalud.presentation.citas.CitasViewModel
import pe.edu.upeu.andinasalud.presentation.detalle.DetalleCitaViewModel
import pe.edu.upeu.andinasalud.presentation.solicitud.SolicitudViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame


@OptIn(ExperimentalCoroutinesApi::class)
class AppModuleTest {

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun detenerKoin() {
        stopKoin()
        Dispatchers.resetMain()
    }

    private fun grafoCompleto(): Koin = startKoin {
        modules(dataModule, domainModule, presentationModule, platformModule)
    }.koin

    @Test
    fun elRepositorioSeResuelvePorSuInterfazDeDominio() {

        val koin = grafoCompleto()

        assertIs<CitaRepositoryFake>(koin.get<CitaRepository>())
    }

    @Test
    fun elRepositorioEsUnicoEnTodaLaAplicacion() {

        val koin = grafoCompleto()

        assertSame(koin.get<CitaRepository>(), koin.get<CitaRepository>())
    }

    @Test
    fun resuelveLosTresCasosDeUso() {

        val koin = grafoCompleto()

        koin.get<ObtenerCitasUseCase>()
        koin.get<SolicitarCitaUseCase>()
        koin.get<CancelarCitaUseCase>()
    }

    @Test
    fun resuelveLosViewModelDeNavegacion() {

        val koin = grafoCompleto()

        val citas = koin.get<CitasViewModel>()
        val solicitud = koin.get<SolicitudViewModel>()
        assertIs<CitasViewModel>(citas)
        assertIs<SolicitudViewModel>(solicitud)
    }

    @Test
    fun elDetalleSeResuelveConSuId() {

        val koin = grafoCompleto()

        val detalle = koin.get<DetalleCitaViewModel>(parameters = { parametersOf(1L) })

        assertIs<DetalleCitaViewModel>(detalle)
        koin.get<DetalleCitaViewModel>(parameters = { parametersOf(2L) })
    }
}
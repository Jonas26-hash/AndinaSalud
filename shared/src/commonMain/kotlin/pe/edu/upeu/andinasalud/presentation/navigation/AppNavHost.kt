package pe.edu.upeu.andinasalud.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pe.edu.upeu.andinasalud.presentation.citas.CitasScreen
import pe.edu.upeu.andinasalud.presentation.citas.CitasViewModel
import pe.edu.upeu.andinasalud.presentation.detalle.DetalleCitaScreen
import pe.edu.upeu.andinasalud.presentation.detalle.DetalleCitaViewModel
import pe.edu.upeu.andinasalud.presentation.inicio.InicioScreen
import pe.edu.upeu.andinasalud.presentation.perfil.PerfilScreen
import pe.edu.upeu.andinasalud.presentation.solicitud.SolicitudScreen
import pe.edu.upeu.andinasalud.presentation.solicitud.SolicitudViewModel


@Composable
fun AppNavHost(
    navController: NavHostController,
    citasViewModel: CitasViewModel,
    temaOscuro: Boolean,
    onTemaOscuroChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navController,
        startDestination = Destinos.INICIO,
        modifier = modifier.fillMaxSize()
    ) {

        composable(Destinos.INICIO) {
            val uiState by citasViewModel.uiState.collectAsStateWithLifecycle()
            InicioScreen(
                uiState = uiState,
                onReintentar = citasViewModel::cargar,
                onVerCitas = { navController.navigate(Destinos.CITAS) },
                onSolicitarCita = { navController.navigate(Destinos.SOLICITUD) }
            )
        }

        composable(Destinos.CITAS) {
            val uiState by citasViewModel.uiState.collectAsStateWithLifecycle()
            CitasScreen(
                uiState = uiState,
                onBusquedaChange = citasViewModel::onBusquedaChange,
                onFiltroChange = citasViewModel::onFiltroChange,
                onCitaClick = { citaId ->
                    navController.navigate(Destinos.detalleCita(citaId))
                },
                onReintentar = citasViewModel::cargar
            )
        }

        composable(Destinos.PERFIL) {
            val uiState by citasViewModel.uiState.collectAsStateWithLifecycle()
            PerfilScreen(
                uiState = uiState,
                temaOscuro = temaOscuro,
                onTemaOscuroChange = onTemaOscuroChange
            )
        }

        composable(Destinos.SOLICITUD) {
            SolicitudScreen(
                viewModel = koinViewModel<SolicitudViewModel>(),
                onCitaRegistrada = {
                    navController.navigate(Destinos.CITAS) {
                        popUpTo(Destinos.SOLICITUD) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Destinos.DETALLE_CITA,
            arguments = listOf(
                navArgument("citaId") { type = NavType.LongType }
            )
        ) { entrada ->
            val citaId = entrada.arguments?.getLong("citaId")
                ?: return@composable

            DetalleCitaScreen(
                viewModel = koinViewModel(parameters = { parametersOf(citaId) }),
                onCitaCancelada = {
                    navController.popBackStack()
                }
            )
        }
    }
}
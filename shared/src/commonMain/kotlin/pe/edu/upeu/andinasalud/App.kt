package pe.edu.upeu.andinasalud

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.andinasalud.presentation.citas.CitasViewModel
import pe.edu.upeu.andinasalud.presentation.navigation.AppNavHost
import pe.edu.upeu.andinasalud.presentation.navigation.Destinos
import pe.edu.upeu.andinasalud.presentation.navigation.tabsInferiores
import pe.edu.upeu.andinasalud.presentation.theme.AndinaSaludTheme


@Composable
fun App() = KoinContext {

    var temaOscuro by rememberSaveable {
        mutableStateOf(false)
    }

    val navController = rememberNavController()

    val citasViewModel: CitasViewModel = koinViewModel()

    AndinaSaludTheme(darkTheme = temaOscuro) {

        val entradaActual by navController.currentBackStackEntryAsState()
        val rutaActual = entradaActual?.destination?.route.orEmpty()
        val esRutaInferior = rutaActual in Destinos.rutasInferiores

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = Destinos.tituloDe(rutaActual)
                        )
                    },
                    navigationIcon = {
                        if (!esRutaInferior) {
                            IconButton(
                                onClick = { navController.popBackStack() }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                if (esRutaInferior) {
                    NavigationBar {
                        tabsInferiores.forEach { tab ->
                            NavigationBarItem(
                                selected = rutaActual == tab.ruta,
                                onClick = {
                                    navController.navigate(tab.ruta) {
                                        popUpTo(Destinos.INICIO) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icono,
                                        contentDescription = tab.titulo
                                    )
                                },
                                label = {
                                    Text(tab.titulo)
                                }
                            )
                        }
                    }
                }
            }
        ) { paddingValores ->

            AppNavHost(
                navController = navController,
                citasViewModel = citasViewModel,
                temaOscuro = temaOscuro,
                onTemaOscuroChange = { temaOscuro = it },
                modifier = Modifier.padding(paddingValores)
            )
        }
    }
}
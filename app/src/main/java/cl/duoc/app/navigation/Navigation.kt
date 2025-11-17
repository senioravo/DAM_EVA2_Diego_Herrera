package cl.duoc.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.duoc.app.ui.screens.HomeScreen
import cl.duoc.app.ui.screens.AjustesScreen
import cl.duoc.app.ui.screens.PlantelScreen
import cl.duoc.app.ui.screens.auth.LoginScreen
import cl.duoc.app.ui.screens.auth.RegisterScreen
import cl.duoc.app.ui.screens.catalog.CatalogScreen
import cl.duoc.app.ui.viewmodel.AuthViewModel
import cl.duoc.app.ui.viewmodel.SettingsViewModel

@Composable
fun PlantBuddyNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.authState.collectAsState()
    
    // Observar cambios en el estado de autenticación y redirigir al login si se cierra sesión
    LaunchedEffect(authState.isLoggedIn) {
        if (!authState.isLoggedIn && navController.currentDestination?.route != Screen.Login.route 
            && navController.currentDestination?.route != Screen.Register.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        // Pantallas de autenticación
        composable(Screen.Login.route) {
            // Navegar automáticamente al Home si ya está autenticado
            LaunchedEffect(authState.isLoggedIn) {
                if (authState.isLoggedIn) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Pantallas principales (requieren autenticación)
        composable(Screen.Home.route) {
            HomeScreen(modifier = modifier)
        }
        
        composable(Screen.Catalogo.route) {
            CatalogScreen()
        }
        
        composable(Screen.Plantel.route) {
            PlantelScreen(modifier = modifier)
        }
        
        composable(Screen.Ajustes.route) {
            AjustesScreen(
                modifier = modifier,
                authViewModel = authViewModel,
                settingsViewModel = settingsViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
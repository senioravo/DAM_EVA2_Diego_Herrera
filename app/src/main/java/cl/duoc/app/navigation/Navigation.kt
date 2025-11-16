package cl.duoc.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.duoc.app.ui.screens.HomeScreen
import cl.duoc.app.ui.screens.AjustesScreen
import cl.duoc.app.ui.screens.plantel.PlantelScreen
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
    
    // Determinar la ruta inicial según el estado de autenticación
    val startDestination = if (authState.isLoggedIn) Screen.Home.route else Screen.Login.route
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Pantallas de autenticación
        composable(Screen.Login.route) {
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
            val plantelViewModel = androidx.compose.runtime.remember { 
                cl.duoc.app.ui.screens.plantel.PlantelViewModel() 
            }
            cl.duoc.app.ui.screens.plantel.PlantelScreen(
                modifier = modifier,
                viewModel = plantelViewModel
            )
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
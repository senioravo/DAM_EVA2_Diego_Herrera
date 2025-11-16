package cl.duoc.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.duoc.app.ui.screens.LoginScreen
import cl.duoc.app.ui.screens.HomeScreen
import cl.duoc.app.ui.screens.CatalogoScreen
import cl.duoc.app.ui.screens.PlantelScreen
import cl.duoc.app.ui.screens.AjustesScreen
import cl.duoc.app.ui.screens.catalog.CatalogScreen

@Composable
fun PlantBuddyNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                modifier = modifier,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
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
            AjustesScreen(modifier = modifier)
        }
    }
}
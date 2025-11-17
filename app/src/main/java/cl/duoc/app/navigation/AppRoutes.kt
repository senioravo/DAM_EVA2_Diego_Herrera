package cl.duoc.app.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Catalogo : Screen("catalogo")
    object Plantel : Screen("plantel")
    object Ajustes : Screen("ajustes")
}
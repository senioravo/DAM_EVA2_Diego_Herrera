package cl.duoc.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import cl.duoc.app.navigation.Screen
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import android.content.res.Configuration
import cl.duoc.app.ui.theme.PlantBuddyTheme
import androidx.compose.ui.graphics.Color

data class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun BottomNavigationBar(navController: NavController, currentRouteOverride: String? = null) {
    val items = listOf(
        NavigationItem(Screen.Home.route, Icons.Default.Home, "Home"),
        NavigationItem(Screen.Catalogo.route, Icons.AutoMirrored.Filled.List, "Catalogo"),
        NavigationItem(Screen.Cart.route, Icons.Default.ShoppingCart, "Carrito"),
        NavigationItem(Screen.Plantel.route, Icons.Default.Spa, "Plantel"),
        NavigationItem(Screen.Ajustes.route, Icons.Default.Settings, "Ajustes")
    )

    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 16.dp, // Aumenta la elevación para crear un efecto de sombra más pronunciado
        tonalElevation = 4.dp, // Añade elevación tonal para dar profundidad
        modifier = Modifier.height(120.dp) // Incrementar la altura de la barra de navegación
    ) {
        NavigationBar(containerColor = Color.Transparent) {

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentRouteOverride ?: navBackStackEntry?.destination?.route

            items.forEach { item ->
                val selected = currentRoute == item.route

                NavigationBarItem(
                    icon = {
                        // Dibujamos el icono sobre un Surface pequeño para crear un fondo y esquinas redondeadas
                        val backgroundColor = if (selected) MaterialTheme.colorScheme.onPrimary
                                              else MaterialTheme.colorScheme.onSecondary
                        val contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                                           else MaterialTheme.colorScheme.onSurfaceVariant

                        Surface(
                            color = backgroundColor,
                            shape = RoundedCornerShape(12.dp),
                            tonalElevation = if (selected) 4.dp else 0.dp,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = contentColor,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    },
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    // Ocultar el indicador por defecto para que no interfiera con nuestro fondo redondeado
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "NavBar - default")
@Composable
fun BottomNavigationBarPreview() {
    PlantBuddyTheme {
        val navController = rememberNavController()
        BottomNavigationBar(navController = navController)
    }
}

@Preview(showBackground = true, name = "NavBar - Catalogo selected")
@Composable
fun BottomNavigationBarPreviewSelected() {
    PlantBuddyTheme {
        val navController = rememberNavController()
        // Usamos el override para asegurar que el preview muestre el estado seleccionado
        BottomNavigationBar(navController = navController, currentRouteOverride = Screen.Catalogo.route)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "NavBar - dark")
@Composable
fun BottomNavigationBarPreviewDark() {
    PlantBuddyTheme(darkTheme = true) {
        val navController = rememberNavController()
        BottomNavigationBar(navController = navController)
    }
}

package cl.duoc.app

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceControl
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import cl.duoc.app.navigation.PlantBuddyNavigation
import cl.duoc.app.navigation.Screen
import cl.duoc.app.ui.components.BottomNavigationBar
import cl.duoc.app.ui.theme.PlantBuddyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Configurar refresh rate alto ANTES de setContent
        setupHighRefreshRate()
        
        // Forzar aceleración por hardware para animaciones suaves
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        
        // CRÍTICO: Forzar frame callback a 120Hz
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes = window.attributes.apply {
                preferredRefreshRate = 120f
                preferredDisplayModeId = 2 // ID del modo 120Hz según logs
            }
        }
        
        setContent {
            PlantBuddyTheme {
                PlantBuddyApp()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Reforzar el refresh rate al volver a la app
        setupHighRefreshRate()
        
        // Mantener pantalla activa para evitar throttling
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Re-aplicar configuración de refresh rate en onResume
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.postDelayed({
                val params = window.attributes
                params.preferredRefreshRate = 120f
                params.preferredDisplayModeId = 2
                window.attributes = params
                Log.d("RefreshRate", "✓ Refresh rate replicado en onResume")
            }, 100)
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Quitar flag al pausar para ahorrar batería
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    
    @Suppress("DEPRECATION")
    private fun setupHighRefreshRate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                // PASO 0: Forzar modo de alto rendimiento
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
                
                // PASO 0.5: Verificar soporte de alto refresh rate
                val hasHighRefreshRate = display?.supportedModes?.any { 
                    it.refreshRate >= 90f 
                } ?: false
                Log.d("RefreshRate", "Soporte de alto refresh rate: $hasHighRefreshRate")
                
                // Usar setFrameRate API (Android 12+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        // Forzar 120fps usando la nueva API
                        window.decorView.post {
                            try {
                                val viewRootImpl = window.decorView.javaClass
                                    .getMethod("getViewRootImpl")
                                    .invoke(window.decorView)
                                
                                if (viewRootImpl != null) {
                                    val surfaceControl = viewRootImpl.javaClass
                                        .getMethod("getSurfaceControl")
                                        .invoke(viewRootImpl)
                                    
                                    if (surfaceControl != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val transaction = SurfaceControl.Transaction()
                                        transaction.javaClass
                                            .getMethod(
                                                "setFrameRate",
                                                SurfaceControl::class.java,
                                                Float::class.java,
                                                Int::class.java,
                                                Int::class.java
                                            )
                                            .invoke(
                                                transaction,
                                                surfaceControl,
                                                120f,
                                                0, // FRAME_RATE_COMPATIBILITY_DEFAULT
                                                1  // CHANGE_FRAME_RATE_ALWAYS
                                            )
                                        transaction.apply()
                                        Log.d("RefreshRate", "✓ setFrameRate(120fps) aplicado via reflection")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.w("RefreshRate", "setFrameRate no disponible, usando método legacy")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("RefreshRate", "Error en setFrameRate: ${e.message}")
                    }
                }
                
                // PASO 1: CRÍTICO para MIUI: Configurar ANTES de obtener el display
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val params = window.attributes
                    params.preferredRefreshRate = 120f
                    window.attributes = params
                }
                
                val display = windowManager.defaultDisplay
                val supportedModes = display.supportedModes
                
                Log.d("RefreshRate", "=== Información del dispositivo ===")
                Log.d("RefreshRate", "Fabricante: ${Build.MANUFACTURER}")
                Log.d("RefreshRate", "Modelo: ${Build.MODEL}")
                Log.d("RefreshRate", "Android: ${Build.VERSION.SDK_INT}")
                
                Log.d("RefreshRate", "\n=== Modos disponibles ===")
                supportedModes.forEachIndexed { index, mode ->
                    Log.d("RefreshRate", "[$index] ${mode.refreshRate}Hz - ${mode.physicalWidth}x${mode.physicalHeight} - ID:${mode.modeId}")
                }
                
                val currentMode = display.mode
                Log.d("RefreshRate", "\nModo actual: ${currentMode.refreshRate}Hz (ID:${currentMode.modeId})")
                
                // PASO 2: Buscar el modo de mayor refresh rate
                val highRefreshMode = supportedModes
                    .filter { it.refreshRate >= 119f } // >= 119 para capturar 120Hz
                    .maxByOrNull { it.refreshRate }
                
                if (highRefreshMode != null) {
                    Log.d("RefreshRate", "\n=== Configurando 120Hz ===")
                    Log.d("RefreshRate", "Modo seleccionado: ${highRefreshMode.refreshRate}Hz (ID:${highRefreshMode.modeId})")
                    
                    // PASO 3: Aplicar el modo - Método múltiple para MIUI
                    val params = window.attributes
                    
                    // Método 1: preferredDisplayModeId (estándar Android)
                    params.preferredDisplayModeId = highRefreshMode.modeId
                    Log.d("RefreshRate", "✓ preferredDisplayModeId = ${highRefreshMode.modeId}")
                    
                    // Método 2: preferredRefreshRate (Android 11+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        params.preferredRefreshRate = highRefreshMode.refreshRate
                        Log.d("RefreshRate", "✓ preferredRefreshRate = ${highRefreshMode.refreshRate}")
                    }
                    
                    // PASO 4: Aplicar los cambios MÚLTIPLES VECES (fix MIUI)
                    window.attributes = params
                    
                    // PASO 5: Re-aplicar después de un frame
                    window.decorView.post {
                        window.attributes = params
                        
                        // PASO 6: Forzar redibujado y verificación
                        window.decorView.postDelayed({
                            // Re-aplicar una tercera vez para MIUI
                            val finalParams = window.attributes
                            finalParams.preferredDisplayModeId = highRefreshMode.modeId
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                finalParams.preferredRefreshRate = highRefreshMode.refreshRate
                            }
                            window.attributes = finalParams
                            
                            window.decorView.invalidate()
                            val verifyMode = windowManager.defaultDisplay.mode
                            Log.d("RefreshRate", "\n=== Verificación Final ===")
                            Log.d("RefreshRate", "Refresh rate aplicado: ${verifyMode.refreshRate}Hz")
                            
                            if (verifyMode.refreshRate < 119f) {
                                Log.w("RefreshRate", "⚠️ ADVERTENCIA: No se aplicó 120Hz")
                                Log.w("RefreshRate", "Verifica en Ajustes MIUI:")
                                Log.w("RefreshRate", "Configuración > Pantalla > Frecuencia de actualización")
                                Log.w("RefreshRate", "Selecciona 'Establecer frecuencia por aplicación'")
                                Log.w("RefreshRate", "Busca 'Plant Buddy' y selecciona 120Hz")
                            } else {
                                Log.d("RefreshRate", "✅ 120Hz activado correctamente")
                            }
                        }, 200)
                    }
                    
                } else {
                    Log.w("RefreshRate", "⚠️ No hay modos de 120Hz disponibles en este dispositivo")
                    Log.w("RefreshRate", "Máximo disponible: ${supportedModes.maxOfOrNull { it.refreshRate }}Hz")
                }
            } catch (e: Exception) {
                Log.e("RefreshRate", "❌ Error configurando refresh rate: ${e.message}", e)
            }
        } else {
            Log.w("RefreshRate", "⚠️ Android < M (6.0) - API de refresh rate no disponible")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantBuddyApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Ocultar TopBar y BottomBar en la pantalla de login
    val showBars = currentRoute != Screen.Login.route
    
    Scaffold(
        bottomBar = {
            if (showBars) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        PlantBuddyNavigation(
            navController = navController,
            modifier = if (showBars) Modifier.padding(top = innerPadding.calculateTopPadding()) else Modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlantBuddyAppPreview() {
    PlantBuddyTheme {
        PlantBuddyApp()
    }
}
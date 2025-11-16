package cl.duoc.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.duoc.app.R
import cl.duoc.app.ui.theme.PlantBuddyTheme


@Composable
fun HomeScreen(modifier: Modifier = Modifier) {

    Column(

        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    start = Offset(0f, 1000f),
                    end = Offset(1000f, 2000f)
                )
            )
            .then(modifier)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .offset(x = -0.dp, y = (-80).dp),

                verticalAlignment = Alignment.Bottom
            ) {
                // Columna izquierda: Card de fondo cuadrado
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f), // Mantiene proporción cuadrada
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                ) {
                    // Contenido vacío del card
                }

                Spacer(modifier = Modifier.width(16.dp))

            // Columna derecha: Textos
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), // Aprovecha toda la altura disponible

                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Bienvenido a",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Normal),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = "Plant Buddy",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Icono de logo (placeholder)
                    Icon(
                        imageVector = Icons.Default.LocalFlorist,
                        contentDescription = "Logo de Plant Buddy",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(145.dp)
                    )
                }
            }
            }


            
            // Imagen posicionada para sobresalir fuera del Row
            Image(
                painter = painterResource(id = R.drawable.plant_home_screen),
                contentDescription = "Plant icon",
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = -66.dp, y = (-100).dp) // x=16dp para alinear con el padding, y=-20dp para que comience 20dp antes del borde inferior
            )
        }
        Spacer(Modifier.height(56.dp))
        // Card adicional debajo de los anteriores
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-116).dp), // Mueve el card hacia arriba para eliminar el espacio
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Tu compañero ideal para el cuidado de plantas",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Sección de características
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-92).dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "🌱 Características Principales",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "• Catálogo completo de plantas\n• Recordatorios de riego personalizados\n• Consejos de cuidado específicos\n• Seguimiento del crecimiento\n• Identificación de enfermedades",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sección de consejos
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-92).dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "💡 Consejos del Día",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Las plantas necesitan luz indirecta para prosperar. Evita la exposición directa al sol durante las horas más calurosas del día para prevenir quemaduras en las hojas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sección de beneficios
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-92).dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "✨ Beneficios de las Plantas",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "• Purifican el aire\n• Reducen el estrés\n• Mejoran la concentración\n• Decoran tu espacio\n• Aumentan la humedad ambiental",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp)) // Espacio para la barra de navegación
    }

}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PlantBuddyTheme {
        HomeScreen()
    }
}
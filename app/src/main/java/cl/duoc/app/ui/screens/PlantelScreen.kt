package cl.duoc.app.ui.screens.plantel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.app.data.model.PlantelPlant
import cl.duoc.app.data.model.PlantState
import cl.duoc.app.ui.screens.plantel.PlantelViewModel

@Composable
fun PlantelScreen(
    modifier: Modifier = Modifier,
    viewModel: PlantelViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Título
            Text(
                text = "Mi Plantel",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            when {
                estado.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                estado.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = estado.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                estado.plants.isEmpty() -> {
                    EmptyPlantelState()
                }
                else -> {
                    PlantelList(
                        plants = estado.plants,
                        onStartAssistance = viewModel::startAssistance,
                        onWaterPlant = viewModel::waterPlant,
                        onRemovePlant = viewModel::removePlant
                    )
                }
            }
        }
    }
}

@Composable
fun PlantelList(
    plants: List<PlantelPlant>,
    onStartAssistance: (Int) -> Unit,
    onWaterPlant: (Int) -> Unit,
    onRemovePlant: (Int) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(plants, key = { it.product.id }) { plant ->
            PlantelPlantCard(
                plant = plant,
                onStartAssistance = { onStartAssistance(plant.product.id) },
                onWaterPlant = { onWaterPlant(plant.product.id) },
                onRemovePlant = { onRemovePlant(plant.product.id) }
            )
        }
    }
}

@Composable
fun PlantelPlantCard(
    plant: PlantelPlant,
    onStartAssistance: () -> Unit,
    onWaterPlant: () -> Unit,
    onRemovePlant: () -> Unit
) {
    val context = LocalContext.current
    val state = plant.getCurrentState()
    val stateColor = when (state) {
        PlantState.NOT_STARTED -> MaterialTheme.colorScheme.outline
        PlantState.HEALTHY -> MaterialTheme.colorScheme.primary
        PlantState.NEEDS_WATER -> MaterialTheme.colorScheme.tertiary
        PlantState.URGENT_CARE -> MaterialTheme.colorScheme.error
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de la planta
            PlantImage(
                imageUrl = plant.product.imageUrl,
                plantName = plant.product.name,
                modifier = Modifier.size(80.dp)
            )
            
            // Información de la planta
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = plant.customTitle ?: plant.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Estado con color
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(stateColor, shape = RoundedCornerShape(6.dp))
                    )
                    Text(
                        text = plant.getStateText(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = stateColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Botón de acción
                if (state == PlantState.NOT_STARTED) {
                    Button(
                        onClick = onStartAssistance,
                        modifier = Modifier.padding(top = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Iniciar asistencia")
                    }
                } else if (state == PlantState.NEEDS_WATER || state == PlantState.URGENT_CARE) {
                    Button(
                        onClick = onWaterPlant,
                        modifier = Modifier.padding(top = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = stateColor
                        )
                    ) {
                        Text("Regar ahora")
                    }
                }
            }
            
            // Botón eliminar
            IconButton(
                onClick = onRemovePlant,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar planta",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun PlantImage(
    imageUrl: String,
    plantName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val resourceId = androidx.compose.runtime.remember(imageUrl) {
        if (imageUrl.isNotEmpty()) {
            context.resources.getIdentifier(
                imageUrl,
                "drawable",
                context.packageName
            )
        } else {
            0
        }
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (resourceId != 0) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = plantName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = plantName.take(2).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyPlantelState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No tienes plantas en tu plantel",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Text(
                text = "Agrega plantas desde el catálogo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}
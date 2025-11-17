package cl.duoc.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.app.data.model.PlantelPlant
import cl.duoc.app.data.model.PlantState
import cl.duoc.app.ui.screens.plantel.PlantelViewModel

@Composable
fun PlantelScreen(
    modifier: Modifier = Modifier,
    viewModel: PlantelViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    var selectedPlantId by remember { mutableStateOf<Int?>(null) }
    
    // Obtener la planta actualizada del estado
    val selectedPlant = selectedPlantId?.let { id ->
        estado.plants.find { it.product.id == id }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
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
                        onRemovePlant = viewModel::removePlant,
                        onPlantClick = { selectedPlantId = it.product.id }
                    )
                }
            }
    }
    
    // Diálogo de detalles con animación
    AnimatedVisibility(
            visible = selectedPlant != null,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(300)
            ),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(300)
            )
        ) {
            selectedPlant?.let { plant ->
                PlantDetailDialog(
                    plant = plant,
                    onDismiss = { selectedPlantId = null },
                    onUpdateTitle = { newTitle ->
                        viewModel.updateCustomTitle(plant.product.id, newTitle)
                    },
                    onWaterPlant = {
                        viewModel.waterPlant(plant.product.id)
                    },
                    onToggleNotifications = {
                        viewModel.toggleNotifications(plant.product.id)
                    }
                )
            }
    }
}

@Composable
fun PlantelList(
    plants: List<PlantelPlant>,
    onStartAssistance: (Int) -> Unit,
    onWaterPlant: (Int) -> Unit,
    onRemovePlant: (Int) -> Unit,
    onPlantClick: (PlantelPlant) -> Unit
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
                onRemovePlant = { onRemovePlant(plant.product.id) },
                onClick = { onPlantClick(plant) }
            )
        }
    }
}

@Composable
fun PlantelPlantCard(
    plant: PlantelPlant,
    onStartAssistance: () -> Unit,
    onWaterPlant: () -> Unit,
    onRemovePlant: () -> Unit,
    onClick: () -> Unit
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                // Título personalizado o nombre
                if (plant.customTitle != null) {
                    Text(
                        text = plant.customTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = plant.product.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Text(
                        text = plant.product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
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

@Composable
fun PlantDetailDialog(
    plant: PlantelPlant,
    onDismiss: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onWaterPlant: () -> Unit,
    onToggleNotifications: () -> Unit
) {
    var isEditingTitle by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(plant.customTitle ?: plant.product.name) }
    var notificationsEnabled by remember(plant.product.id) { mutableStateOf(plant.notificationsEnabled) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            if (isEditingTitle) {
                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Row {
                            IconButton(onClick = {
                                onUpdateTitle(editedTitle)
                                isEditingTitle = false
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Guardar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = {
                                editedTitle = plant.customTitle ?: plant.product.name
                                isEditingTitle = false
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar"
                                )
                            }
                        }
                    }
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plant.customTitle ?: plant.product.name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { isEditingTitle = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar título"
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = 100,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        )
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Calendario de riego
                if (plant.assistanceStarted) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historial de riego",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(
                            onClick = {
                                // Cambiar estado local inmediatamente
                                notificationsEnabled = !notificationsEnabled
                                
                                // Actualizar en el repositorio
                                onToggleNotifications()
                                
                                if (notificationsEnabled) {
                                    // Verificar permisos primero
                                    if (!cl.duoc.app.notifications.NotificationPermissionHelper.hasNotificationPermission(context)) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Por favor, permite las notificaciones en la configuración",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                        
                                        // Abrir configuración
                                        cl.duoc.app.notifications.NotificationPermissionHelper.openNotificationSettings(context)
                                        
                                        // Revertir el cambio
                                        notificationsEnabled = false
                                        return@IconButton
                                    }
                                    
                                    // Mostrar notificación de confirmación
                                    val plantName = plant.customTitle ?: plant.product.name
                                    val daysUntilWatering = plant.product.wateringCycleDays
                                    
                                    // Usar try-catch para manejar errores de MIUI
                                    try {
                                        cl.duoc.app.notifications.NotificationHelper.showNotificationsEnabled(
                                            context = context,
                                            plantName = plantName,
                                            daysUntilWatering = daysUntilWatering
                                        )
                                        
                                        // Programar recordatorio
                                        cl.duoc.app.notifications.WateringReminderReceiver.scheduleWateringReminder(
                                            context = context,
                                            plantId = plant.product.id,
                                            plantName = plantName,
                                            daysUntilWatering = daysUntilWatering
                                        )
                                        
                                        // Mostrar mensaje de éxito
                                        android.widget.Toast.makeText(
                                            context,
                                            "¡Notificaciones activadas!",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    } catch (e: SecurityException) {
                                        android.util.Log.e("PlantDetailDialog", "Permisos denegados", e)
                                        android.widget.Toast.makeText(
                                            context,
                                            if (cl.duoc.app.notifications.NotificationPermissionHelper.isMIUI()) {
                                                "MIUI detectado: Ve a Configuración > Notificaciones y permite las notificaciones"
                                            } else {
                                                "Por favor, permite las notificaciones en la configuración"
                                            },
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                        
                                        // Revertir el cambio
                                        notificationsEnabled = false
                                    } catch (e: Exception) {
                                        android.util.Log.e("PlantDetailDialog", "Error al activar notificaciones", e)
                                        android.widget.Toast.makeText(
                                            context,
                                            "Error al activar notificaciones: ${e.message}",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                        
                                        // Revertir el cambio
                                        notificationsEnabled = false
                                    }
                                } else {
                                    // Cancelar recordatorio
                                    cl.duoc.app.notifications.WateringReminderReceiver.cancelWateringReminder(
                                        context = context,
                                        plantId = plant.product.id
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (notificationsEnabled) {
                                    Icons.Default.Notifications
                                } else {
                                    Icons.Default.NotificationsOff
                                },
                                contentDescription = if (notificationsEnabled) {
                                    "Desactivar notificaciones"
                                } else {
                                    "Activar notificaciones"
                                },
                                tint = if (notificationsEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                }
                            )
                        }
                    }
                    
                    WateringCalendar(
                        wateringHistory = plant.wateringHistory,
                        startDate = plant.lastWateredDate ?: plant.addedDate,
                        wateringCycleDays = plant.product.wateringCycleDays
                    )
                }
                
                Divider()
                
                // Información de la planta
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Información",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    InfoRow(label = "Nombre", value = plant.product.name)
                    InfoRow(label = "Categoría", value = plant.product.category)
                    
                    Text(
                        text = "Descripción",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = plant.product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    
                    InfoRow(
                        label = "Ciclo de riego",
                        value = "${plant.product.wateringCycleDays} días"
                    )
                }
            }
        },
        confirmButton = {
            if (plant.assistanceStarted) {
                Button(onClick = {
                    onWaterPlant()
                    onDismiss()
                }) {
                    Text("Regar ahora")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun WateringCalendar(
    wateringHistory: List<java.time.LocalDateTime>,
    startDate: java.time.LocalDateTime,
    wateringCycleDays: Int
) {
    var displayedMonth by remember { mutableStateOf(java.time.YearMonth.now()) }
    val today = java.time.LocalDate.now()
    
    // Obtener días del mes
    val firstDayOfMonth = displayedMonth.atDay(1)
    val lastDayOfMonth = displayedMonth.atEndOfMonth()
    val daysInMonth = displayedMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Domingo
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Navegación de mes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Mes anterior"
                )
            }
            
            Text(
                text = "${displayedMonth.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))} ${displayedMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Mes siguiente"
                )
            }
        }
        
        // Encabezados de días de la semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("D", "L", "M", "X", "J", "V", "S").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        // Grid de días del mes
        var dayCounter = 1
        val totalRows = ((daysInMonth + firstDayOfWeek) / 7.0).toInt() + 1
        
        for (week in 0 until totalRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0 until 7) {
                    val position = week * 7 + dayOfWeek
                    
                    if (position >= firstDayOfWeek && dayCounter <= daysInMonth) {
                        val currentDate = displayedMonth.atDay(dayCounter)
                        val wasWatered = wateringHistory.any { waterDate ->
                            waterDate.toLocalDate() == currentDate
                        }
                        val isToday = currentDate == today
                        
                        CalendarDay(
                            day = dayCounter,
                            wasWatered = wasWatered,
                            isToday = isToday,
                            modifier = Modifier.weight(1f)
                        )
                        dayCounter++
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    day: Int,
    wasWatered: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    wasWatered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (wasWatered || isToday) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
            if (wasWatered) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Regado",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlantelScreenPreview() {
    MaterialTheme {
        PlantelScreen()
    }
}
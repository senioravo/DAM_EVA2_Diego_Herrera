package cl.duoc.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.duoc.app.ui.viewmodel.AuthViewModel
import cl.duoc.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val settingsState by settingsViewModel.settingsState.collectAsState()
    val currentUser = settingsState.currentUser ?: authState.currentUser
    
    // Log para debug
    LaunchedEffect(currentUser?.profileImageUrl) {
        android.util.Log.d("AjustesScreen", "currentUser actualizado - profileImageUrl: ${currentUser?.profileImageUrl}")
    }
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangeImageDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    
    // Mostrar Snackbar con mensajes
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(settingsState.successMessage) {
        settingsState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            settingsViewModel.clearMessages()
        }
    }
    
    LaunchedEffect(settingsState.errorMessage) {
        settingsState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            settingsViewModel.clearMessages()
        }
    }
    
    Column(
        modifier = modifier
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
            .padding(16.dp)
    ) {
            // Título
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // SECCIÓN CUENTA
            SettingsSection(title = "Cuenta") {
                // Perfil del usuario
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Imagen de perfil
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentUser?.profileImageUrl != null && currentUser.profileImageUrl.isNotBlank()) {
                            // Cargar imagen desde URI con Coil
                            val imageUrl = currentUser.profileImageUrl
                            android.util.Log.d("AjustesScreen", "Cargando imagen de perfil desde: $imageUrl")
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .listener(
                                        onError = { _, result ->
                                            android.util.Log.e("AjustesScreen", "Error al cargar imagen: ${result.throwable.message}")
                                        },
                                        onSuccess = { _, _ ->
                                            android.util.Log.d("AjustesScreen", "Imagen cargada exitosamente")
                                        }
                                    )
                                    .build(),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = android.R.drawable.ic_menu_report_image),
                                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.size(100.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = currentUser?.email ?: "Usuario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (currentUser?.isAdmin == true) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Administrador",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                Divider()
                
                // Botón: Cambiar imagen de perfil
                SettingsButton(
                    icon = Icons.Default.Image,
                    text = "Cambiar imagen de perfil",
                    onClick = { showChangeImageDialog = true }
                )
                
                Divider()
                
                // Botón: Cambiar contraseña
                SettingsButton(
                    icon = Icons.Default.Lock,
                    text = "Cambiar contraseña",
                    onClick = { showChangePasswordDialog = true }
                )
                
                Divider()
                
                // Botón: Cerrar sesión
                SettingsButton(
                    icon = Icons.Default.Logout,
                    text = "Cerrar sesión",
                    onClick = { showLogoutDialog = true },
                    iconTint = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // SECCIÓN GENERAL
            SettingsSection(title = "General") {
                // Switch: Modo oscuro
                SettingsToggle(
                    icon = Icons.Default.DarkMode,
                    text = "Modo oscuro",
                    checked = settingsState.isDarkMode,
                    onCheckedChange = { settingsViewModel.toggleDarkMode() }
                )
                
                Divider()
                
                // Botón: Comentarios y reclamos
                SettingsButton(
                    icon = Icons.Default.Feedback,
                    text = "Comentarios y reclamos",
                    onClick = { /* TODO: Implementar */ }
                )
                
                Divider()
                
                // Botón: Ayuda
                SettingsButton(
                    icon = Icons.Default.Help,
                    text = "Ayuda",
                    onClick = { /* TODO: Implementar */ }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
    }
    
    // Dialogs
    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                authViewModel.logout()
                onLogout()
                showLogoutDialog = false
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
    
    if (showChangeImageDialog) {
        ChangeImageDialog(
            onConfirm = { imageUrl ->
                settingsViewModel.updateProfileImage(imageUrl)
                showChangeImageDialog = false
            },
            onDismiss = { showChangeImageDialog = false }
        )
    }
    
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onConfirm = { currentPassword, newPassword, confirmPassword ->
                settingsViewModel.updatePassword(currentPassword, newPassword, confirmPassword)
                showChangePasswordDialog = false
            },
            onDismiss = { showChangePasswordDialog = false }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconTint
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Ir",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cerrar sesión") },
        text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Cerrar sesión")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ChangeImageDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    
    // Launcher para seleccionar imagen desde la galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        android.util.Log.d("ChangeImageDialog", "URI recibida del picker: $uri")
        uri?.let {
            // Intentar otorgar permisos persistentes (puede fallar en algunos proveedores)
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                android.util.Log.d("ChangeImageDialog", "Permisos persistentes otorgados para: $it")
            } catch (e: SecurityException) {
                android.util.Log.w("ChangeImageDialog", "No se pudo otorgar permiso persistente: ${e.message}")
            }
            selectedImageUri = it
            android.util.Log.d("ChangeImageDialog", "selectedImageUri actualizado a: $selectedImageUri")
        }
    }
    
    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar imagen de perfil") },
        text = {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Selecciona una imagen desde tu galería:")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mostrar preview de la imagen seleccionada
                selectedImageUri?.let { uri ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Imagen seleccionada",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seleccionar imagen")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    android.util.Log.d("ChangeImageDialog", "Botón Guardar presionado. selectedImageUri: $selectedImageUri")
                    selectedImageUri?.let { uri ->
                        android.util.Log.d("ChangeImageDialog", "Llamando onConfirm con URI: ${uri.toString()}")
                        onConfirm(uri.toString())
                    } ?: run {
                        android.util.Log.e("ChangeImageDialog", "selectedImageUri es null!")
                    }
                },
                enabled = selectedImageUri != null
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    onConfirm: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar contraseña") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Contraseña actual") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(currentPassword, newPassword, confirmPassword) },
                enabled = currentPassword.isNotBlank() && 
                         newPassword.isNotBlank() && 
                         confirmPassword.isNotBlank() &&
                         newPassword == confirmPassword
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
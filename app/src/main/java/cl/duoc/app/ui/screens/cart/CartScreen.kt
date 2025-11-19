package cl.duoc.app.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.app.data.model.CartItem
import cl.duoc.app.data.model.PaymentMethod
import cl.duoc.app.ui.viewmodel.CartViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    modifier: Modifier = Modifier,
    cartViewModel: CartViewModel = run {
        val context = LocalContext.current
        viewModel { CartViewModel(context) }
    }
) {
    val cartState by cartViewModel.cartState.collectAsState()
    var showCheckoutDialog by remember { mutableStateOf(false) }
    
    // Navegar de vuelta cuando se complete la orden
    LaunchedEffect(cartState.orderCompleted) {
        if (cartState.orderCompleted != null) {
            kotlinx.coroutines.delay(3000)
            cartViewModel.clearOrderCompleted()
        }
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
            text = "Carrito de Compras",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        when {
            cartState.items.isEmpty() && cartState.orderCompleted == null -> {
                EmptyCartState()
            }
            cartState.orderCompleted != null -> {
                OrderCompletedState(order = cartState.orderCompleted!!)
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Lista de productos scrolleable
                    val listState = rememberLazyListState()
                    
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(cartState.items, key = { it.product.id }) { item ->
                            CartItemCard(
                                item = item,
                                onQuantityChange = { newQuantity ->
                                    cartViewModel.updateQuantity(item.product.id, newQuantity)
                                },
                                onRemove = {
                                    cartViewModel.removeFromCart(item.product.id)
                                }
                            )
                        }
                    }
                    
                    // Resumen de compra fijo en la parte inferior
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 120.dp), // Espacio para nav bar (aumentado)
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Productos (${cartState.itemCount})",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = formatPrice(cartState.total),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            HorizontalDivider()
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatPrice(cartState.total),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { showCheckoutDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ir al pago")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de checkout
    if (showCheckoutDialog) {
        CheckoutDialog(
            total = cartState.total,
            isLoading = cartState.isLoading,
            onConfirm = { address, paymentMethod ->
                cartViewModel.processOrder(address, paymentMethod)
                showCheckoutDialog = false
            },
            onDismiss = { showCheckoutDialog = false }
        )
    }
    
    // Mostrar errores
    cartState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { cartViewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { cartViewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Imagen del producto
            val resourceId = remember(item.product.imageUrl) {
                if (item.product.imageUrl.isNotEmpty()) {
                    context.resources.getIdentifier(
                        item.product.imageUrl,
                        "drawable",
                        context.packageName
                    )
                } else 0
            }
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (resourceId != 0) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = resourceId),
                        contentDescription = item.product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    )
                }
            }
            
            // Información del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatPrice(item.product.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Controles de cantidad
                    IconButton(
                        onClick = { onQuantityChange(item.quantity - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Disminuir",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 24.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Botón eliminar
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Subtotal: ${formatPrice(item.subtotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}



@Composable
fun CheckoutDialog(
    total: Double,
    isLoading: Boolean,
    onConfirm: (String, PaymentMethod) -> Unit,
    onDismiss: () -> Unit
) {
    var shippingAddress by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Compra") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Total a pagar: ${formatPrice(total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = shippingAddress,
                    onValueChange = { shippingAddress = it },
                    label = { Text("Dirección de envío") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Método de pago:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Tarjeta de débito
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedPaymentMethod == PaymentMethod.DEBIT_CARD)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    onClick = { selectedPaymentMethod = PaymentMethod.DEBIT_CARD }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "Débito",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tarjeta de Débito",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                // Tarjeta de crédito
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedPaymentMethod == PaymentMethod.CREDIT_CARD)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    onClick = { selectedPaymentMethod = PaymentMethod.CREDIT_CARD }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "Crédito",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Tarjeta de Crédito",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shippingAddress.isNotBlank() && selectedPaymentMethod != null) {
                        onConfirm(shippingAddress, selectedPaymentMethod!!)
                    }
                },
                enabled = !isLoading && shippingAddress.isNotBlank() && selectedPaymentMethod != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Confirmar Pago")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EmptyCartState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
            Text(
                text = "Tu carrito está vacío",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Text(
                text = "Agrega productos desde el catálogo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun OrderCompletedState(order: cl.duoc.app.data.model.Order) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "¡Compra Exitosa!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Orden #${order.id}",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "Total pagado: ${formatPrice(order.total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Divider()
                
                Text(
                    text = "Tu pedido será enviado a:\n${order.shippingAddress}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Método de pago: ${
                        when (order.paymentMethod) {
                            PaymentMethod.DEBIT_CARD -> "Tarjeta de Débito"
                            PaymentMethod.CREDIT_CARD -> "Tarjeta de Crédito"
                        }
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("es").setRegion("CL").build())
    return format.format(price)
}

// ==================== PREVIEWS ====================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Carrito Vacío")
@Composable
fun EmptyCartPreview() {
    cl.duoc.app.ui.theme.PlantBuddyTheme {
        EmptyCartState()
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Item del Carrito")
@Composable
fun CartItemCardPreview() {
    cl.duoc.app.ui.theme.PlantBuddyTheme {
        CartItemCard(
            item = CartItem(
                product = cl.duoc.app.data.model.Product(
                    id = 1,
                    name = "Monstera Deliciosa",
                    description = "Planta tropical de grandes hojas perforadas",
                    price = 25990.0,
                    imageUrl = "monstera",
                    category = "Interior",
                    stock = 10,
                    rating = 4.5f
                ),
                quantity = 2
            ),
            onQuantityChange = {},
            onRemove = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Orden Completada")
@Composable
fun OrderCompletedPreview() {
    cl.duoc.app.ui.theme.PlantBuddyTheme {
        OrderCompletedState(
            order = cl.duoc.app.data.model.Order(
                id = 12345,
                userId = 1,
                items = listOf(),
                total = 51980.0,
                shippingAddress = "Av. Siempre Viva 742, Santiago",
                paymentMethod = PaymentMethod.CREDIT_CARD,
                status = cl.duoc.app.data.model.OrderStatus.COMPLETED,
                createdAt = java.util.Date()
            )
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Diálogo de Checkout")
@Composable
fun CheckoutDialogPreview() {
    cl.duoc.app.ui.theme.PlantBuddyTheme {
        CheckoutDialog(
            total = 51980.0,
            isLoading = false,
            onConfirm = { _, _ -> },
            onDismiss = {}
        )
    }
}

package cl.duoc.app.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.app.data.model.Product
import cl.duoc.app.ui.theme.PlantBuddyTheme
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val gridState = rememberLazyGridState()
    val toolbarOffsetDp by animateDpAsState(
        targetValue = (-estado.toolbarOffset).dp,
        label = "toolbarOffset"
    )
    
    // Observar cambios en la posición del scroll
    LaunchedEffect(gridState) {
        snapshotFlow {
            Pair(
                gridState.firstVisibleItemIndex,
                gridState.firstVisibleItemScrollOffset
            )
        }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                viewModel.onScrollPositionChange(index, offset)
            }
    }
    
    Box(
        modifier = Modifier
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
        // Grid de productos - Fondo, ocupa toda la pantalla
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
                ErrorState(
                    error = estado.error!!,
                    onRetry = viewModel::retry
                )
            }
            estado.filteredProducts.isEmpty() -> {
                EmptyState()
            }
            else -> {
                ProductGrid(
                    products = estado.filteredProducts,
                    gridState = gridState,
                    topPadding = 240.dp // Espacio para toolbar + contador
                )
            }
        }
        
        // Contenedor con clip para ocultar el toolbar cuando sube
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clipToBounds()
        ) {
            // Toolbar colapsable (se oculta con el scroll)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = toolbarOffsetDp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp)
            ) {
            // Título
            Text(
                text = "Catálogo de Plantas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Barra de búsqueda
            SearchBar(
                query = estado.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // Filtros por categoría
            CategoryFilters(
                selectedCategory = estado.selectedCategory,
                onCategorySelected = viewModel::onCategorySelected,
                modifier = Modifier
            )
            }
        }
        
        // Contador de resultados - sticky (siempre visible en la parte superior)
        val counterOffsetDp = (200.dp + toolbarOffsetDp).coerceAtLeast(0.dp)
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = counterOffsetDp)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "${estado.filteredProducts.size} productos encontrados",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Buscar plantas...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun CategoryFilters(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "all" to "Todas",
        "arbustos" to "Arbustos",
        "perennes" to "Perennes",
        "aromáticas" to "Aromáticas",
        "ornamentales" to "Ornamentales",
        "trepadoras" to "Trepadoras"
    )
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        flingBehavior = androidx.compose.foundation.gestures.ScrollableDefaults.flingBehavior()
    ) {
        items(categories, key = { it.first }) { (id, name) ->
            FilterChip(
                selected = selectedCategory == id,
                onClick = { onCategorySelected(id) },
                label = { Text(name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun ProductGrid(
    products: List<Product>,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    topPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = topPadding,
            bottom = 150.dp // Espacio adicional para el nav bar
        ),
        flingBehavior = androidx.compose.foundation.gestures.ScrollableDefaults.flingBehavior()
    ) {
        items(products, key = { it.id }) { product ->
            ProductCard(product = product)
        }
    }
}

@Composable
fun ProductImage(
    imageUrl: String,
    productName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val resourceId = remember(imageUrl) {
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
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (resourceId != 0) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = productName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            // Placeholder si no se encuentra la imagen
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = productName,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    var quantity by remember { mutableStateOf(1) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { /* TODO: Navegar a detalle */ },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen del producto
            ProductImage(
                imageUrl = product.imageUrl,
                productName = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)

            )
            
            // Información del producto
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                //Text(
                //    text = product.description,
                //    style = MaterialTheme.typography.bodySmall,
                //    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                //    maxLines = 2,
                //    overflow = TextOverflow.Ellipsis
                //)
                
                //Spacer(modifier = Modifier.weight(1f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    // Precio
                    Text(
                        text = formatPrice(product.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = product.rating.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Controles de cantidad y botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                                        // Controles de cantidad
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Botón decrementar
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { if (quantity > 1) quantity-- },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Disminuir cantidad",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        
                        // Cantidad
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        
                        // Botón incrementar
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { quantity++ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Aumentar cantidad",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    
                    // Botones de acción
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Botón agregar al carrito
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { /* TODO: Agregar al carrito */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Agregar al carrito",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        
                        // Botón agregar al plantel
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { /* TODO: Agregar al plantel */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFlorist,
                                contentDescription = "Agregar al plantel",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No se encontraron productos",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Intenta con otra búsqueda",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

private fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    return format.format(price)
}

@Preview(showBackground = true)
@Composable
fun CatalogScreenPreview() {
    PlantBuddyTheme {
        CatalogScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ProductCardPreview() {
    PlantBuddyTheme {
        ProductCard(
            product = Product(
                id = 1,
                name = "Monstera Deliciosa",
                description = "Planta tropical de grandes hojas perforadas",
                price = 25990.0,
                imageUrl = "monstera",
                category = "Interior",
                stock = 10,
                rating = 4.5f
            )
        )
    }
}

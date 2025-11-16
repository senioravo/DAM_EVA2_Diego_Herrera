package cl.duoc.app.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CatalogViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {
    
    private val _estado = MutableStateFlow(CatalogUIState())
    val estado: StateFlow<CatalogUIState> = _estado.asStateFlow()
    
    init {
        loadProducts()
    }
    
    private fun loadProducts() {
        viewModelScope.launch {
            _estado.update { it.copy(isLoading = true, error = null) }
            
            repository.getProducts().fold(
                onSuccess = { products ->
                    _estado.update {
                        it.copy(
                            products = products,
                            filteredProducts = products,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _estado.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar productos: ${error.message}"
                        )
                    }
                }
            )
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _estado.update { it.copy(searchQuery = query) }
        filterProducts()
    }
    
    fun onCategorySelected(category: String) {
        _estado.update { it.copy(selectedCategory = category) }
        filterProducts()
    }
    
    private fun filterProducts() {
        viewModelScope.launch {
            val currentState = _estado.value
            val query = currentState.searchQuery
            val category = currentState.selectedCategory
            
            val filtered = currentState.products.filter { product ->
                val matchesSearch = query.isEmpty() || 
                    product.name.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true)
                
                val matchesCategory = category == "all" || 
                    product.category.equals(category, ignoreCase = true)
                
                matchesSearch && matchesCategory
            }
            
            _estado.update { it.copy(filteredProducts = filtered) }
        }
    }
    
    fun retry() {
        loadProducts()
    }
    
    fun onScrollPositionChange(firstVisibleIndex: Int, firstVisibleOffset: Int) {
        val currentState = _estado.value
        val previousIndex = currentState.lastScrollIndex
        val previousOffset = currentState.lastScrollOffset
        
        // Calcular el desplazamiento real en píxeles (más lento)
        val scrollDelta = if (firstVisibleIndex != previousIndex) {
            // Cambio de item
            (firstVisibleIndex - previousIndex) * 5f
        } else {
            // Mismo item: usar la diferencia de offset (reducido a 50% de velocidad)
            (firstVisibleOffset - previousOffset).toFloat() * 0.5f
        }
        
        // Calcular nuevo offset del toolbar (máximo 200dp de desplazamiento)
        val maxOffset = 200f
        val newOffset = (currentState.toolbarOffset + scrollDelta)
            .coerceIn(0f, maxOffset)
        
        _estado.update {
            it.copy(
                toolbarOffset = newOffset,
                lastScrollIndex = firstVisibleIndex,
                lastScrollOffset = firstVisibleOffset
            )
        }
    }
}

package cl.duoc.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.app.model.data.config.AppDatabase
import cl.duoc.app.model.data.repositories.ProductRepository
import cl.duoc.app.model.data.repositories.PlantelRepository
import cl.duoc.app.model.domain.Category
import cl.duoc.app.model.domain.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUIState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: String = "all",
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val toolbarOffset: Float = 0f,
    val lastScrollIndex: Int = 0,
    val lastScrollOffset: Int = 0
)

class CatalogViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val productRepository = ProductRepository(database.productDao())
    private val plantelRepository = PlantelRepository(
        database.plantelPlantDao(),
        database.productDao()
    )
    
    private val _estado = MutableStateFlow(CatalogUIState())
    val estado: StateFlow<CatalogUIState> = _estado.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _estado.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Cargar categorías
                productRepository.getCategories().collect { categories ->
                    _estado.update { it.copy(categories = categories) }
                }
                
                // Cargar productos
                productRepository.getAllProducts().collect { products ->
                    _estado.update {
                        it.copy(
                            products = products,
                            filteredProducts = products,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar productos: ${e.message}"
                    )
                }
            }
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
        loadData()
    }
    
    fun onScrollPositionChange(firstVisibleIndex: Int, firstVisibleOffset: Int) {
        val currentState = _estado.value
        val previousIndex = currentState.lastScrollIndex
        val previousOffset = currentState.lastScrollOffset
        
        val scrollDelta = if (firstVisibleIndex != previousIndex) {
            (firstVisibleIndex - previousIndex) * 5f
        } else {
            (firstVisibleOffset - previousOffset).toFloat() * 0.5f
        }
        
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
    
    fun addToPlantel(product: Product, userId: Int) {
        viewModelScope.launch {
            try {
                plantelRepository.addPlantToUser(userId, product)
            } catch (e: Exception) {
                android.util.Log.e("CatalogViewModel", "Error al agregar planta", e)
            }
        }
    }
}

package cl.duoc.app.ui.screens.catalog

import cl.duoc.app.data.model.Product

data class CatalogUIState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "all",
    val isLoading: Boolean = false,
    val error: String? = null,
    val toolbarOffset: Float = 0f,
    val lastScrollIndex: Int = 0,
    val lastScrollOffset: Int = 0
)

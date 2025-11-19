package cl.duoc.app.model.domain

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String,
    val stock: Int = 0,
    val rating: Float = 0f,
    val wateringCycleDays: Int = 7
)

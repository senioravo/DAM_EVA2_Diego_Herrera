package cl.duoc.app.data.api.dto

// Auth DTOs
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val confirmPassword: String
)

data class AuthResponse(
    val token: String,
    val usuario: UsuarioDTO
)

data class UsuarioDTO(
    val id: Int,
    val email: String,
    val username: String,
    val profileImageUrl: String?
)

// Producto DTOs
data class ProductoDTO(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String?,
    val rating: Float?,
    val categoria: CategoriaDTO?,
    val plantaDetalle: PlantaDetalleDTO?
)

data class CategoriaDTO(
    val id: Int,
    val nombre: String,
    val descripcion: String?
)

data class PlantaDetalleDTO(
    val id: Int,
    val nombreCientifico: String?,
    val tipo: String?,
    val luzRequerida: String?,
    val frecuenciaRiego: String?,
    val temperaturaMin: Float?,
    val temperaturaMax: Float?,
    val toxicidad: String?,
    val dificultadCuidado: String?
)

// Plantel DTOs
data class PlantelPlantDTO(
    val id: Int,
    val usuario: UsuarioDTO,
    val producto: ProductoDTO,
    val customTitle: String?,
    val notes: String?,
    val lastWateredDate: String?,
    val wateringFrequencyDays: Int,
    val notificationsEnabled: Boolean,
    val addedAt: String
)

data class AddPlantRequest(
    val userId: Int,
    val productId: Int
)

data class UpdateTitleRequest(
    val customTitle: String
)

data class UpdateNotesRequest(
    val notes: String
)

// Compra DTOs
data class CompraDTO(
    val id: Int,
    val usuario: UsuarioDTO,
    val detalles: List<DetalleCompraDTO>,
    val total: Double,
    val metodoPago: String,
    val estado: String,
    val createdAt: String
)

data class DetalleCompraDTO(
    val id: Int,
    val producto: ProductoDTO,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
)

data class CreateCompraRequest(
    val userId: Int,
    val items: List<CartItemRequest>,
    val metodoPago: String
)

data class CartItemRequest(
    val productId: Int,
    val quantity: Int
)

data class UpdateStatusRequest(
    val estado: String
)

// Response genérico para errores
data class ErrorResponse(
    val message: String,
    val status: Int
)

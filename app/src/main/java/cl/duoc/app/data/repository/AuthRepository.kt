package cl.duoc.app.data.repository

import android.content.Context
import android.util.Log
import cl.duoc.app.data.api.RetrofitClient
import cl.duoc.app.data.api.dto.LoginRequest
import cl.duoc.app.data.api.dto.RegisterRequest
import cl.duoc.app.data.model.User
import cl.duoc.app.data.preferences.TokenManager
import java.util.Date

class AuthRepository(private val context: Context) {
    
    private val api = RetrofitClient.authApi
    private val tokenManager = TokenManager(context)
    private val TAG = "AuthRepository"
    
    // Login con API
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val request = LoginRequest(email, password)
            val response = api.login(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                // Guardar token JWT
                tokenManager.saveToken(
                    token = authResponse.token,
                    userId = authResponse.usuario.id,
                    email = authResponse.usuario.email
                )
                
                // Convertir a modelo User
                val user = User(
                    id = authResponse.usuario.id,
                    email = authResponse.usuario.email,
                    password = "", // No guardamos password en local
                    profileImageUrl = authResponse.usuario.profileImageUrl,
                    createdAt = Date(),
                    isAdmin = false // TODO: agregar campo isAdmin en backend
                )
                
                Log.d(TAG, "Login exitoso: ${user.email}")
                Result.success(user)
            } else {
                Log.e(TAG, "Error en login: ${response.code()} - ${response.message()}")
                Result.failure(Exception("Email o contraseña incorrectos"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en login", e)
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
    
    // Registro con API
    suspend fun register(email: String, username: String, password: String): Result<User> {
        return try {
            // Validaciones locales
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return Result.failure(Exception("Email inválido"))
            }
            
            if (password.length < 6) {
                return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
            }
            
            if (username.isBlank()) {
                return Result.failure(Exception("El nombre de usuario es requerido"))
            }
            
            val request = RegisterRequest(email, username, password, password)
            val response = api.register(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                // Guardar token JWT
                tokenManager.saveToken(
                    token = authResponse.token,
                    userId = authResponse.usuario.id,
                    email = authResponse.usuario.email
                )
                
                val user = User(
                    id = authResponse.usuario.id,
                    email = authResponse.usuario.email,
                    password = "",
                    profileImageUrl = authResponse.usuario.profileImageUrl,
                    createdAt = Date(),
                    isAdmin = false
                )
                
                Log.d(TAG, "Registro exitoso: ${user.email}")
                Result.success(user)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Datos inválidos"
                    409 -> "El email ya está registrado"
                    else -> "Error al registrar usuario"
                }
                Log.e(TAG, "Error en registro: ${response.code()}")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en registro", e)
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
    
    // Obtener usuario actual
    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = api.getCurrentUser()
            
            if (response.isSuccessful && response.body() != null) {
                val userDTO = response.body()!!
                val user = User(
                    id = userDTO.id,
                    email = userDTO.email,
                    password = "",
                    profileImageUrl = userDTO.profileImageUrl,
                    createdAt = Date(),
                    isAdmin = false
                )
                Result.success(user)
            } else {
                Result.failure(Exception("No se pudo obtener el usuario"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener usuario actual", e)
            Result.failure(e)
        }
    }
    
    // Logout
    suspend fun logout() {
        tokenManager.clearToken()
        Log.d(TAG, "Sesión cerrada")
    }
    
    // Verificar si hay sesión activa
    suspend fun hasActiveSession(): Boolean {
        return tokenManager.hasToken()
    }
    
    // Obtener ID de usuario guardado
    suspend fun getSavedUserId(): Int? {
        return tokenManager.getUserId()
    }
}

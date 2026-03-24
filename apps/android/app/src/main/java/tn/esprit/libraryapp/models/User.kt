package tn.esprit.libraryapp.models

data class User(
    val id: String,
    val username: String,
    val email: String,
    val password: String,
    val profilePicture: String?,
    val role: String,
)

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(val accessToken: String, val refreshToken: String, val userId: String)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val profilePicture: String? = null,
)

data class RegisterResponse(val token: String)

data class ChangePasswordRequest(val email: String, val password: String, val newPassword: String)

data class ChangePasswordResponse(val token: String)

data class GenerateEmailRequest(val email: String)

data class GenerateEmailResponse(val token: String)

data class VerifyEmailRequest(val email: String, val otp: String)

data class VerifyEmailResponse(val token: String)

data class ForgotPasswordRequest(val email: String, val password: String)

data class ForgotPasswordResponse(val token: String)

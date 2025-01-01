package tn.esprit.libraryapp.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import tn.esprit.libraryapp.models.ChangePasswordRequest
import tn.esprit.libraryapp.models.ChangePasswordResponse
import tn.esprit.libraryapp.models.ForgotPasswordRequest
import tn.esprit.libraryapp.models.ForgotPasswordResponse
import tn.esprit.libraryapp.models.GenerateEmailRequest
import tn.esprit.libraryapp.models.GenerateEmailResponse
import tn.esprit.libraryapp.models.LoginRequest
import tn.esprit.libraryapp.models.LoginResponse
import tn.esprit.libraryapp.models.RegisterRequest
import tn.esprit.libraryapp.models.RegisterResponse
import tn.esprit.libraryapp.models.User
import tn.esprit.libraryapp.models.VerifyEmailRequest
import tn.esprit.libraryapp.models.VerifyEmailResponse

interface UserService {
    @GET("user")
    suspend fun getUsers(): List<User>

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<RegisterResponse>

    @PUT("auth/change-password")
    suspend fun changePassword(
        @Body changePasswordRequest: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    @POST("auth/generate-email")
    suspend fun generateEmail(
        @Body generateEmailRequest: GenerateEmailRequest
    ): Response<GenerateEmailResponse>

    @POST("auth/verify-email")
    suspend fun verifyEmail(
        @Body verifyEmailRequest: VerifyEmailRequest
    ): Response<VerifyEmailResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body forgotPasswordRequest: ForgotPasswordRequest
    ): Response<ForgotPasswordResponse>

    @GET("user/profile/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<User>
}

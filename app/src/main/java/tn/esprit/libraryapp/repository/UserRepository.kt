package tn.esprit.libraryapp.repository

import retrofit2.Response
import tn.esprit.libraryapp.api.RetrofitService
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

class UserRepository {
    private val userService = RetrofitService.userService
    suspend fun getUsers(): List<User> {
        return userService.getUsers()
    }

    suspend fun login(loginRequest: LoginRequest): Response<LoginResponse> {
        return userService.login(loginRequest)
    }

    suspend fun register(registerRequest: RegisterRequest): Response<RegisterResponse> {
        return userService.register(registerRequest)
    }


    suspend fun changePassword(changePasswordRequest: ChangePasswordRequest): Response<ChangePasswordResponse> {
        return userService.changePassword(changePasswordRequest)
    }

    suspend fun generateEmail(generateEmailRequest: GenerateEmailRequest): Response<GenerateEmailResponse> {
        return userService.generateEmail(generateEmailRequest)
    }

    suspend fun verifyEmail(verifyEmailRequest: VerifyEmailRequest): Response<VerifyEmailResponse> {
        return userService.verifyEmail(verifyEmailRequest)
    }

    suspend fun forgotPassword(forgotPasswordRequest: ForgotPasswordRequest): Response<ForgotPasswordResponse> {
        return userService.forgotPassword(forgotPasswordRequest)
    }
}

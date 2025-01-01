package tn.esprit.libraryapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
import tn.esprit.libraryapp.repository.UserRepository
import tn.esprit.libraryapp.services.TokenManagerProvider

class AuthViewModel() : ViewModel() {
    private val repository = UserRepository()
    private val _loginResult = MutableStateFlow<Result<LoginResponse>?>(null)
    private val _registerResult = MutableStateFlow<Result<RegisterResponse>?>(null)
    private val _changePasswordResult = MutableStateFlow<Result<ChangePasswordResponse>?>(null)
    private val _verifyEmailResult = MutableStateFlow<Result<VerifyEmailResponse>?>(null)
    private val _forgotPasswordResult = MutableStateFlow<Result<ForgotPasswordResponse>?>(null)
    private val _generateEmailResult = MutableStateFlow<Result<GenerateEmailResponse>?>(null)
    private val _userProfile = MutableStateFlow<User?>(null)
    private val _name = MutableStateFlow("")
    private val _email = MutableStateFlow("ali.ammari@esprit.tn")
    private val _password = MutableStateFlow("password")

    val loginResult: StateFlow<Result<LoginResponse>?> = _loginResult
    val registerResult: StateFlow<Result<RegisterResponse>?> = _registerResult
    val changePasswordResult: StateFlow<Result<ChangePasswordResponse>?> = _changePasswordResult
    val verifyEmailResult: StateFlow<Result<VerifyEmailResponse>?> = _verifyEmailResult
    val forgotPasswordResult: StateFlow<Result<ForgotPasswordResponse>?> = _forgotPasswordResult
    val generateEmailResult: StateFlow<Result<GenerateEmailResponse>?> = _generateEmailResult
    val userProfile: StateFlow<User?> = _userProfile
    val name: StateFlow<String> = _name
    val email: StateFlow<String> = _email
    val password: StateFlow<String> = _password

    fun onNameChange(value: String) {
        _name.value = value
    }


    fun onEmailChange(value: String) {
        _email.value = value
    }


    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun login(loginRequest: LoginRequest) {
        viewModelScope.launch {
            try {
                val response = repository.login(loginRequest)
                if (response.isSuccessful) {
                    val loginResponse: LoginResponse = response.body()!!
                    TokenManagerProvider.getInstance()
                        .saveTokens(
                            loginResponse.accessToken,
                            loginResponse.refreshToken,
                            loginResponse.userId
                        )
                    _loginResult.value = Result.success(loginResponse)
                } else {
                    _loginResult.value = Result.failure(Exception("Login failed"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
                Log.e("AuthViewModel", "Error logging in " + e.message, e)
            }
        }
    }

    fun clearLoginResult() {
        _loginResult.value = null
    }

    fun logout() {
        viewModelScope.launch {
            try {
                TokenManagerProvider.getInstance().clearTokens()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error logging out", e)
            }
        }
    }

    fun register(registerRequest: RegisterRequest) {
        viewModelScope.launch {
            try {
                val response = repository.register(registerRequest)
                if (response.isSuccessful) {
                    _registerResult.value = Result.success(response.body()!!)
                } else {
                    _registerResult.value = Result.failure(Exception("Registration failed"))
                }
            } catch (e: Exception) {
                _registerResult.value = Result.failure(e)
                Log.e("AuthViewModel", "Error registering", e)
            }
        }
    }

    fun clearRegisterResult() {
        _registerResult.value = null
    }

    fun changePassword(changePasswordRequest: ChangePasswordRequest) {
        viewModelScope.launch {
            try {
                val response = repository.changePassword(changePasswordRequest)
                if (response.isSuccessful) {
                    _changePasswordResult.value = Result.success(response.body()!!)
                } else {
                    _changePasswordResult.value =
                        Result.failure(Exception("Password change failed"))
                }
            } catch (e: Exception) {
                _changePasswordResult.value = Result.failure(e)
                Log.e("AuthViewModel", "Error changing password", e)
            }
        }
    }

    fun generateEmail(generateEmailRequest: GenerateEmailRequest) {
        viewModelScope.launch {
            try {
                val response = repository.generateEmail(generateEmailRequest)
                if (response.isSuccessful) {
                    _generateEmailResult.value = Result.success(response.body()!!)
                } else {
                    _generateEmailResult.value =
                        Result.failure(Exception("Email generation failed"))
                }
            } catch (e: Exception) {
                _generateEmailResult.value = Result.failure(e)
                Log.e("AuthViewModel", "Error generating email", e)
            }
        }
    }

    fun verifyEmail(verifyEmailRequest: VerifyEmailRequest) {
        viewModelScope.launch {
            try {
                val response = repository.verifyEmail(verifyEmailRequest)
                if (response.isSuccessful) {
                    _verifyEmailResult.value = Result.success(response.body()!!)
                } else {
                    _verifyEmailResult.value =
                        Result.failure(Exception("Email verification failed"))
                }
            } catch (e: Exception) {
                _verifyEmailResult.value = Result.failure(e)
                Log.e("AuthViewModel", "Error verifying email", e)
            }
        }
    }

    fun forgotPassword(forgotPasswordRequest: ForgotPasswordRequest) {
        viewModelScope.launch {
            try {
                val response = repository.forgotPassword(forgotPasswordRequest)
                if (response.isSuccessful) {
                    _forgotPasswordResult.value = Result.success(response.body()!!)
                } else {
                    _forgotPasswordResult.value =
                        Result.failure(Exception("Forgot password failed"))
                }
            } catch (e: Exception) {
                _forgotPasswordResult.value = Result.failure(e)
                Log.e("AuthViewModel", "Error forgot password", e)
            }
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val userId = TokenManagerProvider.getInstance().userId.first()
                userId?.let {
                    val response = repository.getUserProfile(it)
                    if (response.isSuccessful) {
                        _userProfile.value = response.body()
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user profile", e)
            }
        }
    }
}

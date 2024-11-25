package tn.esprit.libraryapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class AuthViewModel : ViewModel() {
    private val repository = UserRepository()
    private val _users = MutableLiveData<List<User>>()
    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    private val _registerResult = MutableLiveData<Result<RegisterResponse>>()
    private val _changePasswordResult = MutableLiveData<Result<ChangePasswordResponse>>()
    private val _verifyEmailResult = MutableLiveData<Result<VerifyEmailResponse>>()
    private val _forgotPasswordResult = MutableLiveData<Result<ForgotPasswordResponse>>()
    private val _generateEmailResult = MutableLiveData<Result<GenerateEmailResponse>>()

    val loginResult: LiveData<Result<LoginResponse>> = _loginResult
    val registerResult: LiveData<Result<RegisterResponse>> = _registerResult
    val changePasswordResult: LiveData<Result<ChangePasswordResponse>> = _changePasswordResult
    val verifyEmailResult: LiveData<Result<VerifyEmailResponse>> = _verifyEmailResult
    val forgotPasswordResult: LiveData<Result<ForgotPasswordResponse>> = _forgotPasswordResult
    val generateEmailResult: LiveData<Result<GenerateEmailResponse>> = _generateEmailResult
    val users: LiveData<List<User>> = _users

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val cards = repository.getUsers()
                _users.value = cards
            } catch (e: Exception) {
            }
        }
    }

    fun login(loginRequest: LoginRequest) {
        viewModelScope.launch {
            try {
                val response = repository.login(loginRequest)
                if (response.isSuccessful) {
                    _loginResult.value = Result.success(response.body()!!)
                } else {
                    _loginResult.value = Result.failure(Exception("Login failed"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
                Log.e("AuthViewModel", "Error logging in " + e.message, e)
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
}

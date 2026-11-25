package upca.example.cloudpad.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val auth = Firebase.auth

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, errorMessage = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, errorMessage = null) }
    }

    fun toggleAuthMode() {
        _uiState.update {
            it.copy(
                isRegisterMode = !it.isRegisterMode,
                errorMessage = null
            )
        }
    }

    fun setGoogleError(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = message
            )
        }
    }

    fun submitAuth(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (email.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Por favor, introduza o seu email.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Formato de email inválido.") }
            return
        }
        if (password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Por favor, introduza a sua palavra-passe.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "A palavra-passe tem de ter pelo menos 6 caracteres.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        if (_uiState.value.isRegisterMode) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = mapFirebaseErrorToPortuguese(exception, isRegister = true)
                        )
                    }
                }
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = mapFirebaseErrorToPortuguese(exception, isRegister = false)
                        )
                    }
                }
        }
    }

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            }
            .addOnFailureListener { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = mapFirebaseErrorToPortuguese(exception, isRegister = false)
                    )
                }
            }
    }

    private fun mapFirebaseErrorToPortuguese(exception: Exception, isRegister: Boolean): String {
        return when (exception) {
            is FirebaseAuthInvalidUserException -> "Não existe nenhuma conta associada a este email."
            is FirebaseAuthInvalidCredentialsException -> {
                if (isRegister) {
                    "O endereço de email introduzido é inválido."
                } else {
                    "Email ou palavra-passe incorretos."
                }
            }
            is FirebaseAuthUserCollisionException -> "Já existe uma conta registada com este email."
            is FirebaseAuthWeakPasswordException -> "A palavra-passe é demasiado fraca. Use pelo menos 6 caracteres com números ou símbolos."
            is FirebaseNetworkException -> "Sem ligação à rede. Verifique a sua ligação à Internet."
            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "O formato do email não é válido."
                    "ERROR_WRONG_PASSWORD" -> "Palavra-passe incorreta."
                    "ERROR_USER_NOT_FOUND" -> "Utilizador não encontrado."
                    "ERROR_USER_DISABLED" -> "Esta conta foi desativada."
                    "ERROR_TOO_MANY_REQUESTS" -> "Demasiadas tentativas falhadas. Tente novamente mais tarde."
                    "ERROR_OPERATION_NOT_ALLOWED" -> "Este método de autenticação não está ativado no Firebase."
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Este email já se encontra registado."
                    else -> if (isRegister) "Erro ao criar conta. Tente novamente." else "Erro ao iniciar sessão. Tente novamente."
                }
            }
            else -> {
                val msg = exception.localizedMessage ?: ""
                when {
                    msg.contains("network error", ignoreCase = true) -> "Erro de rede. Verifique a sua ligação à Internet."
                    msg.contains("badly formatted", ignoreCase = true) -> "O formato do email não é válido."
                    msg.contains("password is invalid", ignoreCase = true) -> "Palavra-passe inválida."
                    msg.contains("no user record", ignoreCase = true) -> "Utilizador não encontrado."
                    else -> if (isRegister) "Erro ao criar conta. Tente novamente." else "Erro ao iniciar sessão. Tente novamente."
                }
            }
        }
    }
}

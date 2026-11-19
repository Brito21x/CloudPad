package upca.example.cloudpad.ui.notes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import upca.example.cloudpad.models.Note

enum class AutoSaveStatus {
    IDLE,
    SAVING,
    SAVED,
    ERROR
}

data class NoteEditorUiState(
    val noteId: String = "",
    val title: String = "",
    val content: String = "",
    val saveStatus: AutoSaveStatus = AutoSaveStatus.IDLE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class NoteEditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var autoSaveJob: Job? = null

    fun initializeNote(noteId: String?) {
        val currentUser = auth.currentUser ?: return

        if (noteId == null || noteId == "new" || noteId.isEmpty()) {
            // Nova nota: gera um ID de documento imediatamente para poder salvar de forma transparente
            val newId = db.collection("notes").document().id
            _uiState.update {
                it.copy(
                    noteId = newId,
                    title = "",
                    content = "",
                    saveStatus = AutoSaveStatus.IDLE
                )
            }
        } else {
            // Carregar nota existente
            _uiState.update { it.copy(noteId = noteId, isLoading = true) }
            db.collection("notes").document(noteId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val note = document.toObject(Note::class.java)
                        _uiState.update {
                            it.copy(
                                title = note?.title ?: "",
                                content = note?.content ?: "",
                                isLoading = false,
                                saveStatus = AutoSaveStatus.SAVED
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
                .addOnFailureListener { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.localizedMessage
                        )
                    }
                }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
        triggerAutoSave()
    }

    fun onContentChange(newContent: String) {
        _uiState.update { it.copy(content = newContent) }
        triggerAutoSave()
    }

    private fun triggerAutoSave() {
        val currentUserId = auth.currentUser?.uid ?: return
        val currentNoteId = _uiState.value.noteId
        val title = _uiState.value.title
        val content = _uiState.value.content

        // Só salva se houver algum texto escrito
        if (title.isBlank() && content.isBlank()) {
            return
        }

        _uiState.update { it.copy(saveStatus = AutoSaveStatus.SAVING) }

        // Cancela o debounce anterior se o utilizador continuar a teclar
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(500) // Debounce de meio segundo enquanto escreve

            val noteData = hashMapOf(
                "id" to currentNoteId,
                "userId" to currentUserId,
                "title" to title,
                "content" to content,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            db.collection("notes")
                .document(currentNoteId)
                .set(noteData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("CloudPad", "Nota $currentNoteId guardada automaticamente.")
                    _uiState.update { it.copy(saveStatus = AutoSaveStatus.SAVED) }
                }
                .addOnFailureListener { error ->
                    Log.e("CloudPad", "Erro ao guardar nota", error)
                    _uiState.update {
                        it.copy(
                            saveStatus = AutoSaveStatus.ERROR,
                            errorMessage = error.localizedMessage
                        )
                    }
                }
        }
    }
}

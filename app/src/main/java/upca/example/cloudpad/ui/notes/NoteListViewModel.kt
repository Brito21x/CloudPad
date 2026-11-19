package upca.example.cloudpad.ui.notes

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import upca.example.cloudpad.models.Note

data class NotesListUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val userEmail: String? = null,
    val error: String? = null
)

class NoteListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotesListUiState())
    val uiState: StateFlow<NotesListUiState> = _uiState.asStateFlow()

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    init {
        _uiState.update { it.copy(userEmail = auth.currentUser?.email) }
        listenToNotes()
    }

    private fun listenToNotes() {
        val currentUser = auth.currentUser ?: return
        _uiState.update { it.copy(isLoading = true) }

        db.collection("notes")
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("CloudPad", "Erro ao carregar notas", e)
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                    return@addSnapshotListener
                }

                val notesList = mutableListOf<Note>()
                if (snapshot != null) {
                    for (doc in snapshot.documents) {
                        val note = doc.toObject(Note::class.java) ?: Note()
                        note.id = doc.id
                        notesList.add(note)
                    }
                }
                // Ordenar por data de atualização mais recente
                notesList.sortByDescending { it.updatedAt?.toDate() }

                _uiState.update {
                    it.copy(
                        notes = notesList,
                        isLoading = false,
                        error = null
                    )
                }
            }
    }

    fun deleteNote(noteId: String) {
        db.collection("notes").document(noteId).delete()
    }

    fun signOut(onSignedOut: () -> Unit) {
        auth.signOut()
        onSignedOut()
    }
}

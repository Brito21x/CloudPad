package upca.example.cloudpad.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import upca.example.cloudpad.Screen
import upca.example.cloudpad.models.Note
import upca.example.cloudpad.ui.components.MyTopBar
import upca.example.cloudpad.ui.components.NoteCard
import upca.example.cloudpad.ui.theme.CloudPadTheme
import java.util.Date

@Composable
fun NotesListScreen(
    navController: NavController,
    viewModel: NoteListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NotesListContent(
        uiState = uiState,
        onAddNoteClick = {
            navController.navigate(Screen.NoteEditor.createRoute("new"))
        },
        onNoteClick = { noteId ->
            navController.navigate(Screen.NoteEditor.createRoute(noteId))
        },
        onDeleteNote = { noteId ->
            viewModel.deleteNote(noteId)
        },
        onSignOutClick = {
            viewModel.signOut {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(Screen.NotesList.route) { inclusive = true }
                }
            }
        }
    )
}

@Composable
fun NotesListContent(
    uiState: NotesListUiState,
    onAddNoteClick: () -> Unit,
    onNoteClick: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onSignOutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            MyTopBar(
                title = "CloudPad",
                isBaseScreen = true,
                actions = {
                    IconButton(onClick = onSignOutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sair",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar Nota",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading && uiState.notes.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.notes.isEmpty() -> {
                    EmptyNotesState(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onNoteClick(note.id) },
                                onDelete = { onDeleteNote(note.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotesState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ainda não tem notas",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Toque no botão + para começar a escrever notas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, name = "Lista de Notas")
@Composable
fun NotesListContentPreview() {
    CloudPadTheme {
        NotesListContent(
            uiState = NotesListUiState(
                userEmail = "usuario@cloudpad.com",
                notes = listOf(
                    Note(
                        id = "1",
                        title = "Ideias para o Projeto",
                        content = "1. Configurar Firestore\n2. Sincronização automática\n3. Design moderno com Jetpack Compose",
                        updatedAt = Timestamp(Date())
                    ),
                    Note(
                        id = "2",
                        title = "Lista de Compras",
                        content = "Café, Leite, Pão, Frutas frescas",
                        updatedAt = Timestamp(Date())
                    )
                )
            ),
            onAddNoteClick = {},
            onNoteClick = {},
            onDeleteNote = {},
            onSignOutClick = {}
        )
    }
}

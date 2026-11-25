package upca.example.cloudpad.ui.screens.noteEdit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import upca.example.cloudpad.ui.components.MyTopBar
import upca.example.cloudpad.ui.theme.CloudPadTheme

@Composable
fun NoteEditorScreen(
    noteId: String?,
    navController: NavController = rememberNavController(),
    onNavigateBack: () -> Unit = { navController.popBackStack() },
    viewModel: NoteEditorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(noteId) {
        viewModel.initializeNote(noteId)
    }

    NoteEditorContent(
        uiState = uiState,
        navController = navController,
        onTitleChange = { viewModel.onTitleChange(it) },
        onContentChange = { viewModel.onContentChange(it) },
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun NoteEditorContent(
    uiState: NoteEditorUiState,
    navController: NavController = rememberNavController(),
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val statusText = when (uiState.saveStatus) {
        AutoSaveStatus.SAVING -> "A guardar..."
        AutoSaveStatus.SAVED -> "Guardado na nuvem"
        AutoSaveStatus.ERROR -> "Erro ao guardar"
        AutoSaveStatus.IDLE -> "CloudPad"
    }

    Scaffold(
        topBar = {
            MyTopBar(
                title = statusText,
                navController = navController,
                isBaseScreen = false,
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        when (uiState.saveStatus) {
                            AutoSaveStatus.SAVING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            AutoSaveStatus.SAVED -> {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Guardado",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            AutoSaveStatus.ERROR -> {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Erro",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            AutoSaveStatus.IDLE -> {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Sincronização",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            TextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                placeholder = {
                    Text(
                        text = "Título",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    )
                },
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = uiState.content,
                onValueChange = onContentChange,
                placeholder = {
                    Text(
                        text = "Comece a escrever a sua nota...",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    )
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Preview(showBackground = true, name = "Editor - Guardado")
@Composable
fun NoteEditorSavedPreview() {
    CloudPadTheme {
        NoteEditorContent(
            uiState = NoteEditorUiState(
                title = "Planeamento da Semana",
                content = "1. Teste",
                saveStatus = AutoSaveStatus.SAVED
            ),
            onTitleChange = {},
            onContentChange = {},
            onNavigateBack = {}
        )
    }
}

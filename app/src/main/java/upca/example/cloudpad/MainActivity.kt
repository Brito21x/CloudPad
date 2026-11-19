package upca.example.cloudpad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import upca.example.cloudpad.ui.auth.AuthScreen
import upca.example.cloudpad.ui.notes.NoteEditorScreen
import upca.example.cloudpad.ui.notes.NotesListScreen
import upca.example.cloudpad.ui.theme.CloudPadTheme

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object NotesList : Screen("notes_list")
    data object NoteEditor : Screen("note_editor/{noteId}") {
        fun createRoute(noteId: String) = "note_editor/$noteId"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CloudPadTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CloudPadApp()
                }
            }
        }
    }
}

@Composable
fun CloudPadApp() {
    val navController = rememberNavController()
    val isUserLoggedIn = Firebase.auth.currentUser != null
    val startDestination = if (isUserLoggedIn) Screen.NotesList.route else Screen.Auth.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Auth.route) {
            AuthScreen(navController = navController)
        }

        composable(route = Screen.NotesList.route) {
            NotesListScreen(navController = navController)
        }

        composable(
            route = Screen.NoteEditor.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    defaultValue = "new"
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: "new"
            NoteEditorScreen(
                noteId = noteId,
                navController = navController
            )
        }
    }
}

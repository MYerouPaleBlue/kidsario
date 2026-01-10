package net.miksoft.kidsario

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import net.miksoft.kidsario.presentation.home.HomeComponent
import net.miksoft.kidsario.presentation.home.HomeViewModel
import net.miksoft.kidsario.presentation.countingObjects.CountingObjectsComponent
import net.miksoft.kidsario.presentation.countingObjects.CountingObjectsViewModel
import net.miksoft.kidsario.presentation.drawingLetters.DrawingLettersComponent
import net.miksoft.kidsario.presentation.drawingLetters.DrawingLettersViewModel
import net.miksoft.kidsario.presentation.maze.MazeComponent
import net.miksoft.kidsario.presentation.maze.MazeViewModel
import net.miksoft.kidsario.theme.KidsarioTheme

// Enum to represent different screens in the app
enum class Screen {
    HOME,
    COUNTING_OBJECTS,
    DRAWING_LETTERS,
    MAZE
}

@Composable
@Preview
fun App() {
    KidsarioTheme {
        // State to track the current screen
        var currentScreen by remember { mutableStateOf(Screen.HOME) }

        // Create ViewModel instances
        val homeViewModel = remember { HomeViewModel() }
        val countingObjectsViewModel = remember { CountingObjectsViewModel() }
        val drawingLettersViewModel = remember { DrawingLettersViewModel() }
        val mazeViewModel = remember { MazeViewModel() }

        // Handle navigation from home to mini games
        val onMiniGameClicked = { gameId: String ->
            when (gameId) {
                "counting_objects" -> currentScreen = Screen.COUNTING_OBJECTS
                "drawing_letters" -> currentScreen = Screen.DRAWING_LETTERS
                "maze" -> currentScreen = Screen.MAZE
                else -> homeViewModel.onMiniGameClicked(gameId)
            }
        }

        // Handle navigation back to home
        val onNavigateBack = {
            currentScreen = Screen.HOME
        }

        val modifier = Modifier.padding(WindowInsets.systemBars.asPaddingValues())

        // Display the current screen
        when (currentScreen) {
            Screen.HOME -> {
                HomeComponent(
                    modifier = modifier,
                    viewModel = homeViewModel,
                    onMiniGameClicked = onMiniGameClicked
                )
            }
            Screen.COUNTING_OBJECTS -> {
                CountingObjectsComponent(
                    modifier = modifier,
                    viewModel = countingObjectsViewModel,
                    onNavigateBack = onNavigateBack
                )
            }
            Screen.DRAWING_LETTERS -> {
                DrawingLettersComponent(
                    modifier = modifier,
                    viewModel = drawingLettersViewModel,
                    onNavigateBack = onNavigateBack
                )
            }
            Screen.MAZE -> {
                MazeComponent(
                    modifier = modifier,
                    viewModel = mazeViewModel,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}

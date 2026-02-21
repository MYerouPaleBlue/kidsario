package net.miksoft.kidsario.presentation.wordsGame

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kidsario.composeapp.generated.resources.*
import net.miksoft.kidsario.theme.GameColors
import org.jetbrains.compose.resources.imageResource

@Composable
fun WordsGameComponent(
    modifier: Modifier = Modifier,
    viewModel: WordsGameViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showSettingsDialog) {
        WordsGameSettingsDialog(
            selectedLanguage = uiState.language,
            selectedDuration = uiState.timerDuration,
            selectedWordCount = uiState.optionCount,
            onLanguageSelected = { viewModel.changeLanguage(it) },
            onDurationSelected = { viewModel.changeTimerDuration(it) },
            onWordCountSelected = { viewModel.changeWordCount(it) },
            onDismiss = { viewModel.toggleSettingsDialog() }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                elevation = 8.dp,
                title = {
                    Text(
                        "Words Game",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopGame()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSettingsDialog() }) {
                        Text(
                            "⚙️",
                            fontSize = 24.sp
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isShortHeight = maxHeight < 560.dp
            val scrollState = rememberScrollState()
            val contentModifier = if (isShortHeight) {
                Modifier.verticalScroll(scrollState)
            } else {
                Modifier
            }

            Column(
                modifier = contentModifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(GameColors.GameBackgroundColor),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isShortHeight) 8.dp else 16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = GameColors.CardBackgroundColor,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Pick words that start with the letter",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Card(
                    backgroundColor = GameColors.AlternateCardBackgroundColor,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = uiState.targetLetter.toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                    )
                }

                WordsScoreDisplay(
                    score = uiState.score,
                    highScore = uiState.highScore,
                    remainingTime = uiState.remainingTime,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.isGameOver) {
                    GameOverPanel(
                        score = uiState.score,
                        onPlayAgain = { viewModel.startGame() }
                    )
                } else if (!uiState.isGameActive) {
                    Button(
                        onClick = { viewModel.startGame() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Start",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

                val animalImage = when (uiState.currentAnimal) {
                    WordsGameAnimal.HIPPO -> imageResource(Res.drawable.puzzle_hippo)
                    WordsGameAnimal.WHALE -> imageResource(Res.drawable.puzzle_whale)
                    WordsGameAnimal.SQUIRREL -> imageResource(Res.drawable.puzzle_squirrel)
                    WordsGameAnimal.CRANE -> imageResource(Res.drawable.puzzle_crane)
                }
                val animalSize = if (isShortHeight) 110.dp else 140.dp

                AnimatedVisibility(
                    visible = uiState.isGameActive && !uiState.isRefreshing,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    if (isShortHeight) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = GameColors.CardBackgroundColor,
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        uiState.currentOptions.forEach { option ->
                                            Button(
                                                onClick = { viewModel.selectWord(option) },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface),
                                                shape = RoundedCornerShape(20.dp)
                                            ) {
                                                Text(
                                                    text = option.word,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colors.onSurface,
                                                    modifier = Modifier.padding(6.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Canvas(modifier = Modifier.size(32.dp, 16.dp)) {
                                    val tailPath = Path().apply {
                                        moveTo(size.width / 2f, size.height)
                                        lineTo(0f, 0f)
                                        lineTo(size.width, 0f)
                                        close()
                                    }
                                    drawPath(path = tailPath, color = GameColors.CardBackgroundColor)
                                }
                            }

                            Card(
                                backgroundColor = Color.White,
                                elevation = 6.dp,
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Image(
                                    bitmap = animalImage,
                                    contentDescription = "Game animal",
                                    modifier = Modifier.size(animalSize),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = GameColors.CardBackgroundColor,
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        uiState.currentOptions.forEach { option ->
                                            Button(
                                                onClick = { viewModel.selectWord(option) },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface),
                                                shape = RoundedCornerShape(20.dp)
                                            ) {
                                                Text(
                                                    text = option.word,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colors.onSurface,
                                                    modifier = Modifier.padding(6.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Canvas(modifier = Modifier.size(32.dp, 16.dp)) {
                                    val tailPath = Path().apply {
                                        moveTo(size.width / 2f, size.height)
                                        lineTo(0f, 0f)
                                        lineTo(size.width, 0f)
                                        close()
                                    }
                                    drawPath(path = tailPath, color = GameColors.CardBackgroundColor)
                                }
                            }

                            Card(
                                backgroundColor = Color.White,
                                elevation = 6.dp,
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Image(
                                    bitmap = animalImage,
                                    contentDescription = "Game animal",
                                    modifier = Modifier.size(animalSize),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                if (uiState.lastResultCorrect != null && uiState.isGameActive) {
                    val feedbackColor = if (uiState.lastResultCorrect == true) {
                        GameColors.CorrectColor
                    } else {
                        GameColors.IncorrectColor
                    }

                    Surface(
                        color = feedbackColor,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            text = if (uiState.lastResultCorrect == true) "Great choice!" else "Try another word!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WordsScoreDisplay(
    score: Int,
    highScore: Int,
    remainingTime: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.weight(1f),
            backgroundColor = GameColors.CardBackgroundColor,
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Score",
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "$score",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GameColors.CircleColor
                )
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            backgroundColor = GameColors.AlternateCardBackgroundColor,
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Best",
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "$highScore",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GameColors.HeartColor
                )
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            backgroundColor = MaterialTheme.colors.primaryVariant,
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Time",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = "$remainingTime s",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun GameOverPanel(score: Int, onPlayAgain: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = GameColors.CardBackgroundColor,
        elevation = 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Time's up!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "You found $score words",
                fontSize = 18.sp
            )
            Button(
                onClick = onPlayAgain,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Play Again",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WordsGameSettingsDialog(
    selectedLanguage: WordLanguage,
    selectedDuration: Int,
    selectedWordCount: Int,
    onLanguageSelected: (WordLanguage) -> Unit,
    onDurationSelected: (Int) -> Unit,
    onWordCountSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Game Settings",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Language", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WordLanguage.values().forEach { language ->
                        Button(
                            onClick = { onLanguageSelected(language) },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (language == selectedLanguage) {
                                    MaterialTheme.colors.primary
                                } else {
                                    MaterialTheme.colors.surface
                                }
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = language.displayName,
                                color = if (language == selectedLanguage) {
                                    Color.White
                                } else {
                                    MaterialTheme.colors.onSurface
                                }
                            )
                        }
                    }
                }

                Text("Timer", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60, 90).forEach { duration ->
                        Button(
                            onClick = { onDurationSelected(duration) },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (duration == selectedDuration) {
                                    MaterialTheme.colors.primary
                                } else {
                                    MaterialTheme.colors.surface
                                }
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$duration s",
                                color = if (duration == selectedDuration) {
                                    Color.White
                                } else {
                                    MaterialTheme.colors.onSurface
                                }
                            )
                        }
                    }
                }

                Text("Words", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 4, 5).forEach { count ->
                        Button(
                            onClick = { onWordCountSelected(count) },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (count == selectedWordCount) {
                                    MaterialTheme.colors.primary
                                } else {
                                    MaterialTheme.colors.surface
                                }
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$count",
                                color = if (count == selectedWordCount) {
                                    Color.White
                                } else {
                                    MaterialTheme.colors.onSurface
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
            ) {
                Text("Close")
            }
        }
    )
}
package net.miksoft.kidsario.presentation.mathGame

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kidsario.composeapp.generated.resources.Res
import kidsario.composeapp.generated.resources.puzzle_crane
import kidsario.composeapp.generated.resources.puzzle_hippo
import kidsario.composeapp.generated.resources.puzzle_squirrel
import kidsario.composeapp.generated.resources.puzzle_whale
import net.miksoft.kidsario.theme.GameColors
import org.jetbrains.compose.resources.imageResource

@Composable
fun MathGameComponent(
    modifier: Modifier = Modifier,
    viewModel: MathGameViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showSettingsDialog) {
        MathGameSettingsDialog(
            selectedRange = uiState.rangeOption,
            selectedOptionCount = uiState.optionCount,
            selectedOperations = uiState.allowedOperations,
            onRangeSelected = viewModel::changeRangeOption,
            onOptionCountSelected = viewModel::changeOptionCount,
            onOperationToggle = viewModel::toggleOperation,
            onDismiss = viewModel::toggleSettingsDialog
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
                        text = "Math Game",
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
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GameColors.GameBackgroundColor,
                            GameColors.CardBackgroundColor
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val headerShape = RoundedCornerShape(18.dp)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, MaterialTheme.colors.primary.copy(alpha = 0.2f), headerShape),
                    backgroundColor = GameColors.CardBackgroundColor,
                    elevation = 6.dp,
                    shape = headerShape
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Solve the math problem!",
                            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pick the right answer below",
                            fontSize = 14.sp,
                            color = MaterialTheme.colors.onSurface
                        )
                    }
                }

                MathScoreDisplay(
                    score = uiState.score,
                    highScore = uiState.highScore
                )

                MathAnimalPrompt(
                    animal = uiState.currentAnimal,
                    problemText = uiState.currentProblem.format()
                )

                uiState.lastResultCorrect?.let { isCorrect ->
                    val message = if (isCorrect) "Correct!" else "Try again!"
                    val subtitle = if (isCorrect) "You solved it." else "You can do it."
                    val emoji = if (isCorrect) "🎉" else "😊"
                    val color = if (isCorrect) GameColors.CorrectColor else GameColors.IncorrectColor
                    val shape = RoundedCornerShape(18.dp)
                    Card(
                        backgroundColor = color.copy(alpha = 0.12f),
                        elevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, color.copy(alpha = 0.35f), shape),
                        shape = shape
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(color.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = message,
                                    color = color,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = subtitle,
                                    color = MaterialTheme.colors.onSurface,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                if (uiState.isGameActive) {
                    MathOptionsGrid(
                        options = uiState.options,
                        onOptionSelected = viewModel::selectOption
                    )
                }

                if (!uiState.isGameActive) {
                    val startShape = RoundedCornerShape(20.dp)
                    Button(
                        onClick = viewModel::startGame,
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                        elevation = ButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
                        shape = startShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .border(2.dp, Color.White.copy(alpha = 0.4f), startShape)
                    ) {
                        Text(
                            text = "Start",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MathScoreDisplay(score: Int, highScore: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ScoreCard(
            title = "Score",
            value = score.toString(),
            modifier = Modifier.weight(1f)
        )
        ScoreCard(
            title = "Best",
            value = highScore.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScoreCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        backgroundColor = GameColors.AlternateCardBackgroundColor,
        elevation = 5.dp,
        shape = shape,
        modifier = modifier
            .padding(horizontal = 6.dp)
            .border(1.5.dp, MaterialTheme.colors.secondary.copy(alpha = 0.2f), shape)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colors.onSurface)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
private fun MathAnimalPrompt(
    animal: MathGameAnimal,
    problemText: String
) {
    val animalRes = when (animal) {
        MathGameAnimal.HIPPO -> Res.drawable.puzzle_hippo
        MathGameAnimal.WHALE -> Res.drawable.puzzle_whale
        MathGameAnimal.SQUIRREL -> Res.drawable.puzzle_squirrel
        MathGameAnimal.CRANE -> Res.drawable.puzzle_crane
    }

    val promptShape = RoundedCornerShape(20.dp)
    val imageShape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            backgroundColor = GameColors.CardBackgroundColor,
            elevation = 6.dp,
            shape = promptShape,
            modifier = Modifier
                .weight(1f)
                .border(2.dp, MaterialTheme.colors.primary.copy(alpha = 0.2f), promptShape)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${animal.displayName} says:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = problemText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = MaterialTheme.colors.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(110.dp)
                .shadow(6.dp, imageShape)
                .background(Color.White, imageShape)
                .border(2.dp, MaterialTheme.colors.primary.copy(alpha = 0.25f), imageShape)
        ) {
            Image(
                bitmap = imageResource(animalRes),
                contentDescription = animal.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, imageShape)
            )
        }
    }
}

@Composable
private fun MathOptionsGrid(
    options: List<MathOption>,
    onOptionSelected: (MathOption) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val optionShape = RoundedCornerShape(18.dp)
        options.forEach { option ->
            Button(
                onClick = { onOptionSelected(option) },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                elevation = ButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
                shape = optionShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.35f), optionShape)
            ) {
                Text(
                    text = option.value.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
private fun MathGameSettingsDialog(
    selectedRange: MathRangeOption,
    selectedOptionCount: Int,
    selectedOperations: Set<MathOperation>,
    onRangeSelected: (MathRangeOption) -> Unit,
    onOptionCountSelected: (Int) -> Unit,
    onOperationToggle: (MathOperation) -> Unit,
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
                Text("Number Range", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MathRangeOption.values().forEach { range ->
                        SettingsButton(
                            text = range.displayName,
                            isSelected = range == selectedRange,
                            onClick = { onRangeSelected(range) }
                        )
                    }
                }

                Text("Operations", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MathOperation.values().forEach { operation ->
                        SettingsButton(
                            text = operation.symbol,
                            isSelected = selectedOperations.contains(operation),
                            onClick = { onOperationToggle(operation) }
                        )
                    }
                }

                Text("Options", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 4, 5).forEach { count ->
                        SettingsButton(
                            text = count.toString(),
                            isSelected = count == selectedOptionCount,
                            onClick = { onOptionCountSelected(count) }
                        )
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

@Composable
private fun SettingsButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isSelected) {
                MaterialTheme.colors.primary
            } else {
                MaterialTheme.colors.surface
            }
        ),
        modifier = Modifier.height(36.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) {
                Color.White
            } else {
                MaterialTheme.colors.onSurface
            }
        )
    }
}
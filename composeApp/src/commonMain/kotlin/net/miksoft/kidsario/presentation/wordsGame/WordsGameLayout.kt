package net.miksoft.kidsario.presentation.wordsGame

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class WordsGameLayout(
    val isShortHeight: Boolean,
    val animalSize: Dp,
    val wordFontSize: TextUnit,
    val wordPadding: Dp,
    val wordSpacing: Dp,
    val optionsMaxHeight: Dp
)

fun calculateWordsGameLayout(maxHeight: Dp): WordsGameLayout {
    val isShortHeight = maxHeight < 560.dp
    return WordsGameLayout(
        isShortHeight = isShortHeight,
        animalSize = if (isShortHeight) 110.dp else 140.dp,
        wordFontSize = if (isShortHeight) 18.sp else 20.sp,
        wordPadding = if (isShortHeight) 4.dp else 6.dp,
        wordSpacing = if (isShortHeight) 8.dp else 12.dp,
        optionsMaxHeight = if (isShortHeight) maxHeight * 0.45f else maxHeight
    )
}
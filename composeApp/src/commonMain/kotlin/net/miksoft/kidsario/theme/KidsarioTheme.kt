package net.miksoft.kidsario.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Kid-friendly vibrant colors with more playful tones
val Purple = Color(0xFF9B7AFF) // Lighter, more playful purple
val Pink = Color(0xFFFF8DC1) // Softer, more playful pink
val Yellow = Color(0xFFFFE066) // Softer yellow
val Green = Color(0xFF7AE582) // Brighter, more playful green
val Blue = Color(0xFF73D0FF) // Brighter, more playful blue
val Orange = Color(0xFFFFB347) // Softer orange
val Red = Color(0xFFFF6B6B) // Softer red
val Teal = Color(0xFF5FE3E0) // Brighter teal
val LightPurple = Color(0xFFD4C1FF) // Very light purple for backgrounds
val LightPink = Color(0xFFFFD6E5) // Very light pink for backgrounds
val LightYellow = Color(0xFFFFF5C0) // Very light yellow for backgrounds
val LightGreen = Color(0xFFD4F5D4) // Very light green for backgrounds
val LightBlue = Color(0xFFD6F3FF) // Very light blue for backgrounds

// Light theme colors - more playful and kid-friendly
private val LightColorPalette = lightColors(
    primary = Purple,
    primaryVariant = LightPurple,
    secondary = Pink,
    background = LightBlue, // Light blue background for a fresh, playful look
    surface = LightYellow,  // Light yellow surface for warmth and playfulness
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF5C5C5C), // Slightly lighter text for better contrast on light backgrounds
    onSurface = Color(0xFF5C5C5C)     // Slightly lighter text for better contrast on light backgrounds
)

// Dark theme colors (optional, can be used for future dark mode support)
private val DarkColorPalette = darkColors(
    primary = Purple,
    primaryVariant = Purple,
    secondary = Pink,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

// Shape definitions with rounded corners for a friendly look
val KidsarioShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

// Typography with friendly, readable fonts
val KidsarioTypography = Typography(
    h1 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        letterSpacing = 0.sp
    ),
    h2 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        letterSpacing = 0.sp
    ),
    h3 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 0.sp
    ),
    h4 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.sp
    ),
    h5 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    ),
    h6 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 0.sp
    ),
    subtitle1 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = 0.15.sp
    ),
    subtitle2 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.1.sp
    ),
    body1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    ),
    body2 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp
    ),
    button = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 1.25.sp
    ),
    caption = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp
    ),
    overline = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp
    )
)

// Additional colors for game elements - more playful and kid-friendly
object GameColors {
    // Colors for counting objects game shapes - brighter, more playful colors
    val CircleColor = Blue
    val RectangleColor = Pink
    val SquareColor = Green
    val TriangleColor = Yellow
    val StarColor = Orange // Additional shape color
    val HeartColor = Red   // Additional shape color

    // Colors for feedback - softer, more kid-friendly
    val CorrectColor = Color(122, 229, 130, 230) // Softer green with transparency
    val IncorrectColor = Color(255, 107, 107, 230) // Softer red with transparency

    // Colors for drawing - brighter, more playful
    val DrawingColor = Purple
    val DottedLineColor = Color(155, 155, 155, 180) // Softer gray with transparency

    // Background colors for different game elements
    val GameBackgroundColor = LightBlue
    val CardBackgroundColor = LightYellow
    val AlternateCardBackgroundColor = LightPink
}

@Composable
fun KidsarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = KidsarioTypography,
        shapes = KidsarioShapes,
        content = content
    )
}

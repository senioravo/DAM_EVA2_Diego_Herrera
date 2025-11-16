//Paleta 1 Plant Buddy

package cl.duoc.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores personalizados para Plant Buddy

//Variantes DeepSea
private val DeepSea = Color(0xff032936)
private val DeepSeaLight = Color(0xff052C3B)
private val Kelp = Color(0xff115655)
private val Lagoon = Color(0xff197D6D)
private val Ocean = Color(0xff09496A)
private val Plankton = Color(0xff5BD7A3)

//Variantes de Plant
private val PlantEmphasis = Color(0xffaedc62)
private val PlantLighter = Color(0xffdcf1bc)
private val PlantLight = Color(0xffbfdf87)
private val Plant = Color(0xffa1bd68)
private val PlantDark = Color(0xff71854e)
private val PlantDarker = Color(0xff566a33)

//Variantes de Bone
private val BoneEmphasis = Color(0xffffffff)
private val Bone = Color(0xffe4e4e4)
private val BoneLight = Color(0xffededed)
private val BoneLighter = Color(0xffffffff)
private val BoneDark = Color(0xffcdcdcd)
private val BoneDarker = Color(0xffa3a3a3)

//Variantes de Gray
private val GrayEmphasis = Color(0xfff1f1f4)
private val Gray = Color(0xFFEDECF2)
private val GrayLight = Color(0xfff1f1f4)
private val GrayLighter = Color(0xffffffff)
private val GrayDark = Color(0xffdcdbe4)
private val GrayDarker = Color(0xffc5c4cc)

//Variantes de Coal
private val CoalEmphasis = Color(0xff111013)
private val Coal = Color(0xFF151419)
private val CoalLight = Color(0xff1d1c22)
private val CoalLighter = Color(0xff2c2b32)
private val CoalDark = Color(0xff111115)
private val CoalDarker = Color(0xff0c0c12)




private val Asfalt = Color(0xff333333)

private val LightColorScheme = lightColorScheme(
    primary = Plant,
    onPrimary = Color.White,
    primaryContainer = PlantLighter,
    onPrimaryContainer = PlantLight,
    secondary = PlantLighter,
    onSecondary = Color.White,
    tertiary = PlantEmphasis,
    onTertiary = Color.Black,
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xffdfe6df),
    onSurfaceVariant = Color(0xFF424242)
)

private val DarkColorScheme = darkColorScheme(
    primary = PlantDark,
    onPrimary = Color.Black,
    primaryContainer = PlantDarker,
    onPrimaryContainer = PlantLight,
    secondary = PlantLighter,
    onSecondary = BoneDark,
    tertiary = PlantEmphasis,
    onTertiary = Color.Black,
    background = BoneDark,
    onBackground = BoneLight,
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2D4A2D),
    onSurfaceVariant = Color(0xFFB8B8B8)
)

@Composable
fun PlantBuddyTheme(
    darkTheme: Boolean = false, // Puedes cambiar esto por isSystemInDarkTheme()
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
package cl.duoc.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.app.ui.theme.PlantBuddyTheme
import androidx.compose.ui.graphics.Color

@Composable
fun BienvenidaScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Bienvenido a Plant Buddy",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tu compañero ideal para el cuidado de tus plantas.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 60.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BienvenidaScreenPreview() {
    MaterialTheme {
        BienvenidaScreen()
    }
}
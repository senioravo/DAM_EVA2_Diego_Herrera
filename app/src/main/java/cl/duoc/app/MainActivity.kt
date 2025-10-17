package cl.duoc.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.app.ui.HomeScreen

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm = viewModel<cl.duoc.app.ui.HomeViewModel>()

            HomeScreen(
                onPrimaryAction = { vm.inc() },
                onSecondaryAction = { /* TODO: navegar o mostrar snackbar */ }
            )
        }
    }
}
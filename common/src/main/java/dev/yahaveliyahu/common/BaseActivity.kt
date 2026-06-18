package dev.yahaveliyahu.common

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import dev.yahaveliyahu.common.engine.AppConfig
import dev.yahaveliyahu.common.engine.FitnessViewModel
import dev.yahaveliyahu.common.ui.screens.MainScaffold
import dev.yahaveliyahu.common.ui.theme.FitnessTheme

abstract class BaseActivity : ComponentActivity() {

    abstract fun buildConfig(): AppConfig

    private lateinit var vm: FitnessViewModel

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        val config = buildConfig()

        // Convert Compose Color to android.graphics.Color int for SystemBarStyle
        val bgColorInt = android.graphics.Color.argb(
            (config.screenBackground.alpha * 255).toInt(),
            (config.screenBackground.red * 255).toInt(),
            (config.screenBackground.green * 255).toInt(),
            (config.screenBackground.blue * 255).toInt()
        )

        // Enable edge-to-edge with matching status bar and nav bar colors
        // so bars are transparent but show the app background behind them
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(bgColorInt),
            navigationBarStyle = SystemBarStyle.dark(bgColorInt)
        )

        super.onCreate(savedInstanceState)

        vm = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return FitnessViewModel(application, config) as T
                }
            }
        )[FitnessViewModel::class.java]

        requestPermissionsIfNeeded()

        setContent {
            FitnessTheme(config = config) {
                MainScaffold(vm = vm)
            }
        }
    }

    private fun requestPermissionsIfNeeded() {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val missing = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }
}

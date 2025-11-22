package com.example.flam.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.flam.ui.theme.FlamTheme
import com.example.flam.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var permissionGranted by mutableStateOf(false)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            permissionGranted = granted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        permissionGranted = PermissionUtils.hasCameraPermission(this)
        if (!permissionGranted) {
            requestPermissionLauncher.launch(PermissionUtils.CAMERA_PERMISSION)
        }

        setContent {
            FlamTheme {
                FlamApp(permissionGranted)
            }
        }
    }
}
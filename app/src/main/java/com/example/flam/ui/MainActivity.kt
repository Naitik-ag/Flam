package com.example.flam.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.flam.ui.theme.FLAMTheme
import com.example.flam.ui.viewmodel.MainViewModel
import com.example.flam.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
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
            FLAMTheme {
                FlamApp(permissionGranted)
            }
        }
    }
    override fun onPause() {
        super.onPause()
        viewModel.stopCamera()
    }

    override fun onResume() {
        super.onResume()
        viewModel.startCamera()
    }
}
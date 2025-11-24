package com.example.flam.ui

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.flam.ui.theme.FLAMTheme
import com.example.flam.ui.viewmodel.MainViewModel

@Composable
fun FlamApp(
    permissionGranted: Boolean,
    viewModel: MainViewModel = hiltViewModel()
) {
    FLAMTheme{
        if (permissionGranted) {
            MainScreen(viewModel)
        } else {
            Text("Camera permission not granted")
        }
    }
}
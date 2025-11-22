package com.example.flam.ui

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.flam.ui.viewmodel.MainViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlamApp(
    permissionGranted: Boolean,
    viewModel: MainViewModel = hiltViewModel()
) {
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Flam",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
    ) {contentPadding ->
        val message = viewModel.testJNI()
        Log.d("JNI_TEST_UI", message.toString())
        if (permissionGranted) {
            LaunchedEffect(permissionGranted) {
                viewModel.startCamera { frameBytes ->
                    val processed = viewModel.processFrameInNative(frameBytes)
                    Log.d("NativeFrame", "Processed frame = ${processed.size} bytes")
                }
            }
        }else{
            Text(
                text = "Camera permission not granted",
                modifier = Modifier.padding(contentPadding)
            )
        }


        DisposableEffect(Unit) {
            onDispose {
                viewModel.stopCamera()
            }
        }
    }
}

package com.example.flam.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.flam.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {

    LaunchedEffect(Unit) {
        viewModel.startCamera()
    }

    // ViewModel state
    val bitmap by viewModel.bitmap.collectAsState()
    val fps by viewModel.fps.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val t1 by viewModel.t1.collectAsState()
    val t2 by viewModel.t2.collectAsState()

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(12.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Processed Frame",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = 90f
                        }
                )
            } else {
                Text("Waiting for camera…", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("FPS: ${"%.1f".format(fps)}", style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(16.dp))

        Text("Mode", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        val modes = listOf("RAW", "GRAY", "CANNY", "THRESH")

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            modes.forEachIndexed { index, label ->
                Button(
                    onClick = { viewModel.mode.value = index },
                    colors = if (mode == index)
                        ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                    else
                        ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(label)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        when (mode) {

            // CANNY mode → both sliders
            2 -> {
                Text("Lower Threshold (T1): $t1")
                Slider(
                    value = t1.toFloat(),
                    onValueChange = { viewModel.t1.value = it.toInt() },
                    valueRange = 0f..255f
                )

                Spacer(Modifier.height(8.dp))

                Text("Upper Threshold (T2): $t2")
                Slider(
                    value = t2.toFloat(),
                    onValueChange = { viewModel.t2.value = it.toInt() },
                    valueRange = 0f..255f
                )
            }

            // THRESH mode → only T1
            3 -> {
                Text("Threshold (T1): $t1")
                Slider(
                    value = t1.toFloat(),
                    onValueChange = { viewModel.t1.value = it.toInt() },
                    valueRange = 0f..255f
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val base64 = viewModel.captureSnapshot()
                println("Snapshot Base64 length = ${base64?.length}")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Capture Snapshot")
        }
    }
}

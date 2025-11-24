package com.example.flam.ui

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.flam.data.camera.GLTextureRenderer
import com.example.flam.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {

    LaunchedEffect(Unit) {
        viewModel.connectWeb()
    }

    // ViewModel State
    val fps by viewModel.fps.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val t1 by viewModel.t1.collectAsState()
    val t2 by viewModel.t2.collectAsState()
    val processingMs by viewModel.processingMs.collectAsState()   // ADD IN VM
    val frameWidth by viewModel.frameWidthFlow.collectAsState()   // ADD IN VM
    val frameHeight by viewModel.frameHeightFlow.collectAsState() // ADD IN VM

    val scroll = rememberScrollState()

    val surfaceRef = remember { mutableStateOf<GLSurfaceView?>(null) }

    // RequestRender Collector
    LaunchedEffect(Unit) {
        viewModel.glRequest.collect {
            surfaceRef.value?.requestRender()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(12.dp)
    ) {

        // -----------------------------------------------------
        //   VIDEO PREVIEW (GLSurfaceView)
        // -----------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            AndroidView(
                factory = { ctx ->
                    GLSurfaceView(ctx).apply {
                        setEGLContextClientVersion(2)
                        val renderer = GLTextureRenderer(
                            viewModel.glFrame,
                            { viewModel.frameWidth },
                            { viewModel.frameHeight }
                        )
                        setRenderer(renderer)
                        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                        surfaceRef.value = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(16.dp))

        // -----------------------------------------------------
        //   STATS PANEL
        // -----------------------------------------------------
        StatsPanel(
            fps = fps,
            processingTimeMs = processingMs,
            resolution = "${frameWidth}x$frameHeight"
        )

        Spacer(Modifier.height(20.dp))


        // -----------------------------------------------------
        //   MODE SELECTOR
        // -----------------------------------------------------
        Text("Processing Mode", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        ModeSelector(
            current = mode,
            onSelect = { viewModel.mode.value = it }
        )

        Spacer(Modifier.height(20.dp))

        // -----------------------------------------------------
        //   SLIDERS
        // -----------------------------------------------------
        when (mode) {
            2 -> {
                SliderCard(
                    label = "Lower Threshold (T1)",
                    value = t1,
                    onChange = { viewModel.t1.value = it },
                    range = 0..255
                )
                Spacer(Modifier.height(12.dp))

                SliderCard(
                    label = "Upper Threshold (T2)",
                    value = t2,
                    onChange = { viewModel.t2.value = it },
                    range = 0..255
                )
            }

            3 -> {
                SliderCard(
                    label = "Threshold (T1)",
                    value = t1,
                    onChange = { viewModel.t1.value = it },
                    range = 0..255
                )
            }
        }

        Spacer(Modifier.height(30.dp))

        // -----------------------------------------------------
        //   ACTION BAR
        // -----------------------------------------------------
        ActionBar(
            onCapture = {
                viewModel.captureSnapshot()
                viewModel.sendSnapshotToWeb()
            }
        )
    }
}
@Composable
fun StatsPanel(fps: Float, processingTimeMs: Long, resolution: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Statistics", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("FPS", "%.1f".format(fps))
                StatItem("Process (ms)", processingTimeMs.toString())
                StatItem("Res", resolution)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}


@Composable
fun ModeSelector(current: Int, onSelect: (Int) -> Unit) {

    val modes = listOf("RAW", "GRAY", "CANNY", "THRESH")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        modes.forEachIndexed { index, text ->
            FilterChip(
                selected = current == index,
                onClick = { onSelect(index) },
                label = { Text(text) }
            )
        }
    }
}
@Composable
fun SliderCard(label: String, value: Int, onChange: (Int) -> Unit, range: IntRange) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$label: $value")
            Slider(
                value = value.toFloat(),
                onValueChange = { onChange(it.toInt()) },
                valueRange = range.first.toFloat()..range.last.toFloat()
            )
        }
    }
}
@Composable
fun ActionBar(onCapture: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onCapture,
            modifier = Modifier.weight(1f)
        ) { Text("Capture") }

    }
}

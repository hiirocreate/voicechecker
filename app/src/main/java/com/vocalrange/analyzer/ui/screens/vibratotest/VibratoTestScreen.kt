package com.vocalrange.analyzer.ui.screens.vibratotest

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocalrange.analyzer.data.VoiceRepository
import com.vocalrange.analyzer.ui.GenericViewModelFactory
import com.vocalrange.analyzer.ui.components.MicrophonePermissionGate
import com.vocalrange.analyzer.ui.components.VolumeMeter

@SuppressLint("MissingPermission") // MicrophonePermissionGate配下でのみ録音開始ボタンが押せるため安全
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibratoTestScreen(
    repository: VoiceRepository,
    onBack: () -> Unit,
    onFinished: (Long) -> Unit
) {
    val viewModel: VibratoTestViewModel = viewModel(
        factory = GenericViewModelFactory { VibratoTestViewModel(repository) }
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedSessionId) {
        uiState.savedSessionId?.let { onFinished(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ビブラート・安定性測定") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.cancelRecording()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { padding ->
        MicrophonePermissionGate(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "無理のない高さの声を選び、「あー」と一定の高さのまま\n${VibratoTestUiState.TARGET_DURATION_MS / 1000}秒ほど伸ばしてください。",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer24()

                Text(uiState.currentNoteLabel ?: "---", style = MaterialTheme.typography.headlineMedium)

                Spacer16()
                VolumeMeter(volumeDb = uiState.currentVolumeDb, modifier = Modifier.fillMaxWidth())

                if (uiState.isRecording) {
                    Spacer16()
                    val fraction = (uiState.elapsedMs.toFloat() / uiState.targetDurationMs.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                }

                Spacer40()

                if (!uiState.isRecording) {
                    Button(onClick = { viewModel.startRecording() }) {
                        Text(if (uiState.isSaving) "保存中..." else "測定を開始する")
                    }
                } else {
                    OutlinedButton(onClick = { viewModel.stopEarly() }) {
                        Text("ここで終了する")
                    }
                }

                uiState.errorMessage?.let { message ->
                    Spacer16()
                    Snackbar(
                        action = {
                            TextButton(onClick = { viewModel.consumeError() }) {
                                Text("閉じる")
                            }
                        }
                    ) {
                        Text(message)
                    }
                }
            }
        }
    }
}

@Composable
private fun Spacer16() = androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

@Composable
private fun Spacer24() = androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))

@Composable
private fun Spacer40() = androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(40.dp))

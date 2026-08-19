package com.vocalrange.analyzer.ui.screens.rangetest

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
fun RangeTestScreen(
    repository: VoiceRepository,
    onBack: () -> Unit,
    onFinished: (Long) -> Unit
) {
    val viewModel: RangeTestViewModel = viewModel(
        factory = GenericViewModelFactory { RangeTestViewModel(repository) }
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedSessionId) {
        uiState.savedSessionId?.let { onFinished(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("声域測定") },
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "無理のない範囲で、低い声から高い声まで「あー」と\n途切れずにゆっくりスライドさせて発声してください。",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = uiState.currentNoteLabel ?: "---",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = if (uiState.isRecording) {
                        if (uiState.isVoiced) "声を検出中" else "声が検出されていません"
                    } else {
                        "測定開始を押してください"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))
                VolumeMeter(volumeDb = uiState.currentVolumeDb, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RangeStat(label = "最低音", value = uiState.lowestLabel ?: "-")
                    RangeStat(label = "最高音", value = uiState.highestLabel ?: "-")
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (!uiState.isRecording) {
                    Button(onClick = { viewModel.startRecording() }) {
                        Text("測定を開始する")
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = { viewModel.stopRecordingAndSave() },
                            enabled = uiState.canFinish
                        ) {
                            Text(if (uiState.isSaving) "保存中..." else "測定を終了して結果を見る")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { viewModel.cancelRecording() }) {
                            Text("やり直す")
                        }
                        if (!uiState.canFinish) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "もう少し声の高さの幅を広げて発声してください",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                uiState.errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(16.dp))
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
private fun RangeStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(text = value, style = MaterialTheme.typography.titleLarge)
    }
}

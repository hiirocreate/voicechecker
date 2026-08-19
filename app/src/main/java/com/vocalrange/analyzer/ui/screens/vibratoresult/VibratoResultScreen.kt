package com.vocalrange.analyzer.ui.screens.vibratoresult

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocalrange.analyzer.core.VibratoSessionSummary
import com.vocalrange.analyzer.core.VoiceStability
import com.vocalrange.analyzer.data.VoiceRepository
import com.vocalrange.analyzer.ui.GenericViewModelFactory
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibratoResultScreen(
    repository: VoiceRepository,
    sessionId: Long,
    onBack: () -> Unit
) {
    val viewModel: VibratoResultViewModel = viewModel(
        factory = GenericViewModelFactory { VibratoResultViewModel(repository, sessionId) }
    )
    val summary by viewModel.summary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ビブラート測定結果") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) { CircularProgressIndicator() }
                }
                summary == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) { Text("結果が見つかりませんでした") }
                }
                else -> VibratoResultContent(summary!!)
            }
        }
    }
}

@Composable
private fun VibratoResultContent(summary: VibratoSessionSummary) {
    val result = summary.result

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(text = "測定した音: ${summary.targetNoteLabel}", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("音程の安定性", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(stabilityLabel(result.stability), style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "ジッター(揺れの目安): ${"%.1f".format(result.jitterCents)} セント",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "値が小さいほど、声がまっすぐ安定していることを示します",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ビブラート", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                if (result.vibratoDetected && result.rateHz != null && result.extentCentsPeakToPeak != null) {
                    Text("検出: あり", style = MaterialTheme.typography.titleLarge)
                    Text("速さ: ${"%.1f".format(result.rateHz)} 回/秒")
                    Text("深さ: ${result.extentCentsPeakToPeak.roundToInt()} セント(音程の揺れ幅)")
                } else {
                    Text("検出: なし、または判定に十分なデータなし", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "意図的にビブラートをかけていない場合は自然な結果です",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun stabilityLabel(stability: VoiceStability): String = when (stability) {
    VoiceStability.STABLE -> "安定"
    VoiceStability.SLIGHTLY_UNSTABLE -> "やや不安定"
    VoiceStability.UNSTABLE -> "不安定"
}

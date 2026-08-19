package com.vocalrange.analyzer.ui.screens.rangeresult

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.vocalrange.analyzer.core.NoteUtils
import com.vocalrange.analyzer.core.RangeSessionSummary
import com.vocalrange.analyzer.data.VoiceRepository
import com.vocalrange.analyzer.ui.GenericViewModelFactory
import com.vocalrange.analyzer.ui.components.VolumeProfileChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeResultScreen(
    repository: VoiceRepository,
    sessionId: Long,
    onBack: () -> Unit
) {
    val viewModel: RangeResultViewModel = viewModel(
        factory = GenericViewModelFactory { RangeResultViewModel(repository, sessionId) }
    )
    val summary by viewModel.summary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("測定結果") },
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
                    ) {
                        CircularProgressIndicator()
                    }
                }
                summary == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("結果が見つかりませんでした")
                    }
                }
                else -> RangeResultContent(summary!!)
            }
        }
    }
}

@Composable
private fun RangeResultContent(summary: RangeSessionSummary) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "${summary.lowestLabel} 〜 ${summary.highestLabel}",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "音域の広さ: ${formatSpan(summary.semitoneSpan)}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))
        SectionCard(title = "声種の目安") {
            Text(
                text = summary.voiceTypeName,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "音域の重なり度: ${(summary.voiceTypeOverlapRatio * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "※ 声質(声の質感)は含まない、音域のみからの簡易的な目安です",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        SectionCard(title = "平均的な音域との比較") {
            Text("平均的な男性の歌唱音域と比べて", style = MaterialTheme.typography.titleMedium)
            Text("・最低音: ${describeLowDelta(summary.maleComparison.deltaLowSemitones)}")
            Text("・最高音: ${describeHighDelta(summary.maleComparison.deltaHighSemitones)}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("平均的な女性の歌唱音域と比べて", style = MaterialTheme.typography.titleMedium)
            Text("・最低音: ${describeLowDelta(summary.femaleComparison.deltaLowSemitones)}")
            Text("・最高音: ${describeHighDelta(summary.femaleComparison.deltaHighSemitones)}")
        }

        Spacer(modifier = Modifier.height(16.dp))
        SectionCard(title = "音域ごとの声量プロファイル") {
            VolumeProfileChart(
                profile = summary.volumeProfile,
                lowestMidi = summary.lowestMidi,
                highestMidi = summary.highestMidi,
                strongZone = summary.strongZoneMidiRange,
                weakZone = summary.weakZoneMidiRange,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(color = androidx.compose.ui.graphics.Color(0xFF2ECC71), label = "得意な音域")
                LegendDot(color = androidx.compose.ui.graphics.Color(0xFFE05353), label = "苦手な音域")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        SectionCard(title = "おすすめ音域") {
            Text(
                text = "歌うとよい音域: " + (summary.recommendedSingMidiRange?.let { rangeLabel(it) } ?: "算出できませんでした"),
                style = MaterialTheme.typography.titleMedium
            )
            Text("この音域を中心にした曲は歌いやすい可能性が高いです", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "練習に良い音域: " + (summary.recommendedPracticeMidiRange?.let { rangeLabel(it) } ?: "算出できませんでした"),
                style = MaterialTheme.typography.titleMedium
            )
            Text("この音域を意識して発声練習すると、声量や安定感の底上げにつながりやすいです", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionCard(title: String, content: ColumnScopeContent) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

private typealias ColumnScopeContent = @Composable () -> Unit

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .height(10.dp)
                .width(10.dp)
                .padding(end = 4.dp)
        ) {
            drawCircle(color = color, radius = size.minDimension / 2)
        }
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun formatSpan(semitoneSpan: Int): String {
    val octaves = semitoneSpan / 12
    val remainder = semitoneSpan % 12
    return when {
        octaves == 0 -> "${remainder}半音"
        remainder == 0 -> "${octaves}オクターブ"
        else -> "${octaves}オクターブ${remainder}半音"
    }
}

private fun rangeLabel(range: IntRange): String {
    return "${NoteUtils.noteLabel(range.first)} 〜 ${NoteUtils.noteLabel(range.last)}"
}

private fun describeLowDelta(delta: Int): String = when {
    delta < 0 -> "平均より${-delta}半音低い(低音側に余裕があります)"
    delta > 0 -> "平均より${delta}半音高い"
    else -> "平均とほぼ同じ"
}

private fun describeHighDelta(delta: Int): String = when {
    delta > 0 -> "平均より${delta}半音高い(高音側に余裕があります)"
    delta < 0 -> "平均より${-delta}半音低い"
    else -> "平均とほぼ同じ"
}

package com.vocalrange.analyzer.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
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
import com.vocalrange.analyzer.core.RangeSessionSummary
import com.vocalrange.analyzer.core.VibratoSessionSummary
import com.vocalrange.analyzer.data.VoiceRepository
import com.vocalrange.analyzer.ui.GenericViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    repository: VoiceRepository,
    onBack: () -> Unit,
    onOpenRangeSession: (Long) -> Unit,
    onOpenVibratoSession: (Long) -> Unit
) {
    val viewModel: HistoryViewModel = viewModel(
        factory = GenericViewModelFactory { HistoryViewModel(repository) }
    )
    val rangeSessions by viewModel.rangeSessions.collectAsState()
    val vibratoSessions by viewModel.vibratoSessions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("測定履歴") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { padding ->
        if (rangeSessions.isEmpty() && vibratoSessions.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("まだ測定履歴がありません")
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (rangeSessions.isNotEmpty()) {
                item {
                    Text(
                        text = "声域測定",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
                items(rangeSessions) { session ->
                    RangeHistoryRow(session, onClick = { onOpenRangeSession(session.id) })
                }
            }

            if (vibratoSessions.isNotEmpty()) {
                item {
                    Text(
                        text = "ビブラート・安定性測定",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
                items(vibratoSessions) { session ->
                    VibratoHistoryRow(session, onClick = { onOpenVibratoSession(session.id) })
                }
            }
        }
    }
}

private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)

@Composable
private fun RangeHistoryRow(session: RangeSessionSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${session.lowestLabel} 〜 ${session.highestLabel}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${dateFormat.format(Date(session.timestamp))} ・ ${session.voiceTypeName}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun VibratoHistoryRow(session: VibratoSessionSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "測定音: ${session.targetNoteLabel}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = dateFormat.format(Date(session.timestamp)),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

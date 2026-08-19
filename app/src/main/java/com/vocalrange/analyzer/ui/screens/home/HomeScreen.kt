package com.vocalrange.analyzer.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateRangeTest: () -> Unit,
    onNavigateVibratoTest: () -> Unit,
    onNavigateHistory: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("声域アナライザー") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "あなたの声を測定しましょう",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "静かな場所で、マイクに向かって発声してください",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(onClick = onNavigateRangeTest, modifier = Modifier.fillMaxWidth()) {
                Text("声域を測定する")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = onNavigateVibratoTest, modifier = Modifier.fillMaxWidth()) {
                Text("ビブラート・安定性を測定する")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(onClick = onNavigateHistory, modifier = Modifier.fillMaxWidth()) {
                Text("測定履歴を見る")
            }
        }
    }
}

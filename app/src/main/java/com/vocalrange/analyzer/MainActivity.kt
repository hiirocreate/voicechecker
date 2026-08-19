package com.vocalrange.analyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vocalrange.analyzer.ui.navigation.VoiceRangeNavGraph
import com.vocalrange.analyzer.ui.theme.VoiceRangeAnalyzerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as VoiceRangeApplication).repository

        setContent {
            VoiceRangeAnalyzerTheme {
                VoiceRangeNavGraph(repository = repository)
            }
        }
    }
}

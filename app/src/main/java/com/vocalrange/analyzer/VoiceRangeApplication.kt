package com.vocalrange.analyzer

import android.app.Application
import com.vocalrange.analyzer.data.AppDatabase
import com.vocalrange.analyzer.data.VoiceRepository

class VoiceRangeApplication : Application() {

    lateinit var repository: VoiceRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = VoiceRepository(AppDatabase.getInstance(this))
    }
}

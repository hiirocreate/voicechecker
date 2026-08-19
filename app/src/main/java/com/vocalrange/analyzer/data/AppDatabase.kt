package com.vocalrange.analyzer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RangeSessionEntity::class, VibratoSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rangeSessionDao(): RangeSessionDao
    abstract fun vibratoSessionDao(): VibratoSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voice_range_analyzer.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

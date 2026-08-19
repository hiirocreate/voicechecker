package com.vocalrange.analyzer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VibratoSessionDao {
    @Insert
    suspend fun insert(session: VibratoSessionEntity): Long

    @Query("SELECT * FROM vibrato_sessions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<VibratoSessionEntity>>

    @Query("SELECT * FROM vibrato_sessions WHERE id = :id")
    suspend fun getById(id: Long): VibratoSessionEntity?

    @Delete
    suspend fun delete(session: VibratoSessionEntity)
}

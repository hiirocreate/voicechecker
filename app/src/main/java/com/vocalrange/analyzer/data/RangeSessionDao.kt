package com.vocalrange.analyzer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RangeSessionDao {
    @Insert
    suspend fun insert(session: RangeSessionEntity): Long

    @Query("SELECT * FROM range_sessions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<RangeSessionEntity>>

    @Query("SELECT * FROM range_sessions WHERE id = :id")
    suspend fun getById(id: Long): RangeSessionEntity?

    @Delete
    suspend fun delete(session: RangeSessionEntity)
}

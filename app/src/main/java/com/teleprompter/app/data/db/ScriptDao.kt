package com.teleprompter.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptDao {

    @Query("SELECT * FROM scripts ORDER BY updated_at DESC")
    fun getAllScripts(): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE is_favorite = 1 ORDER BY updated_at DESC")
    fun getFavoriteScripts(): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE id = :id")
    suspend fun getScriptById(id: Long): ScriptEntity?

    @Query("SELECT * FROM scripts WHERE id = :id")
    fun getScriptByIdFlow(id: Long): Flow<ScriptEntity?>

    @Query("SELECT * FROM scripts WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updated_at DESC")
    fun searchScripts(query: String): Flow<List<ScriptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: ScriptEntity): Long

    @Update
    suspend fun updateScript(script: ScriptEntity)

    @Delete
    suspend fun deleteScript(script: ScriptEntity)

    @Query("DELETE FROM scripts WHERE id = :id")
    suspend fun deleteScriptById(id: Long)

    @Query("UPDATE scripts SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE scripts SET last_scroll_position = :position WHERE id = :id")
    suspend fun updateScrollPosition(id: Long, position: Float)

    @Query("UPDATE scripts SET last_speed = :speed WHERE id = :id")
    suspend fun updateSpeed(id: Long, speed: Float)

    @Query("UPDATE scripts SET updated_at = :timestamp WHERE id = :id")
    suspend fun updateTimestamp(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE scripts SET content = :content, updated_at = :timestamp WHERE id = :id")
    suspend fun updateContent(id: Long, content: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE scripts SET title = :title, updated_at = :timestamp WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM scripts")
    suspend fun getScriptCount(): Int
}

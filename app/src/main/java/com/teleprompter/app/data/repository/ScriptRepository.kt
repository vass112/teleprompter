package com.teleprompter.app.data.repository

import com.teleprompter.app.data.db.ScriptDao
import com.teleprompter.app.data.db.ScriptEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScriptRepository @Inject constructor(
    private val scriptDao: ScriptDao
) {

    fun getAllScripts(): Flow<List<ScriptEntity>> = scriptDao.getAllScripts()

    fun getFavoriteScripts(): Flow<List<ScriptEntity>> = scriptDao.getFavoriteScripts()

    suspend fun getScriptById(id: Long): ScriptEntity? = scriptDao.getScriptById(id)

    fun getScriptByIdFlow(id: Long): Flow<ScriptEntity?> = scriptDao.getScriptByIdFlow(id)

    fun searchScripts(query: String): Flow<List<ScriptEntity>> = scriptDao.searchScripts(query)

    suspend fun createScript(title: String, content: String): Long {
        val now = System.currentTimeMillis()
        val script = ScriptEntity(
            title = title,
            content = content,
            createdAt = now,
            updatedAt = now
        )
        return scriptDao.insertScript(script)
    }

    suspend fun updateScript(script: ScriptEntity) {
        scriptDao.updateScript(script.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteScript(script: ScriptEntity) {
        scriptDao.deleteScript(script)
    }

    suspend fun deleteScriptById(id: Long) {
        scriptDao.deleteScriptById(id)
    }

    suspend fun duplicateScript(id: Long): Long? {
        val original = scriptDao.getScriptById(id) ?: return null
        val now = System.currentTimeMillis()
        val duplicate = original.copy(
            id = 0,
            title = "${original.title} (Copy)",
            createdAt = now,
            updatedAt = now,
            lastScrollPosition = 0f
        )
        return scriptDao.insertScript(duplicate)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        scriptDao.updateFavorite(id, isFavorite)
    }

    suspend fun updateScrollPosition(id: Long, position: Float) {
        scriptDao.updateScrollPosition(id, position)
    }

    suspend fun updateSpeed(id: Long, speed: Float) {
        scriptDao.updateSpeed(id, speed)
    }

    suspend fun updateContent(id: Long, content: String) {
        scriptDao.updateContent(id, content)
    }

    suspend fun updateTitle(id: Long, title: String) {
        scriptDao.updateTitle(id, title)
    }
}

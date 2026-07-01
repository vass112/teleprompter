package com.teleprompter.app.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teleprompter.app.data.db.ScriptEntity
import com.teleprompter.app.data.repository.ScriptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScriptEditorUiState(
    val script: ScriptEntity? = null,
    val title: String = "",
    val content: String = "",
    val isSaving: Boolean = false,
    val lastSaved: Long? = null,
    val isNewScript: Boolean = true,
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val estimatedReadingTime: Int = 0
)

@HiltViewModel
class ScriptEditorViewModel @Inject constructor(
    private val repository: ScriptRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScriptEditorUiState())
    val uiState: StateFlow<ScriptEditorUiState> = _uiState.asStateFlow()

    private var autosaveJob: Job? = null

    fun loadScript(scriptId: Long?) {
        if (scriptId == null || scriptId == -1L) {
            _uiState.value = _uiState.value.copy(
                isNewScript = true,
                title = "",
                content = "",
                wordCount = 0,
                charCount = 0,
                estimatedReadingTime = 0
            )
            startAutosave()
            return
        }

        viewModelScope.launch {
            val script = repository.getScriptById(scriptId)
            if (script != null) {
                _uiState.value = _uiState.value.copy(
                    script = script,
                    title = script.title,
                    content = script.content,
                    isNewScript = false,
                    wordCount = countWords(script.content),
                    charCount = script.content.length,
                    estimatedReadingTime = estimateReadingTime(script.content)
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isNewScript = true,
                    title = "",
                    content = ""
                )
            }
            startAutosave()
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun onContentChanged(content: String) {
        _uiState.value = _uiState.value.copy(
            content = content,
            wordCount = countWords(content),
            charCount = content.length,
            estimatedReadingTime = estimateReadingTime(content)
        )
    }

    fun saveNow() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isSaving = true)

            if (state.isNewScript) {
                val id = repository.createScript(
                    title = state.title.ifBlank { "Untitled Script" },
                    content = state.content
                )
                _uiState.value = _uiState.value.copy(
                    isNewScript = false,
                    lastSaved = System.currentTimeMillis(),
                    isSaving = false
                )
            } else {
                state.script?.let { script ->
                    repository.updateContent(script.id, state.content)
                    if (state.title.isNotBlank()) {
                        repository.updateTitle(script.id, state.title)
                    }
                    _uiState.value = _uiState.value.copy(
                        lastSaved = System.currentTimeMillis(),
                        isSaving = false
                    )
                }
            }
        }
    }

    private fun startAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            while (true) {
                delay(2000)
                if (_uiState.value.content.isNotEmpty()) {
                    saveNow()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autosaveJob?.cancel()
        if (_uiState.value.content.isNotEmpty()) {
            viewModelScope.launch { saveNow() }
        }
    }

    private fun countWords(text: String): Int {
        return text.split("\\s+".toRegex()).count { it.isNotBlank() }
    }

    private fun estimateReadingTime(text: String): Int {
        val words = countWords(text)
        return maxOf(1, (words / 150) + 1)
    }
}

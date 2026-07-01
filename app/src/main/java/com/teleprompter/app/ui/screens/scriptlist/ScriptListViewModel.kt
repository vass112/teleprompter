package com.teleprompter.app.ui.screens.scriptlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teleprompter.app.data.db.ScriptEntity
import com.teleprompter.app.data.repository.ScriptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScriptListUiState(
    val scripts: List<ScriptEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val showDeleteDialog: Boolean = false,
    val scriptToDelete: ScriptEntity? = null,
    val showRenameDialog: Boolean = false,
    val scriptToRename: ScriptEntity? = null,
    val renameText: String = ""
)

@HiltViewModel
class ScriptListViewModel @Inject constructor(
    private val repository: ScriptRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScriptListUiState())
    val uiState: StateFlow<ScriptListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllScripts().collect { scripts ->
                _uiState.value = _uiState.value.copy(
                    scripts = scripts,
                    isLoading = false
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            if (query.isBlank()) {
                repository.getAllScripts().collect { scripts ->
                    _uiState.value = _uiState.value.copy(scripts = scripts)
                }
            } else {
                repository.searchScripts(query).collect { scripts ->
                    _uiState.value = _uiState.value.copy(scripts = scripts)
                }
            }
        }
    }

    fun onDeleteScript(script: ScriptEntity) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            scriptToDelete = script
        )
    }

    fun onConfirmDelete() {
        _uiState.value.scriptToDelete?.let { script ->
            viewModelScope.launch {
                repository.deleteScript(script)
            }
        }
        dismissDeleteDialog()
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            scriptToDelete = null
        )
    }

    fun onDuplicateScript(script: ScriptEntity) {
        viewModelScope.launch {
            repository.duplicateScript(script.id)
        }
    }

    fun onToggleFavorite(script: ScriptEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(script.id, !script.isFavorite)
        }
    }

    fun onShowRenameDialog(script: ScriptEntity) {
        _uiState.value = _uiState.value.copy(
            showRenameDialog = true,
            scriptToRename = script,
            renameText = script.title
        )
    }

    fun onRenameTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(renameText = text)
    }

    fun onConfirmRename() {
        val script = _uiState.value.scriptToRename ?: return
        val newTitle = _uiState.value.renameText.trim()
        if (newTitle.isNotBlank()) {
            viewModelScope.launch {
                repository.updateTitle(script.id, newTitle)
            }
        }
        dismissRenameDialog()
    }

    fun dismissRenameDialog() {
        _uiState.value = _uiState.value.copy(
            showRenameDialog = false,
            scriptToRename = null,
            renameText = ""
        )
    }

    fun onCreateNewScript() = Unit
}

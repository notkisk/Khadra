package com.example.khadra.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.khadra.data.model.Tree
import com.example.khadra.data.repository.TreeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val treeRepository: TreeRepository
) : ViewModel() {

    private val _trees = MutableStateFlow<List<Tree>>(emptyList())
    val trees: StateFlow<List<Tree>> = _trees

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadTrees()
    }

    fun loadTrees() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                treeRepository.getTrees()
                    .catch { e ->
                        Log.e("MainViewModel", "Error loading trees", e)
                    }
                    .collect { trees ->
                        _trees.value = trees
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }
}

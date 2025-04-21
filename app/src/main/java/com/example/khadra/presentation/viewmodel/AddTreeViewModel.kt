package com.example.khadra.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.khadra.data.model.Tree
import com.example.khadra.data.model.TreeType
import com.example.khadra.data.repository.TreeRepository
import com.example.khadra.data.repository.TreeTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

private const val TAG = "AddTreeViewModel"

@HiltViewModel
class AddTreeViewModel @Inject constructor(
    private val treeRepository: TreeRepository,
    private val treeTypeRepository: TreeTypeRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(AddTreeUiState())
    val uiState: StateFlow<AddTreeUiState> = _uiState.asStateFlow()

    // Add Operation State
    private val _addState = MutableStateFlow<AddTreeState>(AddTreeState.Idle)
    val addState: StateFlow<AddTreeState> = _addState.asStateFlow()

    // Tree Types State
    private val _treeTypes = MutableStateFlow<List<TreeType>>(emptyList())
    val treeTypes: StateFlow<List<TreeType>> = _treeTypes.asStateFlow()

    init {
        Log.d(TAG, "Initializing AddTreeViewModel")
        loadTreeTypes()
    }

    private fun loadTreeTypes() {
        Log.d(TAG, "Loading tree types")
        viewModelScope.launch {
            try {
                treeTypeRepository.getTreeTypes().collect { types ->
                    Log.d(TAG, "Received ${types.size} tree types")
                    _treeTypes.value = types
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading tree types", e)
            }
        }
    }

    fun onEvent(event: AddTreeEvent) {
        Log.d(TAG, "Received event: $event")
        when (event) {
            is AddTreeEvent.NameChanged -> {
                _uiState.value = _uiState.value.copy(name = event.name)
                Log.d(TAG, "Name updated to: ${event.name}")
            }
            is AddTreeEvent.TypeSelected -> {
                _uiState.value = _uiState.value.copy(type = event.type)
                Log.d(TAG, "Type selected: ${event.type}")
            }
            is AddTreeEvent.StatusSelected -> {
                _uiState.value = _uiState.value.copy(status = event.status)
                Log.d(TAG, "Status selected: ${event.status}")
            }
            is AddTreeEvent.LocationChanged -> {
                _uiState.value = _uiState.value.copy(location = event.location)
                Log.d(TAG, "Location updated to: ${event.location}")
            }
            is AddTreeEvent.CoordinatesChanged -> {
                _uiState.value = _uiState.value.copy(
                    coordinates = Pair(event.latitude, event.longitude)
                )
                Log.d(TAG, "Coordinates updated to: (${event.latitude}, ${event.longitude})")
            }
            is AddTreeEvent.ImageUriChanged -> {
                _uiState.value = _uiState.value.copy(selectedImageUri = event.uri)
                Log.d(TAG, "Image URI updated to: ${event.uri}")
            }
            is AddTreeEvent.Submit -> {
                Log.d(TAG, "Submit event received")
                submitTree()
            }
        }
    }

    private fun submitTree() {
        val currentState = _uiState.value
        Log.d(TAG, "Starting tree submission with state: $currentState")

        // Validate required fields
        if (currentState.name.isBlank()) {
            Log.w(TAG, "Validation failed: Name is blank")
            _addState.value = AddTreeState.Error("يرجى إدخال اسم الشجرة")
            return
        }
        if (currentState.type.isNullOrBlank()) {
            Log.w(TAG, "Validation failed: Type is blank")
            _addState.value = AddTreeState.Error("يرجى اختيار نوع الشجرة")
            return
        }
        if (currentState.location.isBlank()) {
            Log.w(TAG, "Validation failed: Location is blank")
            _addState.value = AddTreeState.Error("يرجى إدخال موقع الشجرة")
            return
        }
        if (currentState.status.isBlank()) {
            Log.w(TAG, "Validation failed: Status is blank")
            _addState.value = AddTreeState.Error("يرجى اختيار حالة الشجرة")
            return
        }
        if (currentState.coordinates == null) {
            Log.w(TAG, "Validation failed: Coordinates not set")
            _addState.value = AddTreeState.Error("يرجى تحديد موقع الشجرة على الخريطة")
            return
        }

        Log.d(TAG, "All validations passed, proceeding with tree creation")
        _addState.value = AddTreeState.Loading

        viewModelScope.launch {
            try {
                Log.d(TAG, "Creating new tree object")
                val now = Date()
                val tree = Tree.fromCoordinates(
                    id = UUID.randomUUID().toString(),
                    name = currentState.name,
                    type = currentState.type,
                    status = currentState.status,
                    coordinates = currentState.coordinates,
                    location = currentState.location,
                    urlImage = currentState.selectedImageUri?.toString() ?: "",
                    lastIrrigationAction = now,
                    createdAt = now,
                    updatedAt = now
                )
                Log.d(TAG, "Created tree object: $tree")

                Log.d(TAG, "Attempting to add tree to repository")
                val result = treeRepository.addTree(tree)
                Log.d(TAG, "Repository add result: $result")
                
                _addState.value = AddTreeState.Success
                Log.d(TAG, "Tree added successfully")
                
                // Reset UI state after successful submission
                _uiState.value = AddTreeUiState()
                Log.d(TAG, "UI state reset")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding tree", e)
                _addState.value = AddTreeState.Error(e.message ?: "حدث خطأ أثناء إضافة الشجرة")
            }
        }
    }

    fun resetAddState() {
        Log.d(TAG, "Resetting add state")
        _addState.value = AddTreeState.Idle
    }

    // State Classes
    data class AddTreeUiState(
        val name: String = "",
        val type: String? = null,
        val status: String = "",
        val location: String = "",
        val coordinates: Pair<Double, Double>? = null,
        val selectedImageUri: Uri? = null
    )

    sealed class AddTreeState {
        object Idle : AddTreeState()
        object Loading : AddTreeState()
        object Success : AddTreeState()
        data class Error(val message: String) : AddTreeState()
    }

    sealed class AddTreeEvent {
        data class NameChanged(val name: String) : AddTreeEvent()
        data class TypeSelected(val type: String) : AddTreeEvent()
        data class StatusSelected(val status: String) : AddTreeEvent()
        data class LocationChanged(val location: String) : AddTreeEvent()
        data class CoordinatesChanged(val latitude: Double, val longitude: Double) : AddTreeEvent()
        data class ImageUriChanged(val uri: Uri) : AddTreeEvent()
        object Submit : AddTreeEvent()
    }
}

package com.example.khadra.presentation.view

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.khadra.presentation.viewmodel.AddTreeViewModel
import com.example.khadra.ui.theme.KhadraGreen

private const val TAG = "AddScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    viewModel: AddTreeViewModel = hiltViewModel()
) {
    Log.d(TAG, "AddScreen composable started")
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.uiState.collectAsState()
    val addState by viewModel.addState.collectAsState()
    val treeTypes by viewModel.treeTypes.collectAsState()

    // Handle side effects from addState
    LaunchedEffect(addState) {
        Log.d(TAG, "AddState changed to: $addState")
        when (val currentState = addState) {
            is AddTreeViewModel.AddTreeState.Success -> {
                Log.d(TAG, "Tree added successfully, showing success message")
                snackbarHostState.showSnackbar(
                    message = "تمت إضافة الشجرة بنجاح!",
                    duration = SnackbarDuration.Long
                )
                viewModel.resetAddState()
            }
            is AddTreeViewModel.AddTreeState.Error -> {
                Log.e(TAG, "Error adding tree: ${currentState.message}")
                snackbarHostState.showSnackbar(
                    message = currentState.message,
                    duration = SnackbarDuration.Long
                )
                viewModel.resetAddState()
            }
            is AddTreeViewModel.AddTreeState.Loading -> {
                Log.d(TAG, "Tree addition in progress")
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("إضافة شجرة جديدة", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KhadraGreen
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Picker
                ImagePickerSection(
                    selectedUri = state.selectedImageUri,
                    onImageSelected = { uri ->
                        Log.d(TAG, "Image selected: $uri")
                        viewModel.onEvent(AddTreeViewModel.AddTreeEvent.ImageUriChanged(uri))
                    }
                )

                // Tree Name
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.onEvent(AddTreeViewModel.AddTreeEvent.NameChanged(it)) },
                    label = { Text("اسم الشجرة") },
                    placeholder = { Text("أدخل اسم الشجرة", textAlign = TextAlign.End) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        textDirection = TextDirection.Rtl
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    isError = addState is AddTreeViewModel.AddTreeState.Error && state.name.isBlank()
                )

                // Type and Health Status Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tree Type Dropdown
                    TreeTypeDropDown(
                        selectedType = state.type ?: "",
                        isError = addState is AddTreeViewModel.AddTreeState.Error && state.type.isNullOrBlank(),
                        onTypeSelected = { viewModel.onEvent(AddTreeViewModel.AddTreeEvent.TypeSelected(it)) },
                        modifier = Modifier.weight(1f)
                    )

                    // Tree Health Status Dropdown
                    TreeHealthDropdown(
                        viewModel = viewModel,
                        selectedHealth = state.status,
                        isError = addState is AddTreeViewModel.AddTreeState.Error && state.status.isBlank(),
                        onHealthSelected = { viewModel.onEvent(AddTreeViewModel.AddTreeEvent.StatusSelected(it)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Location
                OutlinedTextField(
                    value = state.location,
                    onValueChange = { viewModel.onEvent(AddTreeViewModel.AddTreeEvent.LocationChanged(it)) },
                    label = { Text("الموقع") },
                    placeholder = { Text("أدخل موقع الشجرة", textAlign = TextAlign.End) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(textDirection = TextDirection.Rtl),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    isError = addState is AddTreeViewModel.AddTreeState.Error && state.location.isBlank()
                )

                // Map Button (Temporary)
                Button(
                    onClick = {
                        Log.d(TAG, "Setting test coordinates")
                        // Example coordinates for Riyadh
                        viewModel.onEvent(AddTreeViewModel.AddTreeEvent.CoordinatesChanged(
                            latitude = 24.7136,
                            longitude = 46.6753
                        ))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("تحديد الموقع على الخريطة")
                }

                // Coordinates (if available)
                if (state.coordinates != null) {
                    Text(
                        text = "الإحداثيات: ${state.coordinates?.first}, ${state.coordinates?.second}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = { 
                        Log.d(TAG, "Submit button clicked")
                        viewModel.onEvent(AddTreeViewModel.AddTreeEvent.Submit) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KhadraGreen,
                        contentColor = Color.White
                    ),
                    enabled = addState !is AddTreeViewModel.AddTreeState.Loading
                ) {
                    if (addState is AddTreeViewModel.AddTreeState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            "غرس الشجرة",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Loading Overlay
            if (addState is AddTreeViewModel.AddTreeState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) { /* Prevent clicks while loading */ },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(color = KhadraGreen)
                            Text("جاري إضافة الشجرة...", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImagePickerSection(
    selectedUri: Uri?,
    onImageSelected: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            Log.d(TAG, "Image picked: $it")
            onImageSelected(it) 
        }
    }

    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                color = KhadraGreen,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { launcher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        if (selectedUri != null) {
            Image(
                painter = rememberAsyncImagePainter(selectedUri),
                contentDescription = "Selected tree image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add image",
                    tint = KhadraGreen,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    "اضغط لإضافة صورة",
                    color = KhadraGreen,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeTypeDropDown(
    selectedType: String,
    isError: Boolean,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Fruit", "Palm", "Evergreen", "Ornamental", "Vegetable")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            readOnly = true,
            label = { Text("نوع الشجرة") },
            trailingIcon = {
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            },
            textStyle = TextStyle(textDirection = TextDirection.Rtl),
            singleLine = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            isError = isError
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onTypeSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeHealthDropdown(
    viewModel: AddTreeViewModel,
    selectedHealth: String,
    isError: Boolean,
    onHealthSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Healthy", "Moderate", "Low", "Critical")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedHealth,
            onValueChange = {},
            readOnly = true,
            label = { Text("حالة الشجرة") },
            trailingIcon = {
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            },
            textStyle = TextStyle(textDirection = TextDirection.Rtl),
            singleLine = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            isError = isError
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onHealthSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

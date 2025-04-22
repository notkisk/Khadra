package com.example.khadra.presentation.view

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.khadra.R
import com.example.khadra.presentation.viewmodel.*
import com.example.khadra.ui.theme.KhadraGreen

private const val TAG = "AddScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    viewModel: AddTreeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    Log.d(TAG, "AddScreen composable started")
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val state = viewModel.uiState.value
    val addState = viewModel.addState.value
    val treeTypes by viewModel.treeTypes.collectAsState()

    var showLocationPermissionDialog by remember { mutableStateOf(false) }

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) {
            viewModel.getCurrentLocation()
        }
    }

    if (showLocationPermissionDialog) {
        PermissionDialog(
            onDismiss = { showLocationPermissionDialog = false },
            onOkClick = {
                showLocationPermissionDialog = false
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            permissionTextProvider = "نحتاج إلى إذن الوصول إلى موقعك لتحديد موقع الشجرة"
        )
    }

    // Handle side effects from addState
    LaunchedEffect(addState) {
        Log.d(TAG, "AddState changed to: $addState")
        when (addState) {
            is AddTreeState.Success -> {
                snackbarHostState.showSnackbar("تمت إضافة الشجرة بنجاح")
                viewModel.resetAddState()
            }
            is AddTreeState.Error -> {
                snackbarHostState.showSnackbar(addState.message)
                viewModel.resetAddState()
            }
            is AddTreeState.Loading -> {
                // Show loading state
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
                        viewModel.onEvent(AddTreeEvent.ImageSelected(uri))
                    }
                )

                // Tree Name
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.onEvent(AddTreeEvent.NameChanged(it)) },
                    label = { Text("اسم الشجرة") },
                    placeholder = { Text("أدخل اسم الشجرة", textAlign = TextAlign.End) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        textDirection = TextDirection.Rtl
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    isError = addState is AddTreeState.Error && state.name.isBlank()
                )

                // Type and Health Status Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tree Type Dropdown
                    TreeTypeDropDown(
                        selectedType = state.type ?: "",
                        isError = addState is AddTreeState.Error && state.type.isNullOrBlank(),
                        onTypeSelected = { viewModel.onEvent(AddTreeEvent.TypeChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )

                    // Tree Health Status Dropdown
                    TreeHealthDropdown(
                        selectedHealth = state.status,
                        isError = addState is AddTreeState.Error && state.status.isBlank(),
                        onHealthSelected = { viewModel.onEvent(AddTreeEvent.StatusChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Location
                OutlinedTextField(
                    value = state.location,
                    onValueChange = { viewModel.onEvent(AddTreeEvent.LocationChanged(it)) },
                    label = { Text("الموقع") },
                    placeholder = { Text("أدخل موقع الشجرة", textAlign = TextAlign.End) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(textDirection = TextDirection.Rtl),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    isError = addState is AddTreeState.Error && state.location.isBlank()
                )

                // Map Button (Real Location)
                Button(
                    onClick = {
                        Log.d(TAG, "Getting current location")
                        if (!viewModel.hasLocationPermission()) {
                            showLocationPermissionDialog = true
                        } else {
                            viewModel.getCurrentLocation()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("تحديد موقعي الحالي")
                }

                // Coordinates (if available)
                if (state.coordinates != null) {
                    Text(
                        text = "الإحداثيات: ${String.format("%.6f", state.coordinates?.first)}, ${String.format("%.6f", state.coordinates?.second)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = { 
                        Log.d(TAG, "Submit button clicked")
                        viewModel.onEvent(AddTreeEvent.Submit) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KhadraGreen,
                        contentColor = Color.White
                    ),
                    enabled = addState !is AddTreeState.Loading
                ) {
                    if (addState is AddTreeState.Loading) {
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
            if (addState is AddTreeState.Loading) {
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val options = listOf("Fruit", "Palm", "Vegetable", "Evergreen", "Ornamental")

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

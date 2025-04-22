package com.example.khadra.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MapLocationPicker(
    onLocationSelected: (Double, Double) -> Unit,
    initialLocation: Pair<Double, Double>? = null,
    modifier: Modifier = Modifier
) {
    // For now, we'll use a placeholder that sets a fixed location
    // TODO: Implement actual map integration
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                // Default to Riyadh coordinates
                onLocationSelected(24.7136, 46.6753)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (initialLocation != null) {
                    "الموقع: ${initialLocation.first}, ${initialLocation.second}"
                } else {
                    "اضغط لتحديد الموقع على الخريطة"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

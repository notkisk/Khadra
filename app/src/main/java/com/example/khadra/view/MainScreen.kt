package com.example.khadra.view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.khadra.ui.theme.KhadraGreen
import com.example.khadra.viewmodel.TreeViewModel

@Composable
fun MainScreen(
    treeViewModel: TreeViewModel,
    modifier: Modifier = Modifier
) {
    val navItemsList = listOf(
        NavItem("Profile", Icons.Outlined.Person),
        NavItem("Map", Icons.Outlined.Place),
        NavItem("Add", Icons.Outlined.AddCircle),
        NavItem("Irrigation", Icons.Outlined.Info),
        NavItem("Home", Icons.Outlined.Home)
    )

    var selectedIndex by remember { mutableIntStateOf(4) } // Default screen is Home

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        bottomBar = {
            Box(
                modifier = Modifier
                    .drawBehind {
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.6f),
                            start = Offset.Zero,
                            end = Offset(size.width, 0f),
                            strokeWidth = 4f
                        )
                    }
                    .fillMaxWidth()
            ) {
                NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                    navItemsList.forEachIndexed { index, item ->
                        if (index == 2) {
                            // Custom Add Button (Bigger + Colored)
                            NavigationBarItem(
                                selected = selectedIndex == index,
                                onClick = { selectedIndex = index },
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp) // ✅ Bigger button
                                            .background(KhadraGreen, shape = CircleShape) // ✅ Custom color
                                            .padding(10.dp) // ✅ Adjust padding for better appearance
                                    ) {
                                        Icon(
                                            item.icon,
                                            contentDescription = item.label,
                                            modifier = Modifier.size(50.dp), // ✅ Bigger icon
                                            tint = Color.White // ✅ White icon for contrast
                                        )
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = KhadraGreen,
                                    selectedTextColor = KhadraGreen,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        } else {
                            // Regular Navigation Item
                            NavigationBarItem(
                                selected = selectedIndex == index,
                                onClick = { selectedIndex = index },
                                icon = {
                                    Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(35.dp))
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = KhadraGreen,
                                    selectedTextColor = KhadraGreen,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        ContentScreen(
            modifier = Modifier.padding(innerPadding),
            selectedIndex = selectedIndex,
            treeViewModel = treeViewModel
        )
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, selectedIndex: Int, treeViewModel: TreeViewModel) {
    when (selectedIndex) {
        0 -> ProfileScreen()
        1 -> MapScreen()
        2 -> AddScreen()
        3 -> IrrigationScreen()
        4 -> HomeScreen() // ✅ Fixed: No infinite recursion
    }
}

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Home Screen", fontSize = 48.sp,fontWeight = FontWeight.ExtraBold)
    }
}

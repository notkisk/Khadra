package com.example.khadra.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.khadra.R
import com.example.khadra.model.NavItem
import com.example.khadra.model.Tree
import com.example.khadra.ui.theme.Inter
import com.example.khadra.ui.theme.KhadraGreen
import com.example.khadra.ui.theme.KhadraTheme
import com.example.khadra.viewmodel.TreeViewModel
import kotlin.math.log


@Composable

fun MainScreen(
    treeViewModel: TreeViewModel,
    modifier: Modifier = Modifier
) {
    val navItemsList = listOf(
        NavItem("Profile", painterResource(R.drawable.ic_outline_person_outline_24)),
        NavItem("Map", painterResource(R.drawable.ic_outline_map_24)),
        NavItem("Add", painterResource(R.drawable.ic_outline_add_circle_outline_24)),
        NavItem("Irrigation", painterResource(R.drawable.ic_water_drop)),
        NavItem("Home", painterResource(R.drawable.ic_outline_home_24))
    )

    var selectedIndex by remember { mutableIntStateOf(4) } // Default screen is Home
    val mod = modifier.fillMaxWidth()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
         topBar = { TopBar(selectedIndex) },
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
                                    Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(38.dp))
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
        4 -> HomeScreen(modifier) // ✅ Fixed: No infinite recursion
    }
}
//hello
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 140.dp),
    ) {
        var textValue by remember { mutableStateOf("") }
            // Search Bar (TextField)
            OutlinedTextField(
                value = textValue,
                onValueChange = { searchQuery -> textValue = searchQuery },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                shape = RoundedCornerShape(32.dp), // ✅ Matches Box clipping
                singleLine = true,
                placeholder = { Text("Search...") },
                trailingIcon = {
                    Icon(
                        modifier = Modifier.size(42.dp),
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.Black
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent, // ✅ Transparent so image is visible
                    focusedIndicatorColor = Color.Gray,
                    unfocusedIndicatorColor = Color.Gray
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = {

                })
            )

    }
}


@Composable
fun TopBar(selectedIndex:Int) {

    var displayTopBar by remember { mutableStateOf(false) }
when (selectedIndex){
    0-> displayTopBar=false
    1-> displayTopBar=true
    2-> displayTopBar=false
    3-> displayTopBar=false
    4-> displayTopBar=true

}

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        color = Color.Black,
        shadowElevation = 100.dp // Elevation creates a shadow effect
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = KhadraGreen,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .size(120.dp)
        ) {
            Box(modifier = Modifier
                .padding(top = 8.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ))
            Text(
                text = when (selectedIndex) {
                    0 -> "الحساب الشخصي"
                    1 -> "الاشجار القريبة منك"
                    2 -> "غرس شجرة"
                    3 -> "اشجار تحتاج سقي"
                    4 -> "خضراء"
                    else -> { "" }
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = Inter,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.4f), // Shadow color
                        offset = Offset(0f, 10f), // Shadow position (x, y)
                        blurRadius = 8f // Blur effect
                    )
                )

            )

        }
    }


}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun PreviewMainScreen() {
    val mockViewModel = TreeViewModel() // If your ViewModel requires parameters, use a mock/fake implementation

    KhadraTheme  {
        MainScreen(treeViewModel = mockViewModel)
    }
}



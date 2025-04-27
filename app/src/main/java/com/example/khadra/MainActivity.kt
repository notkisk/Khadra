package com.example.khadra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.khadra.ui.theme.KhadraTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.khadra.presentation.view.MainScreen
import com.example.khadra.presentation.view.TreeDetailsScreen
import com.example.khadra.presentation.viewmodel.TreeViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KhadraTheme {
                val navController = rememberNavController()
                val treeViewModel = viewModel<TreeViewModel>()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "main",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("main") {
                            MainScreen(
                                treeViewModel = treeViewModel,
                                onNavigateToTreeDetails = { treeId ->
                                    navController.navigate("tree_details/$treeId")
                                }
                            )
                        }
                        composable(
                            route = "tree_details/{treeId}"
                        ) { backStackEntry ->
                            val treeId = backStackEntry.arguments?.getString("treeId") ?: return@composable
                            TreeDetailsScreen(
                                treeId = treeId,
                                viewModel = treeViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onEditTree = { /* Handle edit if needed */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
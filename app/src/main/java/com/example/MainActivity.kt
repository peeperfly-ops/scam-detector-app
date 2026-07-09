package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.AiChatAssistantScreen
import com.example.ui.CommunityForumScreen
import com.example.ui.HomeDashboardScreen
import com.example.ui.NotificationsScreen
import com.example.ui.ProfileAndSettingsScreen
import com.example.ui.ReportScamScreen
import com.example.ui.ScamAnalysisScreen
import com.example.ui.ScamDatabaseScreen
import com.example.ui.SplashScreen
import com.example.ui.theme.ScamDetectorTheme
import com.example.viewmodel.ScamViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ScamViewModel = viewModel()
            val darkModeActive by viewModel.darkMode.collectAsState()

            ScamDetectorTheme(darkTheme = darkModeActive) {
                AppNavigationContainer(viewModel)
            }
        }
    }
}

@Composable
fun AppNavigationContainer(viewModel: ScamViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define navigation items with label and standard Icon
    val bottomNavItems = listOf(
        NavigationItem("home", "Home", Icons.Default.Home),
        NavigationItem("database", "Database", Icons.Default.Search),
        NavigationItem("report", "Report", Icons.Default.BugReport),
        NavigationItem("community", "Radar", Icons.Default.Comment),
        NavigationItem("ai_chat", "AI Advisor", Icons.Default.Chat)
    )

    // Hide bottom bar on Splash and Detailed Analysis screens
    val showBottomBar = currentRoute != "splash" && currentRoute != "analysis" && currentRoute != null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) }
        ) {
            // Splash Route
            composable("splash") {
                SplashScreen(navController = navController)
            }

            // Home / Dashboard Route
            composable("home") {
                HomeDashboardScreen(
                    viewModel = viewModel,
                    navController = navController,
                    onNavigateToScan = { type, query ->
                        viewModel.performScan(type, query)
                        navController.navigate("analysis")
                    }
                )
            }

            // Detailed Scam Analysis Verdict Route
            composable("analysis") {
                ScamAnalysisScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            // Database Lookup Route
            composable("database") {
                ScamDatabaseScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            // Report Scam Route
            composable("report") {
                ReportScamScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            // Community Alerts Feed Route
            composable("community") {
                CommunityForumScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            // AI Chat advisor Route
            composable("ai_chat") {
                AiChatAssistantScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            // Notifications Route
            composable("notifications") {
                NotificationsScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            // Unified Profile & Settings Route
            composable("profile") {
                ProfileAndSettingsScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

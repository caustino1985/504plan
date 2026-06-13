package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.AppDatabase
import com.example.data.CaustinRepository
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.IpTreeScreen
import com.example.ui.screens.LitigationScreen
import com.example.ui.screens.RegulatoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SlateSecondary
import com.example.ui.theme.TealAccent1
import com.example.ui.viewmodel.CaustinTab
import com.example.ui.viewmodel.CaustinViewModel
import com.example.ui.viewmodel.CaustinViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: CaustinViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = CaustinRepository(database)
        CaustinViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentTab by viewModel.currentTab.collectAsState()

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.navigationBarsPadding(),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            val tabs = CaustinTab.values()
                            tabs.forEach { tab ->
                                val isSelected = currentTab == tab
                                val icon = when (tab) {
                                    CaustinTab.LITIGATION -> Icons.Default.AccountBalance
                                    CaustinTab.IP_TREE -> Icons.Default.Build
                                    CaustinTab.COMPLIANCE -> Icons.Default.CheckCircle
                                    CaustinTab.ASSISTANT -> Icons.Default.Star
                                }
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { viewModel.setTab(tab) },
                                    icon = { Icon(icon, contentDescription = tab.label) },
                                    label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NavyPrimary,
                                        selectedTextColor = NavyPrimary,
                                        indicatorColor = TealAccent1.copy(alpha = 0.15f),
                                        unselectedIconColor = SlateSecondary,
                                        unselectedTextColor = SlateSecondary
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            CaustinTab.LITIGATION -> LitigationScreen(viewModel)
                            CaustinTab.IP_TREE -> IpTreeScreen(viewModel)
                            CaustinTab.COMPLIANCE -> RegulatoryScreen(viewModel)
                            CaustinTab.ASSISTANT -> AssistantScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}

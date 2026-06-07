package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.SavedSite
import com.example.ui.WebWrapperViewModel
import com.example.ui.screens.AddWebsiteScreen
import com.example.ui.screens.BrowseScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

enum class Tab {
    Browse, Library, Settings
}

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: WebWrapperViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = androidx.lifecycle.ViewModelProvider(this)[WebWrapperViewModel::class.java]
        
        handleIntent(intent, viewModel)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainOrchestrator(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (::viewModel.isInitialized) {
            handleIntent(intent, viewModel)
        }
    }

    private fun handleIntent(intent: android.content.Intent?, viewModel: WebWrapperViewModel) {
        if (intent == null) return
        
        // 1. Try to read from explicit Intent Extras
        val urlExtra = intent.getStringExtra("url") ?: intent.getStringExtra("uri") ?: intent.getStringExtra("target_url")
        val isFullscreenExtra = intent.getBooleanExtra("fullscreen", false) || intent.getBooleanExtra("fullscreen_mode", false)
        val hideFabExtra = intent.getBooleanExtra("hide_fab", false) || intent.getBooleanExtra("hideFab", false)
        
        if (urlExtra != null) {
            viewModel.selectUrl(
                url = urlExtra,
                name = intent.getStringExtra("name") ?: "Web Link",
                isFullscreen = isFullscreenExtra,
                isJsEnabled = intent.getBooleanExtra("js_enabled", true)
            )
            viewModel.setHideFloatingActionButton(hideFabExtra || isFullscreenExtra)
            viewModel.setActiveTab(Tab.Browse)
            return
        }
        
        // 2. Try to read from Deep Link URI
        val data = intent.data
        if (data != null) {
            val scheme = data.scheme
            if (scheme == "webwrapper" && data.host == "open") {
                val urlParam = data.getQueryParameter("url") ?: data.getQueryParameter("uri")
                if (urlParam != null) {
                    val isFullscreenParam = data.getBooleanQueryParameter("fullscreen", false) || data.getBooleanQueryParameter("fullscreen_mode", false)
                    val hideFabParam = data.getBooleanQueryParameter("hide_fab", false) || data.getBooleanQueryParameter("hideFab", false)
                    val isJsParam = data.getBooleanQueryParameter("js_enabled", true)
                    val nameParam = data.getQueryParameter("name") ?: "Uri Link"
                    
                    viewModel.selectUrl(
                        url = urlParam,
                        name = nameParam,
                        isFullscreen = isFullscreenParam,
                        isJsEnabled = isJsParam
                    )
                    viewModel.setHideFloatingActionButton(hideFabParam || isFullscreenParam)
                    viewModel.setActiveTab(Tab.Browse)
                }
            } else if (scheme == "http" || scheme == "https") {
                val isFullscreenParam = data.getBooleanQueryParameter("fullscreen", false)
                val hide_fabParam = data.getBooleanQueryParameter("hide_fab", false) || data.getBooleanQueryParameter("hideFab", false)
                viewModel.selectUrl(
                    url = data.toString(),
                    name = "Web Link",
                    isFullscreen = isFullscreenParam,
                    isJsEnabled = true
                )
                viewModel.setHideFloatingActionButton(hide_fabParam || isFullscreenParam)
                viewModel.setActiveTab(Tab.Browse)
            }
        }
    }
}

@Composable
fun MainOrchestrator(
    viewModel: WebWrapperViewModel = viewModel()
) {
    val activeTab by viewModel.activeTab.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var siteToEdit by remember { mutableStateOf<SavedSite?>(null) }

    val isFullscreen by viewModel.isFullscreenActive.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Hide bottom navigation if the browse tab has active fullscreen immersive modes enabled
            if (!(activeTab == Tab.Browse && isFullscreen)) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("app_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = activeTab == Tab.Browse,
                        onClick = { viewModel.setActiveTab(Tab.Browse) },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == Tab.Browse) Icons.Default.Language else Icons.Outlined.Language,
                                contentDescription = "Navegar"
                            )
                        },
                        label = { Text("Navegar") },
                        modifier = Modifier.testTag("nav_tab_browse")
                    )

                    NavigationBarItem(
                        selected = activeTab == Tab.Library,
                        onClick = { viewModel.setActiveTab(Tab.Library) },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == Tab.Library) Icons.Default.List else Icons.Outlined.List,
                                contentDescription = "Biblioteca"
                            )
                        },
                        label = { Text("Biblioteca") },
                        modifier = Modifier.testTag("nav_tab_library")
                    )

                    NavigationBarItem(
                        selected = activeTab == Tab.Settings,
                        onClick = { viewModel.setActiveTab(Tab.Settings) },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == Tab.Settings) Icons.Default.Settings else Icons.Outlined.Settings,
                                contentDescription = "Configuració"
                            )
                        },
                        label = { Text("Configuració") },
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                Tab.Browse -> {
                    BrowseScreen(
                        viewModel = viewModel,
                        onNavigateToLibrary = { viewModel.setActiveTab(Tab.Library) },
                        onNavigateToSettings = { viewModel.setActiveTab(Tab.Settings) }
                    )
                }
                Tab.Library -> {
                    LibraryScreen(
                        viewModel = viewModel,
                        onNavigateToBrowse = { viewModel.setActiveTab(Tab.Browse) },
                        onNavigateToSettings = { viewModel.setActiveTab(Tab.Settings) },
                        onTriggerAddDialog = { site ->
                            siteToEdit = site
                            showAddDialog = true
                        }
                    )
                }
                Tab.Settings -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { viewModel.setActiveTab(Tab.Library) }
                    )
                }
            }

            // Beautiful slide-up full screen modal for adding shortcuts
            AnimatedVisibility(
                visible = showAddDialog,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                AddWebsiteScreen(
                    viewModel = viewModel,
                    siteToEdit = siteToEdit,
                    onDismissRequest = {
                        showAddDialog = false
                        siteToEdit = null
                    }
                )
            }
        }
    }
}

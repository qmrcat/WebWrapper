package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.SavedSite
import com.example.ui.WebWrapperViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    viewModel: WebWrapperViewModel,
    onNavigateToLibrary: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val currentUrl by viewModel.currentUrl.collectAsState()
    val currentSiteName by viewModel.currentSiteName.collectAsState()
    val isFullscreen by viewModel.isFullscreenActive.collectAsState()
    val isJsEnabled by viewModel.isJsEnabledActive.collectAsState()
    val progress by viewModel.loadProgress.collectAsState()
    val savedSites by viewModel.allSites.collectAsState()
    val fabAlignmentOnRight by viewModel.fabAlignmentOnRight.collectAsState()
    val hideFloatingActionButton by viewModel.hideFloatingActionButton.collectAsState()
    val isFromShortcut by viewModel.isFromShortcut.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var reloadTrigger by remember { mutableStateOf(0) }
    var showControls by remember { mutableStateOf(true) }
    var showTinyButton by remember { mutableStateOf(true) }

    // Reset showControls and showTinyButton on reload trigger or when progress starts
    LaunchedEffect(progress, reloadTrigger) {
        if (progress < 100 || reloadTrigger > 0) {
            showControls = true
            showTinyButton = true
        }
    }

    // Auto-hide controls and tiny button effect
    LaunchedEffect(showControls, progress, reloadTrigger) {
        if (showControls) {
            showTinyButton = true
            if (progress < 100) {
                // Keep visible while loading (up to 12 seconds in case a resource hangs)
                kotlinx.coroutines.delay(12000)
                showControls = false
            } else {
                // Wait for 5 seconds of idle time and then smoothly hide
                kotlinx.coroutines.delay(5000)
                showControls = false
            }
        } else {
            // Once main controls hide, keep the tiny button visible for 15 seconds, then hide it too
            kotlinx.coroutines.delay(15000)
            showTinyButton = false
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerTonalElevation = 2.dp,
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                // Drawer Profile Header Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data("https://lh3.googleusercontent.com/aida-public/AB6AXuDG5Ef69e_91FtsAPneZ1uRnunzt8bn9wDdRRXC9F3LEoXHnCO886c21q-o9rVzJ5eLkRdfCmSX3UzoNB5d19Ze_Fr8mUtncdetp4XUHIvYgEWGypZZmTQ9Z_0Be0pCTt6dPJZPAOiSV-N4mWWIXyfPxqy-eu_joKivvxfHTLM_qShy_G91kgHd8XaYBpudy7C4z-8jlLpsCLj1q2GWnw355Km80oXZMhmwG2nP0GLqTyQ5Gheb_dkZYuE4VzcUuiHUlr6FHmFb1fY")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "User Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column {
                                Text(
                                    text = "Llocs desats",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Gestiona les teves dreceres",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tancar"
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Navigation Links list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                            label = { Text("Tauler") },
                            selected = currentUrl == "dailyecho://home",
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                viewModel.selectUrl("dailyecho://home", "The Daily Echo", false, true)
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    // Dinamically show Bookmarks from Room database
                    if (savedSites.isNotEmpty()) {
                        item {
                            Text(
                                text = "DRECERES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }

                        items(savedSites.size) { index ->
                            val site = savedSites[index]
                            NavigationDrawerItem(
                                icon = {
                                    AsyncImage(
                                        model = site.faviconUrl ?: "https://www.google.com/s2/favicons?sz=64&domain=" + site.url,
                                        contentDescription = site.name,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                },
                                label = { Text(site.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                selected = currentUrl == site.url,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    viewModel.selectUrl(site.url, site.name, site.isFullscreen, site.isJsEnabled)
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }

                    item {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    item {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.LibraryBooks, contentDescription = null) },
                            label = { Text("Gestor de biblioteca") },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                onNavigateToLibrary()
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    item {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text("Ajustos de l'aplicació") },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                onNavigateToSettings()
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                // Footer Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "WebWrapper",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!isFullscreen && showControls) {
                    Column {
                        CenterAlignedTopAppBar(
                            navigationIcon = {
                                IconButton(onClick = {
                                    coroutineScope.launch { drawerState.open() }
                                }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            },
                            title = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = currentSiteName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = currentUrl,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            actions = {
                                if (currentUrl != "dailyecho://home") {
                                    IconButton(onClick = {
                                        reloadTrigger++
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Actualitzar"
                                        )
                                    }
                                    IconButton(onClick = {
                                        viewModel.selectUrl("dailyecho://home", "The Daily Echo", false, true)
                                    }) {
                                        Icon(Icons.Default.Home, contentDescription = "Home")
                                    }
                                } else {
                                    IconButton(onClick = onNavigateToSettings) {
                                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        if (progress > 0 && progress < 100) {
                            LinearProgressIndicator(
                                progress = { progress / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            },
            floatingActionButtonPosition = if (fabAlignmentOnRight) FabPosition.End else FabPosition.Start,
            floatingActionButton = {
                if (!hideFloatingActionButton) {
                    // Elegant squircle Floating Action Button inside the BrowseScreen to trigger the Menu drawer
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu Drawer",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        ) { innerPadding ->
            var isRefreshing by remember { mutableStateOf(false) }

            // Reset refreshing state when web progress reaches 100%
            LaunchedEffect(progress) {
                if (progress >= 100) {
                    isRefreshing = false
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isFullscreen) PaddingValues(0.dp) else innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (currentUrl == "dailyecho://home") {
                    DailyEchoHome(
                        onNavigateUrl = { targetUrl, siteName ->
                            viewModel.selectUrl(targetUrl, siteName, false, true)
                        }
                    )
                } else {
                    val pullToRefreshState = rememberPullToRefreshState()
                    LaunchedEffect(pullToRefreshState.distanceFraction) {
                        if (pullToRefreshState.distanceFraction > 0.05f && (!showControls || !showTinyButton)) {
                            showControls = true
                            showTinyButton = true
                        }
                    }

                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            isRefreshing = true
                            reloadTrigger++
                        },
                        state = pullToRefreshState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AppWebView(
                            url = currentUrl,
                            isJsEnabled = isJsEnabled,
                            reloadTrigger = reloadTrigger,
                            onProgressChanged = { viewModel.setProgress(it) }
                        )
                    }

                    // Custom loading progress bar for full screen mode
                    if (progress > 0 && progress < 100) {
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopStart)
                                .statusBarsPadding(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.Transparent
                        )
                    }

                    if (showControls) {
                        if (isFullscreen) {
                            // Small floating semi-transparent capsule for web controls when in fullscreen
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .statusBarsPadding()
                                    .padding(top = 10.dp, end = 12.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.75f),
                                tonalElevation = 4.dp,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Reload button
                                    IconButton(
                                        onClick = {
                                            reloadTrigger++
                                            showControls = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Actualitzar",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Other shortcuts are NOT needed when launched from pinnable shortcut
                                    if (!isFromShortcut) {
                                        // Home button
                                        IconButton(
                                            onClick = {
                                                viewModel.selectUrl("dailyecho://home", "The Daily Echo", false, true)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = "Home",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        // Exit fullscreen button
                                        IconButton(
                                            onClick = {
                                                viewModel.selectUrl(currentUrl, currentSiteName, false, isJsEnabled)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FullscreenExit,
                                                contentDescription = "Sortir de pantalla completa",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (showTinyButton) {
                        // Tiny floating translucent button to restore controls
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(top = 10.dp, end = 12.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.35f),
                            tonalElevation = 2.dp,
                            onClick = { showControls = true }
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Mostrar controls i actualitzar",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.61f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Standard Robust Android WebView integration
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AppWebView(
    url: String,
    isJsEnabled: Boolean,
    reloadTrigger: Int,
    onProgressChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    val webView = remember(context) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    onProgressChanged(10)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    onProgressChanged(100)
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    onProgressChanged(newProgress)
                }
            }
        }
    }

    // React to URL, Javascript and reload modifications dynamically
    LaunchedEffect(url, isJsEnabled, reloadTrigger) {
        webView.settings.apply {
            javaScriptEnabled = isJsEnabled
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = if (reloadTrigger > 0) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
        }
        webView.loadUrl(url)
    }

    AndroidView(
        factory = { webView },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * High-fidelity Native Compose reproduction of the beautiful "The Daily Echo" mockup
 */
@Composable
fun DailyEchoHome(onNavigateUrl: (String, String) -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Site Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column {
                Text(
                    text = "The Daily Echo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Notícies destacades del 5 de juny",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Bento Grid Layout: Featured Article
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onNavigateUrl(
                        "https://en.wikipedia.org/wiki/Ubiquitous_computing",
                        "Smart Cities & Computing"
                    )
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://lh3.googleusercontent.com/aida-public/AB6AXuB0OJm1CaEvxN4mSq05JhczI2ZUflPJs8IeokngSrghDcvqeOsEZHLnweC0ppHrK-0hbLVIVUeOkBCsSjNtfs8fKEUjj2RayaHNVt0PKh1uEVXbLX-2Qga1JWGY907Ne_4CQ3g2UZDCsUnviXHPUR3jlYKBT4DDszXK5NF50aK733N6x5bhdWWDMGejr2IRxD5MVc5Ls62xfkrePNVw49AKzbHico4gJIuWGvWhn1NVX_xdSjE4abGKF4ybp0hA2o0Ab2A-3culDPQ")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Futuristic smart city",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TECNOLOGIA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "El futur de la informàtica ubiqua a les ciutats intel·ligents",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.titleLarge.lineHeight * 0.95
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Explorant com les capes d'IA integrades redefineixen la nostra interacció amb la infraestructura urbana...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Bento Grid Layout: Sidebar & Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trending Now Pane
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tendències actuals",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TrendingRow(number = "01", title = "Arquitectura sostenible el 2026")
                    TrendingRow(number = "02", title = "Avenços en la interfície neuronal")
                    TrendingRow(number = "03", title = "Canvis en el mercat global")
                }
            }

            // Server Farm visual square card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://lh3.googleusercontent.com/aida-public/AB6AXuCpxJ76Nttih8vA-ENheAM4pAsrO_EsD_ntoK7iQwrdmezqsb19pU-xwR7fGb8Uvg9Jce8T__pmd1RwUoiOv81M04yAcMJXAeeUs4nP8B8baYtnBRccmlotQFqb6AILt8Be0-XvtyRICzI4a5x4KFWvxan_xZFfdrVDpxVbwnlclS-AByfjX-Q3-sMG6SpsVNZM_gyoAVLT3PjBtEeEqdds2-TpbOW2j8g70I_vzTUgD6qq4cWu4zHsuSYJDugNYXrbp4PiVOCL4UQ")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Server farm database view",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                    )
                }
            }
        }

        // Secondary Grid interactive items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryGridItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Analytics,
                title = "Anàlisi de mercat",
                desc = "Visualització de xarxes de dades descentralitzades.",
                color = MaterialTheme.colorScheme.secondaryContainer,
                onClick = {
                    onNavigateUrl("https://en.wikipedia.org/wiki/Market_analysis", "Market Analysis")
                }
            )
            SecondaryGridItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Eco,
                title = "Tecnologia verda",
                desc = "Finestres solars estàndard en edificis.",
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = {
                    onNavigateUrl("https://en.wikipedia.org/wiki/Environmental_technology", "Green Tech")
                }
            )
        }

        SecondaryGridItem(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.Psychology,
            title = "IA cognitiva",
            desc = "Immersió profunda en la complexa ètica de la presa de decisions autònoma i els sistemes de memòria cau local.",
            color = MaterialTheme.colorScheme.tertiaryContainer,
            onClick = {
                onNavigateUrl("https://en.wikipedia.org/wiki/Cognitive_computing", "Cognitive AI")
            }
        )
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun TrendingRow(number: String, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryGridItem(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = color
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.MainActivity
import com.example.R
import com.example.data.SavedSite
import com.example.ui.WebWrapperViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: WebWrapperViewModel,
    onNavigateToBrowse: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onTriggerAddDialog: (SavedSite?) -> Unit
) {
    val savedSites by viewModel.allSites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val fabAlignmentOnRight by viewModel.fabAlignmentOnRight.collectAsState()

    // Filter websites in real time according to search query
    val filteredSites = remember(savedSites, searchQuery) {
        if (searchQuery.isBlank()) {
            savedSites
        } else {
            savedSites.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.url.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WebWrapper",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configuració",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButtonPosition = if (fabAlignmentOnRight) FabPosition.End else FabPosition.Start,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onTriggerAddDialog(null) },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Afegir lloc web",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar Component
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    placeholder = {
                        Text(
                            text = "Cerca llocs web desats...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Cercar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Netejar text",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    singleLine = true
                )
            }

            // Library section title & dynamic sites badge count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "La teva biblioteca",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = CircleShape,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "${savedSites.size} llocs",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Dynamic websites list or empty state placeholder
            if (filteredSites.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmarks,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No s'ha trobat cap lloc web",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Premeu el botó + per afegir-ne una.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                items(filteredSites) { site ->
                    LibraryShortcutRow(
                        site = site,
                        onLaunch = {
                            viewModel.selectUrl(site.url, site.name, site.isFullscreen, site.isJsEnabled)
                            onNavigateToBrowse()
                        },
                        onDelete = {
                            viewModel.deleteSite(site)
                        },
                        onEdit = {
                            onTriggerAddDialog(site)
                        }
                    )
                }
            }

            // Promotional Bento Cards matching layout asymmetry exactly!
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Promotion 1: Sync Everywhere
                    PromotionCard(
                        title = "Sincronització global",
                        description = "Accedeix a la teva biblioteca des de qualsevol dispositiu de forma segura.",
                        buttonText = "Més informació",
                        icon = Icons.Default.Sync,
                        gradientColor = MaterialTheme.colorScheme.primaryContainer,
                        onActionClick = {}
                    )

                    // Promotion 2: Privacy Focus
                    PromotionCard(
                        title = "Enfocament de privadesa",
                        description = "La teva navegació es manté privada i xifrada.",
                        buttonText = "Configuració",
                        icon = Icons.Default.Security,
                        gradientColor = MaterialTheme.colorScheme.secondaryContainer,
                        onActionClick = onNavigateToSettings
                    )
                }
            }
        }
    }
}

/**
 * Beautiful item design matching list rows from mockup 2
 */
@Composable
fun LibraryShortcutRow(
    site: SavedSite,
    onLaunch: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLaunch() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Favicon avatar
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(site.faviconUrl ?: "https://www.google.com/s2/favicons?sz=128&domain=" + site.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = site.name,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Site name & URL
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = site.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = site.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Launch & config trigger endpoints
            IconButton(onClick = onLaunch) {
                Icon(
                    Icons.Default.Launch,
                    contentDescription = "Obrir URL",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Box {
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Opcions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    val context = LocalContext.current
                    DropdownMenuItem(
                        text = { Text("Afegir a l'inici") },
                        onClick = {
                            expandedMenu = false
                            if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                                val deepLinkUri = Uri.parse(
                                    "webwrapper://open?url=${Uri.encode(site.url)}" +
                                            "&fullscreen=${site.isFullscreen}" +
                                            "&js_enabled=${site.isJsEnabled}" +
                                            "&name=${Uri.encode(site.name)}"
                                )
                                val shortcutIntent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
                                    `package` = context.packageName
                                    setClass(context, MainActivity::class.java)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                                }
                                val shortcutInfo = ShortcutInfoCompat.Builder(context, "shortcut_${site.id}")
                                    .setShortLabel(site.name)
                                    .setLongLabel(site.name)
                                    .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                                    .setIntent(shortcutIntent)
                                    .build()
                                
                                ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
                                Toast.makeText(context, "Sol·licitud enviada per a ${site.name}!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "El teu terminal no admet accessos directes", Toast.LENGTH_SHORT).show()
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Afegir a l'inici",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            expandedMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar lloc web",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            expandedMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Modern promotion blocks
 */
@Composable
fun PromotionCard(
    title: String,
    description: String,
    buttonText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColor: Color,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = gradientColor.copy(alpha = 0.35f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.75f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(text = buttonText, style = MaterialTheme.typography.labelMedium)
                }
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 12.dp, y = 12.dp)
            )
        }
    }
}

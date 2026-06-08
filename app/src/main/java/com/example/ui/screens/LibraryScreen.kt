package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    var showPinDialog by remember { mutableStateOf(false) }

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
                    DropdownMenuItem(
                        text = { Text("Afegir a l'inici") },
                        onClick = {
                            expandedMenu = false
                            showPinDialog = true
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

    if (showPinDialog) {
        PinShortcutDialog(
            site = site,
            onDismiss = { showPinDialog = false }
        )
    }
}

data class EmojiPreset(val emoji: String, val hexColor: String, val label: String)

fun createEmojiBitmap(context: android.content.Context, emoji: String, backgroundColorInt: Int): Bitmap {
    val size = 128
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Draw background circle
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = backgroundColorInt
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    
    // Draw emoji
    paint.color = android.graphics.Color.WHITE
    paint.textSize = size * 0.55f
    paint.textAlign = Paint.Align.CENTER
    
    val fontMetrics = paint.fontMetrics
    val y = (size / 2f) - (fontMetrics.ascent + fontMetrics.descent) / 2f
    
    canvas.drawText(emoji, size / 2f, y, paint)
    return bitmap
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinShortcutDialog(
    site: SavedSite,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var shortcutName by remember { mutableStateOf(site.name) }
    var selectedOption by remember { mutableStateOf("favicon") } // "favicon", "app_icon", "emoji"
    
    // Preset emojis and color hexes
    val emojiPresets = remember {
        listOf(
            EmojiPreset("🌐", "#1E88E5", "Web"),
            EmojiPreset("📝", "#4CAF50", "Notes"),
            EmojiPreset("🛒", "#FF9800", "Botiga"),
            EmojiPreset("🎮", "#9C27B0", "Jocs"),
            EmojiPreset("📺", "#E53935", "Vídeo"),
            EmojiPreset("💬", "#E91E63", "Xat"),
            EmojiPreset("🎵", "#FDD835", "Música"),
            EmojiPreset("📰", "#00ACC1", "Notícies"),
            EmojiPreset("📧", "#5E35B1", "Correu"),
            EmojiPreset("👤", "#00897B", "Perfil"),
            EmojiPreset("❤️", "#D81B60", "Favorit"),
            EmojiPreset("💡", "#FFB300", "Idea")
        )
    }
    
    var selectedPresetIndex by remember { mutableStateOf(0) }
    var isPinningInProgress by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Afegir a la pantalla d'inici",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live preview card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "PREVISTA A LA PANTALLA D'INICI",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Icon Preview
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (selectedOption) {
                                "favicon" -> {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(site.faviconUrl ?: "https://www.google.com/s2/favicons?sz=128&domain=" + site.url)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Favicon Preview",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                "app_icon" -> {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "App Icon Preview",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                                "emoji" -> {
                                    val preset = emojiPresets[selectedPresetIndex]
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(preset.hexColor))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = preset.emoji,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                }
                            }
                        }
                        
                        Text(
                            text = shortcutName.ifEmpty { "Drecera" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Name Input
                OutlinedTextField(
                    value = shortcutName,
                    onValueChange = { shortcutName = it },
                    label = { Text("Nom de la drecera") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Icon source tabs
                Text(
                    text = "Tipus d'icona",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val options = listOf(
                        Triple("favicon", "Favicon", Icons.Default.Language),
                        Triple("app_icon", "App Icon", Icons.Default.Launch),
                        Triple("emoji", "Emoji", Icons.Default.Face)
                    )
                    
                    options.forEach { (type, label, icon) ->
                        val isSelected = selectedOption == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedOption = type },
                            label = { Text(label) },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Emoji options grid helper
                if (selectedOption == "emoji") {
                    Text(
                        text = "Seleccioneu un disseny",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        emojiPresets.chunked(4).forEach { rowPresets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPresets.forEach { preset ->
                                    val index = emojiPresets.indexOf(preset)
                                    val isPresetSelected = selectedPresetIndex == index
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isPresetSelected) MaterialTheme.colorScheme.primaryContainer 
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            )
                                            .border(
                                                width = if (isPresetSelected) 2.dp else 1.dp,
                                                color = if (isPresetSelected) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedPresetIndex = index }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(android.graphics.Color.parseColor(preset.hexColor))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = preset.emoji,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = preset.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                        isPinningInProgress = true
                        
                        val deepLinkUri = Uri.parse(
                            "webwrapper://open?url=${Uri.encode(site.url)}" +
                                    "&fullscreen=${site.isFullscreen}" +
                                    "&js_enabled=${site.isJsEnabled}" +
                                    "&name=${Uri.encode(shortcutName)}"
                        )
                        
                        val shortcutIntent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
                            `package` = context.packageName
                            setClass(context, MainActivity::class.java)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                        }
                        
                        scope.launch {
                            try {
                                val shortcutBuilder = ShortcutInfoCompat.Builder(context, "shortcut_${site.id}")
                                    .setShortLabel(shortcutName)
                                    .setLongLabel(shortcutName)
                                    .setIntent(shortcutIntent)
                                
                                when (selectedOption) {
                                    "app_icon" -> {
                                        shortcutBuilder.setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                                    }
                                    "emoji" -> {
                                        val preset = emojiPresets[selectedPresetIndex]
                                        val bitmap = createEmojiBitmap(
                                            context, 
                                            preset.emoji, 
                                            android.graphics.Color.parseColor(preset.hexColor)
                                        )
                                        shortcutBuilder.setIcon(IconCompat.createWithBitmap(bitmap))
                                    }
                                    "favicon" -> {
                                        val faviconUrl = site.faviconUrl ?: "https://www.google.com/s2/favicons?sz=128&domain=" + site.url
                                        val request = ImageRequest.Builder(context)
                                            .data(faviconUrl)
                                            .allowHardware(false)
                                            .build()
                                        
                                        val loader = coil.Coil.imageLoader(context)
                                        val result = loader.execute(request)
                                        val drawable = result.drawable
                                        
                                        if (drawable != null) {
                                            val sourceBitmap = drawable.toBitmap()
                                            val sizedBitmap = Bitmap.createScaledBitmap(sourceBitmap, 128, 128, true)
                                            shortcutBuilder.setIcon(IconCompat.createWithBitmap(sizedBitmap))
                                        } else {
                                            shortcutBuilder.setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                                        }
                                    }
                                }
                                
                                val shortcutInfo = shortcutBuilder.build()
                                ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
                                Toast.makeText(context, "Sol·licitud enviada per a $shortcutName!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isPinningInProgress = false
                                onDismiss()
                            }
                        }
                    } else {
                        Toast.makeText(context, "El teu terminal no admet accessos directes", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                enabled = !isPinningInProgress && shortcutName.isNotEmpty()
            ) {
                if (isPinningInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creant...")
                } else {
                    Text("Afegir")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isPinningInProgress
            ) {
                Text("Cancel·lar")
            }
        }
    )
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

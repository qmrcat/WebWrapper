package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.SavedSite
import com.example.ui.WebWrapperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWebsiteScreen(
    viewModel: WebWrapperViewModel,
    siteToEdit: SavedSite? = null,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var siteName by remember { mutableStateOf(siteToEdit?.name ?: "") }
    var siteUrl by remember { mutableStateOf(siteToEdit?.url ?: "") }
    var openInFullscreen by remember { mutableStateOf(siteToEdit?.isFullscreen ?: false) }
    var enableJs by remember { mutableStateOf(siteToEdit?.isJsEnabled ?: true) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (siteToEdit != null) "Editar lloc web" else "Afegir nou lloc web",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Outlined.Close, contentDescription = "Tancar")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Configura una drecera per al teu lloc web favorit.", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Outlined.HelpOutline, contentDescription = "Ajuda")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if (siteName.isBlank() || siteUrl.isBlank()) {
                                Toast.makeText(context, "Si us plau, omple tots els camps.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            if (siteToEdit != null) {
                                viewModel.updateSite(
                                    id = siteToEdit.id,
                                    name = siteName.trim(),
                                    url = siteUrl.trim(),
                                    isFullscreen = openInFullscreen,
                                    isJsEnabled = enableJs
                                )
                                Toast.makeText(context, "Drecera modificada correctament", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addSite(
                                    name = siteName.trim(),
                                    url = siteUrl.trim(),
                                    isFullscreen = openInFullscreen,
                                    isJsEnabled = enableJs
                                )
                                Toast.makeText(context, "Drecera desada correctament", Toast.LENGTH_SHORT).show()
                            }
                            onDismissRequest()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (siteToEdit != null) "Desar canvis" else "Desar lloc web",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Visual Section showing beautiful geometric illustration card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .padding(20.dp)
                            .align(Alignment.CenterVertically),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (siteToEdit != null) "Editar lloc web" else "Crea la teva biblioteca",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (siteToEdit != null) "Actualitza el nom, la URL o la configuració de visualització d'aquesta drecera." else "Integra qualsevol servei web directament al teu flux de treball amb opcions de visualització personalitzades.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("https://lh3.googleusercontent.com/aida-public/AB6AXuDfnYclCnMTtMyPh4xvZf5eEwBZqcG2myD2KuuTZqhxt3muGTmPM0m72FERPssowUVA88xldR5WppMF0LnUMD1ecc_YbNHr8Wtjdh27ZuSJky6SQ7bA-xHUpINpVmsC2rB_vaOs8Es0Bjdhm_yykBBmFUE1kkqMoGyrw-4iI0JWz5eRb6VQUHXj79XTyVKUlXNrbliTR3WyKYUuwTCa9BZRFIUPHHUwHJwrSFBW304bjcOpw3CTrd4m8XpuQ6SCrXh2ohVwzWbfBOg")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Il·lustració tècnica abstracta",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        )
                    }
                }
            }

            // Input: Site Name
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Nom del lloc web",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = siteName,
                    onValueChange = { siteName = it },
                    placeholder = { Text("p. ex., El meu tauler") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    singleLine = true
                )
                Text(
                    text = "Dóna un nom reconeixible a la teva drecera.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Input: Website URL
            val isLocalUrl = remember(siteUrl) {
                val clean = siteUrl.trim().lowercase()
                    .replace("http://", "")
                    .replace("https://", "")
                    .split("/").firstOrNull()?.split(":")?.firstOrNull() ?: ""
                clean == "localhost" || clean == "127.0.0.1" || clean.endsWith(".local") || run {
                    val parts = clean.split(".")
                    if (parts.size == 4) {
                        try {
                            val p0 = parts[0].toInt()
                            val p1 = parts[1].toInt()
                            (p0 == 192 && p1 == 168) || p0 == 10 || (p0 == 172 && p1 in 16..31) || (p0 == 169 && p1 == 254)
                        } catch (e: NumberFormatException) {
                            false
                        }
                    } else {
                        false
                    }
                }
            }
            val hasExplicitScheme = siteUrl.startsWith("http://") || siteUrl.startsWith("https://")

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "URL del lloc web",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = siteUrl,
                    onValueChange = { siteUrl = it },
                    placeholder = { Text("www.exemple.com") },
                    leadingIcon = if (hasExplicitScheme) {
                        null
                    } else {
                        {
                            Text(
                                text = if (isLocalUrl) "http://" else "https://",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    singleLine = true
                )
            }

            // Display options toggles styled on a container box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Preferències de visualització",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Toggle: Open in Fullscreen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.Fullscreen,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Obrir en pantalla completa",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Amaga el marc del navegador per a una vista immersiva",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = openInFullscreen,
                            onCheckedChange = { openInFullscreen = it }
                        )
                    }

                    // Toggle: Enable JavaScript
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Code,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Activar JavaScript",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Necessari per a la majoria d'aplicacions web modernes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = enableJs,
                            onCheckedChange = { enableJs = it }
                        )
                    }
                }
            }

            // Info Alert Tip
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Els ajustos avançats de renderització, com la simulació de l'agent d'usuari, es poden configurar al menú de Configuració Global després de desar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

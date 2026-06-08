package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.Tab
import com.example.data.AppDatabase
import com.example.data.SavedSite
import com.example.data.SavedSiteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WebWrapperViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SavedSiteRepository
    val allSites: StateFlow<List<SavedSite>>

    // Current web browsing state
    private val _currentUrl = MutableStateFlow("dailyecho://home")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _currentSiteName = MutableStateFlow("The Daily Echo")
    val currentSiteName: StateFlow<String> = _currentSiteName.asStateFlow()

    private val _isFullscreenActive = MutableStateFlow(false)
    val isFullscreenActive: StateFlow<Boolean> = _isFullscreenActive.asStateFlow()

    private val _isJsEnabledActive = MutableStateFlow(true)
    val isJsEnabledActive: StateFlow<Boolean> = _isJsEnabledActive.asStateFlow()

    // Loading progress (0 to 100)
    private val _loadProgress = MutableStateFlow(0)
    val loadProgress: StateFlow<Int> = _loadProgress.asStateFlow()

    // Search query in Library
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Floating action button position (true = Right/Dreta, false = Left/Esquerra)
    private val _fabAlignmentOnRight = MutableStateFlow(true)
    val fabAlignmentOnRight: StateFlow<Boolean> = _fabAlignmentOnRight.asStateFlow()

    // Screen navigation tracking state
    private val _activeTab = MutableStateFlow(Tab.Library)
    val activeTab: StateFlow<Tab> = _activeTab.asStateFlow()

    // State to entirely hide the floating action button (e.g. for kiosk/full screen action launches)
    private val _hideFloatingActionButton = MutableStateFlow(false)
    val hideFloatingActionButton: StateFlow<Boolean> = _hideFloatingActionButton.asStateFlow()

    // Flag indicating if current page is opened from an app shortcut
    private val _isFromShortcut = MutableStateFlow(false)
    val isFromShortcut: StateFlow<Boolean> = _isFromShortcut.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SavedSiteRepository(database.savedSiteDao())
        
        allSites = repository.allSites.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed default shortcuts if database is empty on first startup
        viewModelScope.launch {
            try {
                val currentSites = repository.allSites.first()
                if (currentSites.isEmpty()) {
                    seedDefaultSites()
                }
            } catch (e: Exception) {
                // Fallback in case of empty emissions issues
                seedDefaultSites()
            }
        }
    }

    private suspend fun seedDefaultSites() {
        val defaults = listOf(
            SavedSite(
                name = "Figma Dashboard",
                url = "https://www.figma.com/files/recent",
                isFullscreen = false,
                isJsEnabled = true,
                faviconUrl = "https://www.google.com/s2/favicons?sz=128&domain=figma.com"
            ),
            SavedSite(
                name = "GitHub Repositories",
                url = "https://github.com/trending",
                isFullscreen = false,
                isJsEnabled = true,
                faviconUrl = "https://www.google.com/s2/favicons?sz=128&domain=github.com"
            ),
            SavedSite(
                name = "Dribbble Inspirations",
                url = "https://dribbble.com/shots",
                isFullscreen = false,
                isJsEnabled = true,
                faviconUrl = "https://www.google.com/s2/favicons?sz=128&domain=dribbble.com"
            )
        )
        for (site in defaults) {
            repository.insert(site)
        }
    }

    fun selectUrl(url: String, name: String, isFullscreen: Boolean, isJsEnabled: Boolean, isShortcut: Boolean = false) {
        _currentUrl.value = url
        _currentSiteName.value = name
        _isFullscreenActive.value = isFullscreen
        _isJsEnabledActive.value = isJsEnabled
        _isFromShortcut.value = isShortcut
    }

    fun setIsFromShortcut(isShortcut: Boolean) {
        _isFromShortcut.value = isShortcut
    }

    fun setProgress(progress: Int) {
        _loadProgress.value = progress
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFabAlignmentOnRight(onRight: Boolean) {
        _fabAlignmentOnRight.value = onRight
    }

    fun setActiveTab(tab: Tab) {
        _activeTab.value = tab
    }

    fun setHideFloatingActionButton(hide: Boolean) {
        _hideFloatingActionButton.value = hide
    }

    private fun isLocalAddress(url: String): Boolean {
        val clean = url.trim().lowercase()
            .replace("http://", "")
            .replace("https://", "")
            .split("/").firstOrNull()?.split(":")?.firstOrNull() ?: ""
        
        if (clean == "localhost" || clean == "127.0.0.1" || clean.endsWith(".local")) return true
        
        val ipParts = clean.split(".")
        if (ipParts.size == 4) {
            try {
                val p0 = ipParts[0].toInt()
                val p1 = ipParts[1].toInt()
                if (p0 == 192 && p1 == 168) return true
                if (p0 == 10) return true
                if (p0 == 172 && (p1 in 16..31)) return true
                if (p0 == 169 && p1 == 254) return true
            } catch (e: NumberFormatException) {
                // Not a number
            }
        }
        return false
    }

    fun addSite(name: String, url: String, isFullscreen: Boolean, isJsEnabled: Boolean) {
        viewModelScope.launch {
            var formattedUrl = url.trim()
            if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
                if (isLocalAddress(formattedUrl)) {
                    formattedUrl = "http://$formattedUrl"
                } else {
                    formattedUrl = "https://$formattedUrl"
                }
            }
            
            val domain = formattedUrl.replace("https://", "").replace("http://", "").split("/").firstOrNull() ?: ""
            val favicon = "https://www.google.com/s2/favicons?sz=128&domain=$domain"
            
            val site = SavedSite(
                name = name,
                url = formattedUrl,
                isFullscreen = isFullscreen,
                isJsEnabled = isJsEnabled,
                faviconUrl = favicon
            )
            repository.insert(site)
        }
    }

    fun updateSite(id: Int, name: String, url: String, isFullscreen: Boolean, isJsEnabled: Boolean) {
        viewModelScope.launch {
            var formattedUrl = url.trim()
            if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
                if (isLocalAddress(formattedUrl)) {
                    formattedUrl = "http://$formattedUrl"
                } else {
                    formattedUrl = "https://$formattedUrl"
                }
            }
            
            val domain = formattedUrl.replace("https://", "").replace("http://", "").split("/").firstOrNull() ?: ""
            val favicon = "https://www.google.com/s2/favicons?sz=128&domain=$domain"
            
            val site = SavedSite(
                id = id,
                name = name,
                url = formattedUrl,
                isFullscreen = isFullscreen,
                isJsEnabled = isJsEnabled,
                faviconUrl = favicon
            )
            repository.update(site)
        }
    }

    fun deleteSite(site: SavedSite) {
        viewModelScope.launch {
            repository.delete(site)
        }
    }
}

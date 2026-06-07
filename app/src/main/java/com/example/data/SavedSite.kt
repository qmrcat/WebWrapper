package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_sites")
data class SavedSite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String,
    val isFullscreen: Boolean = false,
    val isJsEnabled: Boolean = true,
    val faviconUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSiteDao {
    @Query("SELECT * FROM saved_sites ORDER BY timestamp DESC")
    fun getAllSites(): Flow<List<SavedSite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: SavedSite): Long

    @Update
    suspend fun updateSite(site: SavedSite)

    @Delete
    suspend fun deleteSite(site: SavedSite)

    @Query("DELETE FROM saved_sites WHERE id = :id")
    suspend fun deleteSiteById(id: Int)

    @Query("SELECT * FROM saved_sites WHERE id = :id LIMIT 1")
    suspend fun getSiteById(id: Int): SavedSite?
}

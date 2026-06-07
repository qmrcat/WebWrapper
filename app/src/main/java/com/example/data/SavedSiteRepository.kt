package com.example.data

import kotlinx.coroutines.flow.Flow

class SavedSiteRepository(private val dao: SavedSiteDao) {
    val allSites: Flow<List<SavedSite>> = dao.getAllSites()

    suspend fun insert(site: SavedSite): Long = dao.insertSite(site)

    suspend fun update(site: SavedSite) = dao.updateSite(site)

    suspend fun delete(site: SavedSite) = dao.deleteSite(site)

    suspend fun deleteById(id: Int) = dao.deleteSiteById(id)

    suspend fun getById(id: Int): SavedSite? = dao.getSiteById(id)
}

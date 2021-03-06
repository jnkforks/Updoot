package com.ducktapedapps.updoot.api.local.submissionsCache

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.ducktapedapps.updoot.model.LinkData

@Dao
interface SubmissionsCacheDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissions(data: LinkData)

    @Query("DELETE FROM LinkData WHERE id IS :id AND subredditName IS :subreddit")
    suspend fun deleteSubmission(id: String, subreddit: String)

    @RawQuery(observedEntities = [LinkData::class])
    fun observeCachedSubmissions(query: SupportSQLiteQuery): LiveData<List<LinkData>>

    @Query("SELECT * FROM LinkData")
    suspend fun getAllCachedSubmissions(): List<LinkData>

    @Query("SELECT * FROM LinkData WHERE id is :id")
    suspend fun getLinkData(id: String): LinkData
}
package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplianceDao {
    @Query("SELECT * FROM compliance_items ORDER BY id ASC")
    fun getAllComplianceItems(): Flow<List<ComplianceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ComplianceItem>)

    @Update
    suspend fun updateItem(item: ComplianceItem)

    @Query("UPDATE compliance_items SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun toggleCompleted(id: Int, isCompleted: Boolean)
}

@Dao
interface LitigationDao {
    @Query("SELECT * FROM litigation_cases ORDER BY intensityMetric DESC")
    fun getAllCases(): Flow<List<LitigationCase>>

    @Query("SELECT * FROM litigation_cases WHERE region = :region ORDER BY intensityMetric DESC")
    fun getCasesByRegion(region: String): Flow<List<LitigationCase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCases(cases: List<LitigationCase>)

    @Update
    suspend fun updateCase(caseItem: LitigationCase)

    @Query("UPDATE litigation_cases SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun toggleBookmark(id: Int, isBookmarked: Boolean)
}

@Dao
interface AssistantDao {
    @Query("SELECT * FROM assistant_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<AssistantMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AssistantMessage)

    @Query("DELETE FROM assistant_messages")
    suspend fun clearHistory()
}

package com.example.data

import kotlinx.coroutines.flow.Flow

class CaustinRepository(private val db: AppDatabase) {

    private val complianceDao = db.complianceDao()
    private val litigationDao = db.litigationDao()
    private val assistantDao = db.assistantDao()

    val complianceItems: Flow<List<ComplianceItem>> = complianceDao.getAllComplianceItems()
    val litigationCases: Flow<List<LitigationCase>> = litigationDao.getAllCases()
    val assistantMessages: Flow<List<AssistantMessage>> = assistantDao.getAllMessages()

    suspend fun insertComplianceItems(items: List<ComplianceItem>) {
        complianceDao.insertItems(items)
    }

    suspend fun updateComplianceItem(item: ComplianceItem) {
        complianceDao.updateItem(item)
    }

    suspend fun toggleComplianceSelected(id: Int, isCompleted: Boolean) {
        complianceDao.toggleCompleted(id, isCompleted)
    }

    suspend fun insertLitigationCases(cases: List<LitigationCase>) {
        litigationDao.insertCases(cases)
    }

    suspend fun updateLitigationCase(caseItem: LitigationCase) {
        litigationDao.updateCase(caseItem)
    }

    suspend fun toggleLitigationBookmark(id: Int, isBookmarked: Boolean) {
        litigationDao.toggleBookmark(id, isBookmarked)
    }

    suspend fun insertAssistantMessage(message: AssistantMessage) {
        assistantDao.insertMessage(message)
    }

    suspend fun clearAssistantHistory() {
        assistantDao.clearHistory()
    }
}

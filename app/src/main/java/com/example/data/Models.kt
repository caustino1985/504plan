package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "compliance_items")
data class ComplianceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "FDA", "HIPAA", "GINA"
    val requirement: String,
    val importance: String, // "Critical", "High", "Standard"
    val isCompleted: Boolean,
    val auditCode: String,
    val cryptoHash: String
)

@Entity(tableName = "litigation_cases")
data class LitigationCase(
    @PrimaryKey val id: Int,
    val caseNumber: String,
    val title: String,
    val region: String,
    val intensityMetric: Double,
    val summary: String,
    val cryptographicHash: String,
    val isAlert: Boolean,
    val isBookmarked: Boolean,
    val year: Int
)

@Entity(tableName = "assistant_messages")
data class AssistantMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val queryText: String,
    val replyText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val provenanceHash: String
)

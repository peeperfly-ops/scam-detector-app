package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

import com.squareup.moshi.JsonClass

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // PHONE, WEBSITE, EMAIL, SMS, QR, BANK, SOCIAL
    val query: String,
    val riskScore: Int,
    val label: String, // Safe, Suspicious, High Risk
    val reasonsJson: String, // Stored as serialized/comma-separated strings
    val recommendationsJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "scam_reports")
data class ScamReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val details: String,
    val category: String,
    val isAnonymous: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val reports: Int = 0,
    val screenshotBase64: String? = null // Persisted screenshot in base64
)

@Entity(tableName = "saved_searches")
data class SavedSearch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// Data Class representing the structure of our Gemini API Response
@JsonClass(generateAdapter = true)
data class GeminiAnalysisResult(
    val riskScore: Int,
    val label: String, // Safe, Suspicious, High Risk
    val reasons: List<String>,
    val recommendations: List<String>,
    val category: String
)

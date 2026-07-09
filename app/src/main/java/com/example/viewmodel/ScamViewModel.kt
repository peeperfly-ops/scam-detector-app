package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChatMessage
import com.example.data.GeminiAnalysisResult
import com.example.data.SavedSearch
import com.example.data.ScamReport
import com.example.data.ScamRepository
import com.example.data.ScanHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScamViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ScamRepository(
        scanHistoryDao = db.scanHistoryDao(),
        scamReportDao = db.scamReportDao(),
        savedSearchDao = db.savedSearchDao(),
        chatMessageDao = db.chatMessageDao()
    )

    // Exposed DB Flows
    val scanHistory: StateFlow<List<ScanHistory>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scamReports: StateFlow<List<ScamReport>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedSearches: StateFlow<List<SavedSearch>> = repository.allSavedSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.allChatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Scanning States
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<GeminiAnalysisResult?>(null)
    val scanResult: StateFlow<GeminiAnalysisResult?> = _scanResult.asStateFlow()

    private val _activeQuery = MutableStateFlow("")
    val activeQuery: StateFlow<String> = _activeQuery.asStateFlow()

    private val _activeType = MutableStateFlow("")
    val activeType: StateFlow<String> = _activeType.asStateFlow()

    // Preferences and Settings State (Persisted in ViewModel Session)
    private val _darkMode = MutableStateFlow(true) // Cyber app defaults to Dark Mode
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _language = MutableStateFlow("English") // "English" or "Español"
    val language: StateFlow<String> = _language.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _privacyControlsEnabled = MutableStateFlow(true)
    val privacyControlsEnabled: StateFlow<Boolean> = _privacyControlsEnabled.asStateFlow()

    // Simulated Authentication State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    // Active Database Query search fields
    private val _dbSearchQuery = MutableStateFlow("")
    val dbSearchQuery: StateFlow<String> = _dbSearchQuery.asStateFlow()

    // Is the active scan saved by the user?
    val isCurrentScanSaved = MutableStateFlow(false)

    init {
        // Pre-populate data if DB is empty to ensure gorgeous initial visuals
        viewModelScope.launch {
            try {
                val currentReports = repository.allReports.first()
                if (currentReports.isEmpty()) {
                    populateDefaultReports()
                }

                val currentChat = repository.allChatMessages.first()
                if (currentChat.isEmpty()) {
                    repository.clearChat()
                    db.chatMessageDao().insertMessage(
                        ChatMessage(
                            message = "Welcome to ScamShield AI Chat! 🛡️\n\nI can help you analyze text messages, identify phishing tactics, or review suspicious emails. What would you like to verify today?",
                            isUser = false
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("ScamViewModel", "Error in database pre-population", e)
            }
        }
    }

    // Dynamic checks
    fun checkIsSaved(type: String, query: String) {
        viewModelScope.launch {
            repository.isSaved(type, query).collect {
                isCurrentScanSaved.value = it
            }
        }
    }

    // Toggle Saved Status
    fun toggleSaveCurrentScan() {
        val type = _activeType.value
        val query = _activeQuery.value
        if (type.isEmpty() || query.isEmpty()) return

        viewModelScope.launch {
            if (isCurrentScanSaved.value) {
                repository.unsaveQuery(type, query)
                isCurrentScanSaved.value = false
            } else {
                repository.saveQuery(type, query)
                isCurrentScanSaved.value = true
            }
        }
    }

    // Execute Scam Scan
    fun performScan(type: String, query: String) {
        _isScanning.value = true
        _activeType.value = type
        _activeQuery.value = query
        _scanResult.value = null

        viewModelScope.launch {
            try {
                val result = repository.analyzeScam(type, query)
                _scanResult.value = result
                checkIsSaved(type, query)
            } catch (e: Exception) {
                Log.e("ScamViewModel", "Scan execution failed", e)
            } finally {
                _isScanning.value = false
            }
        }
    }

    // AI Chat Actions
    fun sendChatMessage(text: String) {
        if (text.trim().isEmpty()) return
        viewModelScope.launch {
            repository.askAssistant(text)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
            db.chatMessageDao().insertMessage(
                ChatMessage(
                    message = "Chat history cleared. How can I assist you with security concerns now?",
                    isUser = false
                )
            )
        }
    }

    // Scam Reporting
    fun reportScam(title: String, details: String, category: String, isAnonymous: Boolean, screenshotBase64: String?) {
        viewModelScope.launch {
            val report = ScamReport(
                title = title,
                details = details,
                category = category,
                isAnonymous = isAnonymous,
                screenshotBase64 = screenshotBase64
            )
            repository.insertReport(report)
        }
    }

    // Community interactions
    fun likeReport(report: ScamReport) {
        viewModelScope.launch {
            repository.updateReport(report.copy(likes = report.likes + 1))
        }
    }

    fun reportReport(report: ScamReport) {
        viewModelScope.launch {
            repository.updateReport(report.copy(reports = report.reports + 1))
        }
    }

    // Toggle Preferences
    fun toggleDarkMode() {
        _darkMode.value = !_darkMode.value
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    fun toggleNotifications() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }

    fun togglePrivacy() {
        _privacyControlsEnabled.value = !_privacyControlsEnabled.value
    }

    // Auth Simulation
    fun login(email: String) {
        _userEmail.value = email
        _isLoggedIn.value = true
    }

    fun logout() {
        _userEmail.value = ""
        _isLoggedIn.value = false
    }

    // Search Local Scam Database
    fun setDbSearchQuery(query: String) {
        _dbSearchQuery.value = query
    }

    // Clean scan histories
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteHistoryById(id)
        }
    }

    fun deleteSavedSearchItem(id: Int) {
        viewModelScope.launch {
            repository.deleteSavedSearchById(id)
        }
    }

    fun isGeminiConfigured() = repository.isGeminiKeyConfigured()

    // Pre-populates the community forum with elegant realistic data on first launch
    private suspend fun populateDefaultReports() {
        val sample1 = ScamReport(
            title = "USPS Delivery Failure Text",
            details = "I received a message: 'The USPS package has arrived at the warehouse but cannot be delivered due to incomplete address information. Please update link: usps-post-redelivery.xyz'. This is a phishing site designed to steal card details!",
            category = "SMS Phishing",
            isAnonymous = false,
            likes = 34,
            reports = 0
        )
        val sample2 = ScamReport(
            title = "Impersonation of Netflix Bill",
            details = "Got an email claiming: 'Your Netflix membership was suspended because your payment failed.' The email looked super official but had sender: support@netflix-update-billing.cc. Do not open or click links!",
            category = "Email Phishing",
            isAnonymous = true,
            likes = 56,
            reports = 0
        )
        val sample3 = ScamReport(
            title = "Fake Crypto Yield Program",
            details = "A Telegram account @CryptoJack_Yield offered a 300% return in 24 hours. They sent me a QR code to transfer USDT. Be warned: this is an exit-scam wallet!",
            category = "Crypto Scam",
            isAnonymous = false,
            likes = 42,
            reports = 1
        )
        val sample4 = ScamReport(
            title = "IRS Refund Phone Call Scam",
            details = "Received an automated robo-call from +1 (888) 555-0199 claiming I owed $4,000 back taxes and that the local sheriff would arrive in 1 hour if I didn't pay via Apple Gift Cards. The IRS never calls and never demands gift cards!",
            category = "Phone Call scam",
            isAnonymous = false,
            likes = 89,
            reports = 0
        )

        repository.insertReport(sample1)
        repository.insertReport(sample2)
        repository.insertReport(sample3)
        repository.insertReport(sample4)
    }
}

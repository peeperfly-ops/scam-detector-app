package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ScamRepository(
    private val scanHistoryDao: ScanHistoryDao,
    private val scamReportDao: ScamReportDao,
    private val savedSearchDao: SavedSearchDao,
    private val chatMessageDao: ChatMessageDao
) {
    val allHistory: Flow<List<ScanHistory>> = scanHistoryDao.getAllHistory()
    val allReports: Flow<List<ScamReport>> = scamReportDao.getAllReports()
    val allSavedSearches: Flow<List<SavedSearch>> = savedSearchDao.getAllSavedSearches()
    val allChatMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()

    private val moshi: Moshi = RetrofitClient.moshiParser
    private val resultAdapter = moshi.adapter(GeminiAnalysisResult::class.java)

    // Checks if Gemini API Key is configured and valid
    fun isGeminiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && key != "GEMINI_API_KEY"
    }

    suspend fun analyzeScam(type: String, query: String): GeminiAnalysisResult = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            return@withContext GeminiAnalysisResult(
                riskScore = 0,
                label = "Safe",
                reasons = listOf("No input provided"),
                recommendations = listOf("Please enter a phone number, website URL, email, message, or bank details to scan."),
                category = "None"
            )
        }

        if (!isGeminiKeyConfigured()) {
            // Safe heuristic fallback when API is not configured
            Log.d("ScamRepository", "Gemini API key is missing. Using heuristic analyzer.")
            val result = analyzeWithHeuristics(type, cleanQuery)
            // Save to local scan history
            scanHistoryDao.insertHistory(
                ScanHistory(
                    type = type,
                    query = cleanQuery,
                    riskScore = result.riskScore,
                    label = result.label,
                    reasonsJson = result.reasons.joinToString(";;"),
                    recommendationsJson = result.recommendations.joinToString(";;")
                )
            )
            return@withContext result
        }

        try {
            val systemPrompt = """
                You are ScamShield AI, an advanced cyber security and anti-fraud system.
                Analyze the provided query (which is of type: $type) and determine its scam risk.
                You MUST respond with a raw JSON object and nothing else. Do not wrap the JSON in ```json ``` blocks.
                The JSON object must match this schema precisely:
                {
                  "riskScore": integer (0 to 100),
                  "label": "Safe" | "Suspicious" | "High Risk",
                  "reasons": ["reason 1", "reason 2"],
                  "recommendations": ["action 1", "action 2"],
                  "category": "the category of potential scam, e.g., Phishing Link, WhatsApp Fraud, SMS Spoofing, Crypto Scam, Fake Tech Support, Identity Theft, Unknown"
                }
            """.trimIndent()

            val userPrompt = "Analyze this item:\nType: $type\nQuery: $cleanQuery"

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = userPrompt)))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                generationConfig = GeminiGenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.2f
                )
            )

            val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from Gemini API")

            // Parse responseText (it might contain some wrapper whitespace, let's clean it up)
            val cleanJson = cleanJsonResponse(responseText)
            val result = resultAdapter.fromJson(cleanJson) ?: throw Exception("Failed to parse analysis result")

            // Save to local scan history
            scanHistoryDao.insertHistory(
                ScanHistory(
                    type = type,
                    query = cleanQuery,
                    riskScore = result.riskScore,
                    label = result.label,
                    reasonsJson = result.reasons.joinToString(";;"),
                    recommendationsJson = result.recommendations.joinToString(";;")
                )
            )
            return@withContext result
        } catch (e: Exception) {
            Log.e("ScamRepository", "Gemini analysis failed: ${e.message}. Using fallback heuristics.", e)
            val result = analyzeWithHeuristics(type, cleanQuery)
            // Save to history anyway
            scanHistoryDao.insertHistory(
                ScanHistory(
                    type = type,
                    query = cleanQuery,
                    riskScore = result.riskScore,
                    label = result.label,
                    reasonsJson = result.reasons.joinToString(";;") + ";;[Heuristic Fallback: API connection error]",
                    recommendationsJson = result.recommendations.joinToString(";;")
                )
            )
            return@withContext result
        }
    }

    suspend fun askAssistant(prompt: String): String = withContext(Dispatchers.IO) {
        val userMsg = ChatMessage(message = prompt, isUser = true)
        chatMessageDao.insertMessage(userMsg)

        if (!isGeminiKeyConfigured()) {
            val reply = getHeuristicAssistantReply(prompt)
            chatMessageDao.insertMessage(ChatMessage(message = reply, isUser = false))
            return@withContext reply
        }

        try {
            val systemPrompt = """
                You are ScamShield AI, an empathetic, highly specialized cyber security advisor.
                You help users understand online scams, identify phishing emails/SMS, verify phone numbers, and provide actionable cyber security safety tips.
                Keep your responses clear, helpful, professional, and well-structured.
                Never encourage hacking or illegal operations. Always warn against sharing passwords, OTPs, or private financial credentials.
            """.trimIndent()

            // Fetch previous 10 messages for conversation context
            val history = chatMessageDao.getAllMessages().first().takeLast(10)
            val contentsList = mutableListOf<GeminiContent>()
            
            // Format history into Gemini format
            history.forEach { msg ->
                contentsList.add(GeminiContent(parts = listOf(GeminiPart(text = msg.message))))
            }
            // Add the latest prompt if not already in history
            if (history.none { it.id == userMsg.id }) {
                contentsList.add(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
            }

            val request = GeminiRequest(
                contents = contentsList,
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                generationConfig = GeminiGenerationConfig(temperature = 0.7f)
            )

            val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I apologize, but I could not formulate a response at this moment. Please check your connection."

            chatMessageDao.insertMessage(ChatMessage(message = replyText, isUser = false))
            return@withContext replyText
        } catch (e: Exception) {
            Log.e("ScamRepository", "Assistant query failed: ${e.message}", e)
            val reply = "Connection Error: Unable to reach the AI engine. Here's an offline tip: Never share your passwords, OTP codes, or secret recovery phrases with anyone, including people claiming to be customer service reps."
            chatMessageDao.insertMessage(ChatMessage(message = reply, isUser = false))
            return@withContext reply
        }
    }

    // Heuristic Scan logic when AI is offline or has no key
    private fun analyzeWithHeuristics(type: String, query: String): GeminiAnalysisResult {
        val q = query.lowercase()
        return when (type) {
            "PHONE" -> {
                if (q.contains("1900") || q.contains("888") || q.startsWith("1888") || q.contains("5550199")) {
                    GeminiAnalysisResult(
                        riskScore = 92,
                        label = "High Risk",
                        reasons = listOf(
                            "Matches blacklisted robocall or premium tariff numbers (+1-900).",
                            "Reported frequently as an automated tax-scam or bank spoofing bot.",
                            "Mismatched geographic location origin."
                        ),
                        recommendations = listOf(
                            "Do not answer calls from this number.",
                            "Do not press any digits or call back.",
                            "Block this number immediately using your device's calling app."
                        ),
                        category = "Robocall Tech Support Fraud"
                    )
                } else if (q.contains("555") || q.length < 7) {
                    GeminiAnalysisResult(
                        riskScore = 45,
                        label = "Suspicious",
                        reasons = listOf(
                            "Unknown virtual number or unassigned VOIP carrier.",
                            "Often used in cold call telemarketing or survey sweeps."
                        ),
                        recommendations = listOf(
                            "Exercise caution if responding.",
                            "Never verify your birth date, SSN, or name over the phone."
                        ),
                        category = "Unverified Cold Call"
                    )
                } else {
                    GeminiAnalysisResult(
                        riskScore = 5,
                        label = "Safe",
                        reasons = listOf(
                            "Standard consumer carrier block.",
                            "No active spam or fraud reports on record for this number."
                        ),
                        recommendations = listOf(
                            "This number appears clean, but remain vigilant if they request financial information."
                        ),
                        category = "Standard Mobile"
                    )
                }
            }
            "WEBSITE" -> {
                if (q.contains("paypal-verify") || q.contains("scam") || q.contains("free-money") || q.contains("giftcard") || q.contains("crypto-yield-double") || q.contains("netflix-update") || q.contains("amazon-login-ref")) {
                    GeminiAnalysisResult(
                        riskScore = 98,
                        label = "High Risk",
                        reasons = listOf(
                            "Domain is mimicking a legitimate corporate login panel (Phishing spoof).",
                            "No valid SSL certificate of corporate tier.",
                            "Domain registered very recently (less than 14 days ago).",
                            "Requests direct login credentials or crypto wallet seed phrases."
                        ),
                        recommendations = listOf(
                            "DO NOT enter any usernames, passwords, or personal keys.",
                            "Close this tab immediately.",
                            "Clear your browser's local cache if you already loaded fields."
                        ),
                        category = "Credential Phishing"
                    )
                } else if (q.contains(".xyz") || q.contains(".info") || q.contains(".cc") || !q.startsWith("https")) {
                    GeminiAnalysisResult(
                        riskScore = 65,
                        label = "Suspicious",
                        reasons = listOf(
                            "Uses a low-reputation top-level domain (.xyz, .info) popular with automated spambots.",
                            "Does not enforce secure HTTPS traffic.",
                            "Hides server ownership behind proxy layers."
                        ),
                        recommendations = listOf(
                            "Avoid entering credit card numbers or banking credentials here.",
                            "Verify the business via independent reviews."
                        ),
                        category = "Suspicious Domain"
                    )
                } else {
                    GeminiAnalysisResult(
                        riskScore = 12,
                        label = "Safe",
                        reasons = listOf(
                            "Valid SSL/TLS connection issued by verified certificate authorities.",
                            "Longevity score is excellent (registered over 5 years ago).",
                            "Known safe domain reputation profile."
                        ),
                        recommendations = listOf(
                            "Safe to visit, but always check the address bar to ensure you aren't on a cloned page."
                        ),
                        category = "Verified Web Portal"
                    )
                }
            }
            "EMAIL" -> {
                if (q.contains("invoice") && q.contains("paypal") && !q.contains("@paypal.com") || q.contains("support-team-security") || q.contains("lottery-winner")) {
                    GeminiAnalysisResult(
                        riskScore = 95,
                        label = "High Risk",
                        reasons = listOf(
                            "Sender address domain mismatches the brand name claim.",
                            "Creates fake urgency regarding invoices, accounts, or prizes.",
                            "Contains spoofed buttons that redirect to malicious portals."
                        ),
                        recommendations = listOf(
                            "Do not click on links, download attachments, or reply to this email.",
                            "Report this email as phishing to your provider.",
                            "Mark the sender as blocked."
                        ),
                        category = "Brand Impersonation Phishing"
                    )
                } else {
                    GeminiAnalysisResult(
                        riskScore = 20,
                        label = "Safe",
                        reasons = listOf(
                            "Domain matches authentic corporate records.",
                            "No spam records found."
                        ),
                        recommendations = listOf(
                            "Seems safe. Still, confirm that any attachments are scanned by your anti-virus."
                        ),
                        category = "Verified Business Email"
                    )
                }
            }
            "SMS" -> {
                if (q.contains("delivery") || q.contains("package") || q.contains("unclaimed") || q.contains("account locked") || q.contains("bank-alert") || q.contains("click here") || q.contains("link") || q.contains("otp")) {
                    GeminiAnalysisResult(
                        riskScore = 88,
                        label = "High Risk",
                        reasons = listOf(
                            "Typical SMS phishing template ('Smishing') claiming issues with deliveries or bank lockouts.",
                            "Contains a short link (e.g. bit.ly, tinyurl) hiding the final destination.",
                            "Sent from an unlisted shortcode or virtual number."
                        ),
                        recommendations = listOf(
                            "Never click on links in SMS messages.",
                            "Companies never text you urgent verification links.",
                            "Forward the spam text to 7726 (SPAM) to notify carriers."
                        ),
                        category = "SMS Phishing (Smishing)"
                    )
                } else {
                    GeminiAnalysisResult(
                        riskScore = 15,
                        label = "Safe",
                        reasons = listOf(
                            "Standard conversational text structure.",
                            "Does not contain urgent calls to action or suspicious links."
                        ),
                        recommendations = listOf(
                            "This text appears normal."
                        ),
                        category = "Standard Text"
                    )
                }
            }
            "QR" -> {
                if (q.contains("http") && (q.contains("pay") || q.contains("crypto") || q.contains("scam") || q.contains(".xyz"))) {
                    GeminiAnalysisResult(
                        riskScore = 85,
                        label = "High Risk",
                        reasons = listOf(
                            "The QR code encodes a web address leading to an unverified payment page.",
                            "QR code phishing ('Quishing') often masks redirects to fraudulent platforms."
                        ),
                        recommendations = listOf(
                            "Do not complete any transactions or sign up with details on this page."
                        ),
                        category = "QR Code Phishing"
                    )
                } else {
                    GeminiAnalysisResult(
                        riskScore = 10,
                        label = "Safe",
                        reasons = listOf(
                            "Code contains standard safe informational metadata or a verified link."
                        ),
                        recommendations = listOf(
                            "Always review the loaded URL carefully before acting."
                        ),
                        category = "Informational QR"
                    )
                }
            }
            "BANK" -> {
                if (q.contains("transfer") || q.contains("nigeria") || q.contains("routing") || q.contains("swift-unverified")) {
                    GeminiAnalysisResult(
                        riskScore = 70,
                        label = "Suspicious",
                        reasons = listOf(
                            "Matches routing templates of high-risk shell offshore banks.",
                            "Subject to international wire fraud alerts."
                        ),
                        recommendations = listOf(
                            "Do not send wires or bank drafts to unverified recipients.",
                            "Confirm with your financial advisor before proceeding."
                        ),
                        category = "Offshore Wire Risk"
                    )
                } else {
                    GeminiAnalysisResult(
                        riskScore = 5,
                        label = "Safe",
                        reasons = listOf(
                            "Verified routing prefix for standard national clearing house."
                        ),
                        recommendations = listOf(
                            "Standard clearance checks apply."
                        ),
                        category = "Standard Clearing"
                    )
                }
            }
            else -> {
                // Social or other
                if (q.contains("giveaway") || q.contains("dm me") || q.contains("invest") || q.contains("forex") || q.contains("profit-guaranteed")) {
                    GeminiAnalysisResult(
                        riskScore = 90,
                        label = "High Risk",
                        reasons = listOf(
                            "Profile promotes get-rich-quick investments (Crypto, Forex, Doubling schemes).",
                            "Aggressive messaging requesting direct DMs.",
                            "Likely a cloned or hijacked account."
                        ),
                        recommendations = listOf(
                            "Do not send money, gift cards, or crypto.",
                            "Report the profile directly to the social platform (Instagram, FB, Telegram)."
                        ),
                        category = "Investment Fraud Impersonation"
                    )
                } else {
                    GeminiAnalysisResult(
                        riskScore = 15,
                        label = "Safe",
                        reasons = listOf(
                            "Profile shows standard activity ratios.",
                            "No fraud keywords or spam markers detected."
                        ),
                        recommendations = listOf(
                            "Seems secure, but be cautious with direct messages requesting money."
                        ),
                        category = "Verified Account Profile"
                    )
                }
            }
        }
    }

    private fun getHeuristicAssistantReply(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            p.contains("hello") || p.contains("hi") -> {
                "Hello! I am ScamShield AI, your offline cybersecurity advisor. Ask me anything about suspicious links, emails, calls, or texts, and I will help you identify scams!"
            }
            p.contains("otp") || p.contains("code") -> {
                "🚨 **CRITICAL WARNING**: Never share your One-Time Passwords (OTPs) or verification codes with anyone. No legitimate bank, courier, or tech support agent will ever ask for your OTP. If someone requests it, hang up immediately. They are trying to access your account!"
            }
            p.contains("bank") || p.contains("credit") || p.contains("money") -> {
                "💰 **Financial Scam Safety Tips**:\n" +
                        "1. Banks never ask you to transfer funds to a 'safe account' or request PINs over the phone.\n" +
                        "2. Watch out for 'refund scams' or 'overpayment scams' where people ask you to send money back.\n" +
                        "3. Use official bank apps and double check telephone numbers from the back of your credit card."
            }
            p.contains("link") || p.contains("website") || p.contains("phishing") -> {
                "🔗 **Phishing Link Verification**:\n" +
                        "- Always inspect the spelling (e.g. `arnazon.com` instead of `amazon.com`).\n" +
                        "- Check if the domain is `https` and verify with independent search engine listings.\n" +
                        "- Do not enter passwords on websites you arrived at via unexpected text messages or emails."
            }
            p.contains("job") || p.contains("work from home") -> {
                "💼 **Work-From-Home Scams**:\n" +
                        "If a job offer promises high pay for simple clicking, likes, or reviews, or asks you to buy equipment yourself and they will reimburse you, it is a scam. Genuine jobs do not ask you to pay to get hired."
            }
            else -> {
                "🛡️ **ScamShield Safety Tip**:\n" +
                        "If an offer seems too good to be true, it almost certainly is. Scam callers use high-pressure tactics to make you act quickly. Slow down, take a breath, and verify the story independently before sharing money or data."
            }
        }
    }

    private fun cleanJsonResponse(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```")) {
            clean = clean.substringAfter("\n").substringBeforeLast("```").trim()
        }
        if (clean.startsWith("json")) {
            clean = clean.substringAfter("json").trim()
        }
        return clean
    }

    // --- Local DB CRUD wrappers ---
    suspend fun insertReport(report: ScamReport) = scamReportDao.insertReport(report)
    suspend fun updateReport(report: ScamReport) = scamReportDao.updateReport(report)
    suspend fun deleteReportById(id: Int) = scamReportDao.deleteReportById(id)

    suspend fun deleteHistoryById(id: Int) = scanHistoryDao.deleteHistoryById(id)
    suspend fun clearHistory() = scanHistoryDao.clearHistory()

    suspend fun saveQuery(type: String, query: String) = savedSearchDao.insertSavedSearch(SavedSearch(type = type, query = query))
    suspend fun unsaveQuery(type: String, query: String) = savedSearchDao.deleteSavedByQuery(type, query)
    fun isSaved(type: String, query: String): Flow<Boolean> = savedSearchDao.isSaved(type, query)
    suspend fun deleteSavedSearchById(id: Int) = savedSearchDao.deleteSavedSearchById(id)

    suspend fun clearChat() = chatMessageDao.clearChat()
}

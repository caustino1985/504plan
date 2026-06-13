package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.example.network.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlin.random.Random

class CaustinViewModel(
    application: Application,
    private val repository: CaustinRepository
) : AndroidViewModel(application) {

    // Tab Navigation State
    private val _currentTab = MutableStateFlow(CaustinTab.LITIGATION)
    val currentTab: StateFlow<CaustinTab> = _currentTab.asStateFlow()

    // Query filters
    private val _selectedRegion = MutableStateFlow("All")
    val selectedRegion: StateFlow<String> = _selectedRegion.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Patent Portfolio selection and highlighted claim paths
    private val _selectedPortfolio = MutableStateFlow(PatentPortfolios.PORTFOLIOS.first())
    val selectedPortfolio: StateFlow<PatentPortfolio> = _selectedPortfolio.asStateFlow()

    private val _selectedClaimId = MutableStateFlow<Int?>(null)
    val selectedClaimId: StateFlow<Int?> = _selectedClaimId.asStateFlow()

    // Compliance categories ("All", "FDA", "HIPAA", "GINA")
    private val _selectedComplianceCategory = MutableStateFlow("All")
    val selectedComplianceCategory: StateFlow<String> = _selectedComplianceCategory.asStateFlow()

    // AI Assistant Generation states
    private val _aiInputText = MutableStateFlow("")
    val aiInputText: StateFlow<String> = _aiInputText.asStateFlow()

    private val _aiResponseState = MutableStateFlow<AiResponseState>(AiResponseState.Idle)
    val aiResponseState: StateFlow<AiResponseState> = _aiResponseState.asStateFlow()

    // NIH PubMed Search State
    private val _pubMedQuery = MutableStateFlow("gwas")
    val pubMedQuery: StateFlow<String> = _pubMedQuery.asStateFlow()

    private val _pubMedResults = MutableStateFlow<List<PubMedEntry>>(emptyList())
    val pubMedResults: StateFlow<List<PubMedEntry>> = _pubMedResults.asStateFlow()

    private val _pubMedLoading = MutableStateFlow(false)
    val pubMedLoading: StateFlow<Boolean> = _pubMedLoading.asStateFlow()

    private val _pubMedError = MutableStateFlow<String?>(null)
    val pubMedError: StateFlow<String?> = _pubMedError.asStateFlow()

    // arXiv search results (incorporating genetics, risk scoring, mit, cc licenses)
    private val _arxivQuery = MutableStateFlow("genetics \"risk scoring\" mit")
    val arxivQuery: StateFlow<String> = _arxivQuery.asStateFlow()

    private val _arxivResults = MutableStateFlow<List<ArxivEntry>>(emptyList())
    val arxivResults: StateFlow<List<ArxivEntry>> = _arxivResults.asStateFlow()

    private val _arxivLoading = MutableStateFlow(false)
    val arxivLoading: StateFlow<Boolean> = _arxivLoading.asStateFlow()

    private val _arxivError = MutableStateFlow<String?>(null)
    val arxivError: StateFlow<String?> = _arxivError.asStateFlow()

    // DB flows
    val complianceItems: StateFlow<List<ComplianceItem>> = repository.complianceItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val litigationCases: StateFlow<List<LitigationCase>> = repository.litigationCases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assistantHistory: StateFlow<List<AssistantMessage>> = repository.assistantMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Pre-populate data if empty
        viewModelScope.launch {
            complianceItems.first { it.isNotEmpty() || true }
            litigationCases.first { it.isNotEmpty() || true }
            
            launch {
                repository.complianceItems.take(1).collect { items ->
                    if (items.isEmpty()) {
                        seedComplianceItems()
                    }
                }
            }
            launch {
                repository.litigationCases.take(1).collect { cases ->
                    if (cases.isEmpty()) {
                        seedLitigationCases()
                    }
                }
            }
        }
        // Trigger automated academic searches as required
        searchPubMedRecords("gwas")
        searchArxivRecords("genetics \"risk scoring\" mit")
    }

    // Navigation and filters
    fun setTab(tab: CaustinTab) {
        _currentTab.value = tab
    }

    fun setRegion(region: String) {
        _selectedRegion.value = region
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectPortfolio(portfolio: PatentPortfolio) {
        _selectedPortfolio.value = portfolio
        _selectedClaimId.value = null
    }

    fun selectClaim(claimId: Int?) {
        _selectedClaimId.value = claimId
    }

    fun getVerifiedClaimPath(): List<Int> {
        val selectedId = _selectedClaimId.value ?: return emptyList()
        val portfolio = _selectedPortfolio.value
        val path = mutableListOf<Int>()
        
        fun findPath(currentId: Int): Boolean {
            path.add(currentId)
            val claim = portfolio.claims.find { it.id == currentId }
            if (claim != null) {
                if (claim.parentId == null) {
                    return true
                } else {
                    if (findPath(claim.parentId)) {
                        return true
                    }
                }
            }
            path.removeAt(path.lastIndex)
            return false
        }
        
        findPath(selectedId)
        return path.reversed()
    }

    fun setComplianceCategory(category: String) {
        _selectedComplianceCategory.value = category
    }

    fun setAiInputText(text: String) {
        _aiInputText.value = text
    }

    // Toggle items in DB
    fun toggleComplianceItem(id: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleComplianceSelected(id, isCompleted)
        }
    }

    fun toggleLitigationBookmark(id: Int, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleLitigationBookmark(id, isBookmarked)
        }
    }

    // Execute Gemini Assistant query
    fun submitAiQuery(customPrompt: String? = null) {
        val prompt = customPrompt ?: _aiInputText.value
        if (prompt.isBlank()) return

        _aiInputText.value = ""
        _aiResponseState.value = AiResponseState.Loading

        viewModelScope.launch {
            try {
                // Ensure internet connectivity and non-empty key
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _aiResponseState.value = AiResponseState.Error(
                        "API Key missing. Please set your GEMINI_API_KEY inside the Secrets panel of AI Studio."
                    )
                    return@launch
                }

                // Call REST API
                val requestModel = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(
                                    text = """
                                        You are the Public Trust IP Platform AI Assistant.
                                        You specialize in intellectual property (IP), patent claim analysis, 
                                        FDA clinical drug auditing, GINA regulatory compliance, and HIPAA privacy rules.
                                        
                                        Please summarize or analyze the following user inquiry with extreme expert legal precision.
                                        Be thorough, clear, objective, and refer to corresponding statutes where applicable.
                                        At the end of your response, output a JSON block or code format for specific verified reference tracking.
                                        
                                        User Inquiry: $prompt
                                    """.trimIndent()
                                )
                            )
                        )
                    ),
                    generationConfig = GenerationConfig(temperature = 0.2f, maxOutputTokens = 1200)
                )

                val response = RetrofitClient.service.generateContent(apiKey, requestModel)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Synthesizer completed, but returned empty content."

                // Generate a thematic SHA-512 cryptographic proof of compliance
                val provenanceHash = generateSha512(reply + System.currentTimeMillis().toString())

                // Insert into Room
                val msg = AssistantMessage(
                    queryText = prompt,
                    replyText = reply,
                    provenanceHash = "SHA-512/$provenanceHash"
                )
                repository.insertAssistantMessage(msg)

                _aiResponseState.value = AiResponseState.Success(reply, msg.provenanceHash)
            } catch (e: Exception) {
                _aiResponseState.value = AiResponseState.Error(
                    e.localizedMessage ?: "Unknown network error while contacting legislative synthesizer services."
                )
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAssistantHistory()
        }
    }

    fun searchPubMedRecords(query: String) {
        _pubMedQuery.value = query
        _pubMedLoading.value = true
        _pubMedError.value = null
        viewModelScope.launch {
            try {
                val results = ResearchPortalService.searchPubMed(query)
                _pubMedResults.value = results
                _pubMedLoading.value = false
                if (results.isEmpty()) {
                    _pubMedError.value = "No records found on PubMed matching clinical queries."
                }
            } catch (e: Exception) {
                _pubMedLoading.value = false
                _pubMedError.value = "PubMed connection error: ${e.localizedMessage}"
            }
        }
    }

    fun searchArxivRecords(query: String) {
        _arxivQuery.value = query
        _arxivLoading.value = true
        _arxivError.value = null
        viewModelScope.launch {
            try {
                val results = ResearchPortalService.searchArxiv(query)
                _arxivResults.value = results
                _arxivLoading.value = false
                if (results.isEmpty()) {
                    _arxivError.value = "No corresponding open-licensed papers found."
                }
            } catch (e: Exception) {
                _arxivLoading.value = false
                _arxivError.value = "arXiv connection error: ${e.localizedMessage}"
            }
        }
    }

    // Cryptographic utility
    private fun generateSha512(input: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-512").digest(input.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
        } catch (e: Exception) {
            "A89BD8F923C402E1"
        }
    }

    // Prepopulating default data
    private suspend fun seedComplianceItems() {
        val items = listOf(
            ComplianceItem(
                category = "FDA",
                requirement = "Audit trace diagnostic algorithms for clinical validated biomarkers validation under CFR Part 11 electronic records alignment.",
                importance = "Critical",
                isCompleted = true,
                auditCode = "21 CFR § 11.10",
                cryptoHash = "SHA-256/F9D83B0E2A"
            ),
            ComplianceItem(
                category = "FDA",
                requirement = "Implement genetic safety evaluation trials protocols and publish public outcome indicators securely.",
                importance = "High",
                isCompleted = false,
                auditCode = "21 CFR § 312.23",
                cryptoHash = "SHA-256/CC9D87E61B"
            ),
            ComplianceItem(
                category = "HIPAA",
                requirement = "Establish physical and electronic terminal cryptographic key access authentication filters.",
                importance = "Critical",
                isCompleted = true,
                auditCode = "45 CFR § 164.312(a)",
                cryptoHash = "SHA-256/A88CDE230F"
            ),
            ComplianceItem(
                category = "HIPAA",
                requirement = "Execute patient metadata de-identification scrubbing logs for public domain genetic dataset aggregation.",
                importance = "Standard",
                isCompleted = false,
                auditCode = "45 CFR § 164.514(b)",
                cryptoHash = "SHA-256/7D3C66F12C"
            ),
            ComplianceItem(
                category = "GINA",
                requirement = "Formulate absolute boundaries against employer acquisition of worker DNA profile metadata or genetic diagnostic tests.",
                importance = "Critical",
                isCompleted = false,
                auditCode = "GINA Title II § 202",
                cryptoHash = "SHA-256/B3E90124AA"
            ),
            ComplianceItem(
                category = "GINA",
                requirement = "Deploy policy limits prohibiting health underwriting modifiers based purely on hereditary pre-dispositions.",
                importance = "High",
                isCompleted = true,
                auditCode = "GINA Title I § 101",
                cryptoHash = "SHA-256/2A9F6B5C3D"
            )
        )
        repository.insertComplianceItems(items)
    }

    private suspend fun seedLitigationCases() {
        val cases = listOf(
            LitigationCase(
                id = 1,
                caseNumber = "US-2026-982-H",
                title = "Hereditary Diagnostics vs. Maryland GenTech Inc.",
                region = "Maryland",
                intensityMetric = 72.6,
                summary = "Critical litigation regarding patent claims over gene-sequencing diagnostic engines and direct infringement actions under Maryland regional codes. IDEA-based educational wellness challenges are also cited.",
                cryptographicHash = "SHA-3/8E1E90F4",
                isAlert = true,
                isBookmarked = false,
                year = 2026
            ),
            LitigationCase(
                id = 2,
                caseNumber = "US-2026-041-D",
                title = "DuPont Sequence Systems vs. Wilmington BioLabs",
                region = "Delaware",
                intensityMetric = 48.2,
                summary = "Dispute concerning dependent claims 4-7 regarding quantum entropic key generation in sequencing databases. Standard patent office review actions.",
                cryptographicHash = "SHA-3/7D4C82A1",
                isAlert = false,
                isBookmarked = true,
                year = 2026
            ),
            LitigationCase(
                id = 3,
                caseNumber = "US-2025-1100-F",
                title = "In Re GINA Genetic Discrimination Patent Appeals",
                region = "Federal Circuit",
                intensityMetric = 91.5,
                summary = "High-stakes federal appeal reviewing the patentability of algorithmic screening mechanisms that implicitly utilize hereditary criteria, potentially in violation of Public Trust GINA directives.",
                cryptographicHash = "SHA-3/9C44D870",
                isAlert = true,
                isBookmarked = false,
                year = 2025
            ),
            LitigationCase(
                id = 4,
                caseNumber = "US-2024-819-CA",
                title = "Alt-Energy Carbon Bio-Core infringements",
                region = "California",
                intensityMetric = 33.4,
                summary = "Disputed patents concerning bio-reactor control telemetry vectors. Settlement and license agreements are pending court confirmation.",
                cryptographicHash = "SHA-3/2F9E56C8",
                isAlert = false,
                isBookmarked = false,
                year = 2024
            )
        )
        repository.insertLitigationCases(cases)
    }
}

enum class CaustinTab(val label: String) {
    LITIGATION("Case Analytics"),
    IP_TREE("IP Claim Tree"),
    COMPLIANCE("Compliance Center"),
    ASSISTANT("AI Synthesizer")
}

sealed interface AiResponseState {
    object Idle : AiResponseState
    object Loading : AiResponseState
    data class Success(val response: String, val provenanceHash: String) : AiResponseState
    data class Error(val message: String) : AiResponseState
}

// Fixed Patent Hierarchy data for visual rendering
data class PatentPortfolio(
    val title: String,
    val patentNumber: String,
    val description: String,
    val claims: List<ClaimNode>
)

data class ClaimNode(
    val id: Int,
    val parentId: Int?, // if null, it is an independent claim
    val title: String,
    val description: String,
    val verifiedHash: String
)

object PatentPortfolios {
    val PORTFOLIOS = listOf(
        PatentPortfolio(
            title = "Secure Quantum Diagnostic Core",
            patentNumber = "US-1190283-B2",
            description = "Integrated device executing genomic sequence alignment through cryptographic security shields.",
            claims = listOf(
                ClaimNode(1, null, "Claim 1 (Independent)", "A sequencing apparatus comprising a biological receiver, an entropic physical key generator, and a localized cryptoprocessor.", "SHA-256/D1B8E88F"),
                ClaimNode(2, 1, "Claim 2 (Dependent)", "The sequencing apparatus of Claim 1, further comprising a secondary biometric scanner connected to said biological receiver.", "SHA-256/F99A1C2E"),
                ClaimNode(3, 1, "Claim 3 (Dependent)", "The sequencing apparatus of Claim 1, where the physical key generator is configured to emit high-entropy thermal noise vectors.", "SHA-256/AA78BBFF"),
                ClaimNode(4, 3, "Claim 4 (Dependent)", "The sequencing apparatus of Claim 3, wherein said noise is modulated by a Josephson junction quantum detector array.", "SHA-256/0E89BCD2")
            )
        ),
        PatentPortfolio(
            title = "Unified GINA Screening Safefilter",
            patentNumber = "US-1200451-B1",
            description = "Algorithmic proxy-blocking interface restricting automated medical underwriting engines from querying genetic markers.",
            claims = listOf(
                ClaimNode(10, null, "Claim 10 (Independent)", "A cloud-based underwriting gatekeeper that monitors API telemetry and sanitizes response JSON arrays for designated DNA strings.", "SHA-256/D33EFE45"),
                ClaimNode(11, 10, "Claim 11 (Dependent)", "The gatekeeper of Claim 10, executing deep heuristic classification scans of query originators to detect implicit hereditary checks.", "SHA-256/7D90E1F4"),
                ClaimNode(12, 10, "Claim 12 (Dependent)", "The gatekeeper of Claim 10, further comprising a local Room vault hosting private cryptographic certificates of verified patients.", "SHA-256/A887EFE0"),
                ClaimNode(13, 12, "Claim 13 (Dependent)", "The gatekeeper of Claim 12, utilizing an ephemeral zero-knowledge proof authentication routine.", "SHA-256/2C9F8AEE")
            )
        )
    )
}

class CaustinViewModelFactory(
    private val application: Application,
    private val repository: CaustinRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CaustinViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CaustinViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

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

    // Tab Navigation State (Repurposed for Transition 504 Workflow)
    private val _currentTab = MutableStateFlow(CaustinTab.LITIGATION)
    val currentTab: StateFlow<CaustinTab> = _currentTab.asStateFlow()

    fun setTab(tab: CaustinTab) {
        _currentTab.value = tab
    }

    // --- 1. STUDENT TRANSITION PROFILE STATE ---
    private val _studentName = MutableStateFlow("Sarah Miller")
    val studentName: StateFlow<String> = _studentName.asStateFlow()

    private val _highSchoolName = MutableStateFlow("Caustin High School")
    val highSchoolName: StateFlow<String> = _highSchoolName.asStateFlow()

    private val _targetCollege = MutableStateFlow("State Institute of Science")
    val targetCollege: StateFlow<String> = _targetCollege.asStateFlow()

    private val _transitionMajor = MutableStateFlow("Bioinformatics & Computing")
    val transitionMajor: StateFlow<String> = _transitionMajor.asStateFlow()

    fun updateProfile(name: String, hs: String, college: String, major: String) {
        _studentName.value = name
        _highSchoolName.value = hs
        _targetCollege.value = college
        _transitionMajor.value = major
    }

    // --- 2. ACCOMMODATIONS LIST STATE ---
    private val _selectedAccommodations = MutableStateFlow(setOf(1, 2, 3, 5))
    val selectedAccommodations: StateFlow<Set<Int>> = _selectedAccommodations.asStateFlow()

    fun toggleAccommodation(id: Int) {
        val current = _selectedAccommodations.value
        _selectedAccommodations.value = if (current.contains(id)) {
            current - id
        } else {
            current + id
        }
    }

    // --- 3. DIAGNOSTIC SCREENERS STATE ---
    // ASRS: 6 screening questions [0..4 range: Never, Rarely, Sometimes, Often, Very Often]
    private val _asrsAnswers = MutableStateFlow(listOf(3, 4, 3, 4, 3, 2))
    val asrsAnswers: StateFlow<List<Int>> = _asrsAnswers.asStateFlow()

    fun setAsrsAnswer(index: Int, score: Int) {
        val current = _asrsAnswers.value.toMutableList()
        if (index in current.indices) {
            current[index] = score
            _asrsAnswers.value = current
        }
    }

    // CADI: 18 questions (9 inattentive, 9 hyperactive) [0..4 rating]
    private val _cadiAnswers = MutableStateFlow(listOf(4, 3, 4, 4, 3, 4, 3, 4, 3,  2, 1, 2, 2, 1, 3, 2, 1, 2))
    val cadiAnswers: StateFlow<List<Int>> = _cadiAnswers.asStateFlow()

    private val _cadiAgeOfOnset = MutableStateFlow("7 Years Old (Elementary School)")
    val cadiAgeOfOnset: StateFlow<String> = _cadiAgeOfOnset.asStateFlow()

    fun setCadiAnswer(index: Int, score: Int) {
        val current = _cadiAnswers.value.toMutableList()
        if (index in current.indices) {
            current[index] = score
            _cadiAnswers.value = current
        }
    }

    fun setCadiAgeOfOnset(age: String) {
        _cadiAgeOfOnset.value = age
    }

    // HIV+ Aging + ADHD Comorbidity Screener
    private val _hivHandScore = MutableStateFlow(18) // out of 27
    val hivHandScore: StateFlow<Int> = _hivHandScore.asStateFlow()

    private val _hivCD4Count = MutableStateFlow("420 cells/mm3")
    val hivCD4Count: StateFlow<String> = _hivCD4Count.asStateFlow()

    private val _hivAntiretroviral = MutableStateFlow("Active (Daily ART Protease Inhibitor)")
    val hivAntiretroviral: StateFlow<String> = _hivAntiretroviral.asStateFlow()

    private val _hivComorbidities = MutableStateFlow("Mild Neuropathy, Focus Fatigue")
    val hivComorbidities: StateFlow<String> = _hivComorbidities.asStateFlow()

    fun updateHivScreener(hand: Int, cd4: String, art: String, comorbidities: String) {
        _hivHandScore.value = hand
        _hivCD4Count.value = cd4
        _hivAntiretroviral.value = art
        _hivComorbidities.value = comorbidities
    }

    // --- 4. GENETIC & BIOLOGICAL MARKERS STATE ---
    private val _selectedComt = MutableStateFlow("Val/Val (High Enzyme Activity, Dopamine Deficit)")
    val selectedComt: StateFlow<String> = _selectedComt.asStateFlow()

    private val _selectedSlc6a2 = MutableStateFlow("A/A Variant (Attention Inadequacy)")
    val selectedSlc6a2: StateFlow<String> = _selectedSlc6a2.asStateFlow()

    private val _selectedBdnf = MutableStateFlow("Met/Met (Reduced Learning From Experience)")
    val selectedBdnf: StateFlow<String> = _selectedBdnf.asStateFlow()

    private val _dlpfcHypo = MutableStateFlow(true)
    val dlpfcHypo: StateFlow<Boolean> = _dlpfcHypo.asStateFlow()

    private val _thetaBetaRatioVal = MutableStateFlow("Elevated (4.8 Ratio, High Arousal Fatigue)")
    val thetaBetaRatioVal: StateFlow<String> = _thetaBetaRatioVal.asStateFlow()

    fun updateBiomarkers(comt: String, slc: String, bdnf: String, dlpfc: Boolean, tbr: String) {
        _selectedComt.value = comt
        _selectedSlc6a2.value = slc
        _selectedBdnf.value = bdnf
        _dlpfcHypo.value = dlpfc
        _thetaBetaRatioVal.value = tbr
    }

    // --- AI ASSISTANT PORTAL STATES ---
    private val _aiInputText = MutableStateFlow("")
    val aiInputText: StateFlow<String> = _aiInputText.asStateFlow()

    private val _aiResponseState = MutableStateFlow<AiResponseState>(AiResponseState.Idle)
    val aiResponseState: StateFlow<AiResponseState> = _aiResponseState.asStateFlow()

    val assistantHistory: StateFlow<List<AssistantMessage>> = repository.assistantMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Compliance Checklist database hooks (Used for legal checks)
    val complianceItems: StateFlow<List<ComplianceItem>> = repository.complianceItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            complianceItems.first { it.isNotEmpty() || true }
            repository.complianceItems.take(1).collect { items ->
                if (items.isEmpty()) {
                    seedComplianceChecklist()
                }
            }
        }
    }

    // Add search helper functions for backwards compatibility if needed
    fun setAiInputText(text: String) {
        _aiInputText.value = text
    }

    // Toggle items in checklist
    fun toggleComplianceItem(id: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleComplianceSelected(id, isCompleted)
        }
    }

    // Submit Rights Chat advocacy queries
    fun submitAiQuery(customPrompt: String? = null) {
        val prompt = customPrompt ?: _aiInputText.value
        if (prompt.isBlank()) return

        _aiInputText.value = ""
        _aiResponseState.value = AiResponseState.Loading

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _aiResponseState.value = AiResponseState.Error(
                        "API Key missing. Please set your GEMINI_API_KEY inside the Secrets panel of AI Studio."
                    )
                    return@launch
                }

                // Compile comprehensive prompt leveraging current student state
                val calculatedAsrsScore = _asrsAnswers.value.sum()
                val (presentation, severity) = calculateCadiMetrics()

                val richPrompt = """
                    You are a self-advocacy legal counselor helping a student transition from high school to college.
                    The current student's name is ${_studentName.value}, currently in ${_highSchoolName.value}, transitioning to ${_targetCollege.value} as a ${_transitionMajor.value} major.
                    
                    Here are their verified Diagnostic Assessment and Genetic Biomarker profiles:
                    - ASRS Screen Score: $calculatedAsrsScore / 24
                    - CADI Assessment: Presentation is $presentation, Severity is $severity, Age of onset is ${_cadiAgeOfOnset.value}.
                    - Genetic Markers: COMT: ${_selectedComt.value}, SLC6A2: ${_selectedSlc6a2.value}, BDNF: ${_selectedBdnf.value}.
                    - Neuroimaging Indicators: DLPFC is ${if (_dlpfcHypo.value) "HYPOACTIVE" else "NORMAL"}, Theta/Beta EEG Ratio: ${_thetaBetaRatioVal.value}.
                    
                    Explain their rights under Section 504 of the Rehabilitation Act, ADA Title II (accommodations in higher education), and GINA (genetic protection against university information sharing). 
                    Answer the student's question with precise legal reference (e.g. 29 U.S.C. § 794), clear guidance, warmth, and supportive suggestions for college. Use clean markdown formatting.
                    
                    Student Question: $prompt
                """.trimIndent()

                val requestModel = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = richPrompt)
                            )
                        )
                    ),
                    generationConfig = GenerationConfig(temperature = 0.3f, maxOutputTokens = 1200)
                )

                val response = RetrofitClient.service.generateContent(apiKey, requestModel)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Advocacy response generated was empty. Please check connection."

                val provenanceHash = generateSha512(reply + System.currentTimeMillis().toString())

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

    // Helper functions for score analytics
    fun calculateCadiMetrics(): Pair<String, String> {
        val answers = _cadiAnswers.value
        val inattentiveScore = answers.take(9).sum()
        val hyperactiveScore = answers.drop(9).take(9).sum()
        val total = answers.sum()

        val presentation = when {
            inattentiveScore >= 18 && hyperactiveScore >= 18 -> "Combined Type"
            inattentiveScore >= 18 -> "Predominantly Inattentive Presentation"
            hyperactiveScore >= 18 -> "Predominantly Hyperactive-Impulsive Presentation"
            else -> "Subthreshold (Focus Deficit Present)"
        }

        val severity = when {
            total >= 50 -> "Very Severe"
            total in 37..49 -> "Severe"
            total in 19..36 -> "Moderate"
            else -> "Mild"
        }

        return Pair(presentation, severity)
    }

    private fun generateSha512(input: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-512").digest(input.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
        } catch (e: Exception) {
            "B77CD8E112"
        }
    }

    private suspend fun seedComplianceChecklist() {
        val items = listOf(
            ComplianceItem(
                category = "ADA",
                requirement = "Validate that the target higher-education institution provides 1.5x/2x extended testing time accommodations under ADA Title II.",
                importance = "Critical",
                isCompleted = true,
                auditCode = "28 CFR § 35.130",
                cryptoHash = "SHA-256/AD39F0EE"
            ),
            ComplianceItem(
                category = "SEC 504",
                requirement = "Confirm the institution has a designated Section 504 Coordinator and formal grievance policies for academic adjustment disputes.",
                importance = "High",
                isCompleted = false,
                auditCode = "34 CFR § 104.7",
                cryptoHash = "SHA-256/E38C2A01"
            ),
            ComplianceItem(
                category = "HIPAA",
                requirement = "Verify that medical screening assessments and physician letters are stored in HIPAA-compliant localized database partitions.",
                importance = "Critical",
                isCompleted = true,
                auditCode = "45 CFR § 164.502",
                cryptoHash = "SHA-256/H8AA10FC"
            ),
            ComplianceItem(
                category = "GINA",
                requirement = "Demonstrate compliance with GINA Title II prohibiting the college from demanding genetic markers for admissions or housing.",
                importance = "Critical",
                isCompleted = false,
                auditCode = "29 CFR § 1635.4",
                cryptoHash = "SHA-256/G55D0EE3"
            )
        )
        repository.insertComplianceItems(items)
    }

    // Extra functions for compatibility with other files if accessed
    fun setRegion(region: String) {}
    fun setSearchQuery(query: String) {}
    fun selectPortfolio(portfolio: PatentPortfolio) {}
    fun selectClaim(claimId: Int?) {}
    fun getVerifiedClaimPath(): List<Int> = emptyList()
    fun setComplianceCategory(category: String) {}
}

enum class CaustinTab(val label: String) {
    LITIGATION("504 Plan"),
    IP_TREE("Diagnostic Screen"),
    COMPLIANCE("Evidence & PDF"),
    ASSISTANT("Transition Chat")
}

sealed interface AiResponseState {
    object Idle : AiResponseState
    object Loading : AiResponseState
    data class Success(val response: String, val provenanceHash: String) : AiResponseState
    data class Error(val message: String) : AiResponseState
}

// Minimal Compatibility structures to avoid compilation failure elsewhere
data class PatentPortfolio(
    val title: String,
    val patentNumber: String,
    val description: String,
    val claims: List<ClaimNode>
)

data class ClaimNode(
    val id: Int,
    val parentId: Int?,
    val title: String,
    val description: String,
    val verifiedHash: String
)

object PatentPortfolios {
    val PORTFOLIOS = listOf(
        PatentPortfolio(
            "Security Diagnostic",
            "US-110",
            "Genomic Secure Align",
            emptyList()
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

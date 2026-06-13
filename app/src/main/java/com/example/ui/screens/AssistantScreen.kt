package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AssistantMessage
import com.example.network.ArxivEntry
import com.example.network.PubMedEntry
import com.example.ui.theme.*
import com.example.ui.viewmodel.AiResponseState
import com.example.ui.viewmodel.CaustinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    viewModel: CaustinViewModel,
    modifier: Modifier = Modifier
) {
    val aiInputText by viewModel.aiInputText.collectAsState()
    val aiResponseState by viewModel.aiResponseState.collectAsState()
    val assistantHistory by viewModel.assistantHistory.collectAsState()

    // PubMed states
    val pubMedQuery by viewModel.pubMedQuery.collectAsState()
    val pubMedResults by viewModel.pubMedResults.collectAsState()
    val pubMedLoading by viewModel.pubMedLoading.collectAsState()
    val pubMedError by viewModel.pubMedError.collectAsState()

    // arXiv states
    val arxivQuery by viewModel.arxivQuery.collectAsState()
    val arxivResults by viewModel.arxivResults.collectAsState()
    val arxivLoading by viewModel.arxivLoading.collectAsState()
    val arxivError by viewModel.arxivError.collectAsState()

    var activeMode by remember { mutableStateOf("ai") } // "ai" or "academic"
    var academicSubMode by remember { mutableStateOf("pubmed") } // "pubmed" or "arxiv"
    
    var customPubMedInput by remember { mutableStateOf("") }
    var customArxivInput by remember { mutableStateOf("") }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        // Institutional Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LEGISLATIVE SYNTHESIZER ENGINE",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "RESEARCH INTEGRATION HUB",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Top Navigation Tabs for AI and Academic Mode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (activeMode == "ai") MaterialTheme.colorScheme.primary else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { activeMode = "ai" }
                    .padding(vertical = 8.dp)
                    .testTag("mode_selector_ai"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (activeMode == "ai") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI CO-SYNTHESIZER",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (activeMode == "ai") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (activeMode == "academic") MaterialTheme.colorScheme.primary else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { activeMode = "academic" }
                    .padding(vertical = 8.dp)
                    .testTag("mode_selector_academic"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = if (activeMode == "academic") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACADEMIC PORTAL",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (activeMode == "academic") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (activeMode == "ai") {
            // ================== AI CHAT MODE INTERFACE ==================
            // Suggestion templates carousel
            if (aiResponseState is AiResponseState.Idle && assistantHistory.isEmpty()) {
                Text(
                    text = "SELECT AN AUDIT QUICK TEMPLATE TO BEGIN SYNTHESIS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val suggestedPrompts = listOf(
                    "Summarize GINA Title II genetic restrictions for health databases",
                    "Explain how 21 CFR Part 11 applies to diagnostic claim alignment",
                    "Evaluate legal liability of Maryland IDEA clinical disputes",
                    "Trace independent Claim 1 dependencies within Patent US-1190283"
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestedPrompts) { promptText ->
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .height(110.dp)
                                .clickable {
                                    viewModel.setAiInputText(promptText)
                                    viewModel.submitAiQuery(promptText)
                                }
                                .testTag("suggested_prompt_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Quick idea",
                                    tint = AmberAccent2,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = promptText,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3,
                                    lineHeight = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Subtitle indicator
            if (assistantHistory.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VERIFIED RESEARCH HISTORY LOGS (${assistantHistory.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Purge Archives",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberAccent2,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.clearHistory() }
                            .testTag("purge_history_btn")
                    )
                }
            }

            // Conversation history logs
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Processing status loader
                if (aiResponseState is AiResponseState.Loading) {
                    item {
                        LoadingSkeletonCard()
                    }
                }

                // Error display card
                if (aiResponseState is AiResponseState.Error) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_error_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error notification",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "SYNTHESIS HALTED",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = (aiResponseState as AiResponseState.Error).message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Current successfully processed answer if active
                if (aiResponseState is AiResponseState.Success) {
                    val successState = aiResponseState as AiResponseState.Success
                    item {
                        CurrentSynthesizedCard(
                            query = "Current Query",
                            reply = successState.response,
                            provenanceHash = successState.provenanceHash
                        )
                    }
                }

                // Historic records from database
                items(assistantHistory, key = { it.id }) { message ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_msg_card_${message.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = "Archive",
                                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "REGULATORY ARCHIVE ID #${message.id}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = dateFormatter.format(Date(message.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // User Inquiry
                            Text(
                                text = "Inquiry: " + message.queryText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // AI Response
                            Text(
                                text = message.replyText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Provenance Hash anchor
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = TealAccent1.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = TealAccent1,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SECURE VERIFICATION: " + message.provenanceHash,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TealAccent1,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Static Bottom Input Row
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = aiInputText,
                        onValueChange = { viewModel.setAiInputText(it) },
                        placeholder = { Text("Ask regulatory assistant...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_assistant_input_field"),
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    IconButton(
                        onClick = { viewModel.submitAiQuery() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = NavyPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("ai_assistant_submit_btn"),
                        enabled = aiInputText.isNotBlank() && aiResponseState !is AiResponseState.Loading
                    ) {
                        if (aiResponseState is AiResponseState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "Submit query button")
                        }
                    }
                }
            }
        } else {
            // ================== ACADEMIC LITERATURE EXPLORATION GATEWAY ==================
            // Sub Tabs for PubMed vs arXiv
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { academicSubMode = "pubmed" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (academicSubMode == "pubmed") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (academicSubMode == "pubmed") MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NIH PubMed", style = MaterialTheme.typography.labelLarge)
                }
                
                Button(
                    onClick = { academicSubMode = "arxiv" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (academicSubMode == "arxiv") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (academicSubMode == "arxiv") MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("arXiv Tools", style = MaterialTheme.typography.labelLarge)
                }
            }

            // Real search inputs & information layouts
            if (academicSubMode == "pubmed") {
                // NIH PubMed Research Portal
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "NIH PUBMED RESEARCH PORTAL (GWAS)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealAccent1,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Execute direct Entrez API searches for clinical biomarkers, sequencing, and genetic indicators.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customPubMedInput,
                                onValueChange = { customPubMedInput = it },
                                placeholder = { Text("e.g. gwas ADHD") },
                                modifier = Modifier.weight(1f).height(50.dp).testTag("pubmed_search_input"),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true,
                                trailingIcon = {
                                    if (customPubMedInput.isNotEmpty()) {
                                        IconButton(onClick = { customPubMedInput = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear text", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            )
                            Button(
                                onClick = {
                                    val queryText = customPubMedInput.ifBlank { "gwas" }
                                    viewModel.searchPubMedRecords(queryText)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(50.dp).testTag("pubmed_search_btn")
                            ) {
                                Text("Search")
                            }
                        }
                    }
                }

                // Show active results of PubMed
                if (pubMedLoading) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = TealAccent1)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Quering NCBI Entrez database...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                } else if (pubMedError != null) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(text = pubMedError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Text(
                        text = "NIH PUBLIC SEARCH RESULTS (Query: \"$pubMedQuery\")",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(pubMedResults) { entry ->
                            PubMedResultCard(entry = entry) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(entry.url))
                                context.startActivity(intent)
                            }
                        }
                    }
                }

            } else {
                // arXiv Automated Tool Discovery
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ARXIV AUTOMATED SOFTWARE DISCOVERY",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberAccent2,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scans arXiv papers mentioning genetics & risk scoring to isolate permissive open licenses (MIT, Creative Commons) for immediate clinical deployment.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customArxivInput,
                                onValueChange = { customArxivInput = it },
                                placeholder = { Text("e.g. genetics 'risk scoring'") },
                                modifier = Modifier.weight(1f).height(50.dp).testTag("arxiv_search_input"),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true,
                                trailingIcon = {
                                    if (customArxivInput.isNotEmpty()) {
                                        IconButton(onClick = { customArxivInput = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear text", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            )
                            Button(
                                onClick = {
                                    val queryText = customArxivInput.ifBlank { "genetics \"risk scoring\" mit" }
                                    viewModel.searchArxivRecords(queryText)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(50.dp).testTag("arxiv_search_btn")
                            ) {
                                Text("Scan")
                            }
                        }
                    }
                }

                // Show active results of arXiv
                if (arxivLoading) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AmberAccent2)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Mapping open patents and libraries from export.arxiv.org...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                } else if (arxivError != null) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(text = arxivError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Text(
                        text = "DISCOVERED GENETIC TOOLS (${arxivResults.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(arxivResults) { paper ->
                            ArxivResultCard(paper = paper) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paper.url))
                                context.startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PubMedResultCard(
    entry: PubMedEntry,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("pubmed_card_entry"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = TealAccent1.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "PubMed ID: " + entry.id,
                        style = MaterialTheme.typography.labelSmall,
                        color = TealAccent1,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = entry.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Journal: " + entry.journal,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Button(
                onClick = onOpen,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().height(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open NCBI Entrez Archive", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun ArxivResultCard(
    paper: ArxivEntry,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("arxiv_card_entry"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    paper.licensesMentioned.forEach { lic ->
                        Surface(
                            color = if (lic == "MIT License") AmberAccent2.copy(alpha = 0.1f) else TealAccent1.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = lic,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (lic == "MIT License") AmberAccent2 else TealAccent1,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = paper.published,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = paper.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Authors: " + paper.authors,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = paper.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onOpen,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().height(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.DownloadForOffline, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Access Scientific PDF Paper", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun LoadingSkeletonCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("skeleton_loader_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    color = TealAccent1,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "RETRIEVING LEGISLATIVE BILL ARCHIVES & CFR STATUTES...",
                    style = MaterialTheme.typography.labelSmall,
                    color = TealAccent1,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun CurrentSynthesizedCard(
    query: String,
    reply: String,
    provenanceHash: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("current_synthesized_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, TealAccent1),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success Verification",
                    tint = TealAccent1,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LEGISLATION SYNTHESIS COMPLETED",
                    style = MaterialTheme.typography.labelSmall,
                    color = TealAccent1,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = reply,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = NavyPrimary.copy(alpha = 0.05f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "SECURE PROVENANCE RECORD CO-PROOF",
                        style = MaterialTheme.typography.labelSmall,
                        color = NavyPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = provenanceHash,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

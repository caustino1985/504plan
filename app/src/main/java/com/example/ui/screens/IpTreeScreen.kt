package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.CaustinViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun IpTreeScreen(
    viewModel: CaustinViewModel,
    modifier: Modifier = Modifier
) {
    val asrsAnswers by viewModel.asrsAnswers.collectAsState()
    val cadiAnswers by viewModel.cadiAnswers.collectAsState()
    val onsetAge by viewModel.cadiAgeOfOnset.collectAsState()
    
    val hivHandScore by viewModel.hivHandScore.collectAsState()
    val hivCD4 by viewModel.hivCD4Count.collectAsState()
    val hivArt by viewModel.hivAntiretroviral.collectAsState()
    val hivComorbidities by viewModel.hivComorbidities.collectAsState()

    var activeSubMode by remember { mutableStateOf("asrs") } // "asrs", "cadi", "hiv"

    val asrsQuestions = remember {
        listOf(
            "1. How often do you have trouble wrapping up final details of a project or assignment?",
            "2. How often do you have difficulty getting things in order when you have to perform a task that requires organization?",
            "3. How often do you have problems remembering appointments or obligations?",
            "4. When you have a task that requires a lot of thought, how often do you avoid or delay getting started?",
            "5. How often do you fidget or squirm with your hands or feet when you have to sit down for a long time?",
            "6. How often do you feel overly active and compelled to do things, as if you were driven by a motor?"
        )
    }

    val cadiQuestions = remember {
        listOf(
            // Inattentive (9 items)
            "C1. Suffer from wandering focus during prolonged scientific reading sets?",
            "C2. Miss fine critical rules during high-density lab assignments?",
            "C3. Struggle to maintain attention in 80+ minute lectures/colloquiums?",
            "C4. Experience chronic blockades organizing multi-step computing project files?",
            "C5. Misplace notebooks, reference standards, or hardware components?",
            "C6. Highly sensitive / easily distracted by background visual movements?",
            "C7. Delay initiation of lengthy research analysis reports?",
            "C8. Forget daily academic obligations/liaison checkpoint cards?",
            "C9. Fail to follow instructional paths sequentially without drifting?",
            // Hyperactive/Impulsive (9 items)
            "C10. Find yourself fidgeting or tapping keyboard keys excessively?",
            "C11. Experience inner restlessness or severe mental tension?",
            "C12. Leave your seat during long exams or assembly briefings?",
            "C13. Talk excessively or blurt out answers before problems finish?",
            "C14. Struggle with patience when waiting in queues or loops?",
            "C15. Constantly interrupt fellow student peer discussions?",
            "C16. Act on immediate impulse without mapping strategic failures?",
            "C17. Have extreme difficulties engaging quietly in individual studies?",
            "C18. Find yourself constantly 'on the go' as if motor-driven?"
        )
    }

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
                text = "CLINICAL DIAGNOSTIC SCREENERS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "NEURO-COGNITIVE SCREENING",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Sub Navigation Tabs
        TabRow(
            selectedTabIndex = when (activeSubMode) {
                "asrs" -> 0
                "cadi" -> 1
                else -> 2
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = activeSubMode == "asrs",
                onClick = { activeSubMode = "asrs" },
                text = { Text("ASRS Scale") },
                modifier = Modifier.testTag("tab_asrs")
            )
            Tab(
                selected = activeSubMode == "cadi",
                onClick = { activeSubMode = "cadi" },
                text = { Text("CADI Assessment") },
                modifier = Modifier.testTag("tab_cadi")
            )
            Tab(
                selected = activeSubMode == "hiv",
                onClick = { activeSubMode = "hiv" },
                text = { Text("HIV+ Aging / HAND") },
                modifier = Modifier.testTag("tab_hiv_screener")
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ASRS Mode Screen Render
            if (activeSubMode == "asrs") {
                item {
                    val score = asrsAnswers.sum()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Adult ADHD Self-Report Scale (ASRS v1.1)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Six standardized screening questions calibrated to track executive function and attentional consistency. Scores >= 14 indicate highly diagnostic levels of ADHD impairment.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = score / 24f,
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = if (score >= 14) Color.Red else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Rating: $score / 24",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (score >= 14) "POSITIVE RISK FLAG (Severe)" else "Borderline Attention Deficits",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (score >= 14) Color.Red else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                itemsIndexed(asrsQuestions) { index, question ->
                    val value = asrsAnswers.getOrElse(index) { 2 }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = question,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val ratings = listOf("Never", "Rarely", "Sometime", "Often", "VeryOf")
                                ratings.forEachIndexed { rIndex, rLabel ->
                                    val rSelected = value == rIndex
                                    FilterChip(
                                        selected = rSelected,
                                        onClick = { viewModel.setAsrsAnswer(index, rIndex) },
                                        label = { Text(rLabel, fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("asrs_${index}_chip_${rIndex}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CADI Mode Screen Render
            if (activeSubMode == "cadi") {
                item {
                    val (presentation, severity) = viewModel.calculateCadiMetrics()
                    val totalScore = cadiAnswers.sum()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "CADI Comprehensive Diagnostic Interview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reported Age of Symptom Onset: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                OutlinedTextField(
                                    value = onsetAge,
                                    onValueChange = { viewModel.setCadiAgeOfOnset(it) },
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("input_cadi_onset"),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Total Rating Score: $totalScore / 72", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "ADHD Type: $presentation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Badge(containerColor = MaterialTheme.colorScheme.onTertiaryContainer) {
                                    Text(
                                        text = severity,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(6.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                itemsIndexed(cadiQuestions) { index, question ->
                    val value = cadiAnswers.getOrElse(index) { 3 }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = question,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Rating Frequency: $value",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Slider(
                                    value = value.toFloat(),
                                    onValueChange = { viewModel.setCadiAnswer(index, it.toInt()) },
                                    valueRange = 0f..4f,
                                    steps = 3,
                                    modifier = Modifier.weight(1f).testTag("cadi_slider_$index")
                                )
                            }
                        }
                    }
                }
            }

            // HIV+ Aging Mode Screen Render
            if (activeSubMode == "hiv") {
                item {
                    var editingHand by remember { mutableStateOf(hivHandScore.toString()) }
                    var editingCd4 by remember { mutableStateOf(hivCD4) }
                    var editingArt by remember { mutableStateOf(hivArt) }
                    var editingComorb by remember { mutableStateOf(hivComorbidities) }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "HIV+ Aging & Neurocognitive Comorbidity Screener",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tracks overlapping clinical indicators of focus drops, HAND (HIV-associated Neurocognitive Disorders), medication routines, and CD4 boundaries.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = editingHand,
                                onValueChange = { 
                                    editingHand = it
                                    it.toIntOrNull()?.let { score ->
                                        viewModel.updateHivScreener(score, editingCd4, editingArt, editingComorb)
                                    }
                                },
                                label = { Text("HAND Cognitive Slowing Score (0-27)") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("input_hiv_hand"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = editingCd4,
                                onValueChange = { 
                                    editingCd4 = it
                                    viewModel.updateHivScreener(hivHandScore, it, editingArt, editingComorb)
                                },
                                label = { Text("CD4 T-Cell Count (lab-verified)") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("input_hiv_cd4"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = editingArt,
                                onValueChange = { 
                                    editingArt = it
                                    viewModel.updateHivScreener(hivHandScore, editingCd4, it, editingComorb)
                                },
                                label = { Text("Antiretroviral Routine (ART status)") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("input_hiv_art"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = editingComorb,
                                onValueChange = { 
                                    editingComorb = it
                                    viewModel.updateHivScreener(hivHandScore, editingCd4, editingArt, it)
                                },
                                label = { Text("Secondary Cognitive Comorbidities") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("input_hiv_comorb"),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    viewModel.updateHivScreener(
                                        editingHand.toIntOrNull() ?: 18,
                                        editingCd4,
                                        editingArt,
                                        editingComorb
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().testTag("save_hiv_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("VERIFY & COMMIT COMORBIDITY RATINGS")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

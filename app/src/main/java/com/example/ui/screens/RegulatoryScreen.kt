package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ComplianceItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.CaustinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegulatoryScreen(
    viewModel: CaustinViewModel,
    modifier: Modifier = Modifier
) {
    val complianceItems by viewModel.complianceItems.collectAsState()
    val selectedComplianceCategory by viewModel.selectedComplianceCategory.collectAsState()

    // Dynamically calculate progress
    val (completedCount, totalCount, compliancePercentage) = remember(complianceItems) {
        if (complianceItems.isEmpty()) {
            Triple(0, 0, 0)
        } else {
            val comp = complianceItems.count { it.isCompleted }
            val tot = complianceItems.size
            val perc = (comp.toFloat() / tot * 100).toInt()
            Triple(comp, tot, perc)
        }
    }

    // Filter items
    val filteredItems = remember(complianceItems, selectedComplianceCategory) {
        complianceItems.filter { item ->
            selectedComplianceCategory == "All" || item.category.equals(selectedComplianceCategory, ignoreCase = true)
        }
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
                text = "REGULATORY COMPLIANCE CENTER",
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
                text = "STANDARDS AUDITING",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Live Compliance Progress Wheel and Analytics Panel
            item {
                ComplianceStatsPanel(
                    completedCount = completedCount,
                    totalCount = totalCount,
                    percentage = compliancePercentage
                )
            }

            // Category Filter Tab Chips
            item {
                Column {
                    Text(
                        text = "FILTER COMPLIANCE BY STATUTE FAMILY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    val categories = listOf("All", "FDA", "HIPAA", "GINA")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedComplianceCategory == category,
                                onClick = { viewModel.setComplianceCategory(category) },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("compliance_family_$category")
                            )
                        }
                    }
                }
            }

            // Requirements Checklist
            item {
                Text(
                    text = "AUDIT STANDARDS REQUIREMENTS (${filteredItems.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Display Checklist cards
            items(filteredItems, key = { it.id }) { item ->
                ComplianceChecklistCard(
                    item = item,
                    onCheckedChange = { isChecked ->
                        viewModel.toggleComplianceItem(item.id, isChecked)
                    }
                )
            }

            // Methodology Transparency Card
            item {
                MethodologyTransparencyCard()
            }
        }
    }
}

// Stats metrics element Composable
@Composable
fun ComplianceStatsPanel(
    completedCount: Int,
    totalCount: Int,
    percentage: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular progress indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(84.dp)
                    .padding(4.dp)
            ) {
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = TealAccent1,
                    strokeWidth = 6.dp,
                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealAccent1
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Explanation
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "OVERALL REGULATORY SHIELD",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Verified Compliance",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your architecture safely satisfies $completedCount of $totalCount required legal parameters across GINA Title I, FDA Part 11, and HIPAA Security rules.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ComplianceChecklistCard(
    item: ComplianceItem,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compliance_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onCheckedChange(it) },
                modifier = Modifier
                    .padding(end = 4.dp)
                    .testTag("compliance_checkbox_${item.id}"),
                colors = CheckboxDefaults.colors(
                    checkedColor = TealAccent1,
                    uncheckedColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (item.category) {
                                "FDA" -> NavyPrimary.copy(alpha = 0.08f)
                                "HIPAA" -> SlateSecondary.copy(alpha = 0.08f)
                                else -> TealAccent1.copy(alpha = 0.08f)
                            },
                        ) {
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (item.category) {
                                    "FDA" -> NavyPrimary
                                    "HIPAA" -> SlateSecondary
                                    else -> TealAccent1
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Statute key code
                        Text(
                            text = item.auditCode,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Importance Tier tag
                    val importanceColor = when (item.importance) {
                        "Critical" -> AmberAccent2
                        "High" -> MaterialTheme.colorScheme.primary
                        else -> SlateSecondary
                    }
                    Text(
                        text = item.importance.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = importanceColor
                    )
                }

                // Core description
                Text(
                    text = item.requirement,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(6.dp))

                // Encrypted Hash details showing forensic tracking
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.cryptoHash,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                        )
                    }

                    Text(
                        text = if (item.isCompleted) "PASSED AUDIT" else "VERIFICATION PENDING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isCompleted) TealAccent1 else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// Methodology Transparency Component
@Composable
fun MethodologyTransparencyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NavyPrimary),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AmberAccent2)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = AmberAccent2,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "METHODOLOGY TRANSPARENCY MANUAL",
                    style = MaterialTheme.typography.labelMedium,
                    color = AmberAccent2,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "We mathematically calculate compliance weight based purely on statutory checklist parameters registered in database tables.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Forensic calculation formula visual markup
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "AUDIT ACCURACY FORMULA",
                        style = MaterialTheme.typography.labelSmall,
                        color = TealAccent1,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "C_ratio = ( ∑ S_checked / S_total ) * 100",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Where S represents active regulated statutory components",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Encrypted key",
                    tint = TealAccent1,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AUDITED SOURCE REGISTRY: HHS / FDA / EEOC CADRE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TealAccent1,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

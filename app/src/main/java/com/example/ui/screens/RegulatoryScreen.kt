package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ComplianceItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.CaustinViewModel
import com.example.util.PdfReportGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegulatoryScreen(
    viewModel: CaustinViewModel,
    modifier: Modifier = Modifier
) {
    val complianceItems by viewModel.complianceItems.collectAsState()
    val context = LocalContext.current

    val comtVal by viewModel.selectedComt.collectAsState()
    val slcVal by viewModel.selectedSlc6a2.collectAsState()
    val bdnfVal by viewModel.selectedBdnf.collectAsState()
    val dlpfcVal by viewModel.dlpfcHypo.collectAsState()
    val tbrVal by viewModel.thetaBetaRatioVal.collectAsState()

    val (completed, total) = remember(complianceItems) {
        Pair(complianceItems.count { it.isCompleted }, complianceItems.size)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Institutional Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MEDICAL-LEGAL EVIDENCE HARMONIZER",
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
                        text = "EXPORT REGULATORY COMPLIANCE PACKET",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Verify the dynamic linkage between medical biomarkers, functional limitations, and security policies before exporting the official Section 504 transition packet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // EXPORT PORTFOLIO HIGH-CONTRAST BUTTON CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("download_report_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXPORT OFFICIAL TRANSITION PLAN PORTFOLIO",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generates a professional-grade, print-ready, 5-page PDF document incorporating high school enrollment details, clinical screener indices, the evidence matrix, and standard signatures. This is the official document for university DSS consideration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            PdfReportGenerator.generateDossierReport(context, viewModel)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("download_report_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = "Download Report")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "COMPILE & GENERATE 5-PAGE TRANSITION PLAN",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // BIOMETRIC AND GENOMIC REGISTER MATRIX SUMMARY
        item {
            Text(
                text = "BIOMETRIC-GENOMIC PROFILE MATRIX",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    BiomarRow("COMT rs4680 Variant", comtVal, Icons.Default.FilterVintage)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    BiomarRow("SLC6A2 Attentional Variant", slcVal, Icons.Default.TrackChanges)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    BiomarRow("BDNF Met/Met Polymorph", bdnfVal, Icons.Default.OnlinePrediction)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    BiomarRow("fMRI DLPFC Bloodflow Activation", if (dlpfcVal) "Hypoactive BOLD profile detected" else "Normal BOLD profile", Icons.Default.CenterFocusStrong)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    BiomarRow("EEG Cortical Theta/Beta Waves", tbrVal, Icons.Default.Timeline)
                }
            }
        }

        // PRIVACY AND STATUTORY POLICY AUDIT CHECKLIST
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HIPAA & GINA CIVIL PRIVACY SAFEGUARDS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "$completed of $total SECURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Checklist cards
        items(complianceItems, key = { it.id }) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("compliance_item_${item.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isCompleted,
                        onCheckedChange = { viewModel.toggleComplianceItem(item.id, it) },
                        modifier = Modifier.testTag("checkbox_compliance_${item.id}")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.auditCode,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Badge(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                                Text(
                                    text = item.importance,
                                    fontSize = 8.sp,
                                    modifier = Modifier.padding(4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.requirement,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Proven Integrity Hash: ${item.cryptoHash}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Light
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BiomarRow(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

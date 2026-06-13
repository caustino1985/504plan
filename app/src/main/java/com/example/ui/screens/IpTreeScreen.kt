package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.CaustinTab
import com.example.ui.viewmodel.ClaimNode
import com.example.ui.viewmodel.CaustinViewModel
import com.example.ui.viewmodel.PatentPortfolios

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpTreeScreen(
    viewModel: CaustinViewModel,
    modifier: Modifier = Modifier
) {
    val selectedPortfolio by viewModel.selectedPortfolio.collectAsState()
    val selectedClaimId by viewModel.selectedClaimId.collectAsState()
    val verifiedPath = viewModel.getVerifiedClaimPath()

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
                text = "INTELLECTUAL PROPERTY ARCHITECTURE",
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
                text = "PATENT CLAIM HIERARCHY",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Portfolio Selection Row / Carousel
        Text(
            text = "SELECT ACTIVE INTELLECTUAL PROPERTY ASSET",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        PatentPortfolios.PORTFOLIOS.forEach { portfolio ->
            val isSelected = selectedPortfolio.patentNumber == portfolio.patentNumber
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { viewModel.selectPortfolio(portfolio) }
                    .testTag("portfolio_select_${portfolio.patentNumber}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(8.dp),
                border = if (isSelected) BorderStroke(1.dp, AmberAccent2) else null,
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = portfolio.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = portfolio.patentNumber,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) TealAccent1 else MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isSelected) TealAccent1 else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Portfolio Description card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = selectedPortfolio.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Title for Tree Visual
        Text(
            text = "INTERACTIVE INHERITANCE FORENSIC PATH",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Lazy List layout displaying nodes beautifully connected by line vectors
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(selectedPortfolio.claims) { claim ->
                val isHighlighted = verifiedPath.contains(claim.id)
                val isDirectSelected = selectedClaimId == claim.id
                val indentVal = getClaimIndentation(claim.id, selectedPortfolio.claims)

                ClaimTreeNodeView(
                    claim = claim,
                    indentation = indentVal,
                    isHighlighted = isHighlighted,
                    isDirectSelected = isDirectSelected,
                    onNodeClick = {
                        if (isDirectSelected) {
                            viewModel.selectClaim(null)
                        } else {
                            viewModel.selectClaim(claim.id)
                        }
                    }
                )
            }

            // Path Details verification panel
            item {
                AnimatedVisibility(visible = selectedClaimId != null) {
                    val activeClaim = selectedPortfolio.claims.find { it.id == selectedClaimId }
                    if (activeClaim != null) {
                        ClaimDetailVerificationPanel(
                            claim = activeClaim,
                            verifiedPathIds = verifiedPath,
                            portfolioClaims = selectedPortfolio.claims,
                            onExploreWithAi = {
                                viewModel.setAiInputText(
                                    "Analyze patent Claim structure: ${activeClaim.title} within portfolio '${selectedPortfolio.title}'. Explain dependency risks, scope coverage, and relevant diagnostic/GINA statutes."
                                )
                                viewModel.setTab(CaustinTab.ASSISTANT)
                                viewModel.submitAiQuery()
                            }
                        )
                    }
                }
            }
        }
    }
}

// Draw claims and connections
@Composable
fun ClaimTreeNodeView(
    claim: ClaimNode,
    indentation: Int,
    isHighlighted: Boolean,
    isDirectSelected: Boolean,
    onNodeClick: () -> Unit
) {
    val borderColorAnimate by animateColorAsState(
        targetValue = if (isDirectSelected) AmberAccent2 else if (isHighlighted) TealAccent1 else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
        animationSpec = tween(300)
    )

    val backgroundAnimate by animateColorAsState(
        targetValue = if (isDirectSelected) AmberAccent2.copy(alpha = 0.12f) else if (isHighlighted) TealAccent1.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Render branching curves in a custom vertical Canvas
        if (indentation > 0) {
            Canvas(
                modifier = Modifier
                    .width((indentation * 18).dp)
                    .height(48.dp)
            ) {
                val stepWidth = 18.dp.toPx()
                val totalWidth = size.width
                val midHeight = size.height / 2

                // Vertical anchor line
                drawLine(
                    color = if (isHighlighted) TealAccent1 else Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(stepWidth / 2, 0f),
                    end = Offset(stepWidth / 2, size.height),
                    strokeWidth = 2.dp.toPx()
                )

                // Branch line to specific card indentation
                drawLine(
                    color = if (isHighlighted) TealAccent1 else Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(stepWidth / 2, midHeight),
                    end = Offset(totalWidth, midHeight),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Claim Card Block
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onNodeClick() }
                .testTag("claim_node_${claim.id}"),
            colors = CardDefaults.cardColors(containerColor = backgroundAnimate),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, borderColorAnimate),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDirectSelected) 2.dp else 0.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = claim.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDirectSelected) AmberAccent2 else MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (claim.parentId == null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = "INDEPENDENT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Reference claim ${claim.parentId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = claim.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Verified path detail presentation card
@Composable
fun ClaimDetailVerificationPanel(
    claim: ClaimNode,
    verifiedPathIds: List<Int>,
    portfolioClaims: List<ClaimNode>,
    onExploreWithAi: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
            .testTag("claim_detail_panel"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, TealAccent1),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified Path",
                    tint = TealAccent1,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FORENSIC CLAIMS VERIFICATION",
                    style = MaterialTheme.typography.titleLarge,
                    color = TealAccent1,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = claim.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = claim.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CLAIMS DEPENDENCY FOOTPRINT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sequence of verified claims
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                verifiedPathIds.forEachIndexed { idx, id ->
                    val pathClaim = portfolioClaims.find { it.id == id }
                    if (pathClaim != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (id == claim.id) AmberAccent2 else TealAccent1,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Claim ${pathClaim.id}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (idx < verifiedPathIds.lastIndex) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "linked to",
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hash
            Text(
                text = "CRYPTOGRAPHIC ID PROOF",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = claim.verifiedHash,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // AI Action Button linking to Assistant
            Button(
                onClick = { onExploreWithAi() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_synthesize_claim_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp).size(18.dp)
                )
                Text("Synthesize Claim Scope with AI")
            }
        }
    }
}

// Compute tree indent levels recursively
private fun getClaimIndentation(claimId: Int, claims: List<ClaimNode>): Int {
    var indent = 0
    var currentClaim = claims.find { it.id == claimId }
    while (currentClaim?.parentId != null) {
        indent++
        currentClaim = claims.find { it.id == currentClaim!!.parentId }
    }
    return indent
}

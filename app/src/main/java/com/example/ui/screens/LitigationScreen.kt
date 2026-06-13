package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LitigationCase
import com.example.ui.theme.*
import com.example.ui.viewmodel.CaustinViewModel

data class TimelinePoint(val year: Int, val volume: Int, val description: String)
data class CategoryVolume(val category: String, val volume: Int, val description: String)
data class RegionalCaseVolume(val region: String, val volume: Int, val trend: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LitigationScreen(
    viewModel: CaustinViewModel,
    modifier: Modifier = Modifier
) {
    val litigationCases by viewModel.litigationCases.collectAsState()
    val selectedRegion by viewModel.selectedRegion.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Mode Toggle: Cases Database vs. Interactive Metrics Dashboard
    var screenMode by remember { mutableStateOf("dashboard") } // "dashboard" or "database"

    // Dashboard Filters state
    var dateRangeFilter by remember { mutableStateOf("All") } // "All", "2011-2018", "2019-2026"
    var disputeTypeFilter by remember { mutableStateOf("All") } // "All", "Genetics (GINA)", "Patents (IP)", "Privacy (HIPAA)", "Special Ed (IDEA)"

    // Selected nodes for interactive tooltips
    var activeTimelineTooltip by remember { mutableStateOf<TimelinePoint?>(null) }
    var activeCategoryTooltip by remember { mutableStateOf<CategoryVolume?>(null) }
    var activeRegionalTooltip by remember { mutableStateOf<RegionalCaseVolume?>(null) }

    // Filtered litigation cases database logic
    val filteredCases = remember(litigationCases, selectedRegion, searchQuery) {
        litigationCases.filter { caseItem ->
            (selectedRegion == "All" || caseItem.region.equals(selectedRegion, ignoreCase = true)) &&
            (searchQuery.isBlank() || 
             caseItem.title.contains(searchQuery, ignoreCase = true) ||
             caseItem.caseNumber.contains(searchQuery, ignoreCase = true) ||
             caseItem.summary.contains(searchQuery, ignoreCase = true))
        }
    }

    var selectedCabinetCase by remember { mutableStateOf<LitigationCase?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        // Institutional Breadcrumb Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LITIGATION ANALYTICS ARCHIVES",
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
                text = screenMode.uppercase() + " VIEW",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Segmented Switch Mode (Dashboard vs. Database list)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { screenMode = "dashboard" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (screenMode == "dashboard") MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (screenMode == "dashboard") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(6.dp),
                elevation = null
            ) {
                Icon(imageVector = Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Metrics Dashboard", style = MaterialTheme.typography.labelMedium)
            }
            Button(
                onClick = { screenMode = "database" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (screenMode == "database") MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (screenMode == "database") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(6.dp),
                elevation = null
            ) {
                Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Case Database File", style = MaterialTheme.typography.labelMedium)
            }
        }

        if (screenMode == "dashboard") {
            // ============== LITIGATION METRICS DASHBOARD ==============
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Interactive Dashboard Filters Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FilterList, contentDescription = null, tint = TealAccent1, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DASHBOARD PARAMETRIC SCRUBBER",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Date range parameter
                            Text("TEMPORAL ERA BOUNDARY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("All", "2011-2018", "2019-2026").forEach { era ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (dateRangeFilter == era) TealAccent1 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { 
                                                dateRangeFilter = era 
                                                activeTimelineTooltip = null // reset tooltip on filter
                                            }
                                            .padding(vertical = 8.dp)
                                            .testTag("era_selector_$era"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = era,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (dateRangeFilter == era) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Dispute type parameter
                            Text("DISPUTE NOMENCLATURE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val types = listOf("All", "Genetics (GINA)", "Patents (IP)", "Privacy (HIPAA)", "Special Ed (IDEA)")
                                items(types) { t ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (disputeTypeFilter == t) AmberAccent2 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { 
                                                disputeTypeFilter = t 
                                                activeTimelineTooltip = null // reset
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                            .testTag("dispute_type_$t"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = t,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (disputeTypeFilter == t) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Metric Calculations & Interactive Line Graph
                item {
                    // Compute timeline dataset dynamically based on filters
                    val baseTimeline = listOf(
                        TimelinePoint(2011, 14, "Initial CRISPR diagnostic claims filed"),
                        TimelinePoint(2013, 22, "Myriad Genetics Supreme Court decision restricts gene-patentability"),
                        TimelinePoint(2015, 31, "GINA Underwriting challenges surge over genomic database mining"),
                        TimelinePoint(2017, 45, "FDA Electronic clinical records Part 11 auditing enforced"),
                        TimelinePoint(2019, 58, "HIPAA cryptographic key access compliance litigation escalates"),
                        TimelinePoint(2021, 64, "Sequence database breaches trigger public class action suits"),
                        TimelinePoint(2023, 76, "Bio-Core bio-reactor patent infringement appeals filed"),
                        TimelinePoint(2025, 95, "Appeals against automated genetic underwriting reach federal courts"),
                        TimelinePoint(2026, 72, "Public genomic sequencing litigation trials peak in Maryland/Delaware")
                    )

                    // 1. Filter by era
                    val filteredTimeline = baseTimeline.filter {
                        when (dateRangeFilter) {
                            "2011-2018" -> it.year <= 2018
                            "2019-2026" -> it.year >= 2019
                            else -> true
                        }
                    }

                    // 2. Adjust volume scale depending on selected category representing real dynamics
                    val finalTimeline = filteredTimeline.map {
                        val multiplier = when (disputeTypeFilter) {
                            "Genetics (GINA)" -> 0.45f
                            "Patents (IP)" -> 0.35f
                            "Privacy (HIPAA)" -> 0.15f
                            "Special Ed (IDEA)" -> 0.20f
                            else -> 1.0f
                        }
                        val scaledVolume = (it.volume * multiplier).toInt().coerceAtLeast(3)
                        TimelinePoint(it.year, scaledVolume, it.description)
                    }

                    // Auto-select latest year on filter/load for interactive tooltip representation
                    LaunchedEffect(dateRangeFilter, disputeTypeFilter) {
                        if (finalTimeline.isNotEmpty()) {
                            activeTimelineTooltip = finalTimeline.last()
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("dispute_volume_chart_card"),
                        colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "DISPUTE VOLUME TIMELINE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${finalTimeline.sumOf { it.volume }} Cases Total",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = AmberAccent2,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Interactive: Click year markers under the graph for verified summaries.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                                
                                Surface(
                                    color = TealAccent1,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "ANNUAL VOLUMES",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Canvas Curve Graphic representation
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(Color.White.copy(alpha = 0.03f))
                            ) {
                                val width = size.width
                                val height = size.height

                                // Y guidelines
                                val linesY = 3
                                for (i in 1..linesY) {
                                    val yOffset = height * (i.toFloat() / (linesY + 1))
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.08f),
                                        start = Offset(0f, yOffset),
                                        end = Offset(width, yOffset),
                                        strokeWidth = 1f
                                    )
                                }

                                if (finalTimeline.size >= 2) {
                                    val maxVal = finalTimeline.maxOf { it.volume }.toFloat().coerceAtLeast(10f)
                                    val minVal = 0f
                                    val range = maxVal - minVal

                                    val points = finalTimeline.mapIndexed { idx, pt ->
                                        val x = width * (idx.toFloat() / (finalTimeline.size - 1))
                                        val y = height - (height * ((pt.volume - minVal) / range))
                                        Offset(x, y.coerceIn(5f, height - 5f))
                                    }

                                    // Curve area fill
                                    val areaPath = Path().apply {
                                        moveTo(0f, height)
                                        lineTo(points.first().x, points.first().y)
                                        for (i in 1 until points.size) {
                                            val prev = points[i - 1]
                                            val curr = points[i]
                                            cubicTo((prev.x + curr.x) / 2, prev.y, (prev.x + curr.x) / 2, curr.y, curr.x, curr.y)
                                        }
                                        lineTo(width, height)
                                        close()
                                    }

                                    drawPath(
                                        path = areaPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(TealAccent1.copy(alpha = 0.4f), Color.Transparent)
                                        )
                                    )

                                    // Outline curve
                                    val linePath = Path().apply {
                                        moveTo(points.first().x, points.first().y)
                                        for (i in 1 until points.size) {
                                            val prev = points[i - 1]
                                            val curr = points[i]
                                            cubicTo((prev.x + curr.x) / 2, prev.y, (prev.x + curr.x) / 2, curr.y, curr.x, curr.y)
                                        }
                                    }

                                    drawPath(
                                        path = linePath,
                                        color = TealAccent1,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    // Outer nodes
                                    points.forEachIndexed { i, pt ->
                                        val isHighlighted = finalTimeline[i].year == activeTimelineTooltip?.year
                                        drawCircle(
                                            color = if (isHighlighted) AmberAccent2 else Color.White,
                                            radius = if (isHighlighted) 7.dp.toPx() else 4.dp.toPx(),
                                            center = pt
                                        )
                                        if (isHighlighted) {
                                            drawCircle(
                                                color = AmberAccent2.copy(alpha = 0.35f),
                                                radius = 14.dp.toPx(),
                                                center = pt
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Interactive Horizontal Pills representing Year Markers for easy hover/clicks
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                finalTimeline.forEach { pt ->
                                    val isSelected = pt.year == activeTimelineTooltip?.year
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) AmberAccent2 else Color.White.copy(alpha = 0.1f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable { activeTimelineTooltip = pt }
                                            .padding(horizontal = 4.dp, vertical = 5.dp)
                                            .testTag("chart_year_${pt.year}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = pt.year.toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            // Dynamic Interactive Tooltip Card for Timeline Node
                            AnimatedVisibility(
                                visible = activeTimelineTooltip != null,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                activeTimelineTooltip?.let { tooltip ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag("timeline_tooltip_card"),
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                        border = BorderStroke(1.dp, AmberAccent2.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "POINT REPORT: YEAR ${tooltip.year}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = AmberAccent2,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${tooltip.volume} cases filed",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = tooltip.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.85f),
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Common Dispute Categories Visualizer (Vertical Bars)
                item {
                    val rawCategories = listOf(
                        CategoryVolume("GINA", 48, "Genetic discrimination underwriting disputes and employment compliance conflicts"),
                        CategoryVolume("IP Core", 36, "Functional genomic sequencing patent and claims tree litigation cases"),
                        CategoryVolume("HIPAA", 25, "Cryptographic metadata access keys and diagnostic record protocol breaches"),
                        CategoryVolume("IDEA", 19, "Special education genetic wellness resources and regional code claims"),
                        CategoryVolume("FDA CFR", 15, "Clinical biomarker algorithms trail auditing Part 11 alignments")
                    )

                    // Adjust categories metrics based on selected filters
                    val finalCategories = rawCategories.map {
                        val subMultiplier = when (dateRangeFilter) {
                            "2011-2018" -> 0.62f
                            "2019-2026" -> 0.85f
                            else -> 1.0f
                        }
                        val categoryMultiplier = if (disputeTypeFilter == "All") 1.0f else {
                            if (disputeTypeFilter.startsWith(it.category, ignoreCase = true) || 
                                (it.category == "IP Core" && disputeTypeFilter.contains("Patents")) ||
                                (it.category == "FDA CFR" && disputeTypeFilter.contains("Patents"))) {
                                1.25f
                            } else {
                                0.25f
                            }
                        }
                        CategoryVolume(
                            it.category, 
                            (it.volume * subMultiplier * categoryMultiplier).toInt().coerceAtLeast(2), 
                            it.description
                        )
                    }

                    // Auto-select top bar
                    LaunchedEffect(dateRangeFilter, disputeTypeFilter) {
                        activeCategoryTooltip = finalCategories.first()
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("common_categories_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "COMMON DISPUTE CATEGORIES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Interactive: Click on any category bar.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Draw visual horizontal bars in single column
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                finalCategories.forEach { cat ->
                                    val isSelected = cat.category == activeCategoryTooltip?.category
                                    val percentage = (cat.volume.toFloat() / finalCategories.maxOf { it.volume }.toFloat()).coerceIn(0.1f, 1.0f)
                                    
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { activeCategoryTooltip = cat }
                                            .padding(vertical = 2.dp)
                                            .testTag("category_bar_${cat.category}")
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cat.category,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) AmberAccent2 else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${cat.volume} incidents",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Horizontal bar progress indicator
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(10.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(5.dp))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(percentage)
                                                    .fillMaxHeight()
                                                    .background(
                                                        if (isSelected) AmberAccent2 else TealAccent1,
                                                        RoundedCornerShape(5.dp)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }

                            // Tooltip for Category bar selection
                            AnimatedVisibility(
                                visible = activeCategoryTooltip != null,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                activeCategoryTooltip?.let { tooltip ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag("category_tooltip_card"),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)),
                                        border = BorderStroke(1.dp, TealAccent1.copy(alpha = 0.4f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "ANALYSIS DEEP DIVE: ${tooltip.category}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TealAccent1,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = tooltip.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Geographical Breakdown
                item {
                    val rawRegions = listOf(
                        RegionalCaseVolume("Maryland", 62, "High density clinical biomarker trials and DNA underwriting appeals"),
                        RegionalCaseVolume("Federal Circuit", 47, "Sovereign patent authority challenges and constitutional GINA appeals"),
                        RegionalCaseVolume("Delaware", 33, "Corporate database sequence licenses and encryption breach suits"),
                        RegionalCaseVolume("California", 18, "Silicon Valley bio-reactor patent infringements and regional audits")
                    )

                    // Adjust values based on filters
                    val finalRegions = rawRegions.map {
                        val regMult = when (dateRangeFilter) {
                            "2011-2018" -> 0.55f
                            "2019-2026" -> 0.90f
                            else -> 1.0f
                        }
                        val typeMult = when (disputeTypeFilter) {
                            "All" -> 1.0f
                            "Genetics (GINA)" -> if (it.region == "Federal Circuit" || it.region == "Maryland") 1.1f else 0.40f
                            "Patents (IP)" -> if (it.region == "California" || it.region == "Delaware") 1.2f else 0.45f
                            else -> 0.50f
                        }
                        RegionalCaseVolume(
                            it.region,
                            (it.volume * regMult * typeMult).toInt().coerceAtLeast(1),
                            it.trend
                        )
                    }

                    LaunchedEffect(dateRangeFilter, disputeTypeFilter) {
                        activeRegionalTooltip = finalRegions.first()
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("geographical_distribution_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GEOGRAPHICAL JURISDICTION METRICS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Interactive: Click regions below.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))

                            // Grid representation for regions
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                finalRegions.forEach { reg ->
                                    val isSelected = reg.region == activeRegionalTooltip?.region
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { activeRegionalTooltip = reg }
                                            .testTag("region_metric_row_${reg.region}"),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) TealAccent1.copy(alpha = 0.08f) else Color.Transparent
                                        ),
                                        border = if (isSelected) BorderStroke(1.dp, TealAccent1) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Place,
                                                    contentDescription = null,
                                                    tint = if (isSelected) TealAccent1 else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = reg.region,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) TealAccent1 else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Surface(
                                                color = if (isSelected) TealAccent1 else MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "${reg.volume} disputes",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Tooltip box for region deep-dive details
                            AnimatedVisibility(
                                visible = activeRegionalTooltip != null,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                activeRegionalTooltip?.let { tooltip ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).testTag("regional_tooltip_card"),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "JURISDICTION DETAIL PROFILE: ${tooltip.region}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = tooltip.trend,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ============== ACTIVE CASE DATABASE FILE (ORIGINAL LIST VIEW) ==============
            // Search Input Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search litigation, case filing records, codex...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("litigation_search_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Region Filter Chips
            val regions = listOf("All", "Maryland", "Delaware", "Federal Circuit", "California")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(regions) { reg ->
                    FilterChip(
                        selected = selectedRegion == reg,
                        onClick = { viewModel.setRegion(reg) },
                        label = { Text(reg) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("region_chip_$reg")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Results count
                item {
                    Text(
                        text = "ACTIVE PUBLIC LITIGATION RECORDS (${filteredCases.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Empty state placeholder
                if (filteredCases.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "No results",
                                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Zero matching archives found.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Adjust filters or try a different clinical/corporate search query.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredCases, key = { it.id }) { caseItem ->
                        LitigationCaseCard(
                            caseItem = caseItem,
                            onToggleBookmark = { viewModel.toggleLitigationBookmark(caseItem.id, !caseItem.isBookmarked) },
                            onViewDetails = { selectedCabinetCase = caseItem }
                        )
                    }
                }
            }
        }
    }

    // Detail disclosure modal dialog
    if (selectedCabinetCase != null) {
        val caseDoc = selectedCabinetCase!!
        AlertDialog(
            onDismissRequest = { selectedCabinetCase = null },
            confirmButton = {
                TextButton(onClick = { selectedCabinetCase = null }) {
                    Text("Close Case file", color = MaterialTheme.colorScheme.primary)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Court",
                        modifier = Modifier.padding(end = 8.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = caseDoc.caseNumber,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = caseDoc.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("JURISDICTION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            Text(caseDoc.region, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("DISPUTE SCORE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            Text("${caseDoc.intensityMetric} /10k", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = AmberAccent2)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("CASE SUMMARY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Text(
                        text = caseDoc.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Cryptographic blockchain proof tag
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Encrypted",
                                    tint = TealAccent1,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CRYPTOGRAPHIC FORENSIC HASH",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TealAccent1,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = caseDoc.cryptographicHash,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

// Case Card representation
@Composable
fun LitigationCaseCard(
    caseItem: LitigationCase,
    onToggleBookmark: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("case_card_${caseItem.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = caseItem.caseNumber,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = caseItem.region,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    if (caseItem.isAlert) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning logo",
                            tint = AmberAccent2,
                            modifier = Modifier.size(18.dp).padding(end = 6.dp)
                        )
                    }

                    Icon(
                        imageVector = if (caseItem.isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Bookmark button",
                        tint = if (caseItem.isBookmarked) AmberAccent2 else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onToggleBookmark() }
                            .testTag("case_bookmark_${caseItem.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = caseItem.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = caseItem.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Divider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = TealAccent1
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = caseItem.cryptographicHash,
                        style = MaterialTheme.typography.labelSmall,
                        color = TealAccent1
                    )
                }

                Text(
                    text = "DISPUTE: ${caseItem.intensityMetric}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (caseItem.intensityMetric > 50) AmberAccent2 else TealAccent1
                )
            }
        }
    }
}

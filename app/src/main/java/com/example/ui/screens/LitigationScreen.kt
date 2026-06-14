package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.CaustinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LitigationScreen(
    viewModel: CaustinViewModel,
    modifier: Modifier = Modifier
) {
    val studentName by viewModel.studentName.collectAsState()
    val highSchoolName by viewModel.highSchoolName.collectAsState()
    val targetCollege by viewModel.targetCollege.collectAsState()
    val transitionMajor by viewModel.transitionMajor.value.let { remember { mutableStateOf(viewModel.transitionMajor.value) } } 
    val selectedAccommodations by viewModel.selectedAccommodations.collectAsState()

    // Keep temporary form states to edit easily
    var editingName by remember { mutableStateOf("") }
    var editingHs by remember { mutableStateOf("") }
    var editingCollege by remember { mutableStateOf("") }
    var editingMajor by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    // Init form
    LaunchedEffect(studentName, highSchoolName, targetCollege, transitionMajor) {
        editingName = viewModel.studentName.value
        editingHs = viewModel.highSchoolName.value
        editingCollege = viewModel.targetCollege.value
        editingMajor = viewModel.transitionMajor.value
    }

    val accommodationOptions = remember {
        listOf(
            AccommodationItem(1, "Testing", "Extended Testing Time (1.5x / 2.0x)", "Compensates for rapid prefrontal dopamine depletion during multi-hour evaluations.", "ADA Title II / OSERS"),
            AccommodationItem(2, "Environment", "Reduced-Distraction Private Testing Room", "Mitigates norepinephrine gating deficits by blocking sensory/acoustic distractions.", "ADA Title II"),
            AccommodationItem(3, "Classroom", "Course Lecture Recording Authorization", "Helps offset auditory working memory drops. Allows review of complex sessions.", "Section 504 Recs"),
            AccommodationItem(4, "Classroom", "Live Scribe or Shared Peer Notes", "Compensates for fine-motor control fatigue and attention decay during lecture series.", "Section 504 Recs"),
            AccommodationItem(5, "Support", "Permitted Sensory / Short Breaks (10 min/hr)", "Resets default mode network (DMN) hyperactivity to restore cortical alertness.", "ADA Title II"),
            AccommodationItem(6, "Support", "Weekly Disability Services Liaison Checkpoint", "Supplies structural feedback for executive planning and time alignment tasks.", "Section 504 Recs"),
            AccommodationItem(7, "Technology", "Advance Reading & Syllabus Materials Access", "Enables proactive cognitive scaffolding and scheduling of study sessions.", "Section 504 Recs"),
            AccommodationItem(8, "Technology", "Speech-to-Text Dictation Software Aid", "Bypasses primary written composition blockages caused by plan retrieval gaps.", "ADA Title II"),
            AccommodationItem(9, "Technology", "Visual Graphic Organizers & Priority Checklists", "Mitigates anterior cingulate cortex (ACC) error monitoring issues with visual cues.", "Section 504 Recs")
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Aesthetic Institutional Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SECTION 504 TRANSITION PLAN BUILDER",
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
                        text = "STUDENT ADVOCACY PORTAL",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configure your formal transition accommodations docket below. These forms map clinical symptoms and biomatrices to university-level academic adjustments.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Student Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_profile_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE STUDENT DATA ENROLLMENT",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = {
                                if (isEditing) {
                                    viewModel.updateProfile(editingName, editingHs, editingCollege, editingMajor)
                                }
                                isEditing = !isEditing
                            },
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.CheckCircle else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Save Profile" else "Edit Profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = editingName,
                            onValueChange = { editingName = it },
                            label = { Text("Student Name") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("input_student_name"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editingHs,
                            onValueChange = { editingHs = it },
                            label = { Text("Current High School") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("input_high_school"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editingCollege,
                            onValueChange = { editingCollege = it },
                            label = { Text("Target University/College") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("input_target_college"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editingMajor,
                            onValueChange = { editingMajor = it },
                            label = { Text("Intended Major") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("input_intended_major"),
                            singleLine = true
                        )
                    } else {
                        // Display clean details
                        ProfileDetailRow(label = "Student Advocate", value = studentName, icon = Icons.Default.AccountCircle)
                        ProfileDetailRow(label = "Secondary High School", value = highSchoolName, icon = Icons.Default.School)
                        ProfileDetailRow(label = "Transition College", value = targetCollege, icon = Icons.Default.HomeWork)
                        ProfileDetailRow(label = "Intended Program Major", value = editingMajor, icon = Icons.Default.AssignmentInd)
                    }
                }
            }
        }

        // Accommodations List Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACCOMMODATIONS REQUEST DOCKET",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = "${selectedAccommodations.size} of 9 ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // List of accommodations items
        items(accommodationOptions, key = { it.id }) { item ->
            val isSelected = selectedAccommodations.contains(item.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleAccommodation(item.id) }
                    .testTag("accommodation_item_${item.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { viewModel.toggleAccommodation(item.id) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("checkbox_acc_${item.id}")
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.basis,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Therapeutic Justification: Compensates for biological deficits",
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
fun ProfileDetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

data class AccommodationItem(
    val id: Int,
    val category: String,
    val title: String,
    val description: String,
    val basis: String
)

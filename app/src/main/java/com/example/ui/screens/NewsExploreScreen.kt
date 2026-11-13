package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CircleNote
import com.example.data.model.CommunityChallenge
import com.example.data.model.Post
import com.example.data.model.PublicProfile
import com.example.data.model.QuizItem
import com.example.data.model.StudyCircle
import com.example.data.model.StudyGoal
import com.example.ui.components.RoleAndSpecialBadgeRow
import com.example.ui.components.UserAvatar
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandScholarGold

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewsExploreScreen(
  ownProfile: PublicProfile?,
  allProfiles: List<PublicProfile>,
  posts: List<Post>,
  studyCircles: List<StudyCircle>,
  activeCircleNotes: List<CircleNote>,
  quizzes: List<QuizItem>,
  challenges: List<CommunityChallenge>,
  studyGoals: List<StudyGoal>,
  onOpenProfile: (String) -> Unit,
  onOpenReels: () -> Unit,
  onCreateCircle: (String, String, String, String) -> Unit,
  onToggleJoinCircle: (StudyCircle) -> Unit,
  onSelectCircle: (String?) -> Unit,
  onAddCircleNote: (String, String, String) -> Unit,
  onCreateQuiz: (String, String, String, List<String>, Int, String) -> Unit,
  onCreateChallenge: (String, String, String, Int) -> Unit,
  onToggleJoinChallenge: (CommunityChallenge) -> Unit,
  onAddStudyGoal: (String, String, Int) -> Unit,
  onToggleStudyGoal: (StudyGoal) -> Unit,
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedSubTab by remember { mutableIntStateOf(0) }
  var showCreateCircle by remember { mutableStateOf(false) }
  var selectedCircleForNotes by remember { mutableStateOf<StudyCircle?>(null) }
  var showCreateQuiz by remember { mutableStateOf(false) }
  var showCreateChallenge by remember { mutableStateOf(false) }
  var showAddGoal by remember { mutableStateOf(false) }
  val quizSelections = remember { mutableStateMapOf<String, Int>() }

  val subTabs = listOf("Explore", "Study Circles", "Quiz & Planner")

  Column(modifier = Modifier.fillMaxSize()) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search users, #hashtags, topics, circles...") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
      singleLine = true,
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .testTag("news_search_input"),
    )

    PrimaryTabRow(selectedTabIndex = selectedSubTab) {
      subTabs.forEachIndexed { idx, title ->
        Tab(
          selected = selectedSubTab == idx,
          onClick = { selectedSubTab = idx },
          text = { Text(title, maxLines = 1) },
        )
      }
    }

    val query = searchQuery.trim().lowercase()

    when (selectedSubTab) {
      0 -> {
        // Explore, Trending & Career/Scholarship Hub
        val trendingTags = posts.flatMap { it.hashtags }
          .groupingBy { it.lowercase() }
          .eachCount()
          .entries
          .sortedByDescending { it.value }
          .take(8)
          .map { it.key }
          .ifEmpty { listOf("physics", "calculus", "coding", "biology", "studytips", "scholarships") }

        val matchedUsers = allProfiles.filter {
          query.isBlank() ||
            it.fullName.lowercase().contains(query) ||
            it.username.lowercase().contains(query) ||
            it.interests.any { tag -> tag.lowercase().contains(query) }
        }.take(6)

        val matchedPosts = posts.filter {
          query.isBlank() ||
            it.caption.lowercase().contains(query) ||
            it.subject.lowercase().contains(query) ||
            it.hashtags.any { tag -> tag.lowercase().contains(query) }
        }

        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          item {
            Text("Trending Topics", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              trendingTags.forEach { tag ->
                FilterChip(
                  selected = query == tag,
                  onClick = { searchQuery = if (query == tag) "" else tag },
                  label = { Text("#$tag") },
                )
              }
            }
          }

          item {
            Text("Featured Educators & Creators", style = MaterialTheme.typography.titleMedium)
          }

          items(matchedUsers, key = { it.userId }) { user ->
            Card(
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenProfile(user.userId) },
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                UserAvatar(user.fullName, user.avatarUri, size = 44.dp, hasTeacherBadge = user.hasSpecialTeacherBadge)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(user.fullName, style = MaterialTheme.typography.titleSmall)
                  Text(
                    "@${user.username} • ${user.school.ifBlank { user.educationLevel.ifBlank { "i student" } }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  Spacer(modifier = Modifier.height(3.dp))
                  RoleAndSpecialBadgeRow(user.accountType, user.hasSpecialTeacherBadge)
                }
              }
            }
          }

          item {
            Card(
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.WorkOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Scholarships & Career Resources", style = MaterialTheme.typography.titleMedium)
                }
                Text(
                  "Discover STEM fellowships, summer research internships, and verified educator prep guides.",
                  style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  OutlinedButton(onClick = { searchQuery = "scholarship" }) { Text("Browse Grants") }
                  Button(onClick = onOpenReels) { Text("Knowledge Reels") }
                }
              }
            }
          }

          if (matchedPosts.isNotEmpty()) {
            item {
              Text("Educational Feed Matches (${matchedPosts.size})", style = MaterialTheme.typography.titleMedium)
            }
            items(matchedPosts.take(10), key = { it.id }) { post ->
              Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Text("${post.authorName} • ${post.subject.ifBlank { post.postType }}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(post.caption, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                }
              }
            }
          }
        }
      }

      1 -> {
        // Smart Study Circles, Collaborative Notes & Community Challenges
        val filteredCircles = studyCircles.filter {
          query.isBlank() || it.name.lowercase().contains(query) || it.subject.lowercase().contains(query)
        }
        val ownSubjects = ownProfile?.interests?.map { it.lowercase() }?.toSet().orEmpty()

        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          item {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
              Column {
                Image(
                  painter = painterResource(id = R.drawable.img_study_circle_banner),
                  contentDescription = "Smart Study Circle illustration",
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                  contentScale = ContentScale.Crop,
                )
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text("Smart Study Circles", style = MaterialTheme.typography.titleMedium)
                      Text("Matched by your subjects & collaborative notes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { showCreateCircle = true }, modifier = Modifier.testTag("create_circle_button")) {
                      Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("New")
                    }
                  }
                }
              }
            }
          }

          if (filteredCircles.isEmpty()) {
            item {
              Text(
                "No study circles yet. Create the first study circle for your subject!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          } else {
            items(filteredCircles, key = { it.id }) { circle ->
              val isMember = ownProfile?.userId in circle.memberIds
              val isSmartMatch = circle.subject.lowercase() in ownSubjects
              Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(circle.name, style = MaterialTheme.typography.titleMedium)
                    }
                    if (isSmartMatch) {
                      Surface(color = BrandScholarGold.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                        Text("Smart Match", style = MaterialTheme.typography.labelSmall, color = BrandScholarGold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                      }
                    }
                  }
                  Text("${circle.subject} • ${circle.educationLevel.ifBlank { "All Levels" }} • ${circle.memberIds.size} members", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                  if (circle.description.isNotBlank()) {
                    Text(circle.description, style = MaterialTheme.typography.bodySmall)
                  }
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onToggleJoinCircle(circle) }) {
                      Text(if (isMember) "Joined" else "Join Circle")
                    }
                    Button(onClick = {
                      selectedCircleForNotes = circle
                      onSelectCircle(circle.id)
                    }) {
                      Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("Collaborative Notes")
                    }
                  }
                }
              }
            }
          }

          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Community Challenges", style = MaterialTheme.typography.titleMedium)
              TextButton(onClick = { showCreateChallenge = true }) { Text("+ Launch Challenge") }
            }
          }

          items(challenges, key = { it.id }) { ch ->
            val joined = ownProfile?.userId in ch.participantIds
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = BrandScholarGold, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(ch.title, style = MaterialTheme.typography.titleSmall)
                  Text("${ch.subject} • ${ch.targetDays}-day goal • ${ch.participantIds.size} learners", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = { onToggleJoinChallenge(ch) }) {
                  Text(if (joined) "Joined" else "Join")
                }
              }
            }
          }
        }
      }

      else -> {
        // Quizzes, Study Streak & Daily Study Planner
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          item {
            Card(
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = BrandScholarGold, modifier = Modifier.size(34.dp))
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text("${ownProfile?.studyStreakDays ?: 1}-Day Study Streak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Daily target: ${ownProfile?.dailyStudyGoalMins ?: 60} mins", style = MaterialTheme.typography.bodySmall)
                  }
                }
                Button(onClick = { showAddGoal = true }) { Text("+ Goal") }
              }
            }
          }

          if (studyGoals.isNotEmpty()) {
            item { Text("Daily Study Planner", style = MaterialTheme.typography.titleMedium) }
            items(studyGoals, key = { it.id }) { goal ->
              Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleStudyGoal(goal) }
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Checkbox(checked = goal.completed, onCheckedChange = { onToggleStudyGoal(goal) })
                  Column(modifier = Modifier.weight(1f)) {
                    Text(goal.title, style = MaterialTheme.typography.titleSmall)
                    Text("${goal.subject.ifBlank { "General" }} • ${goal.targetMinutes} mins", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }
              }
            }
          }

          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Practice Quizzes", style = MaterialTheme.typography.titleMedium)
              Button(onClick = { showCreateQuiz = true }) { Text("+ Create Quiz") }
            }
          }

          if (quizzes.isEmpty()) {
            item {
              Text("No quizzes published yet. Teachers and students can publish interactive practice questions here!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          } else {
            items(quizzes, key = { it.id }) { quiz ->
              val selectedOption = quizSelections[quiz.id]
              Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${quiz.title} • ${quiz.subject}", style = MaterialTheme.typography.titleSmall)
                  }
                  Text(quiz.questionPrompt, style = MaterialTheme.typography.bodyLarge)
                  quiz.options.forEachIndexed { optIdx, optionText ->
                    val isSelected = selectedOption == optIdx
                    val isCorrect = optIdx == quiz.correctIndex.toInt()
                    val container = when {
                      selectedOption == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                      isSelected && isCorrect -> BrandEmerald.copy(alpha = 0.25f)
                      isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.20f)
                      isCorrect -> BrandEmerald.copy(alpha = 0.15f)
                      else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }
                    Surface(
                      color = container,
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable { quizSelections[quiz.id] = optIdx },
                    ) {
                      Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Text(optionText, style = MaterialTheme.typography.bodyMedium)
                        if (selectedOption != null && isCorrect) {
                          Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = BrandEmerald, modifier = Modifier.size(18.dp))
                        }
                      }
                    }
                  }
                  if (selectedOption != null && quiz.explanation.isNotBlank()) {
                    Text(
                      text = "Explanation: ${quiz.explanation}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.primary,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Collaborative Notes Modal Sheet
  selectedCircleForNotes?.let { circle ->
    ModalBottomSheet(onDismissRequest = {
      selectedCircleForNotes = null
      onSelectCircle(null)
    }) {
      var noteTitle by remember { mutableStateOf("") }
      var noteContent by remember { mutableStateOf("") }
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text("${circle.name} — Collaborative Notes", style = MaterialTheme.typography.titleLarge)
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (activeCircleNotes.isEmpty()) {
            item { Text("No shared notes yet. Contribute the first note below!", style = MaterialTheme.typography.bodySmall) }
          } else {
            items(activeCircleNotes, key = { it.id }) { note ->
              Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(note.title, style = MaterialTheme.typography.titleSmall)
                  Text("By ${note.authorName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(note.content, style = MaterialTheme.typography.bodySmall)
                }
              }
            }
          }
        }
        OutlinedTextField(value = noteTitle, onValueChange = { noteTitle = it }, label = { Text("Note Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = noteContent, onValueChange = { noteContent = it }, label = { Text("Note Content") }, minLines = 2, modifier = Modifier.fillMaxWidth())
        Button(
          onClick = {
            onAddCircleNote(circle.id, noteTitle, noteContent)
            noteTitle = ""
            noteContent = ""
          },
          enabled = noteTitle.isNotBlank() && noteContent.isNotBlank(),
          modifier = Modifier.fillMaxWidth(),
        ) { Text("Add Collaborative Note") }
      }
    }
  }

  if (showCreateCircle) {
    var name by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Computer Science") }
    var level by remember { mutableStateOf("Undergraduate") }
    var desc by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showCreateCircle = false },
      title = { Text("New Study Circle") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Circle Name") }, singleLine = true)
          OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, singleLine = true)
          OutlinedTextField(value = level, onValueChange = { level = it }, label = { Text("Education Level") }, singleLine = true)
          OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onCreateCircle(name, subject, level, desc)
            showCreateCircle = false
          },
          enabled = name.trim().length >= 2 && subject.isNotBlank(),
        ) { Text("Create") }
      },
      dismissButton = { TextButton(onClick = { showCreateCircle = false }) { Text("Cancel") } },
    )
  }

  if (showCreateQuiz) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var prompt by remember { mutableStateOf("") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var correctIdx by remember { mutableIntStateOf(0) }
    var explanation by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showCreateQuiz = false },
      title = { Text("Create Practice Quiz") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Quiz Title") }, singleLine = true)
          OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, singleLine = true)
          OutlinedTextField(value = prompt, onValueChange = { prompt = it }, label = { Text("Question") })
          OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("Option 1") }, singleLine = true)
          OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("Option 2") }, singleLine = true)
          OutlinedTextField(value = optC, onValueChange = { optC = it }, label = { Text("Option 3 (Optional)") }, singleLine = true)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(0 to "Opt 1", 1 to "Opt 2", 2 to "Opt 3").forEach { (i, lbl) ->
              FilterChip(selected = correctIdx == i, onClick = { correctIdx = i }, label = { Text(lbl) })
            }
          }
          OutlinedTextField(value = explanation, onValueChange = { explanation = it }, label = { Text("Explanation") })
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val opts = listOf(optA, optB, optC).filter { it.isNotBlank() }
            val safeCorrect = correctIdx.coerceIn(0, (opts.size - 1).coerceAtLeast(0))
            onCreateQuiz(title, subject, prompt, opts, safeCorrect, explanation)
            showCreateQuiz = false
          },
          enabled = title.isNotBlank() && prompt.isNotBlank() && optA.isNotBlank() && optB.isNotBlank(),
        ) { Text("Publish") }
      },
      dismissButton = { TextButton(onClick = { showCreateQuiz = false }) { Text("Cancel") } },
    )
  }

  if (showCreateChallenge) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Mathematics") }
    var daysText by remember { mutableStateOf("14") }
    AlertDialog(
      onDismissRequest = { showCreateChallenge = false },
      title = { Text("Launch Community Challenge") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Challenge Title") }, singleLine = true)
          OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, singleLine = true)
          OutlinedTextField(value = daysText, onValueChange = { daysText = it.filter { c -> c.isDigit() } }, label = { Text("Target Days") }, singleLine = true)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onCreateChallenge(title, subject, "Daily learning challenge", daysText.toIntOrNull() ?: 7)
            showCreateChallenge = false
          },
          enabled = title.trim().length >= 2,
        ) { Text("Launch") }
      },
      dismissButton = { TextButton(onClick = { showCreateChallenge = false }) { Text("Cancel") } },
    )
  }

  if (showAddGoal) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("General") }
    var mins by remember { mutableStateOf("45") }
    AlertDialog(
      onDismissRequest = { showAddGoal = false },
      title = { Text("Add Daily Study Goal") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Goal Title") }, singleLine = true)
          OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, singleLine = true)
          OutlinedTextField(value = mins, onValueChange = { mins = it.filter { c -> c.isDigit() } }, label = { Text("Minutes") }, singleLine = true)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onAddStudyGoal(title, subject, mins.toIntOrNull() ?: 30)
            showAddGoal = false
          },
          enabled = title.isNotBlank(),
        ) { Text("Save Goal") }
      },
      dismissButton = { TextButton(onClick = { showAddGoal = false }) { Text("Cancel") } },
    )
  }
}

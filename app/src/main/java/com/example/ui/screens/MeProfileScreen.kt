package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AccentPalette
import com.example.data.model.InterfaceDensity
import com.example.data.model.ModerationReport
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.model.PrivateAccountInfo
import com.example.data.model.PublicProfile
import com.example.data.model.Story
import com.example.data.model.ThemeModePreference
import com.example.data.model.UserAppPreferences
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.PostCard
import com.example.ui.components.RoleAndSpecialBadgeRow
import com.example.ui.components.UserAvatar
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandScholarGold

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MeProfileScreen(
  profile: PublicProfile,
  privateInfo: PrivateAccountInfo?,
  ownPosts: List<Post>,
  allPosts: List<Post>,
  stories: List<Story>,
  likedPostIds: Set<String>,
  savedPostIds: Set<String>,
  activeComments: List<PostComment>,
  followersCount: Int,
  followingCount: Int,
  isAdmin: Boolean,
  allProfiles: List<PublicProfile>,
  reports: List<ModerationReport>,
  themePrefs: UserAppPreferences,
  onSaveProfile: (PublicProfile, PrivateAccountInfo?) -> Unit,
  onSetThemeMode: (ThemeModePreference) -> Unit,
  onSetAccent: (AccentPalette) -> Unit,
  onSetFontScale: (Float) -> Unit,
  onSetDensity: (InterfaceDensity) -> Unit,
  onSetHighContrast: (Boolean) -> Unit,
  onLikePost: (Post) -> Unit,
  onSavePost: (String) -> Unit,
  onRepost: (Post) -> Unit,
  onTogglePinPost: (Post) -> Unit,
  onSelectPostForComments: (String?) -> Unit,
  onSendComment: (Post, String, String) -> Unit,
  onOpenReels: () -> Unit,
  onDeletePost: (String) -> Unit,
  onAdminToggleTeacherBadge: (PublicProfile) -> Unit,
  onAdminToggleSuspend: (PublicProfile) -> Unit,
  onAdminResolveReport: (String, String) -> Unit,
  onDeleteAccount: () -> Unit,
  onSignOut: () -> Unit,
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  var showPersonalizationStudio by remember { mutableStateOf(false) }
  var showThemeSettings by remember { mutableStateOf(false) }
  var showCreatorDashboard by remember { mutableStateOf(false) }
  var showAdminDashboard by remember { mutableStateOf(false) }
  var commentTargetPost by remember { mutableStateOf<Post?>(null) }

  val profileTabs = listOf("Posts", "Reels", "Stories", "Saved", "Liked", "About")

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 28.dp),
  ) {
    // Cover + Avatar Header
    item {
      Box(modifier = Modifier.fillMaxWidth()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        ) {
          if (profile.coverUri.isNotBlank()) {
            AsyncImage(
              model = profile.coverUri,
              contentDescription = "Profile cover",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
            )
          } else {
            Image(
              painter = painterResource(id = R.drawable.img_default_cover),
              contentDescription = "Default profile cover",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
            )
          }
        }

        Box(
          modifier = Modifier
            .padding(start = 16.dp, top = 104.dp)
            .border(3.dp, MaterialTheme.colorScheme.background, CircleShape),
        ) {
          UserAvatar(
            name = profile.fullName,
            avatarUri = profile.avatarUri,
            size = 84.dp,
            hasTeacherBadge = profile.hasSpecialTeacherBadge,
          )
        }

        Row(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(top = 158.dp, end = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          OutlinedButton(
            onClick = { showPersonalizationStudio = true },
            modifier = Modifier.testTag("edit_profile_studio_button"),
          ) {
            Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Edit & Studio")
          }
          IconButton(
            onClick = { showThemeSettings = true },
            modifier = Modifier.testTag("theme_settings_button"),
          ) {
            Icon(Icons.Default.Settings, contentDescription = "Theme & Account Settings")
          }
        }
      }
    }

    // Identity, Bio, Stats, Quick Action Cards
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(profile.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("@${profile.username} • ${profile.school.ifBlank { "i student Campus" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        RoleAndSpecialBadgeRow(profile.accountType, profile.hasSpecialTeacherBadge)

        if (profile.bio.isNotBlank()) {
          Text(profile.bio, style = MaterialTheme.typography.bodyMedium)
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
          StatBlock("$followersCount", "Followers")
          StatBlock("$followingCount", "Following")
          StatBlock("${ownPosts.size}", "Posts")
          StatBlock("${ownPosts.count { it.postType == "reel" }}", "Reels")
          StatBlock("${profile.studyStreakDays}d", "Streak")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          Button(
            onClick = { showCreatorDashboard = true },
            modifier = Modifier.weight(1f).testTag("creator_dashboard_button"),
          ) {
            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Creator Stats")
          }
          if (isAdmin) {
            Button(
              onClick = { showAdminDashboard = true },
              colors = ButtonDefaults.buttonColors(containerColor = BrandScholarGold, contentColor = Color.Black),
              modifier = Modifier.weight(1f).testTag("admin_dashboard_button"),
            ) {
              Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Admin Panel")
            }
          }
        }
      }
    }

    // Profile Tabs
    item {
      PrimaryScrollableTabRow(selectedTabIndex = selectedTab) {
        profileTabs.forEachIndexed { idx, title ->
          Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }, text = { Text(title) })
        }
      }
    }

    when (selectedTab) {
      0 -> {
        if (ownPosts.isEmpty()) {
          item { EmptyProfileTab("No posts yet. Share your first note or photo from Upload!") }
        } else {
          items(ownPosts, key = { it.id }) { post ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
              PostCard(
                post = post,
                isOwnPost = true,
                isLiked = post.id in likedPostIds,
                isSaved = post.id in savedPostIds,
                onAuthorClick = {},
                onLike = { onLikePost(post) },
                onComment = {
                  commentTargetPost = post
                  onSelectPostForComments(post.id)
                },
                onSave = { onSavePost(post.id) },
                onRepost = { onRepost(post) },
                onOpenReels = onOpenReels,
                onEdit = { showPersonalizationStudio = true },
                onTogglePin = { onTogglePinPost(post) },
                onDelete = { onDeletePost(post.id) },
                onReport = {},
                onBlockAuthor = {},
              )
            }
          }
        }
      }
      1 -> {
        val ownReels = ownPosts.filter { it.postType == "reel" }
        if (ownReels.isEmpty()) {
          item { EmptyProfileTab("No Knowledge Reels uploaded yet.") }
        } else {
          items(ownReels, key = { it.id }) { reel ->
            Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth().clickable(onClick = onOpenReels)) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(reel.subject.ifBlank { "Knowledge Reel" }, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(reel.caption, style = MaterialTheme.typography.bodyMedium)
              }
            }
          }
        }
      }
      2 -> {
        val ownStories = stories.filter { it.authorId == profile.userId }
        if (ownStories.isEmpty()) {
          item { EmptyProfileTab("No active stories or highlights.") }
        } else {
          items(ownStories, key = { it.id }) { st ->
            Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth()) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(if (st.isHighlight) "Highlight: ${st.highlightTitle}" else "24h Story", style = MaterialTheme.typography.labelMedium, color = BrandScholarGold)
                Text(st.textContent, style = MaterialTheme.typography.bodyMedium)
              }
            }
          }
        }
      }
      3 -> {
        val saved = allPosts.filter { it.id in savedPostIds }
        if (saved.isEmpty()) {
          item { EmptyProfileTab("Your saved posts and study bookmarks are private to you.") }
        } else {
          items(saved, key = { it.id }) { post ->
            Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth()) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text("${post.authorName} (@${post.authorUsername})", style = MaterialTheme.typography.titleSmall)
                Text(post.caption, style = MaterialTheme.typography.bodyMedium)
              }
            }
          }
        }
      }
      4 -> {
        val liked = allPosts.filter { it.id in likedPostIds }
        if (liked.isEmpty()) {
          item { EmptyProfileTab("Posts you like appear here privately.") }
        } else {
          items(liked, key = { it.id }) { post ->
            Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth()) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text("${post.authorName} (@${post.authorUsername})", style = MaterialTheme.typography.titleSmall)
                Text(post.caption, style = MaterialTheme.typography.bodyMedium)
              }
            }
          }
        }
      }
      else -> {
        item {
          Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text("About & Learning Progress", style = MaterialTheme.typography.titleMedium)
              Text("Education: ${profile.school} (${profile.educationLevel})")
              if (profile.location.isNotBlank()) Text("Location: ${profile.location}")
              if (profile.websiteUrl.isNotBlank()) Text("Website: ${profile.websiteUrl}")
              if (profile.accountType == "teacher") {
                Text("Teaching Expertise: ${profile.teacherExpertise.ifBlank { "General Educator" }}", color = MaterialTheme.colorScheme.primary)
                if (profile.teacherIntro.isNotBlank()) Text(profile.teacherIntro)
              }
              Text("Languages: ${profile.languages.joinToString(", ")}")
              if (profile.interests.isNotEmpty()) {
                Text("Interests: ${profile.interests.joinToString(", ")}")
              }
              if (profile.skills.isNotEmpty()) {
                Text("Skills: ${profile.skills.joinToString(", ")}")
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = BrandScholarGold)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Study Streak: ${profile.studyStreakDays} days • Daily Goal: ${profile.dailyStudyGoalMins} min")
              }
            }
          }
        }
      }
    }
  }

  if (showPersonalizationStudio) {
    ProfilePersonalizationStudioDialog(
      initialProfile = profile,
      initialPrivate = privateInfo,
      onDismiss = { showPersonalizationStudio = false },
      onSave = { updated, updatedPriv ->
        onSaveProfile(updated, updatedPriv)
        showPersonalizationStudio = false
      },
    )
  }

  if (showThemeSettings) {
    ThemeAndAccountSettingsDialog(
      prefs = themePrefs,
      onDismiss = { showThemeSettings = false },
      onSetThemeMode = onSetThemeMode,
      onSetAccent = onSetAccent,
      onSetFontScale = onSetFontScale,
      onSetDensity = onSetDensity,
      onSetHighContrast = onSetHighContrast,
      onDeleteAccount = {
        showThemeSettings = false
        onDeleteAccount()
      },
      onSignOut = {
        showThemeSettings = false
        onSignOut()
      },
    )
  }

  if (showCreatorDashboard) {
    CreatorDashboardDialog(
      profile = profile,
      ownPosts = ownPosts,
      followersCount = followersCount,
      onDismiss = { showCreatorDashboard = false },
    )
  }

  if (showAdminDashboard) {
    AdminDashboardDialog(
      allProfiles = allProfiles,
      reports = reports,
      onDismiss = { showAdminDashboard = false },
      onToggleBadge = onAdminToggleTeacherBadge,
      onToggleSuspend = onAdminToggleSuspend,
      onResolveReport = onAdminResolveReport,
      onRemovePost = onDeletePost,
    )
  }

  commentTargetPost?.let { target ->
    CommentsBottomSheet(
      post = target,
      comments = activeComments,
      onDismiss = {
        commentTargetPost = null
        onSelectPostForComments(null)
      },
      onSendComment = { text, parentId -> onSendComment(target, text, parentId) },
    )
  }
}

@Composable
private fun StatBlock(value: String, label: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun EmptyProfileTab(text: String) {
  Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun ProfilePersonalizationStudioDialog(
  initialProfile: PublicProfile,
  initialPrivate: PrivateAccountInfo?,
  onDismiss: () -> Unit,
  onSave: (PublicProfile, PrivateAccountInfo?) -> Unit,
) {
  val history = remember { mutableStateListOf(initialProfile) }
  var historyIdx by remember { mutableIntStateOf(0) }
  var draft by remember { mutableStateOf(initialProfile) }
  var skillsText by remember { mutableStateOf(initialProfile.skills.joinToString(", ")) }
  var interestsText by remember { mutableStateOf(initialProfile.interests.joinToString(", ")) }
  var languagesText by remember { mutableStateOf(initialProfile.languages.joinToString(", ")) }
  var previewMode by remember { mutableStateOf(false) }

  fun pushDraft(next: PublicProfile) {
    while (history.size > historyIdx + 1) history.removeAt(history.lastIndex)
    history.add(next)
    historyIdx = history.lastIndex
    draft = next
  }

  val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
    if (uri != null) pushDraft(draft.copy(avatarUri = uri.toString()))
  }
  val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
    if (uri != null) pushDraft(draft.copy(coverUri = uri.toString()))
  }

  Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
    Surface(
      modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
      color = MaterialTheme.colorScheme.background,
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TextButton(onClick = onDismiss) { Text("Cancel") }
          Row {
            IconButton(
              onClick = {
                if (historyIdx > 0) {
                  historyIdx--
                  draft = history[historyIdx]
                }
              },
              enabled = historyIdx > 0,
            ) { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo") }
            IconButton(
              onClick = {
                if (historyIdx < history.lastIndex) {
                  historyIdx++
                  draft = history[historyIdx]
                }
              },
              enabled = historyIdx < history.lastIndex,
            ) { Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo") }
          }
          Button(onClick = {
            val finalized = draft.copy(
              skills = skillsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
              interests = interestsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
              languages = languagesText.split(",").map { it.trim() }.filter { it.isNotBlank() },
            )
            onSave(finalized, initialPrivate)
          }) { Text("Save") }
        }

        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Profile Personalization Studio", style = MaterialTheme.typography.titleLarge)
            FilterChip(selected = previewMode, onClick = { previewMode = !previewMode }, label = { Text(if (previewMode) "Edit" else "Preview") })
          }

          if (previewMode) {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                UserAvatar(draft.fullName, draft.avatarUri, size = 72.dp, hasTeacherBadge = draft.hasSpecialTeacherBadge)
                Text(draft.fullName, style = MaterialTheme.typography.headlineSmall)
                Text("@${draft.username} • Layout: ${draft.profileLayout}", style = MaterialTheme.typography.bodySmall)
                RoleAndSpecialBadgeRow(draft.accountType, draft.hasSpecialTeacherBadge)
                Text(draft.bio)
              }
            }
          } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              OutlinedButton(
                onClick = { avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.weight(1f),
              ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Avatar Photo")
              }
              OutlinedButton(
                onClick = { coverPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.weight(1f),
              ) {
                Text("Cover Photo")
              }
            }

            OutlinedTextField(value = draft.fullName, onValueChange = { pushDraft(draft.copy(fullName = it)) }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = draft.username, onValueChange = { pushDraft(draft.copy(username = it)) }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = draft.bio, onValueChange = { pushDraft(draft.copy(bio = it)) }, label = { Text("Bio / Introduction") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = draft.school, onValueChange = { pushDraft(draft.copy(school = it)) }, label = { Text("School / University") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = draft.educationLevel, onValueChange = { pushDraft(draft.copy(educationLevel = it)) }, label = { Text("Education Level") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = draft.location, onValueChange = { pushDraft(draft.copy(location = it)) }, label = { Text("Location (Optional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = draft.websiteUrl, onValueChange = { pushDraft(draft.copy(websiteUrl = it)) }, label = { Text("Website / Portfolio Link") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = interestsText, onValueChange = { interestsText = it }, label = { Text("Interests (comma-separated)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = skillsText, onValueChange = { skillsText = it }, label = { Text("Skills (comma-separated)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = languagesText, onValueChange = { languagesText = it }, label = { Text("Languages (comma-separated)") }, modifier = Modifier.fillMaxWidth())

            if (draft.accountType == "teacher") {
              OutlinedTextField(value = draft.teacherExpertise, onValueChange = { pushDraft(draft.copy(teacherExpertise = it)) }, label = { Text("Teaching Subject Expertise") }, modifier = Modifier.fillMaxWidth())
              OutlinedTextField(value = draft.teacherIntro, onValueChange = { pushDraft(draft.copy(teacherIntro = it)) }, label = { Text("Teacher Introduction") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            }

            Text("Profile Layout Style", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf("editorial", "grid", "compact").forEach { layout ->
                FilterChip(
                  selected = draft.profileLayout == layout,
                  onClick = { pushDraft(draft.copy(profileLayout = layout)) },
                  label = { Text(layout.replaceFirstChar { it.uppercase() }) },
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Private Account (Approve followers)", style = MaterialTheme.typography.bodyMedium)
              Switch(checked = draft.isPrivateAccount, onCheckedChange = { pushDraft(draft.copy(isPrivateAccount = it)) })
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeAndAccountSettingsDialog(
  prefs: UserAppPreferences,
  onDismiss: () -> Unit,
  onSetThemeMode: (ThemeModePreference) -> Unit,
  onSetAccent: (AccentPalette) -> Unit,
  onSetFontScale: (Float) -> Unit,
  onSetDensity: (InterfaceDensity) -> Unit,
  onSetHighContrast: (Boolean) -> Unit,
  onDeleteAccount: () -> Unit,
  onSignOut: () -> Unit,
) {
  var confirmDelete by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Appearance & Account")
      }
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text("Theme Mode", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          ThemeModePreference.entries.forEach { mode ->
            FilterChip(
              selected = prefs.themeMode == mode,
              onClick = { onSetThemeMode(mode) },
              label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
            )
          }
        }

        Text("Brand Accent Color", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          AccentPalette.entries.forEach { pal ->
            FilterChip(
              selected = prefs.accentPalette == pal,
              onClick = { onSetAccent(pal) },
              label = { Text(pal.label) },
            )
          }
        }

        Text("Text Size (${(prefs.fontScale * 100).toInt()}%)", style = MaterialTheme.typography.titleSmall)
        Slider(value = prefs.fontScale, onValueChange = onSetFontScale, valueRange = 0.85f..1.25f)

        Text("Interface Density", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          InterfaceDensity.entries.forEach { d ->
            FilterChip(
              selected = prefs.density == d,
              onClick = { onSetDensity(d) },
              label = { Text(d.name.lowercase().replaceFirstChar { it.uppercase() }) },
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("High Contrast Accessibility", style = MaterialTheme.typography.bodyMedium)
          Switch(checked = prefs.highContrast, onCheckedChange = onSetHighContrast)
        }

        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
          onClick = onSignOut,
          modifier = Modifier.fillMaxWidth().testTag("sign_out_button"),
        ) {
          Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Sign Out")
        }

        if (!confirmDelete) {
          TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Delete My Account Data", color = MaterialTheme.colorScheme.error)
          }
        } else {
          Button(
            onClick = onDeleteAccount,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Confirm Permanent Account Deletion")
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) { Text("Done") }
    },
  )
}

@Composable
private fun CreatorDashboardDialog(
  profile: PublicProfile,
  ownPosts: List<Post>,
  followersCount: Int,
  onDismiss: () -> Unit,
) {
  val totalViews = ownPosts.sumOf { it.viewsCount }
  val totalLikes = ownPosts.sumOf { it.likesCount }
  val totalComments = ownPosts.sumOf { it.commentsCount }
  val engagementRate = if (totalViews > 0) ((totalLikes + totalComments) * 100.0 / totalViews) else 0.0

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Creator & Educator Analytics") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Real-time metrics from your published content:", style = MaterialTheme.typography.bodySmall)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Total Views: $totalViews", style = MaterialTheme.typography.titleSmall)
            Text("Total Likes: $totalLikes • Comments: $totalComments")
            Text("Followers: $followersCount • Engagement: ${"%.1f".format(engagementRate)}%")
          }
        }
        Text("Creator Monetization Eligibility", style = MaterialTheme.typography.titleSmall)
        Text(
          "• Verified Educator or 10+ Educational Posts (${ownPosts.size}/10)\n" +
            "• Community Policy Standing: ${if (!profile.isSuspended) "Good Standing" else "Suspended"}\n" +
            "• Payouts require verified payment provider integration & legal KYC.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    },
    confirmButton = { Button(onClick = onDismiss) { Text("Close") } },
  )
}

@Composable
private fun AdminDashboardDialog(
  allProfiles: List<PublicProfile>,
  reports: List<ModerationReport>,
  onDismiss: () -> Unit,
  onToggleBadge: (PublicProfile) -> Unit,
  onToggleSuspend: (PublicProfile) -> Unit,
  onResolveReport: (String, String) -> Unit,
  onRemovePost: (String) -> Unit,
) {
  Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
    Surface(
      modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
      color = MaterialTheme.colorScheme.background,
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("i student Admin Console", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
          Button(onClick = onDismiss) { Text("Close") }
        }

        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          item {
            Text("Special Teacher Badge & User Moderation", style = MaterialTheme.typography.titleMedium)
            Text(
              "Only authorized admins can award or revoke the Special Teacher Badge.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          items(allProfiles, key = { it.userId }) { user ->
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${user.fullName} (@${user.username}) — ${user.accountType}", style = MaterialTheme.typography.titleSmall)
                RoleAndSpecialBadgeRow(user.accountType, user.hasSpecialTeacherBadge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  if (user.accountType == "teacher") {
                    Button(onClick = { onToggleBadge(user) }) {
                      Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(if (user.hasSpecialTeacherBadge) "Revoke Badge" else "Award Teacher Badge")
                    }
                  }
                  OutlinedButton(onClick = { onToggleSuspend(user) }) {
                    Text(if (user.isSuspended) "Unsuspend" else "Suspend")
                  }
                }
              }
            }
          }

          item {
            Text("Moderation Reports (${reports.size})", style = MaterialTheme.typography.titleMedium)
          }

          items(reports, key = { it.id }) { rep ->
            Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${rep.targetType.uppercase()} • Status: ${rep.status}", style = MaterialTheme.typography.labelLarge)
                Text(rep.reason, style = MaterialTheme.typography.bodySmall)
                if (rep.status == "open") {
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                      if (rep.targetType == "post") onRemovePost(rep.targetId)
                      onResolveReport(rep.id, "resolved")
                    }) { Text("Resolve & Remove") }
                    OutlinedButton(onClick = { onResolveReport(rep.id, "dismissed") }) { Text("Dismiss") }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun PublicProfileViewerScreen(
  target: PublicProfile,
  targetPosts: List<Post>,
  isFollowing: Boolean,
  isPending: Boolean,
  onBack: () -> Unit,
  onToggleFollow: () -> Unit,
  onMessage: () -> Unit,
  onBlock: () -> Unit,
  onReport: (String) -> Unit,
) {
  BackHandler(onBack = onBack)
  var showReport by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
      }
      Text("@${target.username}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
      TextButton(onClick = { showReport = true }) { Text("Report") }
      TextButton(onClick = onBlock) { Text("Block", color = MaterialTheme.colorScheme.error) }
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      item {
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              UserAvatar(target.fullName, target.avatarUri, size = 68.dp, hasTeacherBadge = target.hasSpecialTeacherBadge)
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(target.fullName, style = MaterialTheme.typography.titleLarge)
                Text(target.school.ifBlank { "i student Member" }, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                RoleAndSpecialBadgeRow(target.accountType, target.hasSpecialTeacherBadge)
              }
            }
            if (target.bio.isNotBlank()) Text(target.bio, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
              Button(onClick = onToggleFollow, modifier = Modifier.weight(1f)) {
                Text(if (isFollowing) "Following" else if (isPending) "Requested" else "Follow")
              }
              OutlinedButton(onClick = onMessage, modifier = Modifier.weight(1f)) {
                Text("Message")
              }
            }
          }
        }
      }

      if (target.isPrivateAccount && !isFollowing) {
        item {
          Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
              "This account is private. Follow and get approved to view all posts.",
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(20.dp),
            )
          }
        }
      } else {
        items(targetPosts, key = { it.id }) { post ->
          Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(post.subject.ifBlank { post.postType }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.height(4.dp))
              Text(post.caption, style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
      }
    }
  }

  if (showReport) {
    var reason by remember { mutableStateOf("Spam or inappropriate behavior") }
    AlertDialog(
      onDismissRequest = { showReport = false },
      title = { Text("Report @${target.username}") },
      text = { OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") }) },
      confirmButton = {
        Button(onClick = {
          onReport(reason)
          showReport = false
        }) { Text("Submit") }
      },
      dismissButton = { TextButton(onClick = { showReport = false }) { Text("Cancel") } },
    )
  }
}

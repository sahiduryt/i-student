package com.example.ui.auth

import android.net.Uri
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.BrandDarkNavy
import com.example.ui.theme.BrandDeepViolet
import com.example.ui.theme.BrandRoyalPurple
import com.example.ui.theme.BrandScholarGold
import com.example.ui.theme.BrandSoftLavender

@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = onAuthSuccess,
      onUnauthenticated = { /* stay on interactive screen */ },
      scope = scope,
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(BrandDarkNavy, BrandDeepViolet, BrandRoyalPurple)
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 20.dp)
        .widthIn(max = 560.dp)
        .align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_istudent_logo),
          contentDescription = "i student logo",
          modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp)),
          contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "i student",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "LEARN • SHARE • CONNECT",
            style = MaterialTheme.typography.labelSmall,
            color = BrandSoftLavender,
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column {
          Image(
            painter = painterResource(id = R.drawable.img_auth_hero),
            contentDescription = "i student social learning platform illustration",
            modifier = Modifier
              .fillMaxWidth()
              .height(190.dp)
              .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            contentScale = ContentScale.Crop,
          )
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = "Your Next-Generation Campus & Creator Hub",
              style = MaterialTheme.typography.titleLarge,
              color = Color.White,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Share notes, watch Knowledge Reels, join Smart Study Circles, and collaborate with verified educators.",
              style = MaterialTheme.typography.bodyMedium,
              color = BrandSoftLavender.copy(alpha = 0.9f),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              FeaturePill(Icons.Default.Groups, "Study Circles", Modifier.weight(1f))
              FeaturePill(Icons.Default.VideoLibrary, "Knowledge Reels", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              FeaturePill(Icons.Default.AutoStories, "Shared Notes", Modifier.weight(1f))
              FeaturePill(Icons.Default.Verified, "Teacher Badges", Modifier.weight(1f))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      if (errorMessage != null) {
        Surface(
          color = Color(0xFFEF4444).copy(alpha = 0.18f),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        ) {
          Text(
            text = errorMessage ?: "",
            color = Color(0xFFFEE2E2),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp),
          )
        }
      }

      Button(
        onClick = {
          errorMessage = null
          isLoading = true
          onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
              isLoading = false
              onAuthSuccess()
            },
            onAuthError = { msg ->
              isLoading = false
              errorMessage = msg
            },
            scope = scope,
            onAuthCancelled = {
              isLoading = false
            },
          )
        },
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.White,
          contentColor = BrandDarkNavy,
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("google_sign_in_button"),
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            color = BrandRoyalPurple,
            strokeWidth = 2.5.dp,
          )
        } else {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Login,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Sign in with Google",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "Secured by Google Identity & Cloud Firestore rules.\nStudent & Teacher accounts supported.",
        style = MaterialTheme.typography.bodySmall,
        color = BrandSoftLavender.copy(alpha = 0.75f),
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun FeaturePill(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
  Surface(
    color = Color.White.copy(alpha = 0.12f),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(icon, contentDescription = null, tint = BrandScholarGold, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
        maxLines = 1,
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingProfileScreen(
  defaultName: String,
  defaultEmail: String,
  onComplete: (
    fullName: String,
    username: String,
    accountType: String,
    school: String,
    educationLevel: String,
    gender: String,
    dateOfBirth: String,
    interests: List<String>,
    skills: List<String>,
    bio: String,
    avatarUri: String,
    teacherExpertise: String,
    isPrivate: Boolean,
  ) -> Unit,
  onSignOut: () -> Unit,
) {
  var fullName by remember { mutableStateOf(defaultName) }
  var username by remember {
    mutableStateOf(
      defaultName.lowercase().replace(Regex("[^a-z0-9]"), "").take(14).ifBlank { "istudent" }
    )
  }
  var accountType by remember { mutableStateOf("student") }
  var school by remember { mutableStateOf("") }
  var educationLevel by remember { mutableStateOf("Undergraduate") }
  var gender by remember { mutableStateOf("Prefer not to say") }
  var dateOfBirth by remember { mutableStateOf("") }
  var bio by remember { mutableStateOf("") }
  var teacherExpertise by remember { mutableStateOf("") }
  var isPrivate by remember { mutableStateOf(false) }
  var avatarUri by remember { mutableStateOf("") }
  var selectedInterests by remember { mutableStateOf(setOf("Computer Science", "Mathematics")) }
  var skillsText by remember { mutableStateOf("Note Taking, Problem Solving") }

  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      avatarUri = uri.toString()
    }
  }

  val allSubjects = listOf(
    "Computer Science", "Mathematics", "Physics", "Biology",
    "Literature", "Design", "Economics", "Languages",
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .navigationBarsPadding()
      .verticalScroll(rememberScrollState())
      .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text("Create Your i student Profile", style = MaterialTheme.typography.headlineSmall)
        Text(
          text = defaultEmail,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      OutlinedButton(onClick = onSignOut) {
        Text("Switch")
      }
    }

    // Avatar picker
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer)
          .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
          .clickable {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
          }
          .testTag("onboarding_avatar_picker"),
        contentAlignment = Alignment.Center,
      ) {
        if (avatarUri.isNotBlank()) {
          AsyncImage(
            model = avatarUri,
            contentDescription = "Selected profile picture",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
        } else {
          Icon(Icons.Default.CameraAlt, contentDescription = "Pick photo", tint = MaterialTheme.colorScheme.primary)
        }
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column {
        Text("Profile Photo (Optional)", style = MaterialTheme.typography.titleSmall)
        Text(
          "Tap to pick from gallery via Android Photo Picker",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    Text("Account Type", style = MaterialTheme.typography.titleSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      FilterChip(
        selected = accountType == "student",
        onClick = { accountType = "student" },
        label = { Text("Student") },
        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp)) },
        modifier = Modifier.testTag("account_type_student"),
      )
      FilterChip(
        selected = accountType == "teacher",
        onClick = { accountType = "teacher" },
        label = { Text("Teacher / Educator") },
        leadingIcon = { Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp)) },
        modifier = Modifier.testTag("account_type_teacher"),
      )
    }

    OutlinedTextField(
      value = fullName,
      onValueChange = { fullName = it },
      label = { Text("Full Name") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("onboarding_fullname_input"),
    )

    OutlinedTextField(
      value = username,
      onValueChange = { username = it.lowercase().replace(" ", "") },
      label = { Text("Unique Username (3+ chars)") },
      prefix = { Text("@") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("onboarding_username_input"),
    )

    OutlinedTextField(
      value = school,
      onValueChange = { school = it },
      label = { Text("School, College or University") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )

    OutlinedTextField(
      value = educationLevel,
      onValueChange = { educationLevel = it },
      label = { Text("Education Level / Department") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )

    if (accountType == "teacher") {
      OutlinedTextField(
        value = teacherExpertise,
        onValueChange = { teacherExpertise = it },
        label = { Text("Subject Expertise (e.g. Calculus, Organic Chemistry)") },
        modifier = Modifier.fillMaxWidth(),
      )
    }

    Text("Gender", style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      listOf("Male", "Female", "Prefer not to say").forEach { option ->
        FilterChip(
          selected = gender == option,
          onClick = { gender = option },
          label = { Text(option) },
        )
      }
    }

    OutlinedTextField(
      value = dateOfBirth,
      onValueChange = { dateOfBirth = it },
      label = { Text("Date of Birth (Private by default, e.g. 2004-05-14)") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )

    Text("Learning Interests", style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      allSubjects.forEach { subj ->
        FilterChip(
          selected = subj in selectedInterests,
          onClick = {
            selectedInterests = if (subj in selectedInterests) selectedInterests - subj else selectedInterests + subj
          },
          label = { Text(subj) },
        )
      }
    }

    OutlinedTextField(
      value = skillsText,
      onValueChange = { skillsText = it },
      label = { Text("Skills (comma-separated)") },
      modifier = Modifier.fillMaxWidth(),
    )

    OutlinedTextField(
      value = bio,
      onValueChange = { bio = it },
      label = { Text("Profile Introduction / Bio") },
      minLines = 2,
      modifier = Modifier.fillMaxWidth(),
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text("Private Account", style = MaterialTheme.typography.titleSmall)
        Text(
          "Require follow request approval before others follow you",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
    }

    Button(
      onClick = {
        val parsedSkills = skillsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
        onComplete(
          fullName.ifBlank { "i student Member" },
          username.ifBlank { "member123" },
          accountType,
          school,
          educationLevel,
          gender,
          dateOfBirth,
          selectedInterests.toList(),
          parsedSkills,
          bio,
          avatarUri,
          teacherExpertise,
          isPrivate,
        )
      },
      enabled = fullName.isNotBlank() && username.trim().length >= 3,
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("complete_onboarding_button"),
    ) {
      Text("Launch My i student Account", style = MaterialTheme.typography.titleMedium)
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

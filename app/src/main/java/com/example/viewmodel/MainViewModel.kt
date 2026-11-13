package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firestore.unwrapFirestoreException
import com.example.data.model.AccentPalette
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.model.CircleNote
import com.example.data.model.CommunityChallenge
import com.example.data.model.FollowRelation
import com.example.data.model.InterfaceDensity
import com.example.data.model.ModerationReport
import com.example.data.model.NotificationItem
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.model.PrivateAccountInfo
import com.example.data.model.PublicProfile
import com.example.data.model.QuizItem
import com.example.data.model.Story
import com.example.data.model.StudyCircle
import com.example.data.model.StudyGoal
import com.example.data.model.ThemeModePreference
import com.example.data.model.UiState
import com.example.data.model.UserAppPreferences
import com.example.data.repository.ChatRepository
import com.example.data.repository.SocialRepository
import com.example.data.preferences.ThemePreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
  private val socialRepo: SocialRepository,
  private val chatRepo: ChatRepository,
  private val themeRepo: ThemePreferencesRepository,
  val currentUserId: String,
  private val defaultDisplayName: String,
  private val defaultEmail: String,
) : ViewModel() {

  private val _isAdmin = MutableStateFlow(false)
  val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  private val _activePostIdForComments = MutableStateFlow<String?>(null)
  private val _activeCircleIdForNotes = MutableStateFlow<String?>(null)
  private val _activeChatId = MutableStateFlow<String?>(null)

  init {
    viewModelScope.launch {
      _isAdmin.value = socialRepo.checkIsAdmin(currentUserId)
    }
  }

  private fun <T> Flow<T>.toUiStateFlow(label: String): StateFlow<UiState<T>> =
    map<T, UiState<T>> { UiState.Success(it) }
      .catch { error ->
        val firestoreError = error.unwrapFirestoreException()
        if (firestoreError == null && error is CancellationException) throw error
        val root = firestoreError ?: error
        Log.w(TAG, "Error observing $label", root)
        emit(UiState.Error(root.message ?: "Failed to load $label"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = UiState.Loading,
      )

  val themePreferences: StateFlow<UserAppPreferences> = themeRepo.preferencesFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UserAppPreferences())

  val ownProfileState: StateFlow<UiState<PublicProfile?>> =
    socialRepo.observeProfile(currentUserId).toUiStateFlow("own profile")

  val privateInfo: StateFlow<PrivateAccountInfo?> =
    socialRepo.observePrivateInfo(currentUserId)
      .catch { emit(null) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

  val allProfiles: StateFlow<List<PublicProfile>> =
    socialRepo.observeDiscoverableProfiles()
      .catch { emit(emptyList()) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val postsState: StateFlow<UiState<List<Post>>> =
    socialRepo.observePublicPosts().toUiStateFlow("posts")

  val ownPosts: StateFlow<List<Post>> =
    socialRepo.observeUserPosts(currentUserId)
      .catch { emit(emptyList()) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val likedPostIds: StateFlow<Set<String>> =
    socialRepo.observeLikedPostIds(currentUserId)
      .catch { emit(emptySet()) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptySet())

  val savedPostIds: StateFlow<Set<String>> =
    socialRepo.observeSavedPostIds(currentUserId)
      .catch { emit(emptySet()) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptySet())

  val blockedUserIds: StateFlow<Set<String>> =
    socialRepo.observeBlockedUserIds(currentUserId)
      .catch { emit(emptySet()) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptySet())

  val stories: StateFlow<List<Story>> =
    socialRepo.observePublicStories()
      .catch { emit(emptyList()) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val followingRelations: StateFlow<List<FollowRelation>> =
    socialRepo.observeFollowingRelations(currentUserId)
      .catch { emit(emptyList()) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val followerRelations: StateFlow<List<FollowRelation>> =
    socialRepo.observeFollowerRelations(currentUserId)
      .catch { emit(emptyList()) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val studyCircles: StateFlow<List<StudyCircle>> =
    socialRepo.observeStudyCircles()
      .catch { emit(emptyList()) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val quizzes: StateFlow<List<QuizItem>> =
    socialRepo.observeQuizzes()
      .catch { emit(emptyList()) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val challenges: StateFlow<List<CommunityChallenge>> =
    socialRepo.observeChallenges()
      .catch { emit(emptyList()) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val studyGoals: StateFlow<List<StudyGoal>> =
    socialRepo.observeStudyGoals(currentUserId)
      .catch { emit(emptyList()) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val notifications: StateFlow<List<NotificationItem>> =
    socialRepo.observeNotifications(currentUserId)
      .catch { emit(emptyList()) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val chats: StateFlow<List<ChatThread>> =
    chatRepo.observeChats(currentUserId)
      .catch { emit(emptyList()) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val activeComments: StateFlow<List<PostComment>> =
    _activePostIdForComments.flatMapLatest { postId ->
      if (postId.isNullOrBlank()) flowOf(emptyList())
      else socialRepo.observeComments(postId).catch { emit(emptyList()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val activeCircleNotes: StateFlow<List<CircleNote>> =
    _activeCircleIdForNotes.flatMapLatest { circleId ->
      if (circleId.isNullOrBlank()) flowOf(emptyList())
      else socialRepo.observeCircleNotes(circleId).catch { emit(emptyList()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val activeMessages: StateFlow<List<ChatMessage>> =
    _activeChatId.flatMapLatest { chatId ->
      if (chatId.isNullOrBlank()) flowOf(emptyList())
      else chatRepo.observeMessages(chatId, currentUserId).catch { emit(emptyList()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val reports: StateFlow<List<ModerationReport>> =
    _isAdmin.flatMapLatest { admin ->
      socialRepo.observeReports(admin, currentUserId).catch { emit(emptyList()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val unreadNotificationsCount: StateFlow<Int> =
    notifications.map { list -> list.count { !it.isRead } }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

  val pendingFollowRequests: StateFlow<List<FollowRelation>> =
    followerRelations.map { list -> list.filter { it.status == "pending" } }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val acceptedFollowingIds: StateFlow<Set<String>> =
    followingRelations.map { list -> list.filter { it.status == "accepted" }.map { it.targetId }.toSet() }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptySet())

  val pendingOutgoingFollowIds: StateFlow<Set<String>> =
    followingRelations.map { list -> list.filter { it.status == "pending" }.map { it.targetId }.toSet() }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptySet())

  val acceptedFollowerIds: StateFlow<Set<String>> =
    followerRelations.map { list -> list.filter { it.status == "accepted" }.map { it.followerId }.toSet() }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptySet())

  val skillMatchedProfiles: StateFlow<List<Pair<PublicProfile, Int>>> =
    combine(ownProfileState, allProfiles, blockedUserIds) { ownState, profiles, blocked ->
      val own = (ownState as? UiState.Success)?.data
      val ownTokens = ((own?.interests ?: emptyList()) + (own?.skills ?: emptyList()))
        .map { it.trim().lowercase() }
        .filter { it.isNotBlank() }
        .toSet()
      profiles
        .filter { it.userId != currentUserId && it.userId !in blocked && !it.isSuspended }
        .map { candidate ->
          val candTokens = (candidate.interests + candidate.skills).map { it.trim().lowercase() }.toSet()
          val overlap = if (ownTokens.isEmpty()) 0 else ownTokens.intersect(candTokens).size
          val score = (40 + overlap * 20 + if (candidate.hasSpecialTeacherBadge) 10 else 0).coerceAtMost(99)
          candidate to score
        }
        .sortedByDescending { it.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  fun clearToast() {
    _toastMessage.value = null
  }

  fun showToast(msg: String) {
    _toastMessage.value = msg
  }

  fun selectPostForComments(postId: String?) {
    _activePostIdForComments.value = postId
  }

  fun selectCircleForNotes(circleId: String?) {
    _activeCircleIdForNotes.value = circleId
  }

  fun selectChat(chatId: String?) {
    _activeChatId.value = chatId
  }

  // --- Profile & Onboarding ---

  fun createInitialProfile(
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
  ) {
    viewModelScope.launch {
      val available = socialRepo.isUsernameAvailable(username, currentUserId).getOrDefault(true)
      if (!available) {
        _toastMessage.value = "Username is already taken. Please choose another."
        return@launch
      }
      val profile = PublicProfile(
        userId = currentUserId,
        fullName = fullName.ifBlank { defaultDisplayName.ifBlank { "i student Member" } },
        username = username,
        accountType = accountType,
        bio = bio,
        school = school,
        educationLevel = educationLevel,
        avatarUri = avatarUri,
        interests = interests,
        skills = skills,
        teacherExpertise = teacherExpertise,
        isPrivateAccount = isPrivate,
      )
      val priv = PrivateAccountInfo(
        userId = currentUserId,
        email = defaultEmail,
        gender = gender,
        dateOfBirth = dateOfBirth,
      )
      socialRepo.createProfile(profile, priv)
        .onSuccess { _toastMessage.value = "Welcome to i student!" }
        .onFailure { _toastMessage.value = it.localizedMessage ?: "Failed to create profile" }
    }
  }

  fun saveEditedProfile(profile: PublicProfile, priv: PrivateAccountInfo?) {
    viewModelScope.launch {
      socialRepo.updateProfile(profile)
        .onSuccess {
          if (priv != null) {
            socialRepo.updatePrivateInfo(priv)
          }
          _toastMessage.value = "Profile updated"
        }
        .onFailure { _toastMessage.value = it.localizedMessage ?: "Failed to update profile" }
    }
  }

  // --- Posts & Interactions ---

  fun publishPost(
    postType: String,
    caption: String,
    subject: String,
    mediaUris: List<String>,
    hashtags: List<String>,
    locationTag: String,
    visibility: String,
  ) {
    val author = (ownProfileState.value as? UiState.Success)?.data ?: return
    viewModelScope.launch {
      socialRepo.createPost(author, postType, caption, subject, mediaUris, hashtags, locationTag, visibility)
        .onSuccess { _toastMessage.value = "Published to i student!" }
        .onFailure { _toastMessage.value = it.localizedMessage ?: "Upload failed" }
    }
  }

  fun updateOwnPost(postId: String, caption: String, isPinned: Boolean, visibility: String) {
    viewModelScope.launch {
      socialRepo.updatePost(postId, caption, isPinned, visibility)
        .onSuccess { _toastMessage.value = "Post updated" }
        .onFailure { _toastMessage.value = it.localizedMessage ?: "Failed to update post" }
    }
  }

  fun deleteOwnPost(postId: String) {
    viewModelScope.launch {
      socialRepo.deletePost(postId)
        .onSuccess { _toastMessage.value = "Post deleted" }
        .onFailure { _toastMessage.value = it.localizedMessage ?: "Failed to delete post" }
    }
  }

  fun toggleLike(post: Post) {
    val actor = (ownProfileState.value as? UiState.Success)?.data?.fullName ?: defaultDisplayName
    val liked = post.id in likedPostIds.value
    viewModelScope.launch {
      socialRepo.toggleLike(post, actor, liked)
    }
  }

  fun toggleSave(postId: String) {
    val saved = postId in savedPostIds.value
    viewModelScope.launch {
      socialRepo.toggleSave(postId, saved)
        .onSuccess {
          _toastMessage.value = if (saved) "Removed from Saved" else "Saved to your library"
        }
    }
  }

  fun repostPost(original: Post) {
    val author = (ownProfileState.value as? UiState.Success)?.data ?: return
    viewModelScope.launch {
      socialRepo.createPost(
        author = author,
        postType = "repost",
        caption = "Reposted from @${original.authorUsername}: ${original.caption}",
        subject = original.subject,
        mediaUris = original.mediaUris,
        hashtags = original.hashtags,
        locationTag = original.locationTag,
        visibility = "public",
      ).onSuccess { _toastMessage.value = "Shared to your feed!" }
    }
  }

  fun addComment(post: Post, content: String, parentCommentId: String = "") {
    val author = (ownProfileState.value as? UiState.Success)?.data ?: return
    if (content.isBlank()) return
    viewModelScope.launch {
      socialRepo.addComment(post, author, content, parentCommentId)
        .onFailure { _toastMessage.value = it.localizedMessage ?: "Failed to comment" }
    }
  }

  // --- Stories ---

  fun publishStory(
    storyType: String,
    textContent: String,
    mediaUri: String,
    bgHex: String,
    visibility: String,
    isHighlight: Boolean = false,
    highlightTitle: String = "",
  ) {
    val author = (ownProfileState.value as? UiState.Success)?.data ?: return
    viewModelScope.launch {
      socialRepo.createStory(author, storyType, textContent, mediaUri, bgHex, visibility, isHighlight, highlightTitle)
        .onSuccess { _toastMessage.value = "Story shared!" }
        .onFailure { _toastMessage.value = it.localizedMessage ?: "Failed to share story" }
    }
  }

  fun reactToStory(story: Story) {
    val actor = (ownProfileState.value as? UiState.Success)?.data?.fullName ?: defaultDisplayName
    viewModelScope.launch {
      socialRepo.reactToStory(story, actor)
      _toastMessage.value = "Reaction sent!"
    }
  }

  fun deleteStory(storyId: String) {
    viewModelScope.launch {
      socialRepo.deleteStory(storyId)
        .onSuccess { _toastMessage.value = "Story deleted" }
    }
  }

  // --- Follows & Safety ---

  fun toggleFollow(target: PublicProfile) {
    val actor = (ownProfileState.value as? UiState.Success)?.data?.fullName ?: defaultDisplayName
    val isFollowingOrPending = target.userId in acceptedFollowingIds.value || target.userId in pendingOutgoingFollowIds.value
    viewModelScope.launch {
      if (isFollowingOrPending) {
        socialRepo.unfollowUser(target.userId)
      } else {
        socialRepo.followUser(target, actor)
      }
    }
  }

  fun respondToFollowRequest(followId: String, accept: Boolean) {
    viewModelScope.launch {
      socialRepo.respondToFollowRequest(followId, accept)
        .onSuccess { _toastMessage.value = if (accept) "Follow request accepted" else "Request declined" }
    }
  }

  fun blockUser(targetId: String) {
    viewModelScope.launch {
      socialRepo.blockUser(targetId)
        .onSuccess { _toastMessage.value = "User blocked" }
    }
  }

  fun unblockUser(targetId: String) {
    viewModelScope.launch {
      socialRepo.unblockUser(targetId)
        .onSuccess { _toastMessage.value = "User unblocked" }
    }
  }

  fun submitReport(targetId: String, targetType: String, reason: String) {
    viewModelScope.launch {
      socialRepo.submitReport(targetId, targetType, reason)
        .onSuccess { _toastMessage.value = "Report submitted for review" }
        .onFailure { _toastMessage.value = it.localizedMessage ?: "Failed to submit report" }
    }
  }

  // --- Chats ---

  fun startDirectChat(other: PublicProfile, onReady: (String) -> Unit) {
    viewModelScope.launch {
      chatRepo.getOrCreateDirectChat(other.userId, other.fullName)
        .onSuccess { chatId ->
          _activeChatId.value = chatId
          onReady(chatId)
        }
        .onFailure { _toastMessage.value = it.localizedMessage ?: "Could not open chat" }
    }
  }

  fun createGroupChat(title: String, memberIds: List<String>, onReady: (String) -> Unit) {
    viewModelScope.launch {
      chatRepo.createGroupChat(title, memberIds)
        .onSuccess { chatId ->
          _activeChatId.value = chatId
          onReady(chatId)
        }
        .onFailure { _toastMessage.value = it.localizedMessage ?: "Could not create group" }
    }
  }

  fun sendChatMessage(chat: ChatThread, text: String, messageType: String = "text", replyToText: String = "") {
    val senderName = (ownProfileState.value as? UiState.Success)?.data?.fullName ?: defaultDisplayName
    if (text.isBlank()) return
    viewModelScope.launch {
      chatRepo.sendMessage(chat, senderName, text, messageType, replyToText)
        .onFailure { _toastMessage.value = it.localizedMessage ?: "Failed to send message" }
    }
  }

  fun reactToMessage(chatId: String, messageId: String, emoji: String) {
    viewModelScope.launch {
      chatRepo.reactToMessage(chatId, messageId, emoji)
    }
  }

  fun deleteMessage(chatId: String, messageId: String) {
    viewModelScope.launch {
      chatRepo.deleteMessage(chatId, messageId)
    }
  }

  // --- Learning Features ---

  fun createStudyCircle(name: String, subject: String, level: String, description: String) {
    viewModelScope.launch {
      socialRepo.createStudyCircle(name, subject, level, description)
        .onSuccess { _toastMessage.value = "Study Circle created!" }
        .onFailure { _toastMessage.value = it.localizedMessage ?: "Failed to create circle" }
    }
  }

  fun toggleJoinCircle(circle: StudyCircle) {
    viewModelScope.launch {
      socialRepo.toggleCircleMembership(circle)
    }
  }

  fun addCircleNote(circleId: String, title: String, content: String) {
    val authorName = (ownProfileState.value as? UiState.Success)?.data?.fullName ?: defaultDisplayName
    viewModelScope.launch {
      socialRepo.addCircleNote(circleId, authorName, title, content)
        .onSuccess { _toastMessage.value = "Collaborative note added!" }
    }
  }

  fun createQuiz(title: String, subject: String, question: String, options: List<String>, correctIndex: Int, explanation: String) {
    val authorName = (ownProfileState.value as? UiState.Success)?.data?.fullName ?: defaultDisplayName
    viewModelScope.launch {
      socialRepo.createQuiz(authorName, title, subject, question, options, correctIndex, explanation)
        .onSuccess { _toastMessage.value = "Quiz published!" }
    }
  }

  fun createChallenge(title: String, subject: String, description: String, targetDays: Int) {
    viewModelScope.launch {
      socialRepo.createChallenge(title, subject, description, targetDays)
        .onSuccess { _toastMessage.value = "Challenge launched!" }
    }
  }

  fun toggleJoinChallenge(challenge: CommunityChallenge) {
    viewModelScope.launch {
      socialRepo.toggleJoinChallenge(challenge)
    }
  }

  fun addStudyGoal(title: String, subject: String, targetMinutes: Int) {
    viewModelScope.launch {
      socialRepo.addStudyGoal(title, subject, targetMinutes)
        .onSuccess { _toastMessage.value = "Study goal added" }
    }
  }

  fun toggleStudyGoal(goal: StudyGoal) {
    viewModelScope.launch {
      socialRepo.toggleStudyGoal(goal)
    }
  }

  fun markAllNotificationsRead() {
    viewModelScope.launch {
      socialRepo.markAllNotificationsRead(notifications.value)
    }
  }

  // --- Theme Preferences ---

  fun setThemeMode(mode: ThemeModePreference) = viewModelScope.launch { themeRepo.setThemeMode(mode) }
  fun setAccentPalette(accent: AccentPalette) = viewModelScope.launch { themeRepo.setAccentPalette(accent) }
  fun setFontScale(scale: Float) = viewModelScope.launch { themeRepo.setFontScale(scale) }
  fun setDensity(density: InterfaceDensity) = viewModelScope.launch { themeRepo.setDensity(density) }
  fun setHighContrast(enabled: Boolean) = viewModelScope.launch { themeRepo.setHighContrast(enabled) }

  // --- Admin ---

  fun adminToggleTeacherBadge(target: PublicProfile) {
    val adminName = (ownProfileState.value as? UiState.Success)?.data?.fullName ?: "i student Admin"
    viewModelScope.launch {
      socialRepo.adminSetSpecialTeacherBadge(target.userId, !target.hasSpecialTeacherBadge, adminName)
        .onSuccess {
          _toastMessage.value = if (!target.hasSpecialTeacherBadge) {
            "Awarded Special Teacher Badge to ${target.fullName}"
          } else {
            "Removed Special Teacher Badge from ${target.fullName}"
          }
        }
        .onFailure { _toastMessage.value = it.localizedMessage ?: "Admin action failed" }
    }
  }

  fun adminToggleSuspend(target: PublicProfile) {
    viewModelScope.launch {
      socialRepo.adminSetUserSuspended(target.userId, !target.isSuspended)
        .onSuccess { _toastMessage.value = "Updated suspension status" }
    }
  }

  fun adminResolveReport(reportId: String, status: String) {
    viewModelScope.launch {
      socialRepo.adminUpdateReportStatus(reportId, status)
        .onSuccess { _toastMessage.value = "Report marked $status" }
    }
  }

  fun deleteAccountData(onDeleted: () -> Unit) {
    viewModelScope.launch {
      socialRepo.deleteAccountData()
        .onSuccess { onDeleted() }
        .onFailure { _toastMessage.value = it.localizedMessage ?: "Could not delete account data" }
    }
  }

  private companion object {
    const val TAG = "MainViewModel"
  }
}

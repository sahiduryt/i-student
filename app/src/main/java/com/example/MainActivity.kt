package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.UiState
import com.example.data.preferences.ThemePreferencesRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.SocialRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.OnboardingProfileScreen
import com.example.ui.auth.signOut
import com.example.ui.components.IStudentTopBar
import com.example.ui.screens.ChatConversationScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MeProfileScreen
import com.example.ui.screens.NewsExploreScreen
import com.example.ui.screens.PublicProfileViewerScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.screens.UploadCenterScreen
import com.example.ui.theme.IStudentTheme
import com.example.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AppRoot()
    }
  }
}

private fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
  val listener = FirebaseAuth.AuthStateListener { auth ->
    trySend(auth.currentUser)
  }
  addAuthStateListener(listener)
  awaitClose { removeAuthStateListener(listener) }
}

private enum class MainTab(
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
) {
  HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
  NEWS("News", Icons.Filled.Newspaper, Icons.Outlined.Newspaper, "nav_tab_news"),
  UPLOAD("Upload", Icons.Filled.AddBox, Icons.Outlined.AddBox, "nav_tab_upload"),
  FRIENDS("Friends", Icons.Filled.Group, Icons.Outlined.Group, "nav_tab_friends"),
  ME("Me", Icons.Filled.Person, Icons.Outlined.Person, "nav_tab_me"),
}

@Composable
private fun AppRoot() {
  val auth = Firebase.auth
  val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val user = currentUser

  if (user == null) {
    IStudentTheme {
      AuthScreen(onAuthSuccess = { /* authStateFlow updates automatically */ })
    }
  } else {
    key(user.uid) {
      val vm: MainViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
          initializer {
            val app = checkNotNull(this[APPLICATION_KEY])
            val dbId = app.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(dbId)
            MainViewModel(
              socialRepo = SocialRepository(db, Firebase.auth),
              chatRepo = ChatRepository(db, Firebase.auth),
              themeRepo = ThemePreferencesRepository(app),
              currentUserId = user.uid,
              defaultDisplayName = user.displayName ?: "i student Member",
              defaultEmail = user.email ?: "",
            )
          }
        },
      )
      AuthenticatedAppShell(
        vm = vm,
        defaultName = user.displayName ?: "",
        defaultEmail = user.email ?: "",
      )
    }
  }
}

@Composable
private fun AuthenticatedAppShell(
  vm: MainViewModel,
  defaultName: String,
  defaultEmail: String,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  val themePrefs by vm.themePreferences.collectAsStateWithLifecycle()
  val ownProfileState by vm.ownProfileState.collectAsStateWithLifecycle()
  val privateInfo by vm.privateInfo.collectAsStateWithLifecycle()
  val allProfiles by vm.allProfiles.collectAsStateWithLifecycle()
  val postsState by vm.postsState.collectAsStateWithLifecycle()
  val ownPosts by vm.ownPosts.collectAsStateWithLifecycle()
  val likedIds by vm.likedPostIds.collectAsStateWithLifecycle()
  val savedIds by vm.savedPostIds.collectAsStateWithLifecycle()
  val blockedIds by vm.blockedUserIds.collectAsStateWithLifecycle()
  val stories by vm.stories.collectAsStateWithLifecycle()
  val followingIds by vm.acceptedFollowingIds.collectAsStateWithLifecycle()
  val pendingOutgoingIds by vm.pendingOutgoingFollowIds.collectAsStateWithLifecycle()
  val followerIds by vm.acceptedFollowerIds.collectAsStateWithLifecycle()
  val pendingRequests by vm.pendingFollowRequests.collectAsStateWithLifecycle()
  val activeComments by vm.activeComments.collectAsStateWithLifecycle()
  val studyCircles by vm.studyCircles.collectAsStateWithLifecycle()
  val activeCircleNotes by vm.activeCircleNotes.collectAsStateWithLifecycle()
  val quizzes by vm.quizzes.collectAsStateWithLifecycle()
  val challenges by vm.challenges.collectAsStateWithLifecycle()
  val studyGoals by vm.studyGoals.collectAsStateWithLifecycle()
  val chats by vm.chats.collectAsStateWithLifecycle()
  val activeMessages by vm.activeMessages.collectAsStateWithLifecycle()
  val notifications by vm.notifications.collectAsStateWithLifecycle()
  val unreadAlerts by vm.unreadNotificationsCount.collectAsStateWithLifecycle()
  val skillMatches by vm.skillMatchedProfiles.collectAsStateWithLifecycle()
  val isAdmin by vm.isAdmin.collectAsStateWithLifecycle()
  val reports by vm.reports.collectAsStateWithLifecycle()
  val toastMessage by vm.toastMessage.collectAsStateWithLifecycle()

  var selectedTab by remember { mutableStateOf(MainTab.HOME) }
  var friendsSubTab by remember { mutableIntStateOf(0) }
  var showReelsOverlay by remember { mutableStateOf(false) }
  var activeChatId by remember { mutableStateOf<String?>(null) }
  var viewingUserId by remember { mutableStateOf<String?>(null) }

  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(toastMessage) {
    val msg = toastMessage
    if (msg != null) {
      snackbarHostState.showSnackbar(msg)
      vm.clearToast()
    }
  }

  IStudentTheme(preferences = themePrefs) {
    when (val profileRes = ownProfileState) {
      is UiState.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
      is UiState.Error -> {
        OnboardingProfileScreen(
          defaultName = defaultName,
          defaultEmail = defaultEmail,
          onComplete = { full, usern, type, school, level, gender, dob, ints, skills, bio, avatar, exp, priv ->
            vm.createInitialProfile(full, usern, type, school, level, gender, dob, ints, skills, bio, avatar, exp, priv)
          },
          onSignOut = { signOut(credentialManager, scope) },
        )
      }
      is UiState.Success -> {
        val ownProfile = profileRes.data
        if (ownProfile == null) {
          OnboardingProfileScreen(
            defaultName = defaultName,
            defaultEmail = defaultEmail,
            onComplete = { full, usern, type, school, level, gender, dob, ints, skills, bio, avatar, exp, priv ->
              vm.createInitialProfile(full, usern, type, school, level, gender, dob, ints, skills, bio, avatar, exp, priv)
            },
            onSignOut = { signOut(credentialManager, scope) },
          )
        } else {
          val allPosts = (postsState as? UiState.Success)?.data.orEmpty()
          val currentChat = chats.firstOrNull { it.id == activeChatId }
          val viewedProfile = allProfiles.firstOrNull { it.userId == viewingUserId }

          when {
            showReelsOverlay -> {
              val reelsList = allPosts.filter { it.postType == "reel" }.ifEmpty { allPosts }
              ReelsScreen(
                reels = reelsList,
                likedIds = likedIds,
                savedIds = savedIds,
                followingIds = followingIds,
                allProfiles = allProfiles,
                activeComments = activeComments,
                onBack = { showReelsOverlay = false },
                onLike = { vm.toggleLike(it) },
                onSave = { vm.toggleSave(it) },
                onFollow = { vm.toggleFollow(it) },
                onSelectComments = { vm.selectPostForComments(it) },
                onSendComment = { p, txt, parent -> vm.addComment(p, txt, parent) },
                onOpenAuthor = { uid ->
                  showReelsOverlay = false
                  viewingUserId = uid
                },
              )
            }
            currentChat != null -> {
              ChatConversationScreen(
                chat = currentChat,
                currentUserId = vm.currentUserId,
                messages = activeMessages,
                onBack = {
                  activeChatId = null
                  vm.selectChat(null)
                },
                onSendMessage = { txt, type, reply -> vm.sendChatMessage(currentChat, txt, type, reply) },
                onReact = { msgId, emoji -> vm.reactToMessage(currentChat.id, msgId, emoji) },
                onDelete = { msgId -> vm.deleteMessage(currentChat.id, msgId) },
              )
            }
            viewedProfile != null && viewedProfile.userId != vm.currentUserId -> {
              PublicProfileViewerScreen(
                target = viewedProfile,
                targetPosts = allPosts.filter { it.authorId == viewedProfile.userId },
                isFollowing = viewedProfile.userId in followingIds,
                isPending = viewedProfile.userId in pendingOutgoingIds,
                onBack = { viewingUserId = null },
                onToggleFollow = { vm.toggleFollow(viewedProfile) },
                onMessage = {
                  vm.startDirectChat(viewedProfile) { chatId ->
                    viewingUserId = null
                    activeChatId = chatId
                  }
                },
                onBlock = {
                  vm.blockUser(viewedProfile.userId)
                  viewingUserId = null
                },
                onReport = { reason ->
                  vm.submitReport(viewedProfile.userId, "user", reason)
                },
              )
            }
            else -> {
              val useRail = LocalConfiguration.current.screenWidthDp >= 600
              Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                  IStudentTopBar(
                    title = "i student",
                    unreadAlerts = unreadAlerts,
                    onOpenReels = { showReelsOverlay = true },
                    onOpenAlerts = {
                      selectedTab = MainTab.FRIENDS
                      friendsSubTab = 2
                    },
                  )
                },
                bottomBar = {
                  if (!useRail) {
                    NavigationBar {
                      MainTab.entries.forEach { tab ->
                        val selected = selectedTab == tab
                        NavigationBarItem(
                          selected = selected,
                          onClick = { selectedTab = tab },
                          icon = {
                            Icon(
                              imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                              contentDescription = tab.label,
                            )
                          },
                          label = { Text(tab.label, maxLines = 1) },
                          modifier = Modifier.testTag(tab.testTag),
                        )
                      }
                    }
                  }
                },
              ) { innerPadding ->
                Row(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                ) {
                  if (useRail) {
                    NavigationRail {
                      MainTab.entries.forEach { tab ->
                        val selected = selectedTab == tab
                        NavigationRailItem(
                          selected = selected,
                          onClick = { selectedTab = tab },
                          icon = {
                            Icon(
                              imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                              contentDescription = tab.label,
                            )
                          },
                          label = { Text(tab.label) },
                          modifier = Modifier.testTag(tab.testTag),
                        )
                      }
                    }
                  }

                  Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (selectedTab) {
                      MainTab.HOME -> HomeScreen(
                        ownProfile = ownProfile,
                        postsState = postsState,
                        stories = stories,
                        likedIds = likedIds,
                        savedIds = savedIds,
                        followingIds = followingIds,
                        blockedIds = blockedIds,
                        activeComments = activeComments,
                        onOpenReels = { showReelsOverlay = true },
                        onOpenUpload = { selectedTab = MainTab.UPLOAD },
                        onOpenAuthorProfile = { uid ->
                          if (uid == vm.currentUserId) selectedTab = MainTab.ME else viewingUserId = uid
                        },
                        onLikePost = { vm.toggleLike(it) },
                        onSavePost = { vm.toggleSave(it) },
                        onRepost = { vm.repostPost(it) },
                        onSelectPostForComments = { vm.selectPostForComments(it) },
                        onSendComment = { p, txt, parent -> vm.addComment(p, txt, parent) },
                        onUpdatePost = { id, cap, pin, vis -> vm.updateOwnPost(id, cap, pin, vis) },
                        onDeletePost = { vm.deleteOwnPost(it) },
                        onReportPost = { id, reason -> vm.submitReport(id, "post", reason) },
                        onBlockUser = { vm.blockUser(it) },
                        onPublishStory = { t, txt, media, bg, vis, hi, hTitle ->
                          vm.publishStory(t, txt, media, bg, vis, hi, hTitle)
                        },
                        onReactToStory = { vm.reactToStory(it) },
                        onDeleteStory = { vm.deleteStory(it) },
                      )

                      MainTab.NEWS -> NewsExploreScreen(
                        ownProfile = ownProfile,
                        allProfiles = allProfiles,
                        posts = allPosts,
                        studyCircles = studyCircles,
                        activeCircleNotes = activeCircleNotes,
                        quizzes = quizzes,
                        challenges = challenges,
                        studyGoals = studyGoals,
                        onOpenProfile = { uid ->
                          if (uid == vm.currentUserId) selectedTab = MainTab.ME else viewingUserId = uid
                        },
                        onOpenReels = { showReelsOverlay = true },
                        onCreateCircle = { n, s, l, d -> vm.createStudyCircle(n, s, l, d) },
                        onToggleJoinCircle = { vm.toggleJoinCircle(it) },
                        onSelectCircle = { vm.selectCircleForNotes(it) },
                        onAddCircleNote = { cid, t, c -> vm.addCircleNote(cid, t, c) },
                        onCreateQuiz = { t, s, q, opts, corr, exp -> vm.createQuiz(t, s, q, opts, corr, exp) },
                        onCreateChallenge = { t, s, d, days -> vm.createChallenge(t, s, d, days) },
                        onToggleJoinChallenge = { vm.toggleJoinChallenge(it) },
                        onAddStudyGoal = { t, s, m -> vm.addStudyGoal(t, s, m) },
                        onToggleStudyGoal = { vm.toggleStudyGoal(it) },
                      )

                      MainTab.UPLOAD -> UploadCenterScreen(
                        onPublishPost = { type, cap, subj, media, tags, loc, vis ->
                          vm.publishPost(type, cap, subj, media, tags, loc, vis)
                          selectedTab = MainTab.HOME
                        },
                        onPublishStory = { type, txt, media, bg, vis, hi, hTitle ->
                          vm.publishStory(type, txt, media, bg, vis, hi, hTitle)
                          selectedTab = MainTab.HOME
                        },
                      )

                      MainTab.FRIENDS -> FriendsScreen(
                        currentUserId = vm.currentUserId,
                        skillMatchedProfiles = skillMatches,
                        allProfiles = allProfiles,
                        followingIds = followingIds,
                        pendingOutgoingIds = pendingOutgoingIds,
                        pendingFollowRequests = pendingRequests,
                        chats = chats,
                        notifications = notifications,
                        initialTab = friendsSubTab,
                        onToggleFollow = { vm.toggleFollow(it) },
                        onRespondFollowRequest = { id, acc -> vm.respondToFollowRequest(id, acc) },
                        onOpenProfile = { uid ->
                          if (uid == vm.currentUserId) selectedTab = MainTab.ME else viewingUserId = uid
                        },
                        onOpenDirectChat = { other ->
                          vm.startDirectChat(other) { chatId -> activeChatId = chatId }
                        },
                        onOpenExistingChat = { thread ->
                          vm.selectChat(thread.id)
                          activeChatId = thread.id
                        },
                        onCreateGroupChat = { title, members ->
                          vm.createGroupChat(title, members) { chatId -> activeChatId = chatId }
                        },
                        onMarkAllRead = { vm.markAllNotificationsRead() },
                      )

                      MainTab.ME -> MeProfileScreen(
                        profile = ownProfile,
                        privateInfo = privateInfo,
                        ownPosts = ownPosts,
                        allPosts = allPosts,
                        stories = stories,
                        likedPostIds = likedIds,
                        savedPostIds = savedIds,
                        activeComments = activeComments,
                        followersCount = followerIds.size,
                        followingCount = followingIds.size,
                        isAdmin = isAdmin,
                        allProfiles = allProfiles,
                        reports = reports,
                        themePrefs = themePrefs,
                        onSaveProfile = { p, priv -> vm.saveEditedProfile(p, priv) },
                        onSetThemeMode = { vm.setThemeMode(it) },
                        onSetAccent = { vm.setAccentPalette(it) },
                        onSetFontScale = { vm.setFontScale(it) },
                        onSetDensity = { vm.setDensity(it) },
                        onSetHighContrast = { vm.setHighContrast(it) },
                        onLikePost = { vm.toggleLike(it) },
                        onSavePost = { vm.toggleSave(it) },
                        onRepost = { vm.repostPost(it) },
                        onTogglePinPost = { vm.updateOwnPost(it.id, it.caption, !it.isPinned, it.visibility) },
                        onSelectPostForComments = { vm.selectPostForComments(it) },
                        onSendComment = { p, txt, parent -> vm.addComment(p, txt, parent) },
                        onOpenReels = { showReelsOverlay = true },
                        onDeletePost = { vm.deleteOwnPost(it) },
                        onAdminToggleTeacherBadge = { vm.adminToggleTeacherBadge(it) },
                        onAdminToggleSuspend = { vm.adminToggleSuspend(it) },
                        onAdminResolveReport = { id, status -> vm.adminResolveReport(id, status) },
                        onDeleteAccount = {
                          vm.deleteAccountData { signOut(credentialManager, scope) }
                        },
                        onSignOut = { signOut(credentialManager, scope) },
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
  }
}

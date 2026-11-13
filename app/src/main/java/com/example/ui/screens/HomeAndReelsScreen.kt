package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.model.PublicProfile
import com.example.data.model.Story
import com.example.data.model.UiState
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.PostCard
import com.example.ui.components.RoleAndSpecialBadgeRow
import com.example.ui.components.UserAvatar
import com.example.ui.theme.BrandDarkNavy
import com.example.ui.theme.BrandDeepViolet
import com.example.ui.theme.BrandRoyalPurple
import com.example.ui.theme.BrandScholarGold

@Composable
fun HomeScreen(
  ownProfile: PublicProfile?,
  postsState: UiState<List<Post>>,
  stories: List<Story>,
  likedIds: Set<String>,
  savedIds: Set<String>,
  followingIds: Set<String>,
  blockedIds: Set<String>,
  activeComments: List<PostComment>,
  onOpenReels: () -> Unit,
  onOpenUpload: () -> Unit,
  onOpenAuthorProfile: (String) -> Unit,
  onLikePost: (Post) -> Unit,
  onSavePost: (String) -> Unit,
  onRepost: (Post) -> Unit,
  onSelectPostForComments: (String?) -> Unit,
  onSendComment: (Post, String, String) -> Unit,
  onUpdatePost: (String, String, Boolean, String) -> Unit,
  onDeletePost: (String) -> Unit,
  onReportPost: (String, String) -> Unit,
  onBlockUser: (String) -> Unit,
  onPublishStory: (String, String, String, String, String, Boolean, String) -> Unit,
  onReactToStory: (Story) -> Unit,
  onDeleteStory: (String) -> Unit,
) {
  var selectedFeedFilter by remember { mutableStateOf("All") }
  var viewingStoryIndex by remember { mutableStateOf<Int?>(null) }
  var showCreateStory by remember { mutableStateOf(false) }
  var commentTargetPost by remember { mutableStateOf<Post?>(null) }
  var editTargetPost by remember { mutableStateOf<Post?>(null) }
  var reportTargetPost by remember { mutableStateOf<Post?>(null) }

  val filterOptions = listOf("All", "Following", "Educational", "Reels", "Notes")

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Stories strip
    item {
      Column(modifier = Modifier.padding(top = 8.dp)) {
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          item {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .width(72.dp)
                .clickable { showCreateStory = true }
                .testTag("add_story_button"),
            ) {
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .background(Brush.linearGradient(listOf(BrandRoyalPurple, BrandDeepViolet))),
                contentAlignment = Alignment.Center,
              ) {
                Icon(Icons.Default.Add, contentDescription = "Add Story", tint = Color.White, modifier = Modifier.size(28.dp))
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text("Your Story", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
          }

          items(stories.indices.toList()) { idx ->
            val story = stories[idx]
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .width(72.dp)
                .clickable { viewingStoryIndex = idx },
            ) {
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .border(2.5.dp, BrandScholarGold, CircleShape)
                  .padding(3.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
              ) {
                if (story.mediaUri.isNotBlank()) {
                  AsyncImage(
                    model = story.mediaUri,
                    contentDescription = "${story.authorName} story",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                  )
                } else {
                  Text(
                    text = story.authorName.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                  )
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = story.authorName.split(" ").firstOrNull() ?: "Story",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }

    // Knowledge Reels Quick Card + Filter Chips
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenReels)
            .testTag("home_knowledge_reels_banner"),
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(Brush.horizontalGradient(listOf(BrandDarkNavy, BrandDeepViolet, BrandRoyalPurple)))
              .padding(16.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Knowledge Reels",
                  style = MaterialTheme.typography.titleMedium,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "Swipe through bite-sized study explainers & creator lessons",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White.copy(alpha = 0.85f),
                )
              }
              Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(Icons.Default.PlayArrow, contentDescription = null, tint = BrandDeepViolet, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Watch", color = BrandDeepViolet, style = MaterialTheme.typography.labelLarge)
                }
              }
            }
          }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(filterOptions) { option ->
            FilterChip(
              selected = selectedFeedFilter == option,
              onClick = { selectedFeedFilter = option },
              label = { Text(option) },
            )
          }
        }
      }
    }

    // Posts feed
    when (postsState) {
      is UiState.Loading -> {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(48.dp),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator()
          }
        }
      }
      is UiState.Error -> {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          ) {
            Text(
              text = postsState.message,
              color = MaterialTheme.colorScheme.error,
              modifier = Modifier.padding(16.dp),
            )
          }
        }
      }
      is UiState.Success -> {
        val visiblePosts = postsState.data.filter { post ->
          post.authorId !in blockedIds && when (selectedFeedFilter) {
            "Following" -> post.authorId in followingIds || post.authorId == ownProfile?.userId
            "Educational" -> post.postType == "educational"
            "Reels" -> post.postType == "reel"
            "Notes" -> post.postType == "note"
            else -> true
          }
        }

        if (visiblePosts.isEmpty()) {
          item {
            Card(
              shape = RoundedCornerShape(22.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Icon(
                  Icons.Default.School,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "No posts in this feed yet",
                  style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Share your first study note, photo, or Knowledge Reel to kick off the learning stream!",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(onClick = onOpenUpload) {
                  Text("Create First Post")
                }
              }
            }
          }
        } else {
          items(visiblePosts, key = { it.id }) { post ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
              PostCard(
                post = post,
                isOwnPost = post.authorId == ownProfile?.userId,
                isLiked = post.id in likedIds,
                isSaved = post.id in savedIds,
                onAuthorClick = { onOpenAuthorProfile(post.authorId) },
                onLike = { onLikePost(post) },
                onComment = {
                  commentTargetPost = post
                  onSelectPostForComments(post.id)
                },
                onSave = { onSavePost(post.id) },
                onRepost = { onRepost(post) },
                onOpenReels = onOpenReels,
                onEdit = { editTargetPost = post },
                onTogglePin = {
                  onUpdatePost(post.id, post.caption, !post.isPinned, post.visibility)
                },
                onDelete = { onDeletePost(post.id) },
                onReport = { reportTargetPost = post },
                onBlockAuthor = { onBlockUser(post.authorId) },
              )
            }
          }
        }
      }
    }
  }

  // Story Viewer
  viewingStoryIndex?.let { index ->
    if (index in stories.indices) {
      StoryViewerDialog(
        stories = stories,
        initialIndex = index,
        ownUserId = ownProfile?.userId ?: "",
        onDismiss = { viewingStoryIndex = null },
        onReact = onReactToStory,
        onDelete = { storyId ->
          onDeleteStory(storyId)
          viewingStoryIndex = null
        },
      )
    }
  }

  // Create Story Dialog
  if (showCreateStory) {
    CreateStoryDialog(
      onDismiss = { showCreateStory = false },
      onPublish = { type, text, media, bg, vis, highlight, hTitle ->
        onPublishStory(type, text, media, bg, vis, highlight, hTitle)
        showCreateStory = false
      },
    )
  }

  // Comments Sheet
  commentTargetPost?.let { post ->
    CommentsBottomSheet(
      post = post,
      comments = activeComments,
      onDismiss = {
        commentTargetPost = null
        onSelectPostForComments(null)
      },
      onSendComment = { text, parentId -> onSendComment(post, text, parentId) },
    )
  }

  // Edit Post Dialog
  editTargetPost?.let { post ->
    var editedCaption by remember(post.id) { mutableStateOf(post.caption) }
    AlertDialog(
      onDismissRequest = { editTargetPost = null },
      title = { Text("Edit Post") },
      text = {
        OutlinedTextField(
          value = editedCaption,
          onValueChange = { editedCaption = it },
          label = { Text("Caption") },
          minLines = 3,
          modifier = Modifier.fillMaxWidth(),
        )
      },
      confirmButton = {
        Button(onClick = {
          onUpdatePost(post.id, editedCaption, post.isPinned, post.visibility)
          editTargetPost = null
        }) { Text("Save") }
      },
      dismissButton = {
        TextButton(onClick = { editTargetPost = null }) { Text("Cancel") }
      },
    )
  }

  // Report Dialog
  reportTargetPost?.let { post ->
    var reason by remember { mutableStateOf("Inappropriate or misleading educational claim") }
    AlertDialog(
      onDismissRequest = { reportTargetPost = null },
      title = { Text("Report Content") },
      text = {
        OutlinedTextField(
          value = reason,
          onValueChange = { reason = it },
          label = { Text("Reason") },
          modifier = Modifier.fillMaxWidth(),
        )
      },
      confirmButton = {
        Button(onClick = {
          onReportPost(post.id, reason)
          reportTargetPost = null
        }) { Text("Submit Report") }
      },
      dismissButton = {
        TextButton(onClick = { reportTargetPost = null }) { Text("Cancel") }
      },
    )
  }
}

@Composable
private fun StoryViewerDialog(
  stories: List<Story>,
  initialIndex: Int,
  ownUserId: String,
  onDismiss: () -> Unit,
  onReact: (Story) -> Unit,
  onDelete: (String) -> Unit,
) {
  var currentIndex by remember { mutableStateOf(initialIndex) }
  val story = stories.getOrNull(currentIndex) ?: return

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(BrandDarkNavy)
        .statusBarsPadding()
        .navigationBarsPadding(),
    ) {
      if (story.mediaUri.isNotBlank()) {
        AsyncImage(
          model = story.mediaUri,
          contentDescription = "Story image",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
        )
      } else {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BrandDeepViolet, BrandRoyalPurple, BrandDarkNavy))),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = story.textContent.ifBlank { "i student Story" },
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp),
          )
        }
      }

      // Tap zones for prev/next
      Row(modifier = Modifier.fillMaxSize()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .clickable { if (currentIndex > 0) currentIndex-- },
        )
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .clickable {
              if (currentIndex < stories.lastIndex) currentIndex++ else onDismiss()
            },
        )
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
          .align(Alignment.TopCenter),
      ) {
        LinearProgressIndicator(
          progress = { (currentIndex + 1f) / stories.size.coerceAtLeast(1) },
          modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
          color = Color.White,
          trackColor = Color.White.copy(alpha = 0.3f),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(story.authorName, color = Color.White, style = MaterialTheme.typography.titleMedium)
            if (story.visibility == "close_friends") {
              Text("Close Friends", color = BrandScholarGold, style = MaterialTheme.typography.labelSmall)
            }
          }
          Row {
            if (story.authorId == ownUserId) {
              IconButton(onClick = { onDelete(story.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete story", tint = Color.White)
              }
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close story", tint = Color.White)
            }
          }
        }
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .align(Alignment.BottomCenter),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (story.mediaUri.isNotBlank() && story.textContent.isNotBlank()) {
          Surface(color = Color.Black.copy(alpha = 0.55f), shape = RoundedCornerShape(12.dp)) {
            Text(
              text = story.textContent,
              color = Color.White,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(10.dp),
            )
          }
        } else {
          Spacer(modifier = Modifier.width(1.dp))
        }
        Button(onClick = { onReact(story) }) {
          Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("React (${story.reactionsCount})")
        }
      }
    }
  }
}

@Composable
private fun CreateStoryDialog(
  onDismiss: () -> Unit,
  onPublish: (String, String, String, String, String, Boolean, String) -> Unit,
) {
  var text by remember { mutableStateOf("") }
  var mediaUri by remember { mutableStateOf("") }
  var closeFriendsOnly by remember { mutableStateOf(false) }
  var saveAsHighlight by remember { mutableStateOf(false) }
  var highlightTitle by remember { mutableStateOf("") }

  val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
    if (uri != null) mediaUri = uri.toString()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Create 24h Story") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
          onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(if (mediaUri.isBlank()) "Pick Story Photo (Optional)" else "Photo Selected")
        }
        OutlinedTextField(
          value = text,
          onValueChange = { text = it },
          label = { Text("Story text / study update") },
          minLines = 2,
          modifier = Modifier.fillMaxWidth(),
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Close Friends only", style = MaterialTheme.typography.bodyMedium)
          Switch(checked = closeFriendsOnly, onCheckedChange = { closeFriendsOnly = it })
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Pin as Profile Highlight", style = MaterialTheme.typography.bodyMedium)
          Switch(checked = saveAsHighlight, onCheckedChange = { saveAsHighlight = it })
        }
        if (saveAsHighlight) {
          OutlinedTextField(
            value = highlightTitle,
            onValueChange = { highlightTitle = it },
            label = { Text("Highlight Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onPublish(
            if (mediaUri.isNotBlank()) "photo" else "text",
            text.ifBlank { "Sharing my study progress on i student!" },
            mediaUri,
            "#4C1D95",
            if (closeFriendsOnly) "close_friends" else "public",
            saveAsHighlight,
            highlightTitle,
          )
        },
        enabled = text.isNotBlank() || mediaUri.isNotBlank(),
      ) { Text("Share Story") }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  )
}

@Composable
fun ReelsScreen(
  reels: List<Post>,
  likedIds: Set<String>,
  savedIds: Set<String>,
  followingIds: Set<String>,
  allProfiles: List<PublicProfile>,
  activeComments: List<PostComment>,
  onBack: () -> Unit,
  onLike: (Post) -> Unit,
  onSave: (String) -> Unit,
  onFollow: (PublicProfile) -> Unit,
  onSelectComments: (String?) -> Unit,
  onSendComment: (Post, String, String) -> Unit,
  onOpenAuthor: (String) -> Unit,
) {
  BackHandler(onBack = onBack)

  var isPlaying by remember { mutableStateOf(true) }
  var speedLabel by remember { mutableStateOf("1.0x") }
  var qualityLabel by remember { mutableStateOf("Auto 1080p") }
  var commentPost by remember { mutableStateOf<Post?>(null) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(BrandDarkNavy)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    if (reels.isEmpty()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        Text("No Knowledge Reels Yet", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          "Create a Knowledge Reel from the Upload tab to share bite-sized lessons!",
          color = Color.White.copy(alpha = 0.75f),
          textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Back to Feed") }
      }
    } else {
      val pagerState = rememberPagerState(pageCount = { reels.size })
      VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
      ) { page ->
        val reel = reels[page]
        val authorProfile = allProfiles.firstOrNull { it.userId == reel.authorId }
        val isFollowing = reel.authorId in followingIds

        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BrandDarkNavy, BrandDeepViolet, BrandDarkNavy)))
            .clickable { isPlaying = !isPlaying },
        ) {
          if (reel.mediaUris.isNotEmpty()) {
            AsyncImage(
              model = reel.mediaUris.first(),
              contentDescription = reel.caption,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
            )
          }

          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent, Color.Black.copy(alpha = 0.78f))
                )
              ),
          )

          // Center Play/Pause indicator
          Surface(
            color = Color.Black.copy(alpha = 0.45f),
            shape = CircleShape,
            modifier = Modifier.align(Alignment.Center),
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              tint = Color.White,
              modifier = Modifier
                .padding(16.dp)
                .size(36.dp),
            )
          }

          // Right action rail
          Column(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(end = 14.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              IconButton(onClick = { onLike(reel) }) {
                Icon(
                  imageVector = if (reel.id in likedIds) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                  contentDescription = "Like reel",
                  tint = if (reel.id in likedIds) Color(0xFFEF4444) else Color.White,
                  modifier = Modifier.size(30.dp),
                )
              }
              Text("${reel.likesCount}", color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              IconButton(onClick = {
                commentPost = reel
                onSelectComments(reel.id)
              }) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comments", tint = Color.White, modifier = Modifier.size(28.dp))
              }
              Text("${reel.commentsCount}", color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = { onSave(reel.id) }) {
              Icon(
                imageVector = if (reel.id in savedIds) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Save reel",
                tint = if (reel.id in savedIds) BrandScholarGold else Color.White,
                modifier = Modifier.size(28.dp),
              )
            }
            IconButton(onClick = {
              speedLabel = when (speedLabel) {
                "1.0x" -> "1.25x"
                "1.25x" -> "1.5x"
                "1.5x" -> "2.0x"
                else -> "1.0x"
              }
            }) {
              Icon(Icons.Default.Speed, contentDescription = "Playback speed", tint = Color.White)
            }
            Text(speedLabel, color = Color.White, style = MaterialTheme.typography.labelSmall)
            IconButton(onClick = {
              qualityLabel = if (qualityLabel == "Auto 1080p") "Data Saver 480p" else "Auto 1080p"
            }) {
              Icon(Icons.Default.Tune, contentDescription = "Video quality", tint = Color.White)
            }
          }

          // Bottom metadata
          Column(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(start = 16.dp, end = 84.dp, bottom = 28.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              UserAvatar(
                name = reel.authorName,
                avatarUri = reel.authorAvatarUri,
                size = 38.dp,
                hasTeacherBadge = reel.authorHasTeacherBadge,
                onClick = { onOpenAuthor(reel.authorId) },
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.clickable { onOpenAuthor(reel.authorId) }) {
                Text(reel.authorName, color = Color.White, style = MaterialTheme.typography.titleSmall)
                RoleAndSpecialBadgeRow(reel.authorAccountType, reel.authorHasTeacherBadge)
              }
              if (authorProfile != null) {
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedButton(
                  onClick = { onFollow(authorProfile) },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                  modifier = Modifier.height(32.dp),
                ) {
                  Text(if (isFollowing) "Following" else "Follow", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (reel.subject.isNotBlank()) {
              Surface(color = BrandRoyalPurple, shape = RoundedCornerShape(8.dp)) {
                Text(
                  reel.subject,
                  color = Color.White,
                  style = MaterialTheme.typography.labelSmall,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
            }
            Text(reel.caption, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text(
              "Quality: $qualityLabel • Swipe up for next",
              color = Color.White.copy(alpha = 0.7f),
              style = MaterialTheme.typography.labelSmall,
              modifier = Modifier.padding(top = 4.dp),
            )
          }
        }
      }
    }

    // Top back bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
        .align(Alignment.TopCenter),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(onClick = onBack, modifier = Modifier.testTag("reels_back_button")) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
      }
      Text("Knowledge Reels", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.width(48.dp))
    }
  }

  commentPost?.let { post ->
    CommentsBottomSheet(
      post = post,
      comments = activeComments,
      onDismiss = {
        commentPost = null
        onSelectComments(null)
      },
      onSendComment = { text, parentId -> onSendComment(post, text, parentId) },
    )
  }
}

package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.ui.theme.BrandDeepViolet
import com.example.ui.theme.BrandRoyalPurple
import com.example.ui.theme.BrandScholarGold
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IStudentTopBar(
  title: String,
  unreadAlerts: Int,
  onOpenReels: () -> Unit,
  onOpenAlerts: () -> Unit,
) {
  TopAppBar(
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
    ),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
          painter = painterResource(id = R.drawable.ic_istudent_logo),
          contentDescription = "i student",
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp)),
          contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "LEARN • SHARE • CONNECT",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    },
    actions = {
      IconButton(
        onClick = onOpenReels,
        modifier = Modifier.testTag("topbar_reels_button"),
      ) {
        Icon(
          imageVector = Icons.Default.PlayCircleFilled,
          contentDescription = "Knowledge Reels",
          tint = MaterialTheme.colorScheme.primary,
        )
      }
      IconButton(
        onClick = onOpenAlerts,
        modifier = Modifier.testTag("topbar_alerts_button"),
      ) {
        BadgedBox(
          badge = {
            if (unreadAlerts > 0) {
              Badge { Text(unreadAlerts.coerceAtMost(99).toString()) }
            }
          }
        ) {
          Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
          )
        }
      }
    },
  )
}

@Composable
fun UserAvatar(
  name: String,
  avatarUri: String,
  size: Dp = 44.dp,
  hasTeacherBadge: Boolean = false,
  onClick: (() -> Unit)? = null,
) {
  val initials = name.trim().split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString("") { it.first().uppercase() }
    .ifBlank { "IS" }

  val ringBrush = if (hasTeacherBadge) {
    Brush.linearGradient(listOf(BrandScholarGold, BrandRoyalPurple))
  } else {
    Brush.linearGradient(listOf(BrandRoyalPurple, BrandDeepViolet))
  }

  Box(
    modifier = Modifier
      .size(size)
      .clip(CircleShape)
      .background(ringBrush)
      .padding(2.dp)
      .clip(CircleShape)
      .background(MaterialTheme.colorScheme.primaryContainer)
      .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    contentAlignment = Alignment.Center,
  ) {
    if (avatarUri.isNotBlank()) {
      AsyncImage(
        model = avatarUri,
        contentDescription = "$name avatar",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
      )
    } else {
      Text(
        text = initials,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
fun RoleAndSpecialBadgeRow(
  accountType: String,
  hasSpecialTeacherBadge: Boolean,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Surface(
      color = if (accountType == "teacher") {
        MaterialTheme.colorScheme.secondaryContainer
      } else {
        MaterialTheme.colorScheme.primaryContainer
      },
      shape = RoundedCornerShape(8.dp),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Default.School,
          contentDescription = null,
          modifier = Modifier.size(12.dp),
          tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
          text = if (accountType == "teacher") "Teacher" else "Student",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
      }
    }

    if (hasSpecialTeacherBadge) {
      Surface(
        color = BrandScholarGold.copy(alpha = 0.20f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.border(1.dp, BrandScholarGold.copy(alpha = 0.7f), RoundedCornerShape(8.dp)),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = "Special Teacher Badge",
            modifier = Modifier.size(13.dp),
            tint = BrandScholarGold,
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "Verified Educator",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = BrandScholarGold,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostCard(
  post: Post,
  isOwnPost: Boolean,
  isLiked: Boolean,
  isSaved: Boolean,
  onAuthorClick: () -> Unit,
  onLike: () -> Unit,
  onComment: () -> Unit,
  onSave: () -> Unit,
  onRepost: () -> Unit,
  onOpenReels: () -> Unit,
  onEdit: () -> Unit,
  onTogglePin: () -> Unit,
  onDelete: () -> Unit,
  onReport: () -> Unit,
  onBlockAuthor: () -> Unit,
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("post_card_${post.id}"),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      if (post.isPinned) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(bottom = 8.dp),
        ) {
          Icon(
            Icons.Default.PushPin,
            contentDescription = "Pinned post",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            "Pinned Post",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        UserAvatar(
          name = post.authorName,
          avatarUri = post.authorAvatarUri,
          size = 44.dp,
          hasTeacherBadge = post.authorHasTeacherBadge,
          onClick = onAuthorClick,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
          modifier = Modifier
            .weight(1f)
            .clickable(onClick = onAuthorClick),
        ) {
          Text(
            text = post.authorName,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "@${post.authorUsername} • ${formatTimestamp(post.createdAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(3.dp))
          RoleAndSpecialBadgeRow(post.authorAccountType, post.authorHasTeacherBadge)
        }

        Box {
          IconButton(onClick = { menuExpanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Post options")
          }
          DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            if (isOwnPost) {
              DropdownMenuItem(
                text = { Text(if (post.isPinned) "Unpin Post" else "Pin Post") },
                onClick = {
                  menuExpanded = false
                  onTogglePin()
                },
              )
              DropdownMenuItem(
                text = { Text("Edit Post") },
                onClick = {
                  menuExpanded = false
                  onEdit()
                },
              )
              DropdownMenuItem(
                text = { Text("Delete Post", color = MaterialTheme.colorScheme.error) },
                onClick = {
                  menuExpanded = false
                  onDelete()
                },
              )
            } else {
              DropdownMenuItem(
                text = { Text("Report Content") },
                onClick = {
                  menuExpanded = false
                  onReport()
                },
              )
              DropdownMenuItem(
                text = { Text("Block @${post.authorUsername}", color = MaterialTheme.colorScheme.error) },
                onClick = {
                  menuExpanded = false
                  onBlockAuthor()
                },
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = RoundedCornerShape(8.dp),
        ) {
          Text(
            text = post.postType.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
          )
        }
        if (post.subject.isNotBlank()) {
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp),
          ) {
            Text(
              text = post.subject,
              style = MaterialTheme.typography.labelSmall,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = post.caption,
        style = MaterialTheme.typography.bodyLarge,
      )

      if (post.mediaUris.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { if (post.postType == "reel") onOpenReels() },
        ) {
          AsyncImage(
            model = post.mediaUris.first(),
            contentDescription = "Post media",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
          if (post.postType == "reel") {
            Surface(
              color = Color.Black.copy(alpha = 0.55f),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .align(Alignment.Center)
                .padding(12.dp),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(Icons.Default.PlayCircleFilled, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Watch Knowledge Reel", color = Color.White, style = MaterialTheme.typography.labelLarge)
              }
            }
          }
        }
      } else if (post.postType == "reel") {
        Spacer(modifier = Modifier.height(12.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(BrandDeepViolet, BrandRoyalPurple)))
            .clickable(onClick = onOpenReels),
          contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PlayCircleFilled, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text("Open in Knowledge Reels Player", color = Color.White, style = MaterialTheme.typography.titleSmall)
          }
        }
      }

      if (post.hashtags.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          post.hashtags.forEach { tag ->
            val clean = if (tag.startsWith("#")) tag else "#$tag"
            Text(
              text = clean,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }

      if (post.locationTag.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = post.locationTag,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onLike,
            modifier = Modifier.testTag("like_button_${post.id}"),
          ) {
            Icon(
              imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Like post",
              tint = if (isLiked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Text("${post.likesCount}", style = MaterialTheme.typography.labelMedium)

          Spacer(modifier = Modifier.width(8.dp))
          IconButton(
            onClick = onComment,
            modifier = Modifier.testTag("comment_button_${post.id}"),
          ) {
            Icon(
              Icons.Default.ChatBubbleOutline,
              contentDescription = "Comment",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Text("${post.commentsCount}", style = MaterialTheme.typography.labelMedium)

          Spacer(modifier = Modifier.width(8.dp))
          IconButton(onClick = onRepost) {
            Icon(
              Icons.Default.Repeat,
              contentDescription = "Repost",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.Visibility,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            "${post.viewsCount}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          IconButton(onClick = onSave) {
            Icon(
              imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = "Save post",
              tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
  post: Post,
  comments: List<PostComment>,
  onDismiss: () -> Unit,
  onSendComment: (String, String) -> Unit,
) {
  var commentText by remember { mutableStateOf("") }
  var replyTo by remember { mutableStateOf<PostComment?>(null) }

  ModalBottomSheet(onDismissRequest = onDismiss) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .padding(bottom = 24.dp),
    ) {
      Text(
        text = "Comments (${comments.size})",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 12.dp),
      )

      if (comments.isEmpty()) {
        Text(
          text = "No comments yet. Start the study discussion!",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(vertical = 24.dp),
        )
      } else {
        val topLevel = comments.filter { it.parentCommentId.isBlank() }
        val repliesByParent = comments.filter { it.parentCommentId.isNotBlank() }.groupBy { it.parentCommentId }

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(topLevel, key = { it.id }) { comment ->
            Column {
              CommentItemRow(
                comment = comment,
                isReply = false,
                onReply = { replyTo = comment },
              )
              repliesByParent[comment.id].orEmpty().forEach { reply ->
                CommentItemRow(
                  comment = reply,
                  isReply = true,
                  onReply = { replyTo = comment },
                )
              }
            }
          }
        }
      }

      if (replyTo != null) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            "Replying to @${replyTo?.authorUsername}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
          )
          IconButton(onClick = { replyTo = null }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(16.dp))
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedTextField(
          value = commentText,
          onValueChange = { commentText = it },
          placeholder = { Text("Add a helpful comment...") },
          modifier = Modifier.weight(1f),
          singleLine = true,
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
          onClick = {
            if (commentText.isNotBlank()) {
              onSendComment(commentText, replyTo?.id ?: "")
              commentText = ""
              replyTo = null
            }
          },
          modifier = Modifier.testTag("send_comment_button"),
        ) {
          Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send comment", tint = MaterialTheme.colorScheme.primary)
        }
      }
    }
  }
}

@Composable
private fun CommentItemRow(
  comment: PostComment,
  isReply: Boolean,
  onReply: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = if (isReply) 24.dp else 0.dp, top = if (isReply) 4.dp else 0.dp),
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "${comment.authorName} (@${comment.authorUsername})",
          style = MaterialTheme.typography.labelLarge,
        )
        TextButton(onClick = onReply, modifier = Modifier.height(28.dp)) {
          Text("Reply", style = MaterialTheme.typography.labelSmall)
        }
      }
      Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)
    }
  }
}

fun formatTimestamp(ts: Timestamp?): String {
  val date = ts?.toDate() ?: return "Just now"
  val diff = System.currentTimeMillis() - date.time
  return when {
    diff < 60_000L -> "Just now"
    diff < 3_600_000L -> "${diff / 60_000L}m ago"
    diff < 86_400_000L -> "${diff / 3_600_000L}h ago"
    else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
  }
}

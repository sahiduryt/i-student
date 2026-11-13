package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.model.FollowRelation
import com.example.data.model.NotificationItem
import com.example.data.model.PublicProfile
import com.example.ui.components.RoleAndSpecialBadgeRow
import com.example.ui.components.UserAvatar
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandScholarGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
  currentUserId: String,
  skillMatchedProfiles: List<Pair<PublicProfile, Int>>,
  allProfiles: List<PublicProfile>,
  followingIds: Set<String>,
  pendingOutgoingIds: Set<String>,
  pendingFollowRequests: List<FollowRelation>,
  chats: List<ChatThread>,
  notifications: List<NotificationItem>,
  initialTab: Int = 0,
  onToggleFollow: (PublicProfile) -> Unit,
  onRespondFollowRequest: (String, Boolean) -> Unit,
  onOpenProfile: (String) -> Unit,
  onOpenDirectChat: (PublicProfile) -> Unit,
  onOpenExistingChat: (ChatThread) -> Unit,
  onCreateGroupChat: (String, List<String>) -> Unit,
  onMarkAllRead: () -> Unit,
) {
  var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
  var peopleFilter by remember { mutableStateOf("All") }
  var searchQuery by remember { mutableStateOf("") }
  var showCreateGroup by remember { mutableStateOf(false) }

  val tabs = listOf("Chats", "People & Match", "Alerts (${notifications.count { !it.isRead }})")

  Column(modifier = Modifier.fillMaxSize()) {
    PrimaryTabRow(selectedTabIndex = selectedTab) {
      tabs.forEachIndexed { idx, title ->
        Tab(
          selected = selectedTab == idx,
          onClick = { selectedTab = idx },
          text = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        )
      }
    }

    when (selectedTab) {
      0 -> {
        // Real-time Chats
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Messages", style = MaterialTheme.typography.titleMedium)
              OutlinedButton(onClick = { showCreateGroup = true }, modifier = Modifier.testTag("new_group_chat_button")) {
                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Group")
              }
            }
          }

          if (chats.isEmpty()) {
            item {
              Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                  Spacer(modifier = Modifier.height(8.dp))
                  Text("No conversations yet", style = MaterialTheme.typography.titleMedium)
                  Text(
                    "Open People & Match to message classmates or verified teachers in real time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          } else {
            items(chats, key = { it.id }) { thread ->
              val otherUid = thread.participantIds.firstOrNull { it != currentUserId }
              val otherProfile = allProfiles.firstOrNull { it.userId == otherUid }
              val title = if (thread.isGroup) thread.title else (otherProfile?.fullName ?: thread.title.ifBlank { "Chat" })

              Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onOpenExistingChat(thread) }
                  .testTag("chat_thread_${thread.id}"),
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  UserAvatar(
                    name = title,
                    avatarUri = otherProfile?.avatarUri ?: "",
                    size = 46.dp,
                    hasTeacherBadge = otherProfile?.hasSpecialTeacherBadge == true,
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                      Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                      Text(formatTimestamp(thread.updatedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                      text = thread.lastMessageText,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                }
              }
            }
          }
        }
      }

      1 -> {
        // People, Follow Requests & Skill Match
        Column(modifier = Modifier.fillMaxSize()) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search students, teachers, skills...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
          )
          Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            listOf("All", "Students", "Teachers", "Following").forEach { f ->
              FilterChip(selected = peopleFilter == f, onClick = { peopleFilter = f }, label = { Text(f) })
            }
          }

          val query = searchQuery.trim().lowercase()
          val filtered = skillMatchedProfiles.filter { (user, _) ->
            val matchesRole = when (peopleFilter) {
              "Students" -> user.accountType == "student"
              "Teachers" -> user.accountType == "teacher"
              "Following" -> user.userId in followingIds
              else -> true
            }
            val matchesQuery = query.isBlank() ||
              user.fullName.lowercase().contains(query) ||
              user.username.lowercase().contains(query) ||
              user.skills.any { it.lowercase().contains(query) } ||
              user.interests.any { it.lowercase().contains(query) }
            matchesRole && matchesQuery
          }

          LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            if (pendingFollowRequests.isNotEmpty()) {
              item {
                Text("Incoming Follow Requests (${pendingFollowRequests.size})", style = MaterialTheme.typography.titleMedium)
              }
              items(pendingFollowRequests, key = { it.id }) { req ->
                val requester = allProfiles.firstOrNull { it.userId == req.followerId }
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    UserAvatar(requester?.fullName ?: "User", requester?.avatarUri ?: "", size = 40.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(requester?.fullName ?: "Student", style = MaterialTheme.typography.titleSmall)
                      Text("@${requester?.username ?: "user"}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onRespondFollowRequest(req.id, true) }) {
                      Icon(Icons.Default.Check, contentDescription = "Accept", tint = BrandEmerald)
                    }
                    IconButton(onClick = { onRespondFollowRequest(req.id, false) }) {
                      Icon(Icons.Default.Close, contentDescription = "Decline", tint = MaterialTheme.colorScheme.error)
                    }
                  }
                }
              }
            }

            if (filtered.isEmpty()) {
              item {
                Text("No matching members found yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            } else {
              items(filtered, key = { it.first.userId }) { (user, matchPct) ->
                val isFollowing = user.userId in followingIds
                val isPending = user.userId in pendingOutgoingIds
                Card(
                  shape = RoundedCornerShape(18.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenProfile(user.userId) },
                ) {
                  Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      UserAvatar(user.fullName, user.avatarUri, size = 46.dp, hasTeacherBadge = user.hasSpecialTeacherBadge)
                      Spacer(modifier = Modifier.width(12.dp))
                      Column(modifier = Modifier.weight(1f)) {
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically,
                        ) {
                          Text(user.fullName, style = MaterialTheme.typography.titleSmall)
                          Surface(color = BrandScholarGold.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                            Text("$matchPct% Skill Match", style = MaterialTheme.typography.labelSmall, color = BrandScholarGold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                          }
                        }
                        Text("@${user.username} • ${user.school.ifBlank { "i student" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(3.dp))
                        RoleAndSpecialBadgeRow(user.accountType, user.hasSpecialTeacherBadge)
                      }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                      Button(
                        onClick = { onToggleFollow(user) },
                        modifier = Modifier.weight(1f),
                      ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          when {
                            isFollowing -> "Following"
                            isPending -> "Requested"
                            else -> "Follow"
                          }
                        )
                      }
                      OutlinedButton(
                        onClick = { onOpenDirectChat(user) },
                        modifier = Modifier.weight(1f),
                      ) {
                        Text("Message")
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      else -> {
        // Notifications Center
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Notifications", style = MaterialTheme.typography.titleMedium)
              TextButton(onClick = onMarkAllRead) { Text("Mark All Read") }
            }
          }

          if (notifications.isEmpty()) {
            item {
              Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                  Spacer(modifier = Modifier.height(8.dp))
                  Text("You're all caught up!", style = MaterialTheme.typography.titleSmall)
                }
              }
            }
          } else {
            items(notifications, key = { it.id }) { notif ->
              Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  if (!notif.isRead) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                  }
                  Column(modifier = Modifier.weight(1f)) {
                    Text(notif.message, style = MaterialTheme.typography.bodyMedium, fontWeight = if (!notif.isRead) FontWeight.SemiBold else FontWeight.Normal)
                    Text(formatTimestamp(notif.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (showCreateGroup) {
    var groupTitle by remember { mutableStateOf("") }
    val selectedMembers = remember { mutableStateListOf<String>() }
    AlertDialog(
      onDismissRequest = { showCreateGroup = false },
      title = { Text("Create Group Chat") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = groupTitle, onValueChange = { groupTitle = it }, label = { Text("Group Name") }, singleLine = true)
          Text("Select Members", style = MaterialTheme.typography.labelLarge)
          LazyColumn(modifier = Modifier.height(180.dp)) {
            items(allProfiles.filter { it.userId != currentUserId }, key = { it.userId }) { member ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    if (member.userId in selectedMembers) selectedMembers.remove(member.userId)
                    else selectedMembers.add(member.userId)
                  }
                  .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Checkbox(
                  checked = member.userId in selectedMembers,
                  onCheckedChange = {
                    if (member.userId in selectedMembers) selectedMembers.remove(member.userId)
                    else selectedMembers.add(member.userId)
                  },
                )
                Text("${member.fullName} (@${member.username})", style = MaterialTheme.typography.bodyMedium)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onCreateGroupChat(groupTitle, selectedMembers.toList())
            showCreateGroup = false
          },
          enabled = groupTitle.isNotBlank() && selectedMembers.isNotEmpty(),
        ) { Text("Create Group") }
      },
      dismissButton = { TextButton(onClick = { showCreateGroup = false }) { Text("Cancel") } },
    )
  }
}

@Composable
fun ChatConversationScreen(
  chat: ChatThread,
  currentUserId: String,
  messages: List<ChatMessage>,
  onBack: () -> Unit,
  onSendMessage: (String, String, String) -> Unit,
  onReact: (String, String) -> Unit,
  onDelete: (String) -> Unit,
) {
  BackHandler(onBack = onBack)

  var messageText by remember { mutableStateOf("") }
  var searchQuery by remember { mutableStateOf("") }
  var showSearch by remember { mutableStateOf(false) }
  var replyTo by remember { mutableStateOf<ChatMessage?>(null) }

  val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
    if (uri != null) {
      onSendMessage(uri.toString(), "image", replyTo?.text ?: "")
      replyTo = null
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(chat.title.ifBlank { "Chat" }, style = MaterialTheme.typography.titleMedium, maxLines = 1)
          Text("Online • Real-time Firestore sync", style = MaterialTheme.typography.labelSmall, color = BrandEmerald)
        }
        IconButton(onClick = { showSearch = !showSearch }) {
          Icon(Icons.Default.Search, contentDescription = "Search chat history")
        }
      }
    }

    if (showSearch) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search messages...") },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp),
      )
    }

    val filteredMessages = messages.filter {
      searchQuery.isBlank() || it.text.lowercase().contains(searchQuery.trim().lowercase())
    }

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      contentPadding = PaddingValues(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(filteredMessages, key = { it.id }) { msg ->
        val isMine = msg.senderId == currentUserId
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        ) {
          Surface(
            color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .clickable { replyTo = msg }
              .padding(horizontal = 2.dp),
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              if (!isMine) {
                Text(msg.senderName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
              }
              if (msg.replyToText.isNotBlank()) {
                Surface(
                  color = Color.Black.copy(alpha = 0.14f),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.padding(bottom = 4.dp),
                ) {
                  Text(
                    "Replying to: ${msg.replyToText}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isMine) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(6.dp),
                  )
                }
              }
              if (msg.messageType == "image" && (msg.text.startsWith("content://") || msg.text.startsWith("http"))) {
                AsyncImage(
                  model = msg.text,
                  contentDescription = "Shared photo",
                  modifier = Modifier
                    .size(width = 200.dp, height = 160.dp)
                    .clip(RoundedCornerShape(12.dp)),
                  contentScale = ContentScale.Crop,
                )
              } else {
                Text(
                  text = msg.text,
                  color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                  style = MaterialTheme.typography.bodyMedium,
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = formatTimestamp(msg.createdAt),
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isMine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isMine) {
                  Icon(
                    Icons.Default.DoneAll,
                    contentDescription = msg.deliveryState,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                  )
                  Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete message",
                    modifier = Modifier
                      .size(14.dp)
                      .clickable { onDelete(msg.id) },
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                  )
                }
                Text(
                  text = if (msg.reaction.isBlank()) "👍" else msg.reaction,
                  style = MaterialTheme.typography.labelSmall,
                  modifier = Modifier.clickable { onReact(msg.id, "❤️") },
                )
              }
            }
          }
        }
      }
    }

    if (replyTo != null) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Replying: ${replyTo?.text?.take(40)}", style = MaterialTheme.typography.labelMedium)
        IconButton(onClick = { replyTo = null }, modifier = Modifier.size(24.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(16.dp))
        }
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(
        onClick = {
          imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
      ) {
        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Share photo")
      }
      OutlinedTextField(
        value = messageText,
        onValueChange = { messageText = it },
        placeholder = { Text("Message...") },
        singleLine = true,
        modifier = Modifier.weight(1f).testTag("chat_message_input"),
      )
      Spacer(modifier = Modifier.width(6.dp))
      IconButton(
        onClick = {
          if (messageText.isNotBlank()) {
            onSendMessage(messageText, "text", replyTo?.text ?: "")
            messageText = ""
            replyTo = null
          }
        },
        modifier = Modifier.testTag("chat_send_button"),
      ) {
        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
      }
    }
  }
}

package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

@IgnoreExtraProperties
data class PublicProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val fullName: String = "",
  val username: String = "",
  val accountType: String = "student", // "student" | "teacher"
  val bio: String = "",
  val school: String = "",
  val educationLevel: String = "",
  val location: String = "",
  val websiteUrl: String = "",
  val avatarUri: String = "",
  val coverUri: String = "",
  val accentColorHex: String = "#6D28D9",
  val profileLayout: String = "editorial", // "editorial" | "grid" | "compact"
  val interests: List<String> = emptyList(),
  val skills: List<String> = emptyList(),
  val languages: List<String> = listOf("English"),
  val teacherExpertise: String = "",
  val teacherIntro: String = "",
  val hasSpecialTeacherBadge: Boolean = false,
  val isPrivateAccount: Boolean = false,
  val isDiscoverable: Boolean = true,
  val isSuspended: Boolean = false,
  val studyStreakDays: Long = 1L,
  val dailyStudyGoalMins: Long = 60L,
  val profileVisitsCount: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class PrivateAccountInfo(
  @DocumentId val id: String = "",
  val userId: String = "",
  val email: String = "",
  val gender: String = "Prefer not to say",
  val dateOfBirth: String = "",
  val dobVisibility: String = "private",
  val contactPreference: String = "In-app messages",
  val notifyLikes: Boolean = true,
  val notifyComments: Boolean = true,
  val notifyFollows: Boolean = true,
  val notifyMessages: Boolean = true,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Post(
  @DocumentId val id: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val authorUsername: String = "",
  val authorAccountType: String = "student",
  val authorHasTeacherBadge: Boolean = false,
  val authorAvatarUri: String = "",
  val postType: String = "educational", // "text" | "photo" | "educational" | "reel" | "note" | "repost"
  val caption: String = "",
  val subject: String = "",
  val mediaUris: List<String> = emptyList(),
  val hashtags: List<String> = emptyList(),
  val locationTag: String = "",
  val visibility: String = "public", // "public" | "followers" | "private"
  val isPinned: Boolean = false,
  val likesCount: Long = 0L,
  val commentsCount: Long = 0L,
  val viewsCount: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class PostComment(
  @DocumentId val id: String = "",
  val postId: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val authorUsername: String = "",
  val parentCommentId: String = "",
  val content: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Story(
  @DocumentId val id: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val authorUsername: String = "",
  val storyType: String = "text", // "text" | "photo"
  val textContent: String = "",
  val mediaUri: String = "",
  val bgHex: String = "#4C1D95",
  val visibility: String = "public", // "public" | "close_friends"
  val isHighlight: Boolean = false,
  val highlightTitle: String = "",
  val reactionsCount: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class FollowRelation(
  @DocumentId val id: String = "",
  val followerId: String = "",
  val targetId: String = "",
  val status: String = "accepted", // "accepted" | "pending"
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class ChatThread(
  @DocumentId val id: String = "",
  val participantIds: List<String> = emptyList(),
  val title: String = "",
  val isGroup: Boolean = false,
  val lastMessageText: String = "",
  val lastSenderId: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class ChatMessage(
  @DocumentId val id: String = "",
  val chatId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val participantIds: List<String> = emptyList(),
  val text: String = "",
  val messageType: String = "text", // "text" | "image" | "document" | "voice"
  val replyToText: String = "",
  val reaction: String = "",
  val deliveryState: String = "sent", // "sent" | "delivered" | "seen"
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class StudyCircle(
  @DocumentId val id: String = "",
  val creatorId: String = "",
  val name: String = "",
  val subject: String = "",
  val educationLevel: String = "",
  val description: String = "",
  val memberIds: List<String> = emptyList(),
  val isPublic: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class CircleNote(
  @DocumentId val id: String = "",
  val circleId: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val title: String = "",
  val content: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class QuizItem(
  @DocumentId val id: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val title: String = "",
  val subject: String = "",
  val questionPrompt: String = "",
  val options: List<String> = emptyList(),
  val correctIndex: Long = 0L,
  val explanation: String = "",
  val isPublished: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class CommunityChallenge(
  @DocumentId val id: String = "",
  val creatorId: String = "",
  val title: String = "",
  val subject: String = "",
  val description: String = "",
  val targetDays: Long = 7L,
  val participantIds: List<String> = emptyList(),
  val isActive: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class StudyGoal(
  @DocumentId val id: String = "",
  val userId: String = "",
  val title: String = "",
  val subject: String = "",
  val targetMinutes: Long = 30L,
  val completed: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class NotificationItem(
  @DocumentId val id: String = "",
  val recipientId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val type: String = "like",
  val message: String = "",
  val isRead: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class ModerationReport(
  @DocumentId val id: String = "",
  val reporterId: String = "",
  val targetId: String = "",
  val targetType: String = "post",
  val reason: String = "",
  val status: String = "open", // "open" | "resolved" | "dismissed"
  @ServerTimestamp val createdAt: Timestamp? = null,
)

enum class ThemeModePreference { SYSTEM, LIGHT, DARK }
enum class AccentPalette(val label: String, val hex: Long) {
  ROYAL_PURPLE("Royal Purple", 0xFF6D28D9),
  DEEP_VIOLET("Deep Violet", 0xFF4C1D95),
  MIDNIGHT_NAVY("Midnight Indigo", 0xFF1E3A8A),
  EMERALD_SCHOLAR("Emerald Scholar", 0xFF047857),
  AMBER_SPARK("Amber Spark", 0xFFD97706),
}

enum class InterfaceDensity { COMFORTABLE, COMPACT }

data class UserAppPreferences(
  val themeMode: ThemeModePreference = ThemeModePreference.SYSTEM,
  val accentPalette: AccentPalette = AccentPalette.ROYAL_PURPLE,
  val fontScale: Float = 1.0f,
  val density: InterfaceDensity = InterfaceDensity.COMFORTABLE,
  val highContrast: Boolean = false,
)

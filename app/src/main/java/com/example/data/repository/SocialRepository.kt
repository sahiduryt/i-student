package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firestore.OperationType
import com.example.data.firestore.handleFirestoreError
import com.example.data.model.CircleNote
import com.example.data.model.CommunityChallenge
import com.example.data.model.FollowRelation
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
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SocialRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    Firebase.auth,
  )

  private fun requireUserId(): String =
    auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

  private inline fun <T> safeCollectionFlow(
    path: String,
    crossinline builder: (String?) -> Flow<T>,
  ): Flow<T> = flow {
    val uid = auth.currentUser?.uid
    emitAll(
      builder(uid).catch { error ->
        if (error is CancellationException && error.cause == null) throw error
        if (uid != null && auth.currentUser?.uid == uid) {
          handleFirestoreError(error, OperationType.LIST, path)
        }
        throw error
      }
    )
  }

  // --- Profiles ---

  fun observeProfile(userId: String): Flow<PublicProfile?> =
    safeCollectionFlow("public_profiles/$userId") {
      db.collection("public_profiles").document(userId).snapshots().map { snap ->
        if (snap.exists()) {
          snap.toObject(PublicProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } else null
      }
    }

  fun observeDiscoverableProfiles(): Flow<List<PublicProfile>> =
    safeCollectionFlow("public_profiles") {
      db.collection("public_profiles")
        .whereEqualTo("isDiscoverable", true)
        .snapshots()
        .map { snap ->
          snap.toObjects(PublicProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.hasSpecialTeacherBadge }
        }
    }

  fun observePrivateInfo(userId: String): Flow<PrivateAccountInfo?> =
    safeCollectionFlow("users/$userId/private/account_info") {
      db.collection("users").document(userId).collection("private").document("account_info")
        .snapshots()
        .map { snap ->
          if (snap.exists()) {
            snap.toObject(PrivateAccountInfo::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          } else null
        }
    }

  suspend fun checkIsAdmin(userId: String): Boolean {
    val email = auth.currentUser?.email
    val verified = auth.currentUser?.isEmailVerified == true
    if (email == "sahidsahid4306@gmail.com" && verified) return true
    return try {
      db.collection("admins").document(userId).get().await().exists()
    } catch (e: CancellationException) {
      throw e
    } catch (_: Exception) {
      false
    }
  }

  suspend fun isUsernameAvailable(username: String, currentUserId: String): Result<Boolean> = runCatching {
    val normalized = username.trim().lowercase()
    if (normalized.length < 3) return@runCatching false
    val snap = db.collection("public_profiles")
      .whereEqualTo("isDiscoverable", true)
      .get()
      .await()
    snap.documents.none { doc ->
      doc.id != currentUserId && doc.getString("username")?.lowercase() == normalized
    }
  }

  suspend fun createProfile(profile: PublicProfile, privateInfo: PrivateAccountInfo): Result<Unit> {
    val uid = requireUserId()
    val path = "public_profiles/$uid"
    return try {
      val cleanUsername = profile.username.trim().lowercase().replace(Regex("[^a-z0-9_.]"), "")
        .ifBlank { "student_${uid.take(6)}" }
      val payload = mapOf(
        "userId" to uid,
        "fullName" to profile.fullName.trim().ifBlank { "i student Member" }.take(80),
        "username" to cleanUsername.take(30).padEnd(3, '0'),
        "accountType" to if (profile.accountType == "teacher") "teacher" else "student",
        "bio" to profile.bio.take(400),
        "school" to profile.school.take(120),
        "educationLevel" to profile.educationLevel.take(80),
        "location" to profile.location.take(80),
        "websiteUrl" to profile.websiteUrl.take(200),
        "avatarUri" to profile.avatarUri.take(500),
        "coverUri" to profile.coverUri.take(500),
        "accentColorHex" to profile.accentColorHex.take(16),
        "profileLayout" to profile.profileLayout,
        "interests" to profile.interests.take(15),
        "skills" to profile.skills.take(15),
        "languages" to profile.languages.take(10),
        "teacherExpertise" to profile.teacherExpertise.take(160),
        "teacherIntro" to profile.teacherIntro.take(500),
        "hasSpecialTeacherBadge" to false,
        "isPrivateAccount" to profile.isPrivateAccount,
        "isDiscoverable" to true,
        "isSuspended" to false,
        "studyStreakDays" to profile.studyStreakDays.coerceAtLeast(1L),
        "dailyStudyGoalMins" to profile.dailyStudyGoalMins.coerceIn(5L, 720L),
        "profileVisitsCount" to 0L,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      db.collection("public_profiles").document(uid).set(payload).await()

      val privatePayload = mapOf(
        "userId" to uid,
        "email" to (auth.currentUser?.email ?: privateInfo.email).take(160),
        "gender" to privateInfo.gender.take(40),
        "dateOfBirth" to privateInfo.dateOfBirth.take(40),
        "dobVisibility" to privateInfo.dobVisibility.take(30),
        "contactPreference" to privateInfo.contactPreference.take(60),
        "notifyLikes" to privateInfo.notifyLikes,
        "notifyComments" to privateInfo.notifyComments,
        "notifyFollows" to privateInfo.notifyFollows,
        "notifyMessages" to privateInfo.notifyMessages,
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      db.collection("users").document(uid).collection("private").document("account_info")
        .set(privatePayload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun updateProfile(profile: PublicProfile): Result<Unit> {
    val uid = requireUserId()
    val path = "public_profiles/$uid"
    return try {
      val cleanUsername = profile.username.trim().lowercase().replace(Regex("[^a-z0-9_.]"), "")
        .ifBlank { "student_${uid.take(6)}" }
      val updates = mapOf(
        "fullName" to profile.fullName.trim().ifBlank { "i student Member" }.take(80),
        "username" to cleanUsername.take(30).padEnd(3, '0'),
        "accountType" to if (profile.accountType == "teacher") "teacher" else "student",
        "bio" to profile.bio.take(400),
        "school" to profile.school.take(120),
        "educationLevel" to profile.educationLevel.take(80),
        "location" to profile.location.take(80),
        "websiteUrl" to profile.websiteUrl.take(200),
        "avatarUri" to profile.avatarUri.take(500),
        "coverUri" to profile.coverUri.take(500),
        "accentColorHex" to profile.accentColorHex.take(16),
        "profileLayout" to profile.profileLayout,
        "interests" to profile.interests.take(15),
        "skills" to profile.skills.take(15),
        "languages" to profile.languages.take(10),
        "teacherExpertise" to profile.teacherExpertise.take(160),
        "teacherIntro" to profile.teacherIntro.take(500),
        "isPrivateAccount" to profile.isPrivateAccount,
        "isDiscoverable" to profile.isDiscoverable,
        "studyStreakDays" to profile.studyStreakDays.coerceAtLeast(0L),
        "dailyStudyGoalMins" to profile.dailyStudyGoalMins.coerceIn(5L, 720L),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      db.collection("public_profiles").document(uid).update(updates).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun updatePrivateInfo(info: PrivateAccountInfo): Result<Unit> {
    val uid = requireUserId()
    val path = "users/$uid/private/account_info"
    return try {
      val payload = mapOf(
        "userId" to uid,
        "email" to (auth.currentUser?.email ?: info.email).take(160),
        "gender" to info.gender.take(40),
        "dateOfBirth" to info.dateOfBirth.take(40),
        "dobVisibility" to info.dobVisibility.take(30),
        "contactPreference" to info.contactPreference.take(60),
        "notifyLikes" to info.notifyLikes,
        "notifyComments" to info.notifyComments,
        "notifyFollows" to info.notifyFollows,
        "notifyMessages" to info.notifyMessages,
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      db.collection("users").document(uid).collection("private").document("account_info")
        .set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  // --- Posts & Reels ---

  fun observePublicPosts(): Flow<List<Post>> =
    safeCollectionFlow("posts") {
      db.collection("posts")
        .whereEqualTo("visibility", "public")
        .snapshots()
        .map { snap ->
          snap.toObjects(Post::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedWith(compareByDescending<Post> { it.isPinned }.thenByDescending { it.createdAt ?: Timestamp(0, 0) })
        }
    }

  fun observeUserPosts(userId: String): Flow<List<Post>> =
    safeCollectionFlow("posts") {
      db.collection("posts")
        .whereEqualTo("authorId", userId)
        .snapshots()
        .map { snap ->
          snap.toObjects(Post::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedWith(compareByDescending<Post> { it.isPinned }.thenByDescending { it.createdAt ?: Timestamp(0, 0) })
        }
    }

  suspend fun createPost(
    author: PublicProfile,
    postType: String,
    caption: String,
    subject: String,
    mediaUris: List<String>,
    hashtags: List<String>,
    locationTag: String,
    visibility: String,
  ): Result<String> {
    val uid = requireUserId()
    val docRef = db.collection("posts").document()
    return try {
      val payload = mapOf(
        "authorId" to uid,
        "authorName" to author.fullName.ifBlank { "Member" }.take(80),
        "authorUsername" to author.username.ifBlank { "member" }.take(40),
        "authorAccountType" to author.accountType,
        "authorHasTeacherBadge" to author.hasSpecialTeacherBadge,
        "authorAvatarUri" to author.avatarUri.take(500),
        "postType" to postType,
        "caption" to caption.trim().take(2000),
        "subject" to subject.trim().take(80),
        "mediaUris" to mediaUris.take(10),
        "hashtags" to hashtags.take(15),
        "locationTag" to locationTag.trim().take(100),
        "visibility" to visibility,
        "isPinned" to false,
        "likesCount" to 0L,
        "commentsCount" to 0L,
        "viewsCount" to 1L,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      docRef.set(payload).await()
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updatePost(postId: String, caption: String, isPinned: Boolean, visibility: String): Result<Unit> {
    requireUserId()
    val docRef = db.collection("posts").document(postId)
    return try {
      docRef.update(
        mapOf(
          "caption" to caption.trim().take(2000),
          "isPinned" to isPinned,
          "visibility" to visibility,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun deletePost(postId: String): Result<Unit> {
    requireUserId()
    val docRef = db.collection("posts").document(postId)
    return try {
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      Result.failure(e)
    }
  }

  fun observeLikedPostIds(userId: String): Flow<Set<String>> =
    safeCollectionFlow("users/$userId/liked_posts") {
      db.collection("users").document(userId).collection("liked_posts")
        .snapshots()
        .map { snap -> snap.documents.map { it.id }.toSet() }
    }

  fun observeSavedPostIds(userId: String): Flow<Set<String>> =
    safeCollectionFlow("users/$userId/saved_posts") {
      db.collection("users").document(userId).collection("saved_posts")
        .snapshots()
        .map { snap -> snap.documents.map { it.id }.toSet() }
    }

  suspend fun toggleLike(post: Post, actorName: String, currentlyLiked: Boolean): Result<Unit> {
    val uid = requireUserId()
    val postRef = db.collection("posts").document(post.id)
    val postLikeRef = postRef.collection("likes").document(uid)
    val userLikeRef = db.collection("users").document(uid).collection("liked_posts").document(post.id)
    return try {
      if (currentlyLiked) {
        postLikeRef.delete().await()
        userLikeRef.delete().await()
        if (post.likesCount > 0) {
          postRef.update(
            mapOf(
              "likesCount" to (post.likesCount - 1).coerceAtLeast(0L),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          ).await()
        }
      } else {
        val marker = mapOf("userId" to uid, "postId" to post.id, "createdAt" to FieldValue.serverTimestamp())
        postLikeRef.set(mapOf("userId" to uid, "createdAt" to FieldValue.serverTimestamp())).await()
        userLikeRef.set(marker).await()
        postRef.update(
          mapOf(
            "likesCount" to post.likesCount + 1,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
        if (post.authorId != uid) {
          sendNotification(
            recipientId = post.authorId,
            senderName = actorName,
            type = "like",
            message = "$actorName liked your post",
          )
        }
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, postRef.path)
      Result.failure(e)
    }
  }

  suspend fun toggleSave(postId: String, currentlySaved: Boolean): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("users").document(uid).collection("saved_posts").document(postId)
    return try {
      if (currentlySaved) {
        ref.delete().await()
      } else {
        ref.set(
          mapOf(
            "userId" to uid,
            "postId" to postId,
            "createdAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, ref.path)
      Result.failure(e)
    }
  }

  // --- Comments ---

  fun observeComments(postId: String): Flow<List<PostComment>> =
    safeCollectionFlow("posts/$postId/comments") {
      db.collection("posts").document(postId).collection("comments")
        .whereEqualTo("postId", postId)
        .snapshots()
        .map { snap ->
          snap.toObjects(PostComment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedBy { it.createdAt ?: Timestamp(0, 0) }
        }
    }

  suspend fun addComment(
    post: Post,
    author: PublicProfile,
    content: String,
    parentCommentId: String = "",
  ): Result<Unit> {
    val uid = requireUserId()
    val postRef = db.collection("posts").document(post.id)
    val commentRef = postRef.collection("comments").document()
    return try {
      val payload = mapOf(
        "postId" to post.id,
        "authorId" to uid,
        "authorName" to author.fullName.ifBlank { "Member" }.take(80),
        "authorUsername" to author.username.ifBlank { "member" }.take(40),
        "parentCommentId" to parentCommentId.take(128),
        "content" to content.trim().take(500),
        "createdAt" to FieldValue.serverTimestamp(),
      )
      commentRef.set(payload).await()
      postRef.update(
        mapOf(
          "commentsCount" to post.commentsCount + 1,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      if (post.authorId != uid) {
        sendNotification(
          recipientId = post.authorId,
          senderName = author.fullName,
          type = "comment",
          message = "${author.fullName} commented on your post",
        )
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, commentRef.path)
      Result.failure(e)
    }
  }

  // --- Stories ---

  fun observePublicStories(): Flow<List<Story>> =
    safeCollectionFlow("stories") {
      db.collection("stories")
        .whereEqualTo("visibility", "public")
        .snapshots()
        .map { snap ->
          val cutoffMillis = System.currentTimeMillis() - 24L * 60L * 60L * 1000L
          snap.toObjects(Story::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .filter { story ->
              story.isHighlight || (story.createdAt?.toDate()?.time ?: System.currentTimeMillis()) >= cutoffMillis
            }
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
    }

  suspend fun createStory(
    author: PublicProfile,
    storyType: String,
    textContent: String,
    mediaUri: String,
    bgHex: String,
    visibility: String,
    isHighlight: Boolean = false,
    highlightTitle: String = "",
  ): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("stories").document()
    return try {
      ref.set(
        mapOf(
          "authorId" to uid,
          "authorName" to author.fullName.ifBlank { "Member" }.take(80),
          "authorUsername" to author.username.ifBlank { "member" }.take(40),
          "storyType" to storyType,
          "textContent" to textContent.take(400),
          "mediaUri" to mediaUri.take(500),
          "bgHex" to bgHex.take(16),
          "visibility" to visibility,
          "isHighlight" to isHighlight,
          "highlightTitle" to highlightTitle.take(40),
          "reactionsCount" to 0L,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, ref.path)
      Result.failure(e)
    }
  }

  suspend fun reactToStory(story: Story, actorName: String): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("stories").document(story.id)
    return try {
      ref.update("reactionsCount", story.reactionsCount + 1).await()
      if (story.authorId != uid) {
        sendNotification(
          recipientId = story.authorId,
          senderName = actorName,
          type = "story_reaction",
          message = "$actorName reacted to your story",
        )
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, ref.path)
      Result.failure(e)
    }
  }

  suspend fun deleteStory(storyId: String): Result<Unit> {
    requireUserId()
    val ref = db.collection("stories").document(storyId)
    return try {
      ref.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, ref.path)
      Result.failure(e)
    }
  }

  // --- Follows & Blocks ---

  fun observeFollowingRelations(userId: String): Flow<List<FollowRelation>> =
    safeCollectionFlow("follows") {
      db.collection("follows")
        .whereEqualTo("followerId", userId)
        .snapshots()
        .map { snap -> snap.toObjects(FollowRelation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
    }

  fun observeFollowerRelations(userId: String): Flow<List<FollowRelation>> =
    safeCollectionFlow("follows") {
      db.collection("follows")
        .whereEqualTo("targetId", userId)
        .snapshots()
        .map { snap -> snap.toObjects(FollowRelation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
    }

  suspend fun followUser(target: PublicProfile, actorName: String): Result<Unit> {
    val uid = requireUserId()
    if (uid == target.userId) return Result.success(Unit)
    val followId = "${uid}_${target.userId}"
    val ref = db.collection("follows").document(followId)
    val status = if (target.isPrivateAccount) "pending" else "accepted"
    return try {
      ref.set(
        mapOf(
          "followerId" to uid,
          "targetId" to target.userId,
          "status" to status,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      sendNotification(
        recipientId = target.userId,
        senderName = actorName,
        type = if (status == "pending") "follow_request" else "follow",
        message = if (status == "pending") "$actorName requested to follow you" else "$actorName started following you",
      )
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, ref.path)
      Result.failure(e)
    }
  }

  suspend fun unfollowUser(targetId: String): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("follows").document("${uid}_${targetId}")
    return try {
      ref.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, ref.path)
      Result.failure(e)
    }
  }

  suspend fun respondToFollowRequest(followId: String, accept: Boolean): Result<Unit> {
    requireUserId()
    val ref = db.collection("follows").document(followId)
    return try {
      if (accept) {
        ref.update("status", "accepted").await()
      } else {
        ref.delete().await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, ref.path)
      Result.failure(e)
    }
  }

  fun observeBlockedUserIds(userId: String): Flow<Set<String>> =
    safeCollectionFlow("users/$userId/blocked_users") {
      db.collection("users").document(userId).collection("blocked_users")
        .snapshots()
        .map { snap -> snap.documents.map { it.id }.toSet() }
    }

  suspend fun blockUser(blockedId: String): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("users").document(uid).collection("blocked_users").document(blockedId)
    return try {
      ref.set(
        mapOf(
          "userId" to uid,
          "blockedId" to blockedId,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, ref.path)
      Result.failure(e)
    }
  }

  suspend fun unblockUser(blockedId: String): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("users").document(uid).collection("blocked_users").document(blockedId)
    return try {
      ref.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, ref.path)
      Result.failure(e)
    }
  }

  // --- Study Circles, Notes, Quizzes, Challenges & Goals ---

  fun observeStudyCircles(): Flow<List<StudyCircle>> =
    safeCollectionFlow("study_circles") {
      db.collection("study_circles")
        .whereEqualTo("isPublic", true)
        .snapshots()
        .map { snap ->
          snap.toObjects(StudyCircle::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
    }

  suspend fun createStudyCircle(name: String, subject: String, level: String, description: String): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("study_circles").document()
    return try {
      ref.set(
        mapOf(
          "creatorId" to uid,
          "name" to name.trim().take(80),
          "subject" to subject.trim().take(60),
          "educationLevel" to level.trim().take(60),
          "description" to description.trim().take(400),
          "memberIds" to listOf(uid),
          "isPublic" to true,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, ref.path)
      Result.failure(e)
    }
  }

  suspend fun toggleCircleMembership(circle: StudyCircle): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("study_circles").document(circle.id)
    val nextMembers = if (uid in circle.memberIds) {
      circle.memberIds.filterNot { it == uid }
    } else {
      (circle.memberIds + uid).distinct().take(50)
    }
    return try {
      ref.update("memberIds", nextMembers).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, ref.path)
      Result.failure(e)
    }
  }

  fun observeCircleNotes(circleId: String): Flow<List<CircleNote>> =
    safeCollectionFlow("study_circles/$circleId/notes") {
      db.collection("study_circles").document(circleId).collection("notes")
        .whereEqualTo("circleId", circleId)
        .snapshots()
        .map { snap ->
          snap.toObjects(CircleNote::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
        }
    }

  suspend fun addCircleNote(circleId: String, authorName: String, title: String, content: String): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("study_circles").document(circleId).collection("notes").document()
    return try {
      ref.set(
        mapOf(
          "circleId" to circleId,
          "authorId" to uid,
          "authorName" to authorName.ifBlank { "Member" }.take(80),
          "title" to title.trim().take(120),
          "content" to content.trim().take(4000),
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, ref.path)
      Result.failure(e)
    }
  }

  fun observeQuizzes(): Flow<List<QuizItem>> =
    safeCollectionFlow("quizzes") {
      db.collection("quizzes")
        .whereEqualTo("isPublished", true)
        .snapshots()
        .map { snap ->
          snap.toObjects(QuizItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
    }

  suspend fun createQuiz(
    authorName: String,
    title: String,
    subject: String,
    question: String,
    options: List<String>,
    correctIndex: Int,
    explanation: String,
  ): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("quizzes").document()
    return try {
      ref.set(
        mapOf(
          "authorId" to uid,
          "authorName" to authorName.ifBlank { "Educator" }.take(80),
          "title" to title.trim().take(120),
          "subject" to subject.trim().take(60),
          "questionPrompt" to question.trim().take(500),
          "options" to options.take(6),
          "correctIndex" to correctIndex.toLong(),
          "explanation" to explanation.trim().take(400),
          "isPublished" to true,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, ref.path)
      Result.failure(e)
    }
  }

  fun observeChallenges(): Flow<List<CommunityChallenge>> =
    safeCollectionFlow("challenges") {
      db.collection("challenges")
        .whereEqualTo("isActive", true)
        .snapshots()
        .map { snap ->
          snap.toObjects(CommunityChallenge::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
    }

  suspend fun createChallenge(title: String, subject: String, description: String, targetDays: Int): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("challenges").document()
    return try {
      ref.set(
        mapOf(
          "creatorId" to uid,
          "title" to title.trim().take(100),
          "subject" to subject.trim().take(60),
          "description" to description.trim().take(400),
          "targetDays" to targetDays.coerceIn(1, 365).toLong(),
          "participantIds" to listOf(uid),
          "isActive" to true,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, ref.path)
      Result.failure(e)
    }
  }

  suspend fun toggleJoinChallenge(challenge: CommunityChallenge): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("challenges").document(challenge.id)
    val next = if (uid in challenge.participantIds) {
      challenge.participantIds.filterNot { it == uid }
    } else {
      (challenge.participantIds + uid).distinct().take(50)
    }
    return try {
      ref.update("participantIds", next).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, ref.path)
      Result.failure(e)
    }
  }

  fun observeStudyGoals(userId: String): Flow<List<StudyGoal>> =
    safeCollectionFlow("users/$userId/study_goals") {
      db.collection("users").document(userId).collection("study_goals")
        .snapshots()
        .map { snap ->
          snap.toObjects(StudyGoal::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
    }

  suspend fun addStudyGoal(title: String, subject: String, targetMinutes: Int): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("users").document(uid).collection("study_goals").document()
    return try {
      ref.set(
        mapOf(
          "userId" to uid,
          "title" to title.trim().take(120),
          "subject" to subject.trim().take(60),
          "targetMinutes" to targetMinutes.coerceIn(5, 720).toLong(),
          "completed" to false,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, ref.path)
      Result.failure(e)
    }
  }

  suspend fun toggleStudyGoal(goal: StudyGoal): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("users").document(uid).collection("study_goals").document(goal.id)
    return try {
      ref.update("completed", !goal.completed).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, ref.path)
      Result.failure(e)
    }
  }

  // --- Notifications, Reports & Admin ---

  fun observeNotifications(userId: String): Flow<List<NotificationItem>> =
    safeCollectionFlow("users/$userId/notifications") {
      db.collection("users").document(userId).collection("notifications")
        .snapshots()
        .map { snap ->
          snap.toObjects(NotificationItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
    }

  suspend fun sendNotification(
    recipientId: String,
    senderName: String,
    type: String,
    message: String,
  ) {
    val uid = auth.currentUser?.uid ?: return
    val ref = db.collection("users").document(recipientId).collection("notifications").document()
    try {
      ref.set(
        mapOf(
          "recipientId" to recipientId,
          "senderId" to uid,
          "senderName" to senderName.ifBlank { "i student Member" }.take(80),
          "type" to type.take(40),
          "message" to message.take(300),
          "isRead" to false,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, ref.path)
    }
  }

  suspend fun markAllNotificationsRead(notifications: List<NotificationItem>) {
    val uid = requireUserId()
    notifications.filter { !it.isRead && it.id.isNotBlank() }.forEach { notif ->
      try {
        db.collection("users").document(uid).collection("notifications").document(notif.id)
          .update("isRead", true)
          .await()
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, "users/$uid/notifications/${notif.id}")
      }
    }
  }

  suspend fun submitReport(targetId: String, targetType: String, reason: String): Result<Unit> {
    val uid = requireUserId()
    val ref = db.collection("reports").document(UUID.randomUUID().toString())
    return try {
      ref.set(
        mapOf(
          "reporterId" to uid,
          "targetId" to targetId.take(128),
          "targetType" to targetType,
          "reason" to reason.trim().take(300),
          "status" to "open",
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, ref.path)
      Result.failure(e)
    }
  }

  fun observeReports(isAdmin: Boolean, userId: String): Flow<List<ModerationReport>> =
    safeCollectionFlow("reports") {
      val query = if (isAdmin) {
        db.collection("reports")
      } else {
        db.collection("reports").whereEqualTo("reporterId", userId)
      }
      query.snapshots().map { snap ->
        snap.toObjects(ModerationReport::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
      }
    }

  suspend fun adminSetSpecialTeacherBadge(targetUserId: String, award: Boolean, adminName: String): Result<Unit> {
    requireUserId()
    val ref = db.collection("public_profiles").document(targetUserId)
    return try {
      ref.update(
        mapOf(
          "hasSpecialTeacherBadge" to award,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      if (award) {
        sendNotification(
          recipientId = targetUserId,
          senderName = adminName,
          type = "teacher_badge",
          message = "Congratulations! You have been awarded the Special Teacher Badge.",
        )
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, ref.path)
      Result.failure(e)
    }
  }

  suspend fun adminSetUserSuspended(targetUserId: String, suspended: Boolean): Result<Unit> {
    requireUserId()
    val ref = db.collection("public_profiles").document(targetUserId)
    return try {
      ref.update(
        mapOf(
          "isSuspended" to suspended,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, ref.path)
      Result.failure(e)
    }
  }

  suspend fun adminUpdateReportStatus(reportId: String, status: String): Result<Unit> {
    requireUserId()
    val ref = db.collection("reports").document(reportId)
    return try {
      ref.update("status", status).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, ref.path)
      Result.failure(e)
    }
  }

  suspend fun deleteAccountData(): Result<Unit> {
    val uid = requireUserId()
    return try {
      db.collection("users").document(uid).collection("private").document("account_info").delete().await()
      db.collection("public_profiles").document(uid).delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, "public_profiles/$uid")
      Result.failure(e)
    }
  }
}

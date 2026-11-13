package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.ChatThread
import com.example.data.model.PrivateAccountInfo
import com.example.data.model.PublicProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class SocialRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createProfileAndPost_authenticatedOwner_succeedsAndEmitsRealtime() = runBlocking {
    val uid = signInTestUser("alice_${UUID.randomUUID()}@test.com")
    val repo = SocialRepository(firestore, auth)

    val profile = PublicProfile(
      userId = uid,
      fullName = "Alice Scholar",
      username = "alicescholar_${uid.take(4)}",
      accountType = "student",
      school = "State University",
      interests = listOf("Physics", "AI"),
    )
    val priv = PrivateAccountInfo(userId = uid, email = "alice@test.com", gender = "Female")
    val createRes = withTimeout(DEFAULT_TIMEOUT_MS) { repo.createProfile(profile, priv) }
    assertTrue(createRes.isSuccess)

    val postRes = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.createPost(
        author = profile,
        postType = "educational",
        caption = "Wave-particle duality summary #physics",
        subject = "Physics",
        mediaUris = emptyList(),
        hashtags = listOf("physics"),
        locationTag = "Library",
        visibility = "public",
      )
    }
    assertTrue(postRes.isSuccess)
    val postId = postRes.getOrThrow()

    val posts = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observePublicPosts().first { list -> list.any { it.id == postId } }
    }
    assertTrue(posts.any { it.id == postId })
  }

  @Test
  fun teacherCannotSelfAwardBadge_failsWithPermissionDenied() = runBlocking {
    val teacherUid = signInTestUser("teacher_${UUID.randomUUID()}@test.com")
    val repo = SocialRepository(firestore, auth)
    val profile = PublicProfile(
      userId = teacherUid,
      fullName = "Prof. Vance",
      username = "profvance_${teacherUid.take(4)}",
      accountType = "teacher",
    )
    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.createProfile(profile, PrivateAccountInfo(userId = teacherUid)).getOrThrow()
    }

    val badgeResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.adminSetSpecialTeacherBadge(teacherUid, award = true, adminName = "Self")
    }
    assertFalse(badgeResult.isSuccess)
    val ex = badgeResult.exceptionOrNull()?.unwrapFirestoreException()
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
  }

  @Test
  fun crossUserPrivateInfo_failsWithPermissionDenied() = runBlocking {
    val bobUid = signInTestUser("bob_${UUID.randomUUID()}@test.com")
    val bobRepo = SocialRepository(firestore, auth)
    withTimeout(DEFAULT_TIMEOUT_MS) {
      bobRepo.createProfile(
        PublicProfile(userId = bobUid, fullName = "Bob", username = "bob_${bobUid.take(4)}"),
        PrivateAccountInfo(userId = bobUid, email = "bob@test.com"),
      ).getOrThrow()
    }

    signInTestUser("charlie_${UUID.randomUUID()}@test.com")
    var denied = false
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("users").document(bobUid)
          .collection("private").document("account_info").get().await()
      }
    } catch (e: Exception) {
      denied = e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
    }
    assertTrue(denied)
  }

  @Test
  fun observePublicPosts_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repo = SocialRepository(firestore, auth)
    var denied = false
    try {
      withTimeout(FLOW_TIMEOUT_MS) { repo.observePublicPosts().first() }
    } catch (e: Exception) {
      denied = e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
    }
    assertTrue(denied)
  }

  @Test
  fun chatThreadAndMessages_participantSucceeds_outsiderDenied() = runBlocking {
    val aliceUid = signInTestUser("chatalice_${UUID.randomUUID()}@test.com")
    val chatRepo = ChatRepository(firestore, auth)
    val peerUid = "peer_${UUID.randomUUID().toString().take(8)}"

    val chatId = withTimeout(DEFAULT_TIMEOUT_MS) {
      chatRepo.getOrCreateDirectChat(peerUid, "Study Peer").getOrThrow()
    }
    val thread = ChatThread(
      id = chatId,
      participantIds = listOf(aliceUid, peerUid).sorted(),
      title = "Study Peer",
      isGroup = false,
    )
    val sendRes = withTimeout(DEFAULT_TIMEOUT_MS) {
      chatRepo.sendMessage(thread, "Alice", "Ready for calculus review?", "text", "")
    }
    assertTrue(sendRes.isSuccess)

    val msgs = withTimeout(FLOW_TIMEOUT_MS) {
      chatRepo.observeMessages(chatId, aliceUid).first { it.isNotEmpty() }
    }
    assertTrue(msgs.any { it.text == "Ready for calculus review?" })

    signInTestUser("outsider_${UUID.randomUUID()}@test.com")
    var denied = false
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("chats").document(chatId).get().await()
      }
    } catch (e: Exception) {
      denied = e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
    }
    assertTrue(denied)
  }

  private companion object {
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}

package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firestore.OperationType
import com.example.data.firestore.handleFirestoreError
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
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

class ChatRepository(
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
      ?: throw IllegalStateException("User must be signed in with Google before accessing chats.")

  fun observeChats(userId: String): Flow<List<ChatThread>> = flow {
    emitAll(
      db.collection("chats")
        .whereArrayContains("participantIds", userId)
        .snapshots()
        .map { snap ->
          snap.toObjects(ChatThread::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          if (error is CancellationException && error.cause == null) throw error
          if (auth.currentUser?.uid == userId) {
            handleFirestoreError(error, OperationType.LIST, "chats")
          }
          throw error
        }
    )
  }

  fun observeMessages(chatId: String, userId: String): Flow<List<ChatMessage>> = flow {
    emitAll(
      db.collection("chats").document(chatId).collection("messages")
        .whereArrayContains("participantIds", userId)
        .snapshots()
        .map { snap ->
          snap.toObjects(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedBy { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          if (error is CancellationException && error.cause == null) throw error
          if (auth.currentUser?.uid == userId) {
            handleFirestoreError(error, OperationType.LIST, "chats/$chatId/messages")
          }
          throw error
        }
    )
  }

  suspend fun getOrCreateDirectChat(otherUserId: String, otherName: String): Result<String> {
    val uid = requireUserId()
    val participants = listOf(uid, otherUserId).sorted()
    val deterministicId = participants.joinToString("_")
    val docRef = db.collection("chats").document(deterministicId)
    return try {
      val existing = docRef.get().await()
      if (!existing.exists()) {
        docRef.set(
          mapOf(
            "participantIds" to participants,
            "title" to otherName.ifBlank { "Direct Chat" }.take(80),
            "isGroup" to false,
            "lastMessageText" to "Conversation started",
            "lastSenderId" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
      Result.success(deterministicId)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun createGroupChat(title: String, memberIds: List<String>): Result<String> {
    val uid = requireUserId()
    val participants = (memberIds + uid).distinct().take(20)
    val docRef = db.collection("chats").document()
    return try {
      docRef.set(
        mapOf(
          "participantIds" to participants,
          "title" to title.trim().ifBlank { "Study Group Chat" }.take(80),
          "isGroup" to true,
          "lastMessageText" to "Group created",
          "lastSenderId" to uid,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun sendMessage(
    chat: ChatThread,
    senderName: String,
    text: String,
    messageType: String = "text",
    replyToText: String = "",
  ): Result<Unit> {
    val uid = requireUserId()
    val chatRef = db.collection("chats").document(chat.id)
    val msgRef = chatRef.collection("messages").document()
    return try {
      msgRef.set(
        mapOf(
          "chatId" to chat.id,
          "senderId" to uid,
          "senderName" to senderName.ifBlank { "Member" }.take(80),
          "participantIds" to chat.participantIds,
          "text" to text.trim().take(1500),
          "messageType" to messageType,
          "replyToText" to replyToText.take(300),
          "reaction" to "",
          "deliveryState" to "sent",
          "createdAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      chatRef.update(
        mapOf(
          "lastMessageText" to text.trim().take(500),
          "lastSenderId" to uid,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, msgRef.path)
      Result.failure(e)
    }
  }

  suspend fun reactToMessage(chatId: String, messageId: String, emoji: String): Result<Unit> {
    requireUserId()
    val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
    return try {
      msgRef.update("reaction", emoji.take(16)).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, msgRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteMessage(chatId: String, messageId: String): Result<Unit> {
    requireUserId()
    val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
    return try {
      msgRef.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, msgRef.path)
      Result.failure(e)
    }
  }
}

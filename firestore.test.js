const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_student_1";
const BOB_UID = "bob_teacher_2";
const ADMIN_UID = "admin_super_3";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read public_profiles or posts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("public_profiles").where("isDiscoverable", "==", true).get()
  );
  await assertFails(
    unauthDb.collection("posts").where("visibility", "==", "public").get()
  );
});

test("PublicProfile: user can create own profile with hasSpecialTeacherBadge=false", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("public_profiles").doc(BOB_UID).set({
      userId: BOB_UID,
      fullName: "Prof. Bob",
      username: "profbob",
      accountType: "teacher",
      hasSpecialTeacherBadge: false,
      isPrivateAccount: false,
      isDiscoverable: true,
      isSuspended: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("PublicProfile: teacher CANNOT self-assign Special Teacher Badge", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("public_profiles").doc(BOB_UID).set({
      userId: BOB_UID,
      fullName: "Prof. Bob",
      username: "profbob",
      accountType: "teacher",
      hasSpecialTeacherBadge: true,
      isPrivateAccount: false,
      isDiscoverable: true,
      isSuspended: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("PublicProfile: teacher CANNOT update own hasSpecialTeacherBadge, only Admin can", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("admins").doc(ADMIN_UID).set({ role: "admin" });
    await db.collection("public_profiles").doc(BOB_UID).set({
      userId: BOB_UID,
      fullName: "Prof. Bob",
      username: "profbob",
      accountType: "teacher",
      hasSpecialTeacherBadge: false,
      isPrivateAccount: false,
      isDiscoverable: true,
      isSuspended: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("public_profiles").doc(BOB_UID).update({
      hasSpecialTeacherBadge: true,
      updatedAt: new Date(),
    })
  );

  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  await assertSucceeds(
    adminDb.collection("public_profiles").doc(BOB_UID).update({
      hasSpecialTeacherBadge: true,
      updatedAt: new Date(),
    })
  );
});

test("PII Isolation: user cannot read another user's private info", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(BOB_UID)
      .collection("private").doc("account_info").set({
        userId: BOB_UID,
        email: "bob@university.edu",
        updatedAt: new Date(),
      });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("private").doc("account_info").get()
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("users").doc(BOB_UID).collection("private").doc("account_info").get()
  );
});

test("Posts: owner creates public post, others can query public posts, only owner can edit caption", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("posts").doc("post_1").set({
      authorId: ALICE_UID,
      authorName: "Alice",
      authorUsername: "alice01",
      postType: "educational",
      caption: "Quantum notes",
      visibility: "public",
      isPinned: false,
      likesCount: 0,
      commentsCount: 0,
      viewsCount: 0,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("posts").where("visibility", "==", "public").get()
  );
  // Bob cannot alter Alice's caption
  await assertFails(
    bobDb.collection("posts").doc("post_1").update({
      caption: "Hacked",
      updatedAt: new Date(),
    })
  );
  // Bob CAN increment likesCount by 1
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").update({
      likesCount: 1,
      updatedAt: new Date(),
    })
  );
  // Bob CANNOT jump likesCount by 100
  await assertFails(
    bobDb.collection("posts").doc("post_1").update({
      likesCount: 100,
      updatedAt: new Date(),
    })
  );
});

test("Chats: only participants can read chat thread and messages", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("chats").doc("chat_ab").set({
      participantIds: [ALICE_UID, BOB_UID],
      isGroup: false,
      lastMessageText: "Hi",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
    await db.collection("chats").doc("chat_ab").collection("messages").doc("msg_1").set({
      chatId: "chat_ab",
      senderId: ALICE_UID,
      senderName: "Alice",
      participantIds: [ALICE_UID, BOB_UID],
      text: "Hello Bob!",
      messageType: "text",
      deliveryState: "sent",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("chats").where("participantIds", "array-contains", ALICE_UID).get()
  );

  const outsiderDb = testEnv.authenticatedContext("charlie_999").firestore();
  await assertFails(
    outsiderDb.collection("chats").doc("chat_ab").get()
  );
  await assertFails(
    outsiderDb.collection("chats").doc("chat_ab").collection("messages").doc("msg_1").get()
  );
  await assertFails(
    outsiderDb.collection("chats").doc("chat_ab").collection("messages").get()
  );
});

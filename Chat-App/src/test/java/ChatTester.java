import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatAppTest {

    @Nested
    class MessageMementoTest {

        private final LocalDateTime time = LocalDateTime.of(2026, 11, 15, 10, 30, 0);
        private final MessageMemento memento = new MessageMemento("hello", time);

        @Test
        void constructorStoresContentAndTimestamp() {
            assertEquals("hello", memento.getContent());
            assertEquals(time, memento.getTimestamp());
        }

        @Test
        void getContentReturnsContent() {
            assertEquals("hello", memento.getContent());
        }

        @Test
        void getTimestampReturnsTimestamp() {
            assertEquals(time, memento.getTimestamp());
        }

        @Test
        void equalsIsTrueForSameContentAndTimestamp() {
            assertEquals(memento, new MessageMemento("hello", time));
            assertEquals(memento, memento);
        }

        @Test
        void equalsIsFalseForDifferentContentTimestampOrType() {
            assertNotEquals(memento, new MessageMemento("goodbye", time));
            assertNotEquals(memento, new MessageMemento("hello", time.plusSeconds(1)));
            assertNotEquals(memento, "hello");
            assertNotEquals(memento, null);
        }

        @Test
        void hashCodeIsConsistentWithEquals() {
            assertEquals(memento.hashCode(), new MessageMemento("hello", time).hashCode());
        }

        @Test
        void toStringContainsContentAndTimestamp() {
            String s = memento.toString();
            assertTrue(s.contains("hello"));
            assertTrue(s.contains(time.toString()));
        }
    }

    @Nested
    class MessageTest {

        private User alice;
        private User bob;
        private User carol;
        private Message message;

        @BeforeEach
        void setUp() {
            ChatServer server = new ChatServer();
            alice = new User("Alice", server);
            bob = new User("Bob", server);
            carol = new User("Carol", server);
            message = new Message(alice, Arrays.asList(bob, carol), "hello");
        }

        @Test
        void constructorStoresSenderRecipientsAndContent() {
            assertSame(alice, message.getSender());
            assertEquals(Arrays.asList(bob, carol), message.getRecipients());
            assertEquals("hello", message.getContent());
        }

        @Test
        void constructorSetsTimestampToNow() {
            LocalDateTime before = LocalDateTime.now();
            Message m = new Message(alice, Arrays.asList(bob), "x");
            LocalDateTime after = LocalDateTime.now();
            assertFalse(m.getTimestamp().isBefore(before));
            assertFalse(m.getTimestamp().isAfter(after));
        }

        @Test
        void constructorCopiesRecipientList() {
            List<User> original = new ArrayList<>(Arrays.asList(bob));
            Message m = new Message(alice, original, "x");
            original.add(carol);
            assertEquals(1, m.getRecipients().size());
        }

        @Test
        void getSenderReturnsSender() {
            assertSame(alice, message.getSender());
        }

        @Test
        void getRecipientsReturnsUnmodifiableList() {
            assertThrows(UnsupportedOperationException.class, () -> message.getRecipients().add(alice));
        }

        @Test
        void getTimestampReturnsTimestamp() {
            assertNotNull(message.getTimestamp());
        }

        @Test
        void getContentReturnsContent() {
            assertEquals("hello", message.getContent());
        }

        @Test
        void createMementoSnapshotsContentAndTimestamp() {
            MessageMemento memento = message.createMemento();
            assertEquals("hello", memento.getContent());
            assertEquals(message.getTimestamp(), memento.getTimestamp());
        }

        @Test
        void matchesIsTrueForItsOwnMemento() {
            assertTrue(message.matches(message.createMemento()));
        }

        @Test
        void matchesIsFalseForDifferentContentOrTimestamp() {
            assertFalse(message.matches(new MessageMemento("other", message.getTimestamp())));
            assertFalse(message.matches(new MessageMemento("hello", message.getTimestamp().plusSeconds(1))));
        }

        @Test
        void toStringShowsSenderRecipientsAndContent() {
            String s = message.toString();
            assertTrue(s.startsWith("["));
            assertTrue(s.endsWith("Alice -> Bob, Carol: hello"));
        }
    }

    @Nested
    class ChatHistoryTest {

        private User alice;
        private User bob;
        private ChatHistory history;   // Alice's history (Alice is the owner)

        @BeforeEach
        void setUp() {
            ChatServer server = new ChatServer();
            alice = new User("Alice", server);
            bob = new User("Bob", server);
            history = alice.getChatHistory();
        }

        private Message sentByAlice(String content) {
            return new Message(alice, Arrays.asList(bob), content);
        }

        private Message sentByBob(String content) {
            return new Message(bob, Arrays.asList(alice), content);
        }

        @Test
        void newHistoryIsEmpty() {
            assertEquals(0, history.size());
            assertTrue(history.getMessages().isEmpty());
            assertNull(history.getLastMessageSent());
        }

        @Test
        void addMessageStoresSentAndReceivedMessagesInOrder() {
            Message m1 = sentByAlice("one");
            Message m2 = sentByBob("two");
            history.addMessage(m1);
            history.addMessage(m2);
            assertEquals(Arrays.asList(m1, m2), history.getMessages());
            assertEquals(2, history.size());
        }

        @Test
        void addMessageCreatesMementoOnlyForMessagesSentByOwner() {
            history.addMessage(sentByBob("received"));
            assertNull(history.getLastMessageSent());

            Message sent = sentByAlice("sent");
            history.addMessage(sent);
            assertEquals(sent.createMemento(), history.getLastMessageSent());
        }

        @Test
        void getLastMessageSentReturnsMostRecentSentMessage() {
            history.addMessage(sentByAlice("first"));
            Message second = sentByAlice("second");
            history.addMessage(second);
            history.addMessage(sentByBob("reply"));   // received messages don't count
            assertEquals("second", history.getLastMessageSent().getContent());
            assertEquals(second.createMemento(), history.getLastMessageSent());
        }

        @Test
        void findMessageReturnsMatchingMessage() {
            Message m = sentByAlice("find me");
            history.addMessage(m);
            assertSame(m, history.findMessage(m.createMemento()));
        }

        @Test
        void findMessageReturnsNullWhenNotPresent() {
            history.addMessage(sentByAlice("one"));
            assertNull(history.findMessage(sentByAlice("never added").createMemento()));
        }

        @Test
        void removeMessageRemovesMessageAndItsMemento() {
            Message first = sentByAlice("first");
            Message second = sentByAlice("second");
            history.addMessage(first);
            history.addMessage(second);

            assertTrue(history.removeMessage(second.createMemento()));

            assertEquals(1, history.size());
            assertEquals(Arrays.asList(first), history.getMessages());
            assertEquals(first.createMemento(), history.getLastMessageSent());   // memento stack popped too
        }

        @Test
        void removeMessageWorksForReceivedMessages() {
            Message received = sentByBob("hi");
            Message sent = sentByAlice("yo");
            history.addMessage(received);
            history.addMessage(sent);

            assertTrue(history.removeMessage(received.createMemento()));

            assertEquals(Arrays.asList(sent), history.getMessages());
            assertEquals(sent.createMemento(), history.getLastMessageSent());   // sent memento untouched
        }

        @Test
        void removeMessageReturnsFalseWhenNotPresent() {
            history.addMessage(sentByAlice("one"));
            assertFalse(history.removeMessage(sentByAlice("other").createMemento()));
            assertEquals(1, history.size());
        }

        @Test
        void getMessagesReturnsUnmodifiableView() {
            assertThrows(UnsupportedOperationException.class, () -> history.getMessages().add(sentByAlice("x")));
        }

        @Test
        void sizeCountsAllMessages() {
            assertEquals(0, history.size());
            history.addMessage(sentByAlice("a"));
            history.addMessage(sentByBob("b"));
            assertEquals(2, history.size());
        }
    }

    @Nested
    class ChatServerTest {

        private ChatServer server;
        private User alice;
        private User bob;
        private User carol;

        @BeforeEach
        void setUp() {
            server = new ChatServer();
            alice = new User("Alice", server);
            bob = new User("Bob", server);
            carol = new User("Carol", server);
            server.registerUser(alice);
            server.registerUser(bob);
            server.registerUser(carol);
        }

        // ---- registerUser / isRegistered ----

        @Test
        void registerUserMakesUserRegistered() {
            User dave = new User("Dave", server);
            assertFalse(server.isRegistered(dave));
            server.registerUser(dave);
            assertTrue(server.isRegistered(dave));
        }

        @Test
        void registerUserTwiceDoesNotResetBlocks() {
            server.blockUser(bob, alice);
            server.registerUser(bob);   // duplicate registration
            assertTrue(server.isBlocked(bob, alice));
        }

        @Test
        void isRegisteredIsFalseForUnknownUser() {
            assertFalse(server.isRegistered(new User("Dave", server)));
        }

        // ---- unregisterUser ----

        @Test
        void unregisterUserRemovesUser() {
            server.unregisterUser(carol);
            assertFalse(server.isRegistered(carol));
            assertTrue(server.isRegistered(alice));
        }

        @Test
        void unregisterUserClearsBlocksInvolvingThatUser() {
            server.blockUser(bob, carol);
            server.blockUser(carol, alice);
            server.unregisterUser(carol);
            server.registerUser(carol);
            assertFalse(server.isBlocked(bob, carol));     // Bob's block on Carol was removed
            assertFalse(server.isBlocked(carol, alice));   // Carol's own blocks were removed
        }

        @Test
        void unregisterUnknownUserDoesNothing() {
            User dave = new User("Dave", server);
            assertDoesNotThrow(() -> server.unregisterUser(dave));
            assertFalse(server.isRegistered(dave));
        }

        // ---- sendMessage ----

        @Test
        void sendMessageDeliversToSingleRecipient() {
            server.sendMessage(alice, Arrays.asList(bob), "hi");

            assertEquals(1, alice.getChatHistory().size());
            assertEquals(1, bob.getChatHistory().size());
            assertEquals(0, carol.getChatHistory().size());
            assertEquals("hi", bob.getChatHistory().getMessages().get(0).getContent());
            assertSame(alice, bob.getChatHistory().getMessages().get(0).getSender());
        }

        @Test
        void sendMessageDeliversToMultipleRecipients() {
            server.sendMessage(alice, Arrays.asList(bob, carol), "meeting");

            assertEquals(1, bob.getChatHistory().size());
            assertEquals(1, carol.getChatHistory().size());
            assertEquals(Arrays.asList(bob, carol),
                    alice.getChatHistory().getMessages().get(0).getRecipients());
        }

        @Test
        void sendMessageFromUnregisteredSenderIsIgnored() {
            User dave = new User("Dave", server);
            server.sendMessage(dave, Arrays.asList(bob), "hi");
            assertEquals(0, dave.getChatHistory().size());
            assertEquals(0, bob.getChatHistory().size());
        }

        @Test
        void sendMessageSkipsUnregisteredRecipients() {
            User dave = new User("Dave", server);
            server.sendMessage(alice, Arrays.asList(bob, dave), "hi");

            assertEquals(1, bob.getChatHistory().size());
            assertEquals(0, dave.getChatHistory().size());
            assertEquals(Arrays.asList(bob), alice.getChatHistory().getMessages().get(0).getRecipients());
        }

        @Test
        void sendMessageSkipsSenderAsRecipientAndDuplicates() {
            server.sendMessage(alice, Arrays.asList(alice, bob, bob), "hi");

            assertEquals(1, alice.getChatHistory().size());   // only her sent copy
            assertEquals(1, bob.getChatHistory().size());     // delivered once
            assertEquals(Arrays.asList(bob), alice.getChatHistory().getMessages().get(0).getRecipients());
        }

        @Test
        void sendMessageWithNoValidRecipientsSendsNothing() {
            User dave = new User("Dave", server);
            server.sendMessage(alice, Arrays.asList(dave), "hi");
            server.sendMessage(alice, Arrays.asList(alice), "hi");
            server.sendMessage(alice, Arrays.<User>asList(), "hi");
            assertEquals(0, alice.getChatHistory().size());
        }

        @Test
        void sendMessageIsNotDeliveredToUsersWhoBlockedTheSender() {
            server.blockUser(carol, alice);
            server.sendMessage(alice, Arrays.asList(bob, carol), "lunch?");

            assertEquals(1, bob.getChatHistory().size());
            assertEquals(0, carol.getChatHistory().size());
            assertEquals(1, alice.getChatHistory().size());   // sender still keeps her copy
        }

        @Test
        void blockingOnlyAffectsTheBlockedSender() {
            server.blockUser(carol, alice);
            server.sendMessage(bob, Arrays.asList(carol), "still reaches you");
            assertEquals(1, carol.getChatHistory().size());
        }

        // ---- blockUser / unblockUser / isBlocked ----

        @Test
        void blockUserMarksSenderAsBlockedForThatRecipientOnly() {
            server.blockUser(bob, alice);
            assertTrue(server.isBlocked(bob, alice));
            assertFalse(server.isBlocked(alice, bob));    // not mutual
            assertFalse(server.isBlocked(carol, alice));  // other users unaffected
        }

        @Test
        void blockUserByUnregisteredUserIsIgnored() {
            User dave = new User("Dave", server);
            server.blockUser(dave, alice);
            assertFalse(server.isBlocked(dave, alice));
        }

        @Test
        void unblockUserRemovesBlock() {
            server.blockUser(bob, alice);
            server.unblockUser(bob, alice);
            assertFalse(server.isBlocked(bob, alice));

            server.sendMessage(alice, Arrays.asList(bob), "hi again");
            assertEquals(1, bob.getChatHistory().size());
        }

        @Test
        void unblockUserWhoWasNotBlockedOrUnregisteredDoesNothing() {
            User dave = new User("Dave", server);
            assertDoesNotThrow(() -> server.unblockUser(bob, alice));
            assertDoesNotThrow(() -> server.unblockUser(dave, alice));
            assertFalse(server.isBlocked(bob, alice));
        }

        @Test
        void isBlockedIsFalseForUnknownRecipient() {
            assertFalse(server.isBlocked(new User("Dave", server), alice));
        }

        // ---- undoLastMessage ----

        @Test
        void undoLastMessageRemovesMessageFromSenderAndAllRecipients() {
            server.sendMessage(alice, Arrays.asList(bob, carol), "oops");

            assertTrue(server.undoLastMessage(alice));

            assertEquals(0, alice.getChatHistory().size());
            assertEquals(0, bob.getChatHistory().size());
            assertEquals(0, carol.getChatHistory().size());
            assertNull(alice.getChatHistory().getLastMessageSent());
        }

        @Test
        void undoLastMessageReturnsFalseWhenNothingToUndo() {
            assertFalse(server.undoLastMessage(alice));
        }

        @Test
        void undoLastMessageOnlyUndoesTheNewestAndThenTheNextNewest() {
            server.sendMessage(alice, Arrays.asList(bob), "first");
            server.sendMessage(alice, Arrays.asList(bob), "second");

            assertTrue(server.undoLastMessage(alice));
            assertEquals(1, bob.getChatHistory().size());
            assertEquals("first", bob.getChatHistory().getMessages().get(0).getContent());

            assertTrue(server.undoLastMessage(alice));
            assertEquals(0, bob.getChatHistory().size());

            assertFalse(server.undoLastMessage(alice));
        }

        @Test
        void undoLastMessageIgnoresMessagesReceivedFromOthers() {
            server.sendMessage(alice, Arrays.asList(bob), "from alice");
            server.sendMessage(bob, Arrays.asList(alice), "from bob");

            assertTrue(server.undoLastMessage(alice));   // undoes Alice's own message only

            assertEquals(1, alice.getChatHistory().size());
            assertEquals("from bob", alice.getChatHistory().getMessages().get(0).getContent());
            assertEquals(1, bob.getChatHistory().size());
            assertEquals("from bob", bob.getChatHistory().getMessages().get(0).getContent());
        }

        @Test
        void undoLastMessageWorksWhenARecipientBlockedTheSender() {
            server.blockUser(carol, alice);
            server.sendMessage(alice, Arrays.asList(bob, carol), "hi");

            assertTrue(server.undoLastMessage(alice));

            assertEquals(0, alice.getChatHistory().size());
            assertEquals(0, bob.getChatHistory().size());
            assertEquals(0, carol.getChatHistory().size());
        }
    }

    @Nested
    class UserTest {

        private ChatServer server;
        private User alice;
        private User bob;
        private User carol;

        @BeforeEach
        void setUp() {
            server = new ChatServer();
            alice = new User("Alice", server);
            bob = new User("Bob", server);
            carol = new User("Carol", server);
            server.registerUser(alice);
            server.registerUser(bob);
            server.registerUser(carol);
        }

        /** Runs the action and returns everything it printed to System.out. */
        private String captureOutput(Runnable action) {
            PrintStream original = System.out;
            ByteArrayOutputStream captured = new ByteArrayOutputStream();
            System.setOut(new PrintStream(captured));
            try {
                action.run();
            } finally {
                System.setOut(original);
            }
            return captured.toString();
        }

        @Test
        void constructorSetsNameAndCreatesEmptyHistoryOwnedByUser() {
            assertEquals("Alice", alice.getName());
            assertNotNull(alice.getChatHistory());
            assertEquals(0, alice.getChatHistory().size());

            // The history belongs to the user: a message Alice sends creates a memento in it
            alice.sendMessage("hi", bob);
            assertNotNull(alice.getChatHistory().getLastMessageSent());
            assertNull(bob.getChatHistory().getLastMessageSent());
        }

        @Test
        void getNameReturnsName() {
            assertEquals("Bob", bob.getName());
        }

        @Test
        void getChatHistoryReturnsTheUsersHistory() {
            assertSame(alice.getChatHistory(), alice.getChatHistory());
            assertNotSame(alice.getChatHistory(), bob.getChatHistory());
        }

        @Test
        void sendMessageVarargsDeliversThroughServer() {
            alice.sendMessage("hello all", bob, carol);
            assertEquals(1, alice.getChatHistory().size());
            assertEquals(1, bob.getChatHistory().size());
            assertEquals(1, carol.getChatHistory().size());
        }

        @Test
        void sendMessageListDeliversThroughServer() {
            alice.sendMessage("hello", Arrays.asList(bob));
            assertEquals(1, bob.getChatHistory().size());
            assertEquals(0, carol.getChatHistory().size());
            assertEquals("hello", bob.getChatHistory().getMessages().get(0).getContent());
        }

        @Test
        void receiveMessageAddsToHistoryAndPrintsIt() {
            Message m = new Message(alice, Arrays.asList(bob), "hi Bob");

            String output = captureOutput(() -> bob.receiveMessage(m));

            assertEquals(1, bob.getChatHistory().size());
            assertSame(m, bob.getChatHistory().getMessages().get(0));
            assertTrue(output.contains("[Bob received] Alice: hi Bob"));
            assertNull(bob.getChatHistory().getLastMessageSent());   // received, not sent
        }

        @Test
        void undoLastMessageRemovesItEverywhere() {
            alice.sendMessage("oops", bob, carol);

            assertTrue(alice.undoLastMessage());

            assertEquals(0, alice.getChatHistory().size());
            assertEquals(0, bob.getChatHistory().size());
            assertEquals(0, carol.getChatHistory().size());
        }

        @Test
        void undoLastMessageReturnsFalseWhenNothingToUndo() {
            assertFalse(alice.undoLastMessage());
        }

        @Test
        void blockUserStopsMessagesFromThatUser() {
            carol.blockUser(alice);
            alice.sendMessage("hi", bob, carol);

            assertTrue(server.isBlocked(carol, alice));
            assertEquals(0, carol.getChatHistory().size());
            assertEquals(1, bob.getChatHistory().size());
        }

        @Test
        void unblockUserRestoresMessages() {
            carol.blockUser(alice);
            carol.unblockUser(alice);
            alice.sendMessage("hi", carol);

            assertFalse(server.isBlocked(carol, alice));
            assertEquals(1, carol.getChatHistory().size());
        }

        @Test
        void viewChatHistoryPrintsHeaderAndMessages() {
            alice.sendMessage("first", bob);
            alice.sendMessage("second", bob);

            String output = captureOutput(() -> alice.viewChatHistory());

            assertTrue(output.contains("Chat history for Alice (2 messages)"));
            assertTrue(output.contains("Alice -> Bob: first"));
            assertTrue(output.contains("Alice -> Bob: second"));
            assertTrue(output.indexOf("first") < output.indexOf("second"));
        }

        @Test
        void viewChatHistoryOnEmptyHistoryPrintsOnlyHeader() {
            String output = captureOutput(() -> carol.viewChatHistory());
            assertTrue(output.contains("Chat history for Carol (0 messages)"));
            assertFalse(output.contains("->"));
        }

        @Test
        void toStringReturnsName() {
            assertEquals("Alice", alice.toString());
        }
    }

}

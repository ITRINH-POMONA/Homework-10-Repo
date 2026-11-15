public class Main {
    public static void main(String[] args){
        ChatServer server = new ChatServer();
        User alice = new User("Alice", server);
        User bob = new User("Bob", server);
        User carol = new User("Carol", server);

        server.registerUser(alice);
        server.registerUser(bob);
        server.registerUser(carol);

        System.out.println("\n1. Sending messages (one recipient and several recipients):");
        alice.sendMessage("Hi Bob!", bob);
        bob.sendMessage("Hey Alice, what's up?", alice);
        alice.sendMessage("Meeting at 3pm", bob, carol);

        System.out.println();
        alice.viewChatHistory();
        bob.viewChatHistory();
        carol.viewChatHistory();

        System.out.println("\n=== 2. Undo the last message:");
        alice.undoLastMessage();
        System.out.println();
        alice.viewChatHistory();
        bob.viewChatHistory();
        carol.viewChatHistory();

        System.out.println("\n3. Undo again, then undo with nothing left to undo:");
        alice.undoLastMessage();
        alice.undoLastMessage();

        System.out.println("\n4. Blocking:");
        carol.blockUser(alice);
        alice.sendMessage("Lunch tomorrow?", bob, carol);   // Bob gets it, Carol doesn't
        bob.sendMessage("Sounds good, Carol?", carol);       // Carol still hears from Bob
        System.out.println();
        carol.viewChatHistory();

        System.out.println("\n5. Unblocking:");
        carol.unblockUser(alice);
        alice.sendMessage("Carol, can you see this now?", carol);

        System.out.println("\n=== 6. Unregistering ===");
        server.unregisterUser(carol);
        alice.sendMessage("Anyone there?", bob, carol);      // Carol is skipped

        System.out.println("\nFinal chat histories:");
        alice.viewChatHistory();
        bob.viewChatHistory();
        carol.viewChatHistory();
    }
}

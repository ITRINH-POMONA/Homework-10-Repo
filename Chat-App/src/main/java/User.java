import java.util.Arrays;
import java.util.List;

public class User {
    private final String name;
    private final ChatServer server;
    private final ChatHistory history;

    public User(String name, ChatServer server) {
        this.name = name;
        this.server = server;
        this.history = new ChatHistory(this);
    }

    public String getName()            { return name; }
    public ChatHistory getChatHistory() { return history; }

    // Used by mediator to send and recieve messages
    public void sendMessage(String content, List<User> recipients) {
        server.sendMessage(this, recipients, content);
    }

    public void receiveMessage(Message message) {
        history.addMessage(message);
        System.out.println("  [" + name + " received] " + message.getSender().getName() + ": " + message.getContent());
    }

    public boolean undoLastMessage() {
        return server.undoLastMessage(this);
    }

    public void blockUser(User other)   { server.blockUser(this, other); }
    public void unblockUser(User other) { server.unblockUser(this, other); }

    public void viewChatHistory() {
        System.out.println("--- Chat history for " + name + " (" + history.size() + " messages) ---");
        for (Message m : history.getMessages()) {
            System.out.println("  " + m);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}

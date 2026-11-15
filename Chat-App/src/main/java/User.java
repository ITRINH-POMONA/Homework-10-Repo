import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class User implements IterableByUser{
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

    public void sendMessage(String content, User... recipients) {
        sendMessage(content, Arrays.asList(recipients));
    }
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

    @Override
    public Iterator<Message> iterator(User userToSearchWith) {
        return history.iterator(userToSearchWith);
    }

    public void viewMessagesWith(User other) {
        System.out.println("--- Messages between " + name + " and " + other.getName() + " ---");
        Iterator<Message> it = iterator(other);
        while (it.hasNext()) {
            System.out.println("  " + it.next());
        }
    }

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

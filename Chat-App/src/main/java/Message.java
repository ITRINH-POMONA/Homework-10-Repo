import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Message {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final User sender;
    private final List<User> recipients;
    private final LocalDateTime timestamp;
    private final String content;

    public Message(User sender, List<User> recipients, String content) {
        this.sender = sender;
        this.recipients = Collections.unmodifiableList(new ArrayList<>(recipients));
        this.timestamp = LocalDateTime.now();
        this.content = content;
    }

    public User getSender()             { return sender; }
    public List<User> getRecipients()   { return recipients; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getContent()          { return content; }

    public MessageMemento createMemento() {
        return new MessageMemento(content, timestamp);
    }

    public boolean matches(MessageMemento memento) {
        return content.equals(memento.getContent()) && timestamp.equals(memento.getTimestamp());
    }

    public String toString() {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < recipients.size(); i++) {
            if (i > 0) names.append(", ");
            names.append(recipients.get(i).getName());
        }
        return "[" + timestamp.format(FORMAT) + "] " + sender.getName() + " -> " + names + ": " + content;
    }
}

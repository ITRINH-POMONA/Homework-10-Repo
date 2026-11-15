import java.time.LocalDateTime;
import java.util.Objects;

public class MessageMemento {
    private final String content;
    private final LocalDateTime timestamp;

    public MessageMemento(String content, LocalDateTime timestamp) {
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getContent()          { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageMemento)) return false;
        MessageMemento other = (MessageMemento) o;
        return content.equals(other.content) && timestamp.equals(other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, timestamp);
    }

    @Override
    public String toString() {
        return "Memento{content='" + content + "', timestamp=" + timestamp + "}";
    }
}

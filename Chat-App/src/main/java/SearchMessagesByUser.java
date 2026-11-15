import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class SearchMessagesByUser implements Iterator<Message> {
    private final List<Message> messages;
    private final User user;
    private int position = 0;   // used as index of the next message to look at

    public SearchMessagesByUser(List<Message> messages, User user) {
        this.messages = new ArrayList<>(messages);
        this.user = user;
    }

    @Override
    public boolean hasNext() {
        while (position < messages.size()) {
            if (involvesUser(messages.get(position))) return true;
            position++;
        }
        return false;
    }

    public Message next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more messages involving " + user.getName());
        }
        return messages.get(position++);
    }

    private boolean involvesUser(Message m) {
        return m.getSender() == user || m.getRecipients().contains(user);
    }
}

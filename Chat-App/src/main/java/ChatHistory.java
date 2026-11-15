import java.util.*;

public class ChatHistory implements IterableByUser {
    private final User owner;
    private final List<Message> messages = new ArrayList<>();
    private final Deque<MessageMemento> sentMementos = new ArrayDeque<>();

    public ChatHistory(User owner) {
        this.owner = owner;
    }

    public void addMessage(Message message) {
        messages.add(message);
        if (message.getSender() == owner) {
            sentMementos.push(message.createMemento());
        }
    }

    public MessageMemento getLastMessageSent() {
        return sentMementos.peek();
    }

    public Message findMessage(MessageMemento memento) {
        for (Message m : messages) {
            if (m.matches(memento)) return m;
        }
        return null;
    }

    public boolean removeMessage(MessageMemento memento) {
        Message found = findMessage(memento);
        if (found == null) return false;
        messages.remove(found);
        if (found.getSender() == owner) {
            sentMementos.remove(memento);
        }
        return true;
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public int size() {
        return messages.size();
    }

    @Override
    public Iterator<Message> iterator(User userToSearchWith) {
        return new SearchMessagesByUser(messages, userToSearchWith);
    }
}

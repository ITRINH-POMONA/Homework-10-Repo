import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatServer {
    private final Set<User> users = new LinkedHashSet<>();
    private final Map<User, Set<User>> blocked = new HashMap<>();

    public void registerUser(User user) {
        if (users.add(user)) {
            blocked.put(user, new HashSet<>());
            System.out.println("[Server] " + user.getName() + " joined the chat.");
        }
    }

    public void unregisterUser(User user) {
        if (users.remove(user)) {
            blocked.remove(user);
            for (Set<User> set : blocked.values()) set.remove(user);
            System.out.println("[Server] " + user.getName() + " left the chat.");
        }
    }

    public boolean isRegistered(User user) {
        return users.contains(user);
    }

    public void sendMessage(User sender, List<User> recipients, String content) {
        if (!users.contains(sender)) {
            System.out.println("[Server] " + sender.getName() + " is not registered and cannot send messages.");
            return;
        }

        // Only registered users other than the sender can be recipients
        List<User> targets = new ArrayList<>();
        for (User r : recipients) {
            if (r != sender && users.contains(r) && !targets.contains(r)) {
                targets.add(r);
            } else {
                System.out.println("[Server] Skipping recipient " + r.getName() + " (not registered or is the sender).");
            }
        }
        if (targets.isEmpty()) {
            System.out.println("[Server] No valid recipients. Message not sent.");
            return;
        }

        Message message = new Message(sender, targets, content);
        sender.getChatHistory().addMessage(message);

        for (User target : targets) {
            if (isBlocked(target, sender)) {
                System.out.println("[Server] " + target.getName() + " has blocked " + sender.getName()
                        + ". Message not delivered to " + target.getName() + ".");
            } else {
                target.receiveMessage(message);
            }
        }
    }

    public void blockUser(User blocker, User toBlock) {
        if (!users.contains(blocker)) return;
        blocked.get(blocker).add(toBlock);
        System.out.println("[Server] " + blocker.getName() + " blocked " + toBlock.getName() + ".");
    }
    public void unblockUser(User blocker, User toUnblock) {
        if (!users.contains(blocker)) return;
        if (blocked.get(blocker).remove(toUnblock)) {
            System.out.println("[Server] " + blocker.getName() + " unblocked " + toUnblock.getName() + ".");
        }
    }

    public boolean isBlocked(User recipient, User sender) {
        Set<User> set = blocked.get(recipient);
        return set != null && set.contains(sender);
    }

    public boolean undoLastMessage(User sender) {
        ChatHistory senderHistory = sender.getChatHistory();
        MessageMemento memento = senderHistory.getLastMessageSent();
        if (memento == null) {
            System.out.println("[Server] " + sender.getName() + " has no message to undo.");
            return false;
        }

        Message message = senderHistory.findMessage(memento);
        for (User recipient : message.getRecipients()) {
            recipient.getChatHistory().removeMessage(memento);
        }
        senderHistory.removeMessage(memento);
        System.out.println("[Server] " + sender.getName() + " undid the message: \"" + memento.getContent() + "\"");
        return true;
    }
}

package db.dao;

import Message.Message;
import db.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DMDAO {

    public void saveMessage(String conversationId, String senderId, String receiverId, String content, long timestamp) {
        String sql = """
    INSERT INTO dm_messages
    (conversation_id, sender_id, receiver_id, content, timestamp)
    VALUES (?, ?, ?, ?, ?)
    """;
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, conversationId);
            ps.setString(2, senderId);
            ps.setString(3, receiverId);
            ps.setString(4, content);
            ps.setLong(5, timestamp);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Message> getMessages(String userId, String friendId) {
        List<Message> messages = new ArrayList<>();
        String conv = userId.compareTo(friendId) < 0 ?
                userId + "_" + friendId :
                friendId + "_" + userId;

        String sql = """
    SELECT sender_id, receiver_id, content, timestamp
    FROM dm_messages
    WHERE conversation_id = ?
    ORDER BY timestamp ASC
    """;

        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, conv);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Message msg = new Message(
                        java.util.UUID.randomUUID().toString(),
                        rs.getString("sender_id"),
                        rs.getString("receiver_id"),
                        rs.getString("content"),
                        Message.MessageType.DIRECT_MESSAGE,
                        rs.getLong("timestamp")
                );
                messages.add(msg);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return messages;
    }

    public List<Message> searchMessages(String userId, String friendId, String keyword) {
        List<Message> messages = new ArrayList<>();

        String conv = userId.compareTo(friendId) < 0
                ? userId + "_" + friendId
                : friendId + "_" + userId;

        String sql = """
    SELECT sender_id, receiver_id, content, timestamp
    FROM dm_messages
    WHERE conversation_id = ?
    AND content LIKE ?
    ORDER BY timestamp ASC
    """;

        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, conv);
            ps.setString(2, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                messages.add(new Message(
                        UUID.randomUUID().toString(),
                        rs.getString("sender_id"),
                        rs.getString("receiver_id"),
                        rs.getString("content"),
                        Message.MessageType.DIRECT_MESSAGE,
                        rs.getLong("timestamp")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return messages;
    }

}

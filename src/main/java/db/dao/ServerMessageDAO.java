package db.dao;

import db.Database;
import java.sql.*;

public class ServerMessageDAO {

    public void saveMessage(String serverId, String senderId, String content, long timestamp) {
        String sql = """
        INSERT INTO server_messages
        (server_id, sender_id, content, timestamp)
        VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, serverId);
            ps.setString(2, senderId);
            ps.setString(3, content);
            ps.setLong(4, timestamp);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

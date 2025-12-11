package db.dao;

import db.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendDAO {

    public void addFriendship(String userId, String friendId) {
        String sql = "INSERT OR IGNORE INTO friends (user_id, friend_id) VALUES (?, ?)";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, friendId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean areFriends(String userA, String userB) {
        String sql = "SELECT 1 FROM friends WHERE user_id=? AND friend_id=?";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, userA);
            ps.setString(2, userB);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getFriends(String userId) {
        List<String> result = new ArrayList<>();
        String sql = "SELECT friend_id FROM friends WHERE user_id=?";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("friend_id"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}

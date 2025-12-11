package db.dao;

import db.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlockedDAO {

    public void blockUser(String userId, String blockedId) {
        String sql = "INSERT OR IGNORE INTO blocked (user_id, blocked_id) VALUES (?, ?)";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, blockedId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void unblockUser(String userId, String blockedId) {
        String sql = "DELETE FROM blocked WHERE user_id=? AND blocked_id=?";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, blockedId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isBlocked(String userId, String otherId) {
        String sql = "SELECT 1 FROM blocked WHERE user_id=? AND blocked_id=?";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, otherId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getBlockedUsers(String userId) {
        List<String> result = new ArrayList<>();
        String sql = "SELECT blocked_id FROM blocked WHERE user_id=?";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("blocked_id"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}

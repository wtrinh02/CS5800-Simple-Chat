package db.dao;

import db.Database;
import db.model.DbUser;

import java.sql.*;

public class UserDAO {

    public boolean exists(String userId) {
        String sql = "SELECT 1 FROM users WHERE id = ?";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Legacy convenience overload – if something still calls the old version
    public void createUser(String id, String username, String email) {
        // default dummy password
        createUser(id, username, email, "NO_PASSWORD_SET");
    }


    public void createUser(String id, String username, String email, String passwordHash) {
        String sql = "INSERT OR IGNORE INTO users (id, username, email, password_hash, online) VALUES (?, ?, ?, ?, 0)";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.setString(4, passwordHash);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public DbUser getUserById(String userId) {
        String sql = "SELECT id, username, email FROM users WHERE id = ?";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new DbUser(
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("email"));
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPasswordHash(String userId) {
        String sql = "SELECT password_hash FROM users WHERE id = ?";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("password_hash");
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void setOnline(String userId, boolean online) {
        String sql = "UPDATE users SET online = ? WHERE id = ?";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, online ? 1 : 0);
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

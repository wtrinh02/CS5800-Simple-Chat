package db.dao;

import db.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServerDAO {

    public boolean exists(String serverId) {
        String sql = "SELECT 1 FROM servers WHERE id=?";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, serverId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createServer(String id, String name, String ownerId) {
        String sql = "INSERT OR IGNORE INTO servers (id, name, owner_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setString(3, ownerId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addMember(String serverId, String userId) {
        String sql = "INSERT OR IGNORE INTO server_members (server_id, user_id) VALUES (?, ?)";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, serverId);
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeMember(String serverId, String userId) {
        String sql = "DELETE FROM server_members WHERE server_id=? AND user_id=?";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, serverId);
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getMembers(String serverId) {
        List<String> members = new ArrayList<>();
        String sql = "SELECT user_id FROM server_members WHERE server_id=?";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, serverId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                members.add(rs.getString("user_id"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return members;
    }

    public List<String> listServers() {
        List<String> servers = new ArrayList<>();
        String sql = "SELECT id, name FROM servers";
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                servers.add(rs.getString("id") + ":" + rs.getString("name"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return servers;
    }
}

package db;

import java.sql.Connection;
import java.sql.Statement;

public class SchemaManager {

    public static void initialize() {
        try {
            Connection conn = Database.getInstance().getConnection();
            Statement stmt = conn.createStatement();


            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    email TEXT,
                    password_hash TEXT NOT NULL,
                    online INTEGER NOT NULL DEFAULT 0
                );
            """);


            stmt.execute("""
                CREATE TABLE IF NOT EXISTS friends (
                    user_id TEXT NOT NULL,
                    friend_id TEXT NOT NULL,
                    PRIMARY KEY (user_id, friend_id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS blocked (
                    user_id TEXT NOT NULL,
                    blocked_id TEXT NOT NULL,
                    PRIMARY KEY (user_id, blocked_id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS dm_messages (
                    id TEXT PRIMARY KEY,
                    conversation_id TEXT NOT NULL,
                    sender_id TEXT NOT NULL,
                    receiver_id TEXT NOT NULL,
                    content TEXT NOT NULL,
                    timestamp INTEGER NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS servers (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    owner_id TEXT NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS server_members (
                    server_id TEXT NOT NULL,
                    user_id TEXT NOT NULL,
                    PRIMARY KEY (server_id, user_id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS server_messages (
                    id TEXT PRIMARY KEY,
                    server_id TEXT NOT NULL,
                    sender_id TEXT NOT NULL,
                    content TEXT NOT NULL,
                    timestamp INTEGER NOT NULL
                );
            """);


            stmt.close();
            System.out.println("Database schema initialized.");

        } catch (Exception e) {
            throw new RuntimeException("Schema initialization FAILED", e);
        }
    }
}

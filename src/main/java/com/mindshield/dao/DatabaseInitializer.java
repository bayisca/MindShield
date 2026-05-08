package com.mindshield.dao;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void init() {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS users (
                            id VARCHAR(50) PRIMARY KEY,
                            username VARCHAR(50) NOT NULL UNIQUE,
                            password VARCHAR(255) NOT NULL,

                            role VARCHAR(20) NOT NULL,
                            -- 'CLIENT' veya 'COUNSELOR'

                            profession VARCHAR(100),
                            -- sadece counselor için dolu olur

                            bio TEXT,
                            profile_image VARCHAR(255),

                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                    """);
            // MEDIA TABLE
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS media (
                            id VARCHAR(50) PRIMARY KEY,
                            title VARCHAR(100),
                            artist VARCHAR(100),
                            url VARCHAR(255),
                            category VARCHAR(50)
                        )
                    """);
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS favorite_songs (
                            id VARCHAR(50) PRIMARY KEY,
                            user_id VARCHAR(50) NOT NULL,
                            media_id VARCHAR(50) NOT NULL,

                            added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            FOREIGN KEY (user_id) REFERENCES users(id),
                            FOREIGN KEY (media_id) REFERENCES media(id)
                        )
                    """);

            // JOURNALS TABLE
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS journals (
                            id VARCHAR(50) PRIMARY KEY,
                            user_id VARCHAR(50),
                            title VARCHAR(100),
                            content TEXT,
                            mood VARCHAR(50),
                            created_at TIMESTAMP,
                            FOREIGN KEY (user_id) REFERENCES users(id)
                        )
                    """);

            // CHATROOMS TABLE
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS chatrooms (
                            id VARCHAR(50) PRIMARY KEY,
                            room_name VARCHAR(100),
                            is_private BOOLEAN
                        )
                    """);

            // 🔥 EKSTRA (OLMASI GEREKENLER)

            // CHATROOM MEMBERS
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS chatroom_members (
                            room_id VARCHAR(50),
                            user_id VARCHAR(50),
                            PRIMARY KEY (room_id, user_id),
                            FOREIGN KEY (room_id) REFERENCES chatrooms(id),
                            FOREIGN KEY (user_id) REFERENCES users(id)
                        )
                    """);

            // MESSAGES
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS chatroomMessages (
                            id VARCHAR(50) PRIMARY KEY,
                            room_id VARCHAR(50),
                            sender_id VARCHAR(50),
                            content TEXT,
                            created_at TIMESTAMP,
                            FOREIGN KEY (room_id) REFERENCES chatrooms(id),
                            FOREIGN KEY (sender_id) REFERENCES users(id)
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS blog_posts (
                            id VARCHAR(50) PRIMARY KEY,
                            user_id VARCHAR(50) NOT NULL,
                            title VARCHAR(255) NOT NULL,
                            content TEXT NOT NULL,
                            image_url VARCHAR(255),
                            tags VARCHAR(255),
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            FOREIGN KEY (user_id) REFERENCES users(id)
                        )
                    """);
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS blog_comments (
                            id VARCHAR(50) PRIMARY KEY,

                            post_id VARCHAR(50) NOT NULL,
                            user_id VARCHAR(50) NOT NULL,

                            content TEXT NOT NULL,

                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            FOREIGN KEY (post_id)
                                REFERENCES blog_posts(id)
                                ON DELETE CASCADE,

                            FOREIGN KEY (user_id)
                                REFERENCES users(id)
                                ON DELETE CASCADE
                        )
                    """);

            // özel dm konusmaları
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS conversations (
                            id VARCHAR(50) PRIMARY KEY,
                            user1_id VARCHAR(50) NOT NULL,
                            user2_id VARCHAR(50) NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            FOREIGN KEY (user1_id) REFERENCES users(id),
                            FOREIGN KEY (user2_id) REFERENCES users(id)
                        )
                    """);
            // özel dmlerin mesajlarının kayıtları
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS DMmessages (
                            id VARCHAR(50) PRIMARY KEY,
                            conversation_id VARCHAR(50) NOT NULL,
                            sender_id VARCHAR(50) NOT NULL,
                            content TEXT NOT NULL,
                            is_read BOOLEAN DEFAULT FALSE,
                            sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            FOREIGN KEY (conversation_id) REFERENCES conversations(id),
                            FOREIGN KEY (sender_id) REFERENCES users(id)
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS forum_categories (
                            id VARCHAR(50) PRIMARY KEY,
                            name VARCHAR(100) NOT NULL UNIQUE,
                            description TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS forum_topics (
                            id VARCHAR(50) PRIMARY KEY,
                            category_id VARCHAR(50) NOT NULL,
                            user_id VARCHAR(50) NOT NULL,

                            title VARCHAR(255) NOT NULL,
                            content TEXT NOT NULL,

                            view_count INT DEFAULT 0,
                            like_count INT DEFAULT 0,

                            is_locked BOOLEAN DEFAULT FALSE,
                            is_pinned BOOLEAN DEFAULT FALSE,

                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            FOREIGN KEY (category_id) REFERENCES forum_categories(id),
                            FOREIGN KEY (user_id) REFERENCES users(id)
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS forum_replies (
                            id VARCHAR(50) PRIMARY KEY,
                            topic_id VARCHAR(50) NOT NULL,
                            user_id VARCHAR(50) NOT NULL,

                            content TEXT NOT NULL,

                            like_count INT DEFAULT 0,

                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            FOREIGN KEY (topic_id) REFERENCES forum_topics(id),
                            FOREIGN KEY (user_id) REFERENCES users(id)
                        )
                    """);
            // yorumlar
            System.out.println("✅ Tablolar başarıyla oluşturuldu.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
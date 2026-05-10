package com.mindshield.dao;

import com.mindshield.models.BaseUser;
import com.mindshield.models.JournalEntry;
import com.mindshield.models.JournalMood;
import com.mindshield.models.StandardUser;
import com.mindshield.ui.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JournalDaoImpl implements JournalDao {
    private final UserDao userDao;
 
    public JournalDaoImpl() {
        this.userDao = new UserDaoImpl();
    }
 
    public JournalDaoImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * H2 Database bağlantısı
     */
    private Connection getConnection() throws SQLException {

        return DatabaseConnection.getConnection();
        
    }

    /** 
     * Yeni journal kaydet
     */
    @Override
    public void save(JournalEntry entry) {

        String sql = """
            INSERT INTO journals (
                id,
                user_id,
                title,
                content,
                mood,
                created_at
            )
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, entry.getId());

            stmt.setString(2, entry.getAuthor().getId());

            stmt.setString(3, entry.getTitle());

            stmt.setString(4, entry.getBody());

            stmt.setString(5, entry.getMood().name());

            stmt.setTimestamp(
                    6,
                    Timestamp.valueOf(entry.getCreatedAt())
            );

            stmt.executeUpdate();
            System.out.println("Journal kaydediliyor...");
System.out.println(entry.getTitle());
System.out.println(entry.getAuthor().getId());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method ismi aynı bırakıldı.
     * DB kullandığımız için burada SQL UPDATE çalışıyor.
     */
    @Override
    public void updateAll() {

        String sql = """
            UPDATE journals
            SET
                title = ?,
                content = ?,
                mood = ?
            WHERE id = ?
        """;

        try (
                Connection conn = getConnection()
        ) {

            conn.setAutoCommit(false);

            List<JournalEntry> entries = findAll();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (JournalEntry entry : entries) {

                    stmt.setString(1, entry.getTitle());

                    stmt.setString(2, entry.getBody());

                    stmt.setString(3, entry.getMood().name());

                    stmt.setString(4, entry.getId());

                    stmt.addBatch();
                }

                stmt.executeBatch();
            }

            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * ID ile sil
     */
    @Override
    public void deleteById(String id) {

        if (id == null) return;

        String sql = """
            DELETE FROM journals
            WHERE id = ?
        """;

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * ID ile journal bul
     */
    @Override
    public JournalEntry findById(String id) {

        if (id == null) return null;

        String sql = """
            SELECT *
            FROM journals
            WHERE id = ?
        """;

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return mapJournal(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Tüm journalları getir
     */
    @Override
    public List<JournalEntry> findAll() {

        List<JournalEntry> entries = new ArrayList<>();

        String sql = """
            SELECT *
            FROM journals
            ORDER BY created_at DESC
        """;

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {

            while (rs.next()) {

                entries.add(mapJournal(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return entries;
    }

    /**
     * Kullanıcının tüm journallarını sil
     */
    @Override
    public void deleteEntriesByAuthorPersona(String persona) {

        if (persona == null || persona.isBlank()) {
            return;
        }

        String sql = """
            DELETE FROM journals
            WHERE user_id = ?
        """;

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, persona);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * ResultSet → JournalEntry dönüşümü
     */
    private JournalEntry mapJournal(ResultSet rs) throws SQLException {
        String userId = rs.getString("user_id");
        BaseUser author = userDao.findById(userId);

        if (author == null) {
            author = new StandardUser(userId, userId, "", UserRole.CLIENT);
        }

        JournalEntry entry = new JournalEntry(
                author,
                rs.getString("title"),
                rs.getString("content"),
                JournalMood.valueOf(rs.getString("mood"))
        );

        entry.setId(rs.getString("id"));

        Timestamp ts = rs.getTimestamp("created_at");

        if (ts != null) {
            entry.setCreatedAt(ts.toLocalDateTime());
        }

        return entry;
    }
}
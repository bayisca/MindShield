package com.mindshield.services;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.mindshield.dao.JournalDao;
import com.mindshield.dao.JournalDaoImpl;
import com.mindshield.exceptions.UnauthorizedException;
import com.mindshield.models.BaseUser;
import com.mindshield.models.JournalEntry;
import com.mindshield.models.JournalMood;
import com.mindshield.ui.UserRole;

/**
 * JourShield: Günlük girişleri yalnızca sahibinin görebileceği şekilde yönetir.
 * Danışanlar ve danışmanlar kendi günlüklerini tutabilir; admin başkasının günlüğüne erişemez.
 */
public class JournalService {
    private final JournalDao journalDao;

    public JournalService() {
        this.journalDao = new JournalDaoImpl();
    }

    public JournalService(JournalDao journalDao) {
        this.journalDao = journalDao;
    }

    private void enforcePrivateJournalUser(BaseUser user) {
        if (user == null) {
            throw new UnauthorizedException("Günlük için giriş yapmalısınız.");
        }
        UserRole role = user.getRole();
        if (role == UserRole.ADMIN) {
            throw new UnauthorizedException("Yöneticiler günlük tutamaz.");
        }
        if (role != UserRole.CLIENT && role != UserRole.ANONYMOUS && role != UserRole.COUNSELOR) {
            throw new UnauthorizedException("Bu özellik hesabınız için kullanılamıyor.");
        }
    }

    /** Hesap silindiğinde çağrılır — {@code journals.user_id} ile eşleşen kayıtlar silinir. */
    public void purgeEntriesForPersona(String userId) {
        journalDao.deleteEntriesByUserId(userId);
    }

    private void enforceOwner(BaseUser user, JournalEntry entry) {
        if (entry == null) {
            throw new UnauthorizedException("Günlük bulunamadı.");
        }
        if (!entry.isAuthor(user)) {
            throw new UnauthorizedException("Bu günlük girişine yalnızca yazarı erişebilir.");
        }
    }

    public JournalEntry createEntry(BaseUser author, String title, String body, JournalMood mood) {
        enforcePrivateJournalUser(author);
        if (title == null || title.trim().isEmpty() || body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Başlık ve içerik boş olamaz.");
        }
        JournalEntry entry = new JournalEntry(author, title.trim(), body.trim(), mood);
        journalDao.save(entry);
        return entry;
    }

    public List<JournalEntry> listMyEntriesForDate(BaseUser user, LocalDate date) {
        enforcePrivateJournalUser(user);
        if (date == null) {
            throw new IllegalArgumentException("Tarih seçilmelidir.");
        }
        return journalDao.findAll().stream()
                .filter(e -> e.isAuthor(user))
                .filter(e -> e.getCreatedAt().toLocalDate().equals(date))
                .sorted(Comparator.comparing(JournalEntry::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

// public List<JournalEntry> listAllMyEntries(BaseUser user) {

//     System.out.println("CURRENT USER: " + user.getId());

//     return journalDao.findAll().stream()
//             .peek(e -> System.out.println("DB USER: " + e.getAuthor().getId()))
//             .filter(e -> e.isAuthor(user))
//             .sorted(Comparator.comparing(JournalEntry::getCreatedAt).reversed())
//             .collect(Collectors.toList());
// }


    public List<JournalEntry> listAllMyEntries(BaseUser user) {
        enforcePrivateJournalUser(user);
        return journalDao.findAll().stream()
                .filter(e -> e.isAuthor(user))
                .sorted(Comparator.comparing(JournalEntry::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public JournalEntry findMineById(BaseUser user, String id) {
        enforcePrivateJournalUser(user);
        JournalEntry entry = journalDao.findById(id);
        enforceOwner(user, entry);
        return entry;
    }

    public JournalEntry updateEntry(BaseUser user, String id, String title, String body, JournalMood mood) {
        enforcePrivateJournalUser(user);
        if (title == null || title.trim().isEmpty() || body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Başlık ve içerik boş olamaz.");
        }
        JournalEntry entry = journalDao.findById(id);
        enforceOwner(user, entry);
        entry.setTitle(title.trim());
        entry.setBody(body.trim());
        entry.setMood(mood);
        journalDao.update(entry);
        return entry;
    }

    public void deleteEntry(BaseUser user, String id) {
        enforcePrivateJournalUser(user);
        JournalEntry entry = journalDao.findById(id);
        enforceOwner(user, entry);
        journalDao.deleteById(id);
    }
}

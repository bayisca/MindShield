package com.mindshield.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mindshield.dao.ModerationDao;
import com.mindshield.dao.ModerationDaoImpl;
import com.mindshield.models.BaseUser;
import com.mindshield.models.BlogPost;
import com.mindshield.models.ChatMessage;
import com.mindshield.models.ModerationReport;

/**
 * Şikayetleri veritabanında saklar; admin arayüzünden işlenir.
 */
public class ModerationService {
    private final ModerationDao moderationDao;

    public ModerationService() {
        this.moderationDao = new ModerationDaoImpl();
    }

    public synchronized void reportBlogPost(BaseUser reporter, BlogPost post, String reason) {
        if (reporter == null || post == null) {
            return;
        }
        String authorPersona = post.getAuthor() != null ? post.getAuthor().getPersona() : "";
        ModerationReport r = new ModerationReport(
                ModerationReport.Kind.BLOG_POST,
                reporter.getPersona(),
                authorPersona,
                post.getId(),
                null,
                null,
                truncate(post.getBody(), 200),
                reason);
        moderationDao.save(r);
    }

    public synchronized void reportRoomMessage(BaseUser reporter, ChatMessage message, String reason) {
        if (reporter == null || message == null || message.getSender() == null) {
            return;
        }
        ModerationReport r = new ModerationReport(
                ModerationReport.Kind.ROOM_MESSAGE,
                reporter.getPersona(),
                message.getSender().getPersona(),
                null,
                message.getRoomId(),
                message.getId(),
                truncate(message.getContent(), 200),
                reason);
        moderationDao.save(r);
    }

    public synchronized void reportDirectUser(BaseUser reporter, String reportedPersona, String reason) {
        if (reporter == null || reportedPersona == null || reportedPersona.isBlank()) {
            return;
        }
        ModerationReport r = new ModerationReport(
                ModerationReport.Kind.DIRECT_USER,
                reporter.getPersona(),
                reportedPersona.trim(),
                null,
                null,
                null,
                "",
                reason);
        moderationDao.save(r);
    }

    /** Yönetici panelinde yalnızca açık şikayetler listelenir. */
    public synchronized List<ModerationReport> listOpenReports() {
        return moderationDao.findAll().stream()
                .filter(r -> r != null && !r.isResolved())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public synchronized ModerationReport findById(String id) {
        return moderationDao.findById(id);
    }

    public synchronized void markResolved(String id) {
        ModerationReport r = findById(id);
        if (r != null) {
            r.setResolved(true);
            moderationDao.update(r);
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.replace('\n', ' ').trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }
}

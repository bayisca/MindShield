package com.mindshield.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.mindshield.models.BaseUser;
import com.mindshield.models.BlogPost;
import com.mindshield.models.ChatMessage;
import com.mindshield.models.ModerationReport;

/**
 * Şikayetleri saklar; admin arayüzünden işlenir.
 */
public class ModerationService {
    private static final String DATA_FILE = "moderation_reports.dat";

    private List<ModerationReport> reports;

    public ModerationService() {
        this.reports = load();
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
        reports.add(r);
        persist();
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
        reports.add(r);
        persist();
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
        reports.add(r);
        persist();
    }

    /** Yönetici panelinde yalnızca açık şikayetler listelenir. */
    public synchronized List<ModerationReport> listOpenReports() {
        return reports.stream()
                .filter(r -> r != null && !r.isResolved())
                .sorted(Comparator.comparing(ModerationReport::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public synchronized ModerationReport findById(String id) {
        if (id == null) {
            return null;
        }
        return reports.stream().filter(r -> id.equals(r.getId())).findFirst().orElse(null);
    }

    public synchronized void markResolved(String id) {
        ModerationReport r = findById(id);
        if (r != null) {
            r.setResolved(true);
            persist();
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.replace('\n', ' ').trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }

    private void persist() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(reports);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<ModerationReport> load() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (List<ModerationReport>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }
}

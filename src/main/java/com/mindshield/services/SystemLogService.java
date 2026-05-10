package com.mindshield.services;

import com.mindshield.dao.SystemLogDao;
import com.mindshield.dao.SystemLogDaoImpl;
import com.mindshield.models.BaseUser;
import com.mindshield.models.Report;
import com.mindshield.models.SystemLog;
import java.util.List;
import java.util.UUID;

public class SystemLogService {
    
    private final SystemLogDao systemLogDao;
    
    private static final SystemLogService INSTANCE = new SystemLogService();
    
    public static SystemLogService getInstance() {
        return INSTANCE;
    }
    
    private SystemLogService() {
        this.systemLogDao = new SystemLogDaoImpl();
    }
    
    public void logAction(String action) {
        SystemLog log = new SystemLog(UUID.randomUUID().toString(), action);
        systemLogDao.saveLog(log);
        System.out.println("[Sistem Logu] " + log.getAction());
    }
    
    public void reportContent(BaseUser reporter, String contentId, String reason) {
        if (reporter == null || reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rapor sebebi boş olamaz.");
        }
        Report report = new Report(UUID.randomUUID().toString(), reporter, contentId, reason);
        systemLogDao.saveReport(report);
        logAction("Yeni Rapor oluşturuldu. Rapor Eden: " + reporter.getPersona());
    }
    
    public List<SystemLog> getAllLogs() {
        return systemLogDao.findAllLogs();
    }
    
    public List<Report> getAllReports() {
        return systemLogDao.findAllReports();
    }
}

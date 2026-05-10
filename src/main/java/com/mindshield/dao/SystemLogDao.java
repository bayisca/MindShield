package com.mindshield.dao;

import com.mindshield.models.SystemLog;
import com.mindshield.models.Report;
import java.util.List;

public interface SystemLogDao {
    void saveLog(SystemLog log);
    List<SystemLog> findAllLogs();
    
    void saveReport(Report report);
    List<Report> findAllReports();
}

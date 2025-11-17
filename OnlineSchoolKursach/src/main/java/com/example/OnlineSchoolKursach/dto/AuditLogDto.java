package com.example.OnlineSchoolKursach.dto;

import java.time.LocalDateTime;

public class AuditLogDto {
    private Long auditId;
    private String tableName;
    private Long recordId;
    private String action;
    private Long userId;
    private LocalDateTime changedAt;
    private String oldValues;
    private String newValues;
    private String userName;

    public AuditLogDto() {}

    public Long getAuditId() { return auditId; }
    public void setAuditId(Long auditId) { this.auditId = auditId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getOldValues() { return oldValues; }
    public void setOldValues(String oldValues) { this.oldValues = oldValues; }

    public String getNewValues() { return newValues; }
    public void setNewValues(String newValues) { this.newValues = newValues; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}


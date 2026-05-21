package com.predix.bff.support;

import com.predix.bff.audit.AuditEventType;
import com.predix.bff.audit.AuditRecorder;

import java.util.ArrayList;
import java.util.List;

public class RecordingAuditRecorder implements AuditRecorder {

    public record AuditRecord(AuditEventType type, String wallet, String ip, String country,
                              String resource, String action, String outcome, String detail) {}

    private final List<AuditRecord> records = new ArrayList<>();

    @Override
    public void log(AuditEventType type, String wallet, String ip, String country,
                      String resource, String action, String outcome, String detail) {
        records.add(new AuditRecord(type, wallet, ip, country, resource, action, outcome, detail));
    }

    public List<AuditRecord> records() {
        return List.copyOf(records);
    }
}

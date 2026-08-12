package com.jobintel.common.events;

public final class EventTypes {

    public static final String JOB_CREATED = "job.created";
    public static final String JOB_UPDATED = "job.updated";
    public static final String JOB_DELETED = "job.deleted";
    public static final String EXPORT_REQUESTED = "export.requested";
    public static final String EXPORT_COMPLETED = "export.completed";
    public static final String EXPORT_FAILED = "export.failed";
    public static final String AUDIT_EVENT = "audit.events";
    public static final String NOTIFICATION_EVENT = "notification.events";

    private EventTypes() {}
}

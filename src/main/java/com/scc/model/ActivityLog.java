package com.scc.model;

import java.sql.Timestamp;

public record ActivityLog(
        int id,
        int userId,
        String actionType,
        String description,
        Timestamp timestamp
) {
    @Override
    public String toString() {
        return String.format("[%s] User ID: %d | Action: %s | Details: %s", timestamp, userId, actionType, description);
    }
}
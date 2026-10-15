package com.scc.model;

public record user(int user_id, String username, String password, String roles, String status) {

    @Override
    public String toString() {
        return String.format("(%d) %s (%s) - status: %s", user_id, username, roles, status);
    }
}

package com.example.studentattendance.models;

import java.io.Serializable;
import java.util.Objects;

public class Account implements Serializable {
    private int code;
    private String username;
    private String password;
    private String role; // "ADMIN", "TEACHER", "STUDENT"

    public Account() {
    }

    public Account(int code, String username, String password) {
        this(code, username, password, inferRole(code, username));
    }

    public Account(int code, String username, String password, String role) {
        this.code = code;
        this.username = username;
        this.password = password;
        this.role = normalizeRole(role, code, username);
    }

    public static String inferRole(int code, String username) {
        if (username != null && username.equalsIgnoreCase("admin")) {
            return "ADMIN";
        }
        if (code >= 100 && code < 200) {
            return "TEACHER";
        }
        if (username != null && username.toLowerCase().startsWith("student")) {
            return "STUDENT";
        }
        if (code >= 200) {
            return "STUDENT";
        }
        if (code > 0 && code < 100) {
            return "ADMIN";
        }
        return "STUDENT";
    }

    public static String normalizeRole(String role, int code, String username) {
        if (role == null || role.isBlank()) {
            return inferRole(code, username);
        }
        String upper = role.trim().toUpperCase();
        if (upper.contains("ADMIN")) return "ADMIN";
        if (upper.contains("TEACH") || upper.contains("DOCTOR") || upper.contains("PROF")) return "TEACHER";
        return "STUDENT";
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        if (role == null || role.isBlank()) {
            role = inferRole(code, username);
        }
        return role;
    }

    public void setRole(String role) {
        this.role = normalizeRole(role, code, username);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getRole());
    }

    public boolean isTeacher() {
        return "TEACHER".equalsIgnoreCase(getRole());
    }

    public boolean isStudent() {
        return "STUDENT".equalsIgnoreCase(getRole());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return code == account.code &&
                Objects.equals(username, account.username) &&
                Objects.equals(password, account.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, username, password);
    }

    @Override
    public String toString() {
        return code + "," + username + "," + getRole();
    }
}

package org.example.models;

public class MoveLogEntry {
    private final String time;
    private final String notation; // חייב להיות String

    public MoveLogEntry(String time, String notation) { // תקן את הטיפוס ל-String
        this.time = time;
        this.notation = notation;
    }

    public String getTime() { return time; }
    public String getNotation() { return notation; }
}
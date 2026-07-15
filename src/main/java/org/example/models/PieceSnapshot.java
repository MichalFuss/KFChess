package org.example.models;

public class PieceSnapshot {
    private final String id;
    private final Piece.Kind kind;
    private final Piece.Color color;
    private final Piece.State state;
    private final double x; // מיקום X מדויק בפיקסלים על המסך
    private final double y; // מיקום Y מדויק בפיקסלים על המסך

    public PieceSnapshot(String id, Piece.Kind kind, Piece.Color color, Piece.State state, double x, double y) {
        this.id = id;
        this.kind = kind;
        this.color = color;
        this.state = state;
        this.x = x;
        this.y = y;
    }

    // Getters לקריאת הנתונים בלבד
    public String getId() { return id; }
    public Piece.Kind getKind() { return kind; }
    public Piece.Color getColor() { return color; }
    public Piece.State getState() { return state; }
    public double getX() { return x; }
    public double getY() { return y; }
}
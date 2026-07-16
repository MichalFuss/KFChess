package org.example.models;

public class Piece {

    public enum Color {
        WHITE('w'),
        BLACK('b');

        private final char symbol;

        Color(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol() {
            return symbol;
        }
    }

    public enum Kind {
        KING('K'),
        QUEEN('Q'),
        ROOK('R'),
        KNIGHT('N'),
        BISHOP('B'),
        PAWN('P');

        private final char symbol;

        Kind(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol() {
            return symbol;
        }
    }

    // מצב הכלי - יעזור לנו מאוד בהחלפת הלוגיקה של MovementManager
    public enum State { IDLE, MOVING, JUMPING, CAPTURED, COOLDOWN }

    private final String id; // זיהוי ייחודי, למשל: "wK_1" או UUID
    private final Color color;
    private final Kind kind;
    private Position cell; // המיקום הנוכחי
    private State state; // מה הכלי עושה כרגע

    public Piece(String id, Color color, Kind kind, Position cell) {
        this.id = id;
        this.color = color;
        this.kind = kind;
        this.cell = cell;
        this.state = State.IDLE;
    }

    // Getters & Setters
    public String getId() { return id; }
    public Color getColor() { return color; }
    public Kind getKind() { return kind; }

    public Position getCell() { return cell; }
    public void setCell(Position cell) { this.cell = cell; }

    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    // בתוך Piece.java
    private long cooldownEndTime = 0;

    public void setCooldown(long durationMillis, long currentTime) {
        this.state = State.COOLDOWN;
        this.cooldownEndTime = currentTime + durationMillis;
    }

    public long getCooldownEndTime() { return cooldownEndTime; }

    @Override
    public String toString() {
        // This will now work correctly!
        return "" + color.getSymbol() + kind.getSymbol();
    }
}
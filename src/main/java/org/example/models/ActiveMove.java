package org.example.models;

public class ActiveMove {
    private final Position from;
    private final Position to;
    private final Piece piece;
    private final long arrivalTimeMillis;
    private final boolean isJump;

    public ActiveMove(Position from, Position to, Piece piece, long arrivalTimeMillis, boolean isJump) {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.arrivalTimeMillis = arrivalTimeMillis;
        this.isJump = isJump;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Piece getPiece() {
        return piece;
    }

    public long getArrivalTimeMillis() {
        return arrivalTimeMillis;
    }

    public boolean isJump() {
        return isJump;
    }

    /**
     * בודק האם זמן המשחק הנוכחי הגיע או עבר את זמן הנחיתה המיועד של הכלי.
     */
    public boolean isComplete(long currentGameTimeMillis) {
        return currentGameTimeMillis >= this.arrivalTimeMillis;
    }
}
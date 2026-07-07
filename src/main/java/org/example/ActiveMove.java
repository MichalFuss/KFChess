// ActiveMove.java
package org.example;

public class ActiveMove {
    private final Position from;
    private final Position to;
    private final Piece piece;
    private final long arrivalTimeMillis;

    public ActiveMove(Position from, Position to, Piece piece, long arrivalTimeMillis) {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.arrivalTimeMillis = arrivalTimeMillis;
    }

    public Position getFrom() { return from; }
    public Position getTo() { return to; }
    public Piece getPiece() { return piece; }
    public long getArrivalTimeMillis() { return arrivalTimeMillis; }

    // בודק האם זמן ההגעה הגיע או עבר
    public boolean isComplete(long currentTimeMillis) {
        return currentTimeMillis >= arrivalTimeMillis;
    }
}
package org.example.events;

import org.example.models.Piece;

public class GameStatusEvent implements GameEvent {
    public enum Status { STARTED, OVER }
    private final Status status;
    private Piece.Color winnerColor;

    public GameStatusEvent(Status status) {
        this.status = status;
    }

    public GameStatusEvent(Status status, Piece.Color winnerColor) {
        this.status = status;
        this.winnerColor = winnerColor;
    }

    public Status getStatus() { return status; }
    public Piece.Color getWinnerColor() { return winnerColor; }
}
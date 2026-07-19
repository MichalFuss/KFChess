package org.example.events;

import org.example.models.MoveLogEntry;
import org.example.models.Piece;

public class MoveLoggedEvent implements GameEvent {
    private final Piece.Color playerColor;
    private final MoveLogEntry moveEntry;

    public MoveLoggedEvent(Piece.Color playerColor, MoveLogEntry moveEntry) {
        this.playerColor = playerColor;
        this.moveEntry = moveEntry;
    }

    public Piece.Color getPlayerColor() { return playerColor; }
    public MoveLogEntry getMoveEntry() { return moveEntry; }
}
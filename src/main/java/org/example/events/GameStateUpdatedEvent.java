package org.example.events;

import org.example.models.GameSnapshot;

public class GameStateUpdatedEvent implements GameEvent {
    private final GameSnapshot snapshot;

    public GameStateUpdatedEvent(GameSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public GameSnapshot getSnapshot() {
        return snapshot;
    }
}
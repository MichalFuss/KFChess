package org.example.events;

public class GameStatusEvent implements GameEvent {
    public enum Status { STARTED, OVER }
    private final Status status;

    public GameStatusEvent(Status status) {
        this.status = status;
    }

    public Status getStatus() { return status; }
}
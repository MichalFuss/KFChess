package org.example.events;

public class NamesUpdatedEvent implements GameEvent {
    private final String whiteName;
    private final String blackName;

    public NamesUpdatedEvent(String whiteName, String blackName) {
        this.whiteName = whiteName;
        this.blackName = blackName;
    }

    public String getWhiteName() { return whiteName; }
    public String getBlackName() { return blackName; }
}
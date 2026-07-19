package org.example.events;

public class PlaySoundEvent implements GameEvent {
    private final String soundType; // למשל: "CAPTURE", "MOVE", "START"

    public PlaySoundEvent(String soundType) {
        this.soundType = soundType;
    }

    public String getSoundType() { return soundType; }
}
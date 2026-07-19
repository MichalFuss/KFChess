package org.example.events;

public class ScoreUpdatedEvent implements GameEvent {
    private final int whiteScore;
    private final int blackScore;

    public ScoreUpdatedEvent(int whiteScore, int blackScore) {
        this.whiteScore = whiteScore;
        this.blackScore = blackScore;
    }

    public int getWhiteScore() { return whiteScore; }
    public int getBlackScore() { return blackScore; }
}
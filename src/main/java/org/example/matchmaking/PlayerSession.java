package org.example.matchmaking;

public class PlayerSession {
    public String username;
    public int elo;
    public long searchStartTime;

    public PlayerSession(String username, int elo) {
        this.username = username;
        this.elo = elo;
        this.searchStartTime = System.currentTimeMillis();
    }
}
package org.example.server;

import org.example.engine.GameEngine;
import org.example.matchmaking.PlayerSession;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameRoom {

    public enum Role {
        WHITE,
        BLACK,
        VIEWER
    }

    private final String roomId;
    private final GameEngine gameEngine;
    private PlayerSession whitePlayer;
    private PlayerSession blackPlayer;
    private final List<PlayerSession> viewers = new CopyOnWriteArrayList<>();
    private boolean isGameOverHandled = false;
    

    public GameRoom(String roomId, GameEngine gameEngine) {
        this.roomId = roomId;
        this.gameEngine = gameEngine;
    }

    public synchronized Role addPlayer(PlayerSession session) {
        if (whitePlayer == null) {
            whitePlayer = session;
            return Role.WHITE;
        } else if (blackPlayer == null) {
            blackPlayer = session;
            return Role.BLACK;
        } else {
            viewers.add(session);
            return Role.VIEWER;
        }
    }

    public boolean isGameOverHandled() {
        return isGameOverHandled;
    }

    public void setGameOverHandled(boolean gameOverHandled) {
        isGameOverHandled = gameOverHandled;
    }

    public String getRoomId() {
        return roomId;
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }

    public PlayerSession getWhitePlayer() {
        return whitePlayer;
    }

    public PlayerSession getBlackPlayer() {
        return blackPlayer;
    }

    public List<PlayerSession> getViewers() {
        return viewers;
    }
}
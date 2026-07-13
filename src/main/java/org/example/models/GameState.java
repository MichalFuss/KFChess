package org.example.models;
import java.util.ArrayList;
import java.util.List;

public class GameState {
    private final Board board;
    private long gameTimeMillis;
    private boolean isGameOver;
    private Position selectedPosition;
    private final List<ActiveMove> activeMoves; // מחליף את מה ששמרת ב-MovementManager

    public GameState(Board board) {
        this.board = board;
        this.gameTimeMillis = 0;
        this.isGameOver = false;
        this.selectedPosition = null;
        this.activeMoves = new ArrayList<>();
    }

    // Getters and Setters עבור הסטייט
    public Board getBoard() { return board; }

    public long getGameTimeMillis() { return gameTimeMillis; }
    public void addTime(long millis) { this.gameTimeMillis += millis; }

    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean gameOver) { isGameOver = gameOver; }

    public Position getSelectedPosition() { return selectedPosition; }
    public void setSelectedPosition(Position selectedPosition) { this.selectedPosition = selectedPosition; }

    public List<ActiveMove> getActiveMoves() { return activeMoves; }
    public void addActiveMove(ActiveMove move) { this.activeMoves.add(move); }
    public void removeActiveMove(ActiveMove move) { this.activeMoves.remove(move); }
}
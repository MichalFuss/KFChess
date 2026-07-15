package org.example.models;

import java.util.List;

public class GameSnapshot {
    private final int boardWidth;
    private final int boardHeight;
    private final List<PieceSnapshot> pieces;
    private final Position selectedPosition; // המשבצת שנבחרה (אם יש, עבור הסימון הצהוב)
    private final boolean isGameOver;
    private final long gameTimeMillis; // הזמן הנוכחי של המשחק

    public GameSnapshot(int boardWidth, int boardHeight, List<PieceSnapshot> pieces,
                        Position selectedPosition, boolean isGameOver, long gameTimeMillis) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.pieces = pieces;
        this.selectedPosition = selectedPosition;
        this.isGameOver = isGameOver;
        this.gameTimeMillis = gameTimeMillis;
    }

    // Getters לקריאת הנתונים בלבד
    public int getBoardWidth() { return boardWidth; }
    public int getBoardHeight() { return boardHeight; }
    public List<PieceSnapshot> getPieces() { return pieces; }
    public Position getSelectedPosition() { return selectedPosition; }
    public boolean isGameOver() { return isGameOver; }
    public long getGameTimeMillis() { return gameTimeMillis; }
}
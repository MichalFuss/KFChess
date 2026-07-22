package org.example.models;

import java.util.ArrayList;
import java.util.List;

public class GameSnapshot {
    private final int boardWidth;
    private final int boardHeight;
    private final List<PieceSnapshot> pieces;
    private final Position selectedPosition; // המשבצת שנבחרה (אם יש, עבור הסימון הצהוב)
    private final boolean isGameOver;
    private final long gameTimeMillis; // הזמן הנוכחי של המשחק
    private final int whiteScore; // תוספת: ניקוד לבן
    private final int blackScore; // תוספת: ניקוד שחור
    public List<MoveLogEntry> whiteMoveHistory;
    public List<MoveLogEntry> blackMoveHistory;

    public GameSnapshot(int boardWidth, int boardHeight, List<PieceSnapshot> pieces,
                        Position selectedPosition, boolean isGameOver, long gameTimeMillis) {
        this(boardWidth, boardHeight, pieces, selectedPosition, isGameOver, gameTimeMillis, 0, 0, new ArrayList<>(), new ArrayList<>());
    }

    // קונסטרוקטור חדש שכולל ניקוד
    public GameSnapshot(int boardWidth, int boardHeight, List<PieceSnapshot> pieces,
                        Position selectedPosition, boolean isGameOver, long gameTimeMillis,
                        int whiteScore, int blackScore) {
        this(boardWidth, boardHeight, pieces, selectedPosition, isGameOver, gameTimeMillis, whiteScore, blackScore, new ArrayList<>(), new ArrayList<>());
    }
    
    // קונסטרוקטור מלא עם היסטוריית מהלכים
    public GameSnapshot(int boardWidth, int boardHeight, List<PieceSnapshot> pieces,
                        Position selectedPosition, boolean isGameOver, long gameTimeMillis,
                        int whiteScore, int blackScore, List<MoveLogEntry> whiteMoveHistory, List<MoveLogEntry> blackMoveHistory) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.pieces = pieces;
        this.selectedPosition = selectedPosition;
        this.isGameOver = isGameOver;
        this.gameTimeMillis = gameTimeMillis;
        this.whiteScore = whiteScore;
        this.blackScore = blackScore;
        this.whiteMoveHistory = whiteMoveHistory != null ? whiteMoveHistory : new ArrayList<>();
        this.blackMoveHistory = blackMoveHistory != null ? blackMoveHistory : new ArrayList<>();
    }

    // Getters לקריאת הנתונים בלבד
    public int getBoardWidth() { return boardWidth; }
    public int getBoardHeight() { return boardHeight; }
    public List<PieceSnapshot> getPieces() { return pieces; }
    public Position getSelectedPosition() { return selectedPosition; }
    public boolean isGameOver() { return isGameOver; }
    public long getGameTimeMillis() { return gameTimeMillis; }
    public int getWhiteScore() { return whiteScore; }
    public int getBlackScore() { return blackScore; }
    
    // תוסף לשידור ידטים סדרים
    public List<MoveLogEntry> getWhiteMoveHistory() { 
        return whiteMoveHistory; 
    }
    public List<MoveLogEntry> getBlackMoveHistory() { 
        return blackMoveHistory; 
    }

    public boolean hasPieceAtPosition(Position pos, Piece.Color color) {
        if (pieces == null) return false;
        double targetX = pos.getCol() * 100.0;
        double targetY = pos.getRow() * 100.0;

        for (PieceSnapshot p : pieces) {
            if (p.getColor() == color) {
                if (Math.abs(p.getX() - targetX) < 50.0 && Math.abs(p.getY() - targetY) < 50.0) {
                    return true;
                }
            }
        }
        return false;
    }
}
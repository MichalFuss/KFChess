package org.example.models;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameState {
    private final Board board;
    private long gameTimeMillis;
    private boolean isGameOver;
    private Position selectedPosition;
    private final List<ActiveMove> activeMoves;
    private int whiteScore = 0;
    private int blackScore = 0;
    private final List<MoveLogEntry> whiteMoves = new java.util.ArrayList<>();
    private final List<MoveLogEntry> blackMoves = new java.util.ArrayList<>();
    private  String whitePlayerName ;
    private  String blackPlayerName ;

    public GameState(Board board) {
        this.board = board;
        this.gameTimeMillis = 0;
        this.isGameOver = false;
        this.selectedPosition = null;
        this.activeMoves = new CopyOnWriteArrayList<>();
    }


    // --- גטרים וסטרים לעדכון הנתונים ---
    public int getWhiteScore() { return whiteScore; }
    public int getBlackScore() { return blackScore; }
    public List<MoveLogEntry> getWhiteMoves() { return whiteMoves; }
    public List<MoveLogEntry> getBlackMoves() { return blackMoves; }
    public String getWhitePlayerName() { return whitePlayerName; }
    public String getBlackPlayerName() { return blackPlayerName; }
    private Piece.Color winnerColor;

    public Piece.Color getWinnerColor() {
        return winnerColor;
    }

    public void setWinnerColor(Piece.Color winnerColor) {
        this.winnerColor = winnerColor;
    }
    public void setPlayerNames(String white, String black) {
        if (white != null && !white.trim().isEmpty()) {
            this.whitePlayerName = white;
        }
        if (black != null && !black.trim().isEmpty()) {
            this.blackPlayerName = black;
        }
    }

    public void setWhitePlayerName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.whitePlayerName = name;
        }
    }

    public void setBlackPlayerName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.blackPlayerName = name;
        }
    }

    public void addWhiteScore(int points) { this.whiteScore += points; }
    public void addBlackScore(int points) { this.blackScore += points; }

    public void addWhiteMove(MoveLogEntry entry) { this.whiteMoves.add(entry); }
    public void addBlackMove(MoveLogEntry entry) { this.blackMoves.add(entry); }



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
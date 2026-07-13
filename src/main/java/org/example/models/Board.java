package org.example.models;

public class Board {
    private final int width;
    private final int height;
    private final Piece[][] grid; // Dynamic 2D array to hold the board state

    // Initializes the board dynamically based on inferred dimensions
    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Piece[height][width]; // Rows come first (height), then columns (width)
    }

    // Places a piece at a specific coordinate on the grid
    public void setPiece(Position pos, Piece piece) {
        grid[pos.getRow()][pos.getCol()] = piece;
        if (piece != null) {
            piece.setCell(pos); // עדכון דו-כיווני: גם הכלי יודע איפה הוא
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // Safely checks if a given row and column are within the board boundaries
    public boolean isWithinBounds(Position pos) {
        return pos.getRow() >= 0 && pos.getRow() < height && pos.getCol() >= 0 && pos.getCol() < width;
    }
    // Retrieves a piece at a specific position
    public Piece getPiece(Position pos) {
        if (!isWithinBounds(pos)) {
            return null;
        }
        return grid[pos.getRow()][pos.getCol()];
    }



}
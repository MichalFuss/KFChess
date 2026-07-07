package org.example;

public class GameController {
    private final Board board;
    private Position selectedPosition;
    private long gameTimeMillis;

    public GameController(Board board) {
        this.board = board;
        this.selectedPosition = null; // No piece selected at start
        this.gameTimeMillis = 0;      // Game clock starts at 0
    }

    // Process a click event at pixel coordinates (x, y)
    public void handleClick(int x, int y) {
        // Convert pixel coordinates to board indices
        int row = y / Board.CELL_SIZE;
        int col = x / Board.CELL_SIZE;

        Position clickedPos = new Position(row, col);

        // Ignore clicks that are outside the board boundaries
        if (!board.isWithinBounds(clickedPos)) {
            return;
        }

        Piece clickedPiece = board.getPiece(clickedPos);

        // Case 1: No piece is currently selected
        if (selectedPosition == null) {
            if (clickedPiece != null) {
                selectedPosition = clickedPos; // Select the piece
            }
            return;
        }

        // Case 2: A piece is already selected
        Piece selectedPiece = board.getPiece(selectedPosition);

        if (clickedPiece != null && clickedPiece.getColor() == selectedPiece.getColor()) {
            // Clicked another friendly piece -> change selection
            selectedPosition = clickedPos;
        } else {
            // Clicked an empty square or an enemy piece -> validate and move request
            if (isValidMove(selectedPosition, clickedPos, selectedPiece)) {
                board.movePiece(selectedPosition, clickedPos);
            }
            selectedPosition = null; // Clear selection after a move attempt (legal or illegal)
        }
    }

    // Comprehensive helper to validate if a move complies with all chess mechanics for Iteration 3
    private boolean isValidMove(Position from, Position to, Piece piece) {
        int deltaRow = to.getRow() - from.getRow();
        int deltaCol = to.getCol() - from.getCol();

        // 1. Check if the piece's pattern allows this jump geometry
        if (!piece.getType().isValidMoveShape(deltaRow, deltaCol)) {
            return false;
        }

        // 2. Except for Knights, verify that the path to the target square is clear of obstructions
        if (piece.getType() != Piece.Type.KNIGHT) {
            if (!board.isPathClear(from, to)) {
                return false;
            }
        }

        return true;
    }

    // Advance the game clock
    public void advanceTime(long millis) {
        if (millis > 0) {
            this.gameTimeMillis += millis;
        }
    }

    // Standard print delegate
    public void printBoard() {
        board.print();
    }
}

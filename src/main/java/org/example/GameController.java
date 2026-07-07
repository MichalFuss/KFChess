package org.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameController {
    private final Board board;
    private Position selectedPosition;
    private long gameTimeMillis;

    // List to manage all pieces currently moving in real-time
    private final List<ActiveMove> activeMoves;

    // Constant for Iteration 6: every move takes exactly 1000ms
    private static final long MOVE_DURATION_PER_SQUARE = 1000;

    public GameController(Board board) {
        this.board = board;
        this.selectedPosition = null; // No piece selected at start
        this.gameTimeMillis = 0;      // Game clock starts at 0
        this.activeMoves = new ArrayList<>();
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
                // Only select the piece if it is not already mid-move
                if (!isPieceMovingFrom(clickedPos)) {
                    selectedPosition = clickedPos; // Select the piece
                }
            }
            return;
        }

        // Case 2: A piece is already selected
        Piece selectedPiece = board.getPiece(selectedPosition);

        if (clickedPiece != null && clickedPiece.getColor() == selectedPiece.getColor()) {
            // Clicked another friendly piece -> change selection (if it's not moving)
            if (!isPieceMovingFrom(clickedPos)) {
                selectedPosition = clickedPos;
            }
        } else {
            // Clicked an empty square or an enemy piece -> validate and move request
            if (isValidMove(selectedPosition, clickedPos, selectedPiece)) {
                // 1. Calculate how many squares the piece is traveling
                int distance = calculateDistance(selectedPosition, clickedPos);

                // 2. Calculate dynamic travel time (1000ms per square)
                long totalTravelTime = distance * MOVE_DURATION_PER_SQUARE;
                long arrivalTime = this.gameTimeMillis + totalTravelTime;

                // Register the active move instead of moving instantly
                activeMoves.add(new ActiveMove(selectedPosition, clickedPos, selectedPiece, arrivalTime));
            }
            selectedPosition = null; // Clear selection after a move attempt
        }
    }

    // Helper to calculate the move distance in squares (Chebyshev distance)
    private int calculateDistance(Position from, Position to) {
        int deltaRow = Math.abs(to.getRow() - from.getRow());
        int deltaCol = Math.abs(to.getCol() - from.getCol());
        return Math.max(deltaRow, deltaCol);
    }

    // Helper to check if a piece on a specific position is already in transit
    private boolean isPieceMovingFrom(Position pos) {
        for (ActiveMove move : activeMoves) {
            if (move.getFrom().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    // Comprehensive helper to validate if a move complies with all chess mechanics for Iteration 3
    private boolean isValidMove(Position from, Position to, Piece piece) {

        // Route pawn movement validation to the specific class
        if (piece.getType() == Piece.Type.PAWN) {
            return PawnMoveValidator.isValidPawnMove(from, to, piece, board);
        }

        // Existing logic for all other pieces
        int deltaRow = to.getRow() - from.getRow();
        int deltaCol = to.getCol() - from.getCol();

        // 1. Check if the piece's pattern allows this jump geometry
        if (!piece.getType().isValidMoveShape(deltaRow, deltaCol)) {
            return false;
        }

        // 2. Validate destination square: cannot capture a friendly piece
        Piece targetPiece = board.getPiece(to);
        if (targetPiece != null && targetPiece.getColor() == piece.getColor()) {
            return false;
        }

        // 3. Validate path clearance for sliding pieces (Knight jumps over blockers)
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

        // Use Iterator to safely remove items from the list while iterating
        Iterator<ActiveMove> iterator = activeMoves.iterator();
        while (iterator.hasNext()) {
            ActiveMove move = iterator.next();

            // Check if the game clock has reached or passed the arrival time
            if (move.isComplete(this.gameTimeMillis)) {
                // Execute the physical move on the board grid
                board.movePiece(move.getFrom(), move.getTo());
                iterator.remove(); // Move is finished, remove from active list
            }
        }
    }

    // Standard print delegate
    public void printBoard() {
        board.print();
    }
}

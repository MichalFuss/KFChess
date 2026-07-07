package org.example;

public class PawnMoveValidator {

    // Validates pawn moves based on color, direction, and target square status
    public static boolean isValidPawnMove(Position from, Position to, Piece pawn, Board board) {

        int deltaRow = to.getRow() - from.getRow();
        int deltaCol = to.getCol() - from.getCol();
        int absCol = Math.abs(deltaCol);

        // Determine direction based on color: White moves up (-1), Black moves down (+1)
        int requiredDeltaRow = (pawn.getColor() == Piece.Color.WHITE) ? -1 : 1;

        // Pawns can only move exactly 1 row forward in this iteration
        if (deltaRow != requiredDeltaRow) {
            return false;
        }

        Piece targetPiece = board.getPiece(to);

        // Case 1: Forward move (no column change) -> must land on an empty square
        if (deltaCol == 0) {
            return targetPiece == null;
        }

        // Case 2: Diagonal capture (1 column away) -> must land on an enemy piece
        if (absCol == 1) {
            return targetPiece != null && targetPiece.getColor() != pawn.getColor();
        }

        // Any other move (e.g., moving 2+ columns sideways) is invalid
        return false;
    }
}
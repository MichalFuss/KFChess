package org.example.rules;

import org.example.models.*;

import java.util.List;

public class QueenRule implements PieceRule {
    @Override
    public boolean isValidMove(Position from, Position to, Piece piece, Board board, List<ActiveMove> activeMoves) {
        boolean isStraight = from.getRow() == to.getRow() || from.getCol() == to.getCol();
        boolean isDiagonal = Math.abs(to.getRow() - from.getRow()) == Math.abs(to.getCol() - from.getCol());

        if (!isStraight && !isDiagonal) return false;

        if (RuleHelper.isDestinationBlockedByFriendly(to, piece.getColor(), board, activeMoves)) return false;
        return RuleHelper.isPathClear(from, to, board, activeMoves);
    }
}
package org.example.rules;

import org.example.models.*;

import java.util.List;

public class KingRule implements PieceRule {
    @Override
    public boolean isValidMove(Position from, Position to, Piece piece, Board board, List<ActiveMove> activeMoves) {
        int deltaRow = Math.abs(to.getRow() - from.getRow());
        int deltaCol = Math.abs(to.getCol() - from.getCol());

        // מקסימום משבצת אחת לכל כיוון
        if (deltaRow > 1 || deltaCol > 1) return false;

        return !RuleHelper.isDestinationBlockedByFriendly(to, piece.getColor(), board, activeMoves);
    }
}
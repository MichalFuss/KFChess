package org.example.rules;

import org.example.models.*;

import java.util.List;

public class BishopRule implements PieceRule {
    @Override
    public boolean isValidMove(Position from, Position to, Piece piece, Board board, List<ActiveMove> activeMoves) {
        // תנועה אלכסונית (הפרש השורות שווה להפרש העמודות)
        if (Math.abs(to.getRow() - from.getRow()) != Math.abs(to.getCol() - from.getCol())) return false;

        if (RuleHelper.isDestinationBlockedByFriendly(to, piece.getColor(), board, activeMoves)) return false;
        return RuleHelper.isPathClear(from, to, board, activeMoves);
    }
}
package org.example.rules;

import org.example.models.*;

import java.util.List;

public class BishopRule implements PieceRule {
    @Override
    public MoveValidationResult isValidMove(Position from, Position to, Piece piece, Board board, List<ActiveMove> activeMoves) {
        // תנועה אלכסונית (הפרש השורות שווה להפרש העמודות)
        if (Math.abs(to.getRow() - from.getRow()) != Math.abs(to.getCol() - from.getCol())) {
            return MoveValidationResult.INVALID_MOVE_PATTERN;
        }

        if (RuleHelper.isDestinationBlockedByFriendly(to, piece.getColor(), board, activeMoves)) {
            return MoveValidationResult.BLOCKED_BY_FRIENDLY;
        }
        
        if (!RuleHelper.isPathClear(from, to, board, activeMoves)) {
            return MoveValidationResult.PATH_BLOCKED;
        }
        
        return MoveValidationResult.VALID;
    }
}
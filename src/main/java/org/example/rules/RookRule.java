package org.example.rules;

import org.example.models.*;

import java.util.List;

public class RookRule implements PieceRule {
    @Override
    public MoveValidationResult isValidMove(Position from, Position to, Piece piece, Board board, List<ActiveMove> activeMoves) {
        // תנועה ישרה בלבד (אותה שורה או אותה עמודה)
        if (from.getRow() != to.getRow() && from.getCol() != to.getCol()) {
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
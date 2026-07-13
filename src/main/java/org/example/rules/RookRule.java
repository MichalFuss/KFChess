package org.example.rules;

import org.example.models.*;

import java.util.List;

public class RookRule implements PieceRule {
    @Override
    public boolean isValidMove(Position from, Position to, Piece piece, Board board, List<ActiveMove> activeMoves) {
        // תנועה ישרה בלבד (אותה שורה או אותה עמודה)
        if (from.getRow() != to.getRow() && from.getCol() != to.getCol()) return false;

        if (RuleHelper.isDestinationBlockedByFriendly(to, piece.getColor(), board, activeMoves)) return false;
        return RuleHelper.isPathClear(from, to, board, activeMoves);
    }
}
package org.example.rules;

import org.example.models.*;

import java.util.List;

public class KnightRule implements PieceRule {
    @Override
    public boolean isValidMove(Position from, Position to, Piece piece, Board board, List<ActiveMove> activeMoves) {
        int deltaRow = Math.abs(to.getRow() - from.getRow());
        int deltaCol = Math.abs(to.getCol() - from.getCol());

        // תנועת L: 2 ו-1 או 1 ו-2
        if (!((deltaRow == 2 && deltaCol == 1) || (deltaRow == 1 && deltaCol == 2))) return false;

        // פרש לא צריך בדיקת נתיב (הוא קופץ), רק בדיקת יעד
        return !RuleHelper.isDestinationBlockedByFriendly(to, piece.getColor(), board, activeMoves);
    }
}

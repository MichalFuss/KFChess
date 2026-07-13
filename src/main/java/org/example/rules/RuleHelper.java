package org.example.rules;

import org.example.models.ActiveMove;
import org.example.models.Board;
import org.example.models.Piece;
import org.example.models.Position;


import java.util.List;

public class RuleHelper {

    /**
     * בודק האם משבצת היעד חסומה על ידי כלי ידידותי (סטטי או מהלך שבדרך לשם).
     */
    public static boolean isDestinationBlockedByFriendly(Position to, Piece.Color friendlyColor, Board board, List<ActiveMove> activeMoves) {
        // 1. בדיקה סטטית: האם יש כלי ידידותי פיזית על הלוח במשבצת היעד
        Piece targetPiece = board.getPiece(to);
        if (targetPiece != null && targetPiece.getColor() == friendlyColor) {
            return true;
        }

        // 2. בדיקה דינמית: האם יש מהלך פעיל של כלי ידידותי שבדרך לנחות שם
        for (ActiveMove move : activeMoves) {
            if (move.getTo().equals(to) && move.getPiece().getColor() == friendlyColor && !move.isJump()) {
                return true;
            }
        }
        return false;
    }

    /**
     * בודק האם הנתיב בין נקודת ההתחלה ליעד נקי מחסימות (לא כולל נקודת היעד עצמה).
     */
    public static boolean isPathClear(Position from, Position to, Board board, List<ActiveMove> activeMoves) {
        int stepRow = Integer.compare(to.getRow() - from.getRow(), 0);
        int stepCol = Integer.compare(to.getCol() - from.getCol(), 0);

        int currRow = from.getRow() + stepRow;
        int currCol = from.getCol() + stepCol;

        // סריקת כל המשבצות לאורך המסלול (ללא משבצת היעד הסופית)
        while (currRow != to.getRow() || currCol != to.getCol()) {
            Position currentPos = new Position(currRow, currCol);

            // חסימה סטטית (כלי עומד)
            if (board.getPiece(currentPos) != null) return false;

            // חסימה דינמית (כלי בדרך לנחות שם)
            for (ActiveMove move : activeMoves) {
                if (move.getTo().equals(currentPos) && !move.isJump()) {
                    return false;
                }
            }

            currRow += stepRow;
            currCol += stepCol;
        }
        return true;
    }
}
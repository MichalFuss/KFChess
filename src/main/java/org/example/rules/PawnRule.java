package org.example.rules;

import org.example.models.*;
import java.util.List;

public class PawnRule implements PieceRule {
    @Override
    public boolean isValidMove(Position from, Position to, Piece piece, Board board, List<ActiveMove> activeMoves) {
        int deltaRow = to.getRow() - from.getRow();
        int deltaCol = to.getCol() - from.getCol();
        int absCol = Math.abs(deltaCol);

        // כיוון תנועה: לבן עולה למעלה (-1), שחור יורד למטה (+1)
        int direction = (piece.getColor() == Piece.Color.WHITE) ? -1 : 1;
        Piece targetPiece = board.getPiece(to);

        // מקרה 1: תנועה ישר קדימה (ללא שינוי עמודה)
        if (deltaCol == 0) {
            // היעד חייב להיות ריק לחלוטין (סטטית ודינמית מטעם כל קבוצה)
            if (targetPiece != null || isSquareOccupiedByAnyActiveMove(to, activeMoves)) return false;

            // צעד אחד קדימה
            if (deltaRow == direction) return true;

            // צעד כפול קדימה
            if (deltaRow == 2 * direction) {
                boolean isStartingRow = false;
                int r = from.getRow();

                // --- התיקון הארכיטקטורי הדינמי ---
                if (piece.getColor() == Piece.Color.WHITE) {
                    // לבן תמיד מתחיל בשורה השנייה מלמטה
                    isStartingRow = (r == board.getHeight() - 2);
                } else {
                    // שחור תמיד מתחיל בשורה השנייה מלמעלה
                    isStartingRow = (r == 1);
                }

                if (isStartingRow) {
                    // בדיקה שהמשבצת באמצע המסלול ריקה לחלוטין (סטטית ודינמית)
                    Position middlePos = new Position(from.getRow() + direction, from.getCol());
                    return board.getPiece(middlePos) == null && !isSquareOccupiedByAnyActiveMove(middlePos, activeMoves);
                }
            }
            return false;
        }

        // מקרה 2: תפיסה באלכסון (בדיוק עמודה אחת הצידה וצעד אחד קדימה)
        if (absCol == 1 && deltaRow == direction) {
            // תפיסה סטטית של אויב על הלוח
            if (targetPiece != null && targetPiece.getColor() != piece.getColor()) {
                return true;
            }
            // תפיסה דינמית: האם יש מהלך פעיל של אויב שנוחת שם כרגע באוויר
            return isSquareOccupiedByEnemyActiveMove(to, piece.getColor(), activeMoves);
        }

        return false;
    }

    // פונקציות עזר פנימיות לבדיקת מהלכים פעילים באוויר (Dynamic Collision)
    private boolean isSquareOccupiedByAnyActiveMove(Position pos, List<ActiveMove> activeMoves) {
        for (ActiveMove move : activeMoves) {
            if (move.getTo().equals(pos) && !move.isJump()) {
                return true;
            }
        }
        return false;
    }

    private boolean isSquareOccupiedByEnemyActiveMove(Position pos, Piece.Color friendlyColor, List<ActiveMove> activeMoves) {
        for (ActiveMove move : activeMoves) {
            if (move.getTo().equals(pos) && move.getPiece().getColor() != friendlyColor && !move.isJump()) {
                return true;
            }
        }
        return false;
    }
}
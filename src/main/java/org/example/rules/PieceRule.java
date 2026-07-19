package org.example.rules;

import org.example.models.Board;
import org.example.models.Piece;
import org.example.models.*;

import java.util.List;

public interface PieceRule {
    /**
     * בודק האם תנועת הכלי מחוקית מבחינת צורת התנועה והחסימות על הלוח.
     * @return MoveValidationResult עם ההתוצאה וההסבר
     */
    MoveValidationResult isValidMove(Position from, Position to, Piece piece, Board board, List<ActiveMove> activeMoves);
}
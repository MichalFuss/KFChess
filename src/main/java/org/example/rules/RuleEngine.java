package org.example.rules;

import org.example.models.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RuleEngine {
    private final Map<Piece.Kind, PieceRule> rules;

    public RuleEngine() {
        rules = new EnumMap<>(Piece.Kind.class);
        rules.put(Piece.Kind.ROOK, new RookRule());
        rules.put(Piece.Kind.BISHOP, new BishopRule());
        rules.put(Piece.Kind.QUEEN, new QueenRule());
        rules.put(Piece.Kind.KNIGHT, new KnightRule());
        rules.put(Piece.Kind.KING, new KingRule());
        rules.put(Piece.Kind.PAWN, new PawnRule());
    }

    /**
     * הפונקציה הראשית שתיקרא מתוך ה-GameController או ה-Engine.
     * מחזירה MoveValidationResult עם התוצאה וההסבר
     */
    public MoveValidationResult validateMove(Position from, Position to, Piece piece, Board board, List<ActiveMove> activeMoves) {
        // 1. הגנה בסיסית: האם זזנו לאותה משבצת?
        if (from.equals(to)) return MoveValidationResult.INVALID_SAME_SQUARE;

        // מניעת תנועה של כלי שנמצא במנוחה קצרה או ארוכה
        if (piece.getState() == Piece.State.SHORT_REST || piece.getState() == Piece.State.LONG_REST) {
            return MoveValidationResult.PIECE_IN_REST;
        }
        // 2. הגנה בסיסית: האם משבצת היעד בתוך גבולות הלוח?
        if (!board.isWithinBounds(to)) return MoveValidationResult.OUT_OF_BOUNDS;
        
        if (piece == null) return MoveValidationResult.NO_PIECE;
        // 3. שליפת החוק הספציפי של סוג הכלי והרצתו
        PieceRule rule = rules.get(piece.getKind());
        if (rule == null) return MoveValidationResult.INVALID_PIECE_KIND;

        return rule.isValidMove(from, to, piece, board, activeMoves);
    }
}
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
     * מחזירה true אם המהלך חוקי לחלוטין.
     */
    public boolean validateMove(Position from, Position to, Piece piece, Board board, List<ActiveMove> activeMoves) {
        // 1. הגנה בסיסית: האם זזנו לאותה משבצת?
        if (from.equals(to)) return false;

        // 2. הגנה בסיסית: האם משבצת היעד בתוך גבולות הלוח?
        if (!board.isWithinBounds(to)) return false;
        
        if (piece == null) return false;
        // 3. שליפת החוק הספציפי של סוג הכלי והרצתו
        PieceRule rule = rules.get(piece.getKind());
        if (rule == null) return false;

        return rule.isValidMove(from, to, piece, board, activeMoves);
    }
}
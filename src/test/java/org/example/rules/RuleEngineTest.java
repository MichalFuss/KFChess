package org.example.rules;

import org.example.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RuleEngineTest {

    private RuleEngine ruleEngine;
    private Board board;
    private List<ActiveMove> activeMoves;

    @BeforeEach
    void setUp() {
        ruleEngine = new RuleEngine();
        // לוח סטנדרטי 8x8 כדי לבדוק את התנהגות מנוע החוקים הכללי
        board = new Board(8, 8);
        activeMoves = new ArrayList<>();
    }

    @Test
    void testValidateMove_MoveToSameSquare_ReturnsFalse() {
        Position pos = new Position(3, 3);
        Piece rook = new Piece("wR1", Piece.Color.WHITE, Piece.Kind.ROOK, pos);
        board.setPiece(pos, rook);

        // ניסיון לזוז מאותה משבצת לאותה משבצת (from == to)
        assertFalse(ruleEngine.validateMove(pos, pos, rook, board, activeMoves),
                "מנוע החוקים חייב לחסום מיידית מהלך שבו היעד שווה למקור");
    }

    @Test
    void testValidateMove_OutOfBoundsDestination_ReturnsFalse() {
        Position from = new Position(7, 7);
        Piece rook = new Piece("wR1", Piece.Color.WHITE, Piece.Kind.ROOK, from);
        board.setPiece(from, rook);

        // יעד מחוץ לגבולות הלוח (שורה 8 בלוח של 8x8 שהאינדקסים שלו הם 0-7)
        Position outOfBoundsTo = new Position(8, 7);
        assertFalse(ruleEngine.validateMove(from, outOfBoundsTo, rook, board, activeMoves),
                "מנוע החוקים חייב לחסום מהלך שמיועד אל מחוץ לגבולות הלוח");
    }

    @Test
    void testValidateMove_DelegationToSpecificRule_RookExample() {
        Position from = new Position(3, 3);
        Piece rook = new Piece("wR1", Piece.Color.WHITE, Piece.Kind.ROOK, from);
        board.setPiece(from, rook);

        // 1. מהלך ישר (חוקי לצריח) - ה-RuleEngine צריך להחזיר true
        Position validTo = new Position(3, 6);
        assertTrue(ruleEngine.validateMove(from, validTo, rook, board, activeMoves),
                "מנוע החוקים צריך לאשר מהלך חוקי של צריח באמצעות האצלת סמכויות לחוק שלו");

        // 2. מהלך אלכסוני (לא חוקי לצריח) - ה-RuleEngine צריך להחזיר false
        Position invalidTo = new Position(5, 5);
        assertFalse(ruleEngine.validateMove(from, invalidTo, rook, board, activeMoves),
                "מנוע החוקים צריך לחסום מהלך לא חוקי של צריח");
    }

    @Test
    void testValidateMove_DelegationToSpecificRule_BishopExample() {
        Position from = new Position(3, 3);
        Piece bishop = new Piece("wB1", Piece.Color.WHITE, Piece.Kind.BISHOP, from);
        board.setPiece(from, bishop);

        // 1. מהלך אלכסוני (חוקי לרץ)
        Position validTo = new Position(5, 5);
        assertTrue(ruleEngine.validateMove(from, validTo, bishop, board, activeMoves),
                "מנוע החוקים צריך לאשר מהלך אלכסוני חוקי עבור רץ");

        // 2. מהלך ישר (לא חוקי לרץ)
        Position invalidTo = new Position(3, 6);
        assertFalse(ruleEngine.validateMove(from, invalidTo, bishop, board, activeMoves),
                "מנוע החוקים צריך לחסום מהלך ישר עבור רץ");
    }
}
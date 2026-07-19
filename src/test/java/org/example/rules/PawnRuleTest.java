package org.example.rules;

import org.example.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PawnRuleTest {
    private PawnRule pawnRule;
    private Board board;
    private List<ActiveMove> activeMoves;

    @BeforeEach
    void setUp() {
        pawnRule = new PawnRule();
        board = new Board(8, 8);
        activeMoves = new ArrayList<>();
    }

    @Test
    void testWhitePawnDoubleStep_Valid() {
        // שורה 6 היא שורת המוצא עבור לבן בלוח 8x8
        Position from = new Position(6, 1);
        Piece whitePawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, whitePawn);

        // יעד חוקי לצעד כפול מ-6 הוא 4
        assertTrue(pawnRule.isValidMove(from, new Position(4, 1), whitePawn, board, activeMoves).isValid());
    }

    @Test
    void testPawnForwardStep_Blocked() {
        Position from = new Position(3, 1);
        Piece whitePawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, whitePawn);
        Position obstacle = new Position(2, 1);
        board.setPiece(obstacle, new Piece("bN_1", Piece.Color.BLACK, Piece.Kind.KNIGHT, obstacle));

        assertEquals(MoveValidationResult.BLOCKED_BY_PIECE, pawnRule.isValidMove(from, obstacle, whitePawn, board, activeMoves));
    }
}
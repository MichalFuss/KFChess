package org.example.rules;

import org.example.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BishopRuleTest {
    private BishopRule bishopRule;
    private Board board;
    private List<ActiveMove> activeMoves;
    private Piece whiteBishop;
    private Position fromPos;

    @BeforeEach
    void setUp() {
        bishopRule = new BishopRule();
        board = new Board(8, 8);
        activeMoves = new ArrayList<>();
        fromPos = new Position(3, 3);
        whiteBishop = new Piece("wB_1", Piece.Color.WHITE, Piece.Kind.BISHOP, fromPos);
        board.setPiece(fromPos, whiteBishop);
    }

    @Test
    void testValidDiagonalMoves_AllFourDirections() {
        assertTrue(bishopRule.isValidMove(fromPos, new Position(1, 5), whiteBishop, board, activeMoves).isValid());
        assertTrue(bishopRule.isValidMove(fromPos, new Position(5, 5), whiteBishop, board, activeMoves).isValid());
        assertTrue(bishopRule.isValidMove(fromPos, new Position(5, 1), whiteBishop, board, activeMoves).isValid());
        assertTrue(bishopRule.isValidMove(fromPos, new Position(1, 1), whiteBishop, board, activeMoves).isValid());
    }

    @Test
    void testInvalidStraightMove() {
        assertEquals(MoveValidationResult.INVALID_MOVE_PATTERN, bishopRule.isValidMove(fromPos, new Position(3, 6), whiteBishop, board, activeMoves));
    }
}
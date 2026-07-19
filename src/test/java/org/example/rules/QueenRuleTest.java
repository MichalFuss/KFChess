package org.example.rules;

import org.example.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class QueenRuleTest {
    private QueenRule queenRule;
    private Board board;
    private List<ActiveMove> activeMoves;
    private Piece whiteQueen;
    private Position fromPos;

    @BeforeEach
    void setUp() {
        queenRule = new QueenRule();
        board = new Board(8, 8);
        activeMoves = new ArrayList<>();
        fromPos = new Position(3, 3);
        whiteQueen = new Piece("wQ_1", Piece.Color.WHITE, Piece.Kind.QUEEN, fromPos);
        board.setPiece(fromPos, whiteQueen);
    }

    @Test
    void testValidQueenMoves() {
        assertTrue(queenRule.isValidMove(fromPos, new Position(3, 7), whiteQueen, board, activeMoves).isValid());
        assertTrue(queenRule.isValidMove(fromPos, new Position(5, 5), whiteQueen, board, activeMoves).isValid());
    }

    @Test
    void testInvalidQueenMove() {
        assertEquals(MoveValidationResult.INVALID_MOVE_PATTERN, queenRule.isValidMove(fromPos, new Position(5, 4), whiteQueen, board, activeMoves));
    }
}
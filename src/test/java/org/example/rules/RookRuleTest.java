package org.example.rules;

import org.example.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RookRuleTest {
    private RookRule rookRule;
    private Board board;
    private List<ActiveMove> activeMoves;
    private Piece whiteRook;
    private Position fromPos;

    @BeforeEach
    void setUp() {
        rookRule = new RookRule();
        board = new Board(8, 8);
        activeMoves = new ArrayList<>();
        fromPos = new Position(3, 3);
        whiteRook = new Piece("wR_1", Piece.Color.WHITE, Piece.Kind.ROOK, fromPos);
        board.setPiece(fromPos, whiteRook);
    }

    @Test
    void testValidRookMoves() {
        assertTrue(rookRule.isValidMove(fromPos, new Position(3, 7), whiteRook, board, activeMoves).isValid());
        assertTrue(rookRule.isValidMove(fromPos, new Position(7, 3), whiteRook, board, activeMoves).isValid());
    }

    @Test
    void testMoveBlockedByStaticFriendlyPiece() {
        Position obstaclePos = new Position(5, 3);
        board.setPiece(obstaclePos, new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, obstaclePos));

        assertEquals(MoveValidationResult.PATH_BLOCKED, rookRule.isValidMove(fromPos, new Position(7, 3), whiteRook, board, activeMoves));
    }
}
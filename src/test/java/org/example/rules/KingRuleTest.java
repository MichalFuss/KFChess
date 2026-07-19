package org.example.rules;

import org.example.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class KingRuleTest {
    private KingRule kingRule;
    private Board board;
    private List<ActiveMove> activeMoves;
    private Piece whiteKing;
    private Position fromPos;

    @BeforeEach
    void setUp() {
        kingRule = new KingRule();
        board = new Board(8, 8);
        activeMoves = new ArrayList<>();
        fromPos = new Position(4, 4);
        whiteKing = new Piece("wK_1", Piece.Color.WHITE, Piece.Kind.KING, fromPos);
        board.setPiece(fromPos, whiteKing);
    }

    @Test
    void testValidKingMoves_AllEightDirections() {
        int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        for (int[] dir : directions) {
            Position toPos = new Position(fromPos.getRow() + dir[0], fromPos.getCol() + dir[1]);
            assertTrue(kingRule.isValidMove(fromPos, toPos, whiteKing, board, activeMoves).isValid());
        }
    }

    @Test
    void testInvalidKingMove_TooFar() {
        assertFalse(kingRule.isValidMove(fromPos, new Position(4, 6), whiteKing, board, activeMoves).isValid());
    }
}
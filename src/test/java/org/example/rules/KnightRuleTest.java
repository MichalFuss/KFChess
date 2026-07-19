package org.example.rules;

import org.example.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class KnightRuleTest {
    private KnightRule knightRule;
    private Board board;
    private List<ActiveMove> activeMoves;
    private Piece whiteKnight;
    private Position fromPos;

    @BeforeEach
    void setUp() {
        knightRule = new KnightRule();
        board = new Board(8, 8);
        activeMoves = new ArrayList<>();
        fromPos = new Position(3, 3);
        whiteKnight = new Piece("wN_1", Piece.Color.WHITE, Piece.Kind.KNIGHT, fromPos);
        board.setPiece(fromPos, whiteKnight);
    }

    @Test
    void testValidKnightMoves() {
        int[][] lMoves = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
        for (int[] move : lMoves) {
            Position toPos = new Position(fromPos.getRow() + move[0], fromPos.getCol() + move[1]);
            assertTrue(knightRule.isValidMove(fromPos, toPos, whiteKnight, board, activeMoves).isValid());
        }
    }
}
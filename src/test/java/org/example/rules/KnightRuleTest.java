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

        // נציב פרש לבן במיקום מרכזי (3, 3)
        fromPos = new Position(3, 3);
        whiteKnight = new Piece("wN_1", Piece.Color.WHITE, Piece.Kind.KNIGHT, fromPos);
        board.setPiece(fromPos, whiteKnight);
    }

    @Test
    void testValidKnightMoves_AllEightLPositions() {
        // לפרש יש 8 משבצות חוקיות סביבו בתנועת L
        int[][] lMoves = {
                {-2, -1}, {-2, 1},
                {-1, -2}, {-1, 2},
                {1, -2},  {1, 2},
                {2, -1},  {2, 1}
        };

        for (int[] move : lMoves) {
            Position toPos = new Position(fromPos.getRow() + move[0], fromPos.getCol() + move[1]);
            assertTrue(knightRule.isValidMove(fromPos, toPos, whiteKnight, board, activeMoves),
                    "הפרש צריך לדעת לזוז ליעד L: " + move[0] + "," + move[1]);
        }
    }

    @Test
    void testInvalidKnightMoves() {
        // תנועה ישרה או אלכסונית רגילה שאינה L צריכה להיכשל
        assertFalse(knightRule.isValidMove(fromPos, new Position(3, 5), whiteKnight, board, activeMoves), "תנועה אופקית אסורה");
        assertFalse(knightRule.isValidMove(fromPos, new Position(5, 5), whiteKnight, board, activeMoves), "תנועה אלכסונית אסורה");
    }

    @Test
    void testKnightJumpsOverObstacles() {
        // נשים כלי חוסם (לא משנה של איזה צד) בדרך של הפרש (למשל ב-3, 4 וב-4, 4)
        board.setPiece(new Position(3, 4), new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, new Position(3, 4)));
        board.setPiece(new Position(4, 4), new Piece("bP_1", Piece.Color.BLACK, Piece.Kind.PAWN, new Position(4, 4)));

        // הפרש זז ליעד (5, 4) - מכיוון שהוא מדלג מעל כלים, המהלך חייב להיות חוקי!
        Position targetPos = new Position(5, 4); // צעד של שורה +2, עמודה +1
        assertTrue(knightRule.isValidMove(fromPos, targetPos, whiteKnight, board, activeMoves),
                "פרש חייב לדלג מעל חסימות בדרך אל היעד");
    }

    @Test
    void testMoveBlockedByFriendlyPieceAtDestination() {
        // נציב כלי ידידותי ביעד (5, 4)
        Position friendlyPos = new Position(5, 4);
        board.setPiece(friendlyPos, new Piece("wP_2", Piece.Color.WHITE, Piece.Kind.PAWN, friendlyPos));

        assertFalse(knightRule.isValidMove(fromPos, friendlyPos, whiteKnight, board, activeMoves),
                "הפרש לא יכול לנחות על כלי ידידותי סטטי");
    }

    @Test
    void testCaptureEnemyPieceAtDestination() {
        // נציב כלי אויב ביעד (5, 4)
        Position enemyPos = new Position(5, 4);
        board.setPiece(enemyPos, new Piece("bB_1", Piece.Color.BLACK, Piece.Kind.BISHOP, enemyPos));

        assertTrue(knightRule.isValidMove(fromPos, enemyPos, whiteKnight, board, activeMoves),
                "הפרש רשאי לנחות ולהכות כלי אויב שנמצא ביעד");
    }
}
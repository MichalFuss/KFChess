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

        // הצבת מלך לבן במיקום מרכזי (4,4)
        fromPos = new Position(4, 4);
        whiteKing = new Piece("wK_1", Piece.Color.WHITE, Piece.Kind.KING, fromPos);
        board.setPiece(fromPos, whiteKing);
    }

    @Test
    void testValidKingMoves_AllEightDirections() {
        // המלך צריך לזוז צעד אחד לכל 8 הכיוונים מסביבו
        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},           {0, 1},
                {1, -1},  {1, 0},  {1, 1}
        };

        for (int[] dir : directions) {
            Position toPos = new Position(fromPos.getRow() + dir[0], fromPos.getCol() + dir[1]);
            assertTrue(kingRule.isValidMove(fromPos, toPos, whiteKing, board, activeMoves),
                    "המלך צריך להיות מסוגל לזוז למיקום יחסי: " + dir[0] + "," + dir[1]);
        }
    }

    @Test
    void testInvalidKingMove_TooFar() {
        // ניסיון לזוז 2 משבצות ימינה - צריך להיכשל
        Position toPosFar = new Position(4, 6);
        assertFalse(kingRule.isValidMove(fromPos, toPosFar, whiteKing, board, activeMoves),
                "המלך אינו יכול לזוז יותר משבצת אחת");
    }

    @Test
    void testMoveBlockedByStaticFriendlyPiece() {
        // נשים רגלי לבן במשבצת (4, 5)
        Position friendlyPos = new Position(4, 5);
        Piece friendlyPawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, friendlyPos);
        board.setPiece(friendlyPos, friendlyPawn);

        // המלך לא יכול לנחות על חבר שלו
        assertFalse(kingRule.isValidMove(fromPos, friendlyPos, whiteKing, board, activeMoves),
                "המלך לא יכול לזוז למשבצת שחוסם אותה כלי ידידותי עומד");
    }

    @Test
    void testMoveBlockedByDynamicFriendlyActiveMove() {
        Position targetPos = new Position(4, 5);
        Position startingPos = new Position(4, 0);
        Piece anotherFriendly = new Piece("wR_1", Piece.Color.WHITE, Piece.Kind.ROOK, startingPos);

        // תיקון סדר הפרמטרים: (from, to, piece, arrivalTimeMillis, isJump)
        ActiveMove activeMove = new ActiveMove(startingPos, targetPos, anotherFriendly, 1000L, false);
        activeMoves.add(activeMove);

        assertFalse(kingRule.isValidMove(fromPos, targetPos, whiteKing, board, activeMoves),
                "המלך לא יכול לזוז למשבצת שכלי ידידותי אחר נמצא בדרך אליה באוויר");
    }

    @Test
    void testCaptureEnemyPiece_IsLegal() {
        // הצבת פרש שחור (אויב) בטווח הליכה של המלך
        Position enemyPos = new Position(3, 4);
        Piece enemyKnight = new Piece("bN_1", Piece.Color.BLACK, Piece.Kind.KNIGHT, enemyPos);
        board.setPiece(enemyPos, enemyKnight);

        // הכאת אויב היא חוקית לחלוטין
        assertTrue(kingRule.isValidMove(fromPos, enemyPos, whiteKing, board, activeMoves),
                "המלך רשאי להכות כלי אויב שנמצא בטווח הצעד שלו");
    }
}
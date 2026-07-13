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

        // נציב מלכה לבנה במרכז (3, 3)
        fromPos = new Position(3, 3);
        whiteQueen = new Piece("wQ_1", Piece.Color.WHITE, Piece.Kind.QUEEN, fromPos);
        board.setPiece(fromPos, whiteQueen);
    }

    @Test
    void testValidQueenStraightMoves() {
        // תנועה ישרה אופקית ואנכית
        assertTrue(queenRule.isValidMove(fromPos, new Position(3, 7), whiteQueen, board, activeMoves), "תנועה אופקית ימינה");
        assertTrue(queenRule.isValidMove(fromPos, new Position(6, 3), whiteQueen, board, activeMoves), "תנועה אנכית למטה");
    }

    @Test
    void testValidQueenDiagonalMoves() {
        // תנועה אלכסונית לכל 4 הכיוונים
        assertTrue(queenRule.isValidMove(fromPos, new Position(5, 5), whiteQueen, board, activeMoves), "אלכסון למטה-ימינה");
        assertTrue(queenRule.isValidMove(fromPos, new Position(1, 1), whiteQueen, board, activeMoves), "אלכסון למעלה-שמאלה");
    }

    @Test
    void testInvalidQueenMove() {
        // תנועה שאינה ישרה ואינה אלכסונית (כמו תנועת L של פרש) צריכה להיכשל
        Position badPos = new Position(5, 4); // שורה +2, עמודה +1
        assertFalse(queenRule.isValidMove(fromPos, badPos, whiteQueen, board, activeMoves),
                "המלכה אינה יכולה לנוע במסלול שאינו ישר או אלכסוני מושלם");
    }

    @Test
    void testMoveBlockedByFriendlyPieceInPath() {
        // נשים רגלי חוסם במסלול הישר של המלכה (ב-3, 5)
        board.setPiece(new Position(3, 5), new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, new Position(3, 5)));

        // ניסיון לעבור מעבר לו (ל-3, 7) - צריך להיכשל
        assertFalse(queenRule.isValidMove(fromPos, new Position(3, 7), whiteQueen, board, activeMoves),
                "המלכה צריכה להיחסם על ידי כלי שנמצא בנתיב הישר שלה");
    }

    @Test
    void testMoveBlockedByDynamicActiveMoveInPath() {
        Position obstaclePos = new Position(5, 5);
        Piece anotherFriendly = new Piece("wN_2", Piece.Color.WHITE, Piece.Kind.KNIGHT, new Position(4, 3));

        // יצירת מהלך פעיל באוויר של כלי ידידותי שבדרך לנחות באלכסון של המלכה
        ActiveMove activeMove = new ActiveMove(new Position(4, 3), obstaclePos, anotherFriendly, 1000L, false);
        activeMoves.add(activeMove);

        // ניסיון של המלכה לזוז ל-(6, 6) שהוא מעבר לחסימה הדינמית באלכסון
        assertFalse(queenRule.isValidMove(fromPos, new Position(6, 6), whiteQueen, board, activeMoves),
                "המלכה צריכה להיחסם דינמית על ידי מהלך פעיל שנמצא בנתיב האלכסוני שלה");
    }

    @Test
    void testCaptureEnemyPieceAtDestination() {
        Position enemyPos = new Position(5, 5);
        board.setPiece(enemyPos, new Piece("bR_1", Piece.Color.BLACK, Piece.Kind.ROOK, enemyPos));

        assertTrue(queenRule.isValidMove(fromPos, enemyPos, whiteQueen, board, activeMoves),
                "המלכה רשאית להכות כלי אויב שנמצא בסוף נתיב פנוי");
    }
}
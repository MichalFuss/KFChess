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
        // נשתמש בלוח ריבועי של 4x4 כדי למנוע החלפות בין רוחב לגובה במערכים הפנימיים
        board = new Board(4, 4);
        activeMoves = new ArrayList<>();
    }

    @Test
    void testWhitePawnDoubleStepFromStart_Valid() {
        // רגלי לבן בשורה התחתונה ביותר (שורה 3)
        Position from = new Position(3, 1);
        Piece whitePawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, whitePawn);

        // צעד כפול קדימה לשורה 1 (הפרש של -2 בשורות)
        Position to = new Position(1, 1);
        assertTrue(pawnRule.isValidMove(from, to, whitePawn, board, activeMoves),
                "רגלי לבן צריך להרשות צעד כפול משורת ההתחלה הדינמית שלו");
    }

    @Test
    void testBlackPawnDoubleStepFromStart_Valid() {
        // רגלי שחור בשורה העליונה ביותר (שורה 0)
        Position from = new Position(0, 1);
        Piece blackPawn = new Piece("bP_1", Piece.Color.BLACK, Piece.Kind.PAWN, from);
        board.setPiece(from, blackPawn);

        // צעד כפול קדימה לשורה 2 (הפרש של +2 בשורות)
        Position to = new Position(2, 1);
        assertTrue(pawnRule.isValidMove(from, to, blackPawn, board, activeMoves),
                "רגלי שחור צריך להרשות צעד כפול משורת ההתחלה הדינמית שלו");
    }

    @Test
    void testWhitePawnDoubleStepFromNonStart_Invalid() {
        // רגלי לבן שלא נמצא בשורת ההתחלה שלו (למשל נמצא בשורה 2)
        Position from = new Position(2, 1);
        Piece whitePawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, whitePawn);

        // ניסיון לצעד כפול לשורה 0
        Position to = new Position(0, 1);
        assertFalse(pawnRule.isValidMove(from, to, whitePawn, board, activeMoves),
                "רגלי אינו מורשה לבצע צעד כפול אם אינו נמצא בשורת ההתחלה");
    }

    @Test
    void testPawnSingleStepForward_ClearPath() {
        Position from = new Position(3, 1);
        Piece whitePawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, whitePawn);

        Position to = new Position(2, 1);
        assertTrue(pawnRule.isValidMove(from, to, whitePawn, board, activeMoves),
                "צעד אחד קדימה בנתיב פנוי חייב להיות חוקי");
    }

    @Test
    void testPawnForwardStep_BlockedByPiece() {
        Position from = new Position(3, 1);
        Piece whitePawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, whitePawn);

        // נשים כלי חוסם ישירות מלפניו (בצעד אחד קדימה - שורה 2)
        Position obstaclePos = new Position(2, 1);
        board.setPiece(obstaclePos, new Piece("bN_1", Piece.Color.BLACK, Piece.Kind.KNIGHT, obstaclePos));

        assertFalse(pawnRule.isValidMove(from, obstaclePos, whitePawn, board, activeMoves),
                "רגלי אינו יכול לצעוד קדימה למשבצת חסומה ע\"י כלי אחר");
    }

    @Test
    void testPawnDiagonalCapture_Valid() {
        Position from = new Position(3, 1);
        Piece whitePawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, whitePawn);

        // נשים כלי אויב באלכסון קדימה (שורה 2, עמודה 2 - שתיהן בטווח של הלוח 4x4)
        Position enemyPos = new Position(2, 2);
        board.setPiece(enemyPos, new Piece("bN_1", Piece.Color.BLACK, Piece.Kind.KNIGHT, enemyPos));

        assertTrue(pawnRule.isValidMove(from, enemyPos, whitePawn, board, activeMoves),
                "רגלי מורשה לנוע באלכסון צעד אחד רק לצורך הכאת כלי אויב");
    }

    @Test
    void testPawnDiagonalMoveWithoutEnemy_Invalid() {
        Position from = new Position(3, 1);
        Piece whitePawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, whitePawn);

        // ניסיון לזוז באלכסון למשבצת ריקה (שורה 2, עמודה 2)
        Position emptyDiagonal = new Position(2, 2);
        assertFalse(pawnRule.isValidMove(from, emptyDiagonal, whitePawn, board, activeMoves),
                "רגלי אינו מורשה לזוז באלכסון אם אין שם כלי אויב להכאה");
    }
}
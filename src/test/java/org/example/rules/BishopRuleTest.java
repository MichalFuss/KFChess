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
        board = new Board(8, 8); // לוח משחק בגודל 8x8 לצורך הבדיקות
        activeMoves = new ArrayList<>();

        // נציב רץ לבן במיקום מרכזי (3, 3) כדי לבחון תנועה חופשית לכל כיווני האלכסון
        fromPos = new Position(3, 3);
        whiteBishop = new Piece("wB_1", Piece.Color.WHITE, Piece.Kind.BISHOP, fromPos);
        board.setPiece(fromPos, whiteBishop);
    }

    @Test
    void testValidDiagonalMoves_AllFourDirections() {
        // רץ צריך לנוע לכל 4 כיווני האלכסונים כאשר הנתיב פנוי (הפרש שורות שווה להפרש עמודות)
        Position upRight = new Position(1, 5); // שורה -2, עמודה +2
        Position downRight = new Position(5, 5); // שורה +2, עמודה +2
        Position downLeft = new Position(5, 1); // שורה +2, עמודה -2
        Position upLeft = new Position(1, 1); // שורה -2, עמודה -2

        assertTrue(bishopRule.isValidMove(fromPos, upRight, whiteBishop, board, activeMoves), "הרץ צריך לזוז למעלה-ימינה באלכסון");
        assertTrue(bishopRule.isValidMove(fromPos, downRight, whiteBishop, board, activeMoves), "הרץ צריך לזוז למטה-ימינה באלכסון");
        assertTrue(bishopRule.isValidMove(fromPos, downLeft, whiteBishop, board, activeMoves), "הרץ צריך לזוז למטה-שמאלה באלכסון");
        assertTrue(bishopRule.isValidMove(fromPos, upLeft, whiteBishop, board, activeMoves), "הרץ צריך לזוז למעלה-שמאלה באלכסון");
    }

    @Test
    void testInvalidStraightMove() {
        // תנועה ישרה (אופקית או אנכית) אינה חוקית עבור רץ
        Position straightPos = new Position(3, 6); // אותה שורה, עמודה שונה
        assertFalse(bishopRule.isValidMove(fromPos, straightPos, whiteBishop, board, activeMoves),
                "רץ אינו מורשה לזוז בקווים ישרים אופקיים או אנכיים");
    }

    @Test
    void testInvalidAsymmetricMove() {
        // תנועה לא סימטרית (שאינה אלכסון מושלם) צריכה להיכשל
        Position badPos = new Position(5, 4); // הפרש שורות 2, הפרש עמודות 1
        assertFalse(bishopRule.isValidMove(fromPos, badPos, whiteBishop, board, activeMoves),
                "רץ אינו מורשה לזוז בנתיב שאינו אלכסון מושלם");
    }

    @Test
    void testMoveBlockedByStaticFriendlyPieceInPath() {
        // נציב רגלי לבן (ידידותי) באמצע האלכסון למטה-ימינה (במשבצת 4, 4)
        Position obstaclePos = new Position(4, 4);
        Piece friendlyPawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, obstaclePos);
        board.setPiece(obstaclePos, friendlyPawn);

        // ננסה לבצע מהלך ארוך יותר באותו אלכסון (למשבצת 5, 5) - צריך להיחסם
        Position targetPos = new Position(5, 5);
        assertFalse(bishopRule.isValidMove(fromPos, targetPos, whiteBishop, board, activeMoves),
                "הרץ צריך להיחסם על ידי כלי ידידותי שעומד סטטית במסלול שלו");
    }

    @Test
    void testMoveBlockedByDynamicFriendlyActiveMoveInPath() {
        // נדמה מצב שבו כלי לבן אחר נמצא כרגע באוויר ובאמצע המסלול של הרץ (במשבצת 2, 2)
        Position obstaclePos = new Position(2, 2);
        Piece anotherFriendly = new Piece("wN_1", Piece.Color.WHITE, Piece.Kind.KNIGHT, new Position(0, 1));

        // יצירת המהלך הפעיל באוויר (from, to, piece, arrivalTimeMillis, isJump)
        ActiveMove activeMove = new ActiveMove(new Position(0, 1), obstaclePos, anotherFriendly, 1000L, false);
        activeMoves.add(activeMove);

        // ניסיון של הרץ לזוז אל המשבצת הבאה באלכסון (1, 1) - צריך להיחסם דינמית
        Position targetPos = new Position(1, 1);
        assertFalse(bishopRule.isValidMove(fromPos, targetPos, whiteBishop, board, activeMoves),
                "הרץ צריך להיחסם על ידי מהלך פעיל של כלי ידידותי שבדרך לנחות בנתיב שלו");
    }

    @Test
    void testCaptureEnemyPieceAtDestination() {
        // נציב כלי שחור (אויב) ישירות על משבצת היעד האלכסונית (5, 5)
        Position enemyPos = new Position(5, 5);
        Piece enemyRook = new Piece("bR_1", Piece.Color.BLACK, Piece.Kind.ROOK, enemyPos);
        board.setPiece(enemyPos, enemyRook);

        // מכיוון שהמסלול פנוי והיעד הוא אויב, המהלך צריך להיות חוקי לחלוטין (הכאה)
        assertTrue(bishopRule.isValidMove(fromPos, enemyPos, whiteBishop, board, activeMoves),
                "רץ מורשה לנוע וללכוד כלי אויב שנמצא בסוף נתיב אלכסוני פנוי");
    }

    @Test
    void testMoveBlockedByFriendlyPieceAtDestination() {
        // נציב כלי לבן (ידידותי) ישירות על משבצת היעד הסופית (5, 5)
        Position friendlyPos = new Position(5, 5);
        Piece friendlyPiece = new Piece("wQ_1", Piece.Color.WHITE, Piece.Kind.QUEEN, friendlyPos);
        board.setPiece(friendlyPos, friendlyPiece);

        // הרץ לא יכול לנחות על משבצת המאוכלסת בכלי מאותו הצבע
        assertFalse(bishopRule.isValidMove(fromPos, friendlyPos, whiteBishop, board, activeMoves),
                "רץ אינו מורשה לנחות על משבצת המאוכלסת בכלי ידידותי עומד");
    }
}
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
        board = new Board(8, 8); // לוח דינמי 8x8 לצורך הבדיקה
        activeMoves = new ArrayList<>();

        // נציב צריח לבן במיקום מרכזי (3, 3) כדי לבדוק תנועה לכל הכיוונים
        fromPos = new Position(3, 3);
        whiteRook = new Piece("wR_1", Piece.Color.WHITE, Piece.Kind.ROOK, fromPos);
        board.setPiece(fromPos, whiteRook);
    }

    @Test
    void testValidHorizontalMove_ClearPath() {
        // תנועה אופקית ימינה לאורך השורה (משורה 3 עמודה 3 לשורה 3 עמודה 7)
        Position toPos = new Position(3, 7);
        assertTrue(rookRule.isValidMove(fromPos, toPos, whiteRook, board, activeMoves),
                "הצריח צריך לזוז אופקית כשהנתיב פנוי");
    }

    @Test
    void testValidVerticalMove_ClearPath() {
        // תנועה אנכית למטה לאורך העמודה (משורה 3 עמודה 3 לשורה 7 עמודה 3)
        Position toPos = new Position(7, 3);
        assertTrue(rookRule.isValidMove(fromPos, toPos, whiteRook, board, activeMoves),
                "הצריח צריך לזוז אנכית כשהנתיב פנוי");
    }

    @Test
    void testInvalidDiagonalMove() {
        // צריח אינו יכול לזוז באלכסון (למשל ל-4,4)
        Position diagonalPos = new Position(4, 4);
        assertFalse(rookRule.isValidMove(fromPos, diagonalPos, whiteRook, board, activeMoves),
                "מהלך אלכסוני של צריח אינו חוקי");
    }

    @Test
    void testMoveBlockedByStaticFriendlyPieceInPath() {
        // נשים כלי ידידותי (wP) באמצע הנתיב האנכי (במשבצת 5, 3)
        Position obstaclePos = new Position(5, 3);
        Piece friendlyPawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, obstaclePos);
        board.setPiece(obstaclePos, friendlyPawn);

        // ננסה לזוז מעבר אליו (למשבצת 7, 3) - צריך להיכשל!
        Position targetPos = new Position(7, 3);
        assertFalse(rookRule.isValidMove(fromPos, targetPos, whiteRook, board, activeMoves),
                "הצריח אינו יכול לדלג מעל כלי חוסם בנתיב");
    }

    @Test
    void testMoveBlockedByDynamicFriendlyActiveMoveInPath() {
        // נדמה כלי ידידותי שנמצא כרגע באוויר ובאמצע המסלול של הצריח (במשבצת 3, 5)
        Position obstaclePos = new Position(3, 5);
        Piece anotherFriendly = new Piece("wN_1", Piece.Color.WHITE, Piece.Kind.KNIGHT, new Position(1, 4));

        // יצירת מהלך פעיל שנוחת בדיוק בנקודת החסימה באותו הזמן
        ActiveMove activeMove = new ActiveMove(new Position(1, 4), obstaclePos, anotherFriendly, 1000L, false);
        activeMoves.add(activeMove);

        // ניסיון של הצריח לזוז אל 3, 6 (מעבר לחסימה הדינמית)
        Position targetPos = new Position(3, 6);
        assertFalse(rookRule.isValidMove(fromPos, targetPos, whiteRook, board, activeMoves),
                "הצריח צריך להיחסם על ידי מהלך פעיל של חבר שנמצא בנתיב שלו");
    }

    @Test
    void testCaptureEnemyPieceAtDestination() {
        // נציב כלי אויב (bR) בדיוק במשבצת היעד (3, 6)
        Position enemyPos = new Position(3, 6);
        Piece enemyRook = new Piece("bR_2", Piece.Color.BLACK, Piece.Kind.ROOK, enemyPos);
        board.setPiece(enemyPos, enemyRook);

        // הנתיב עד אליו פנוי, והיעד הוא אויב -> המהלך חוקי (הכאה)
        assertTrue(rookRule.isValidMove(fromPos, enemyPos, whiteRook, board, activeMoves),
                "צריח רשאי לנוע ולתפוס כלי אויב שנמצא בסוף הנתיב הפנוי");
    }

    @Test
    void testMoveBlockedByFriendlyPieceAtDestination() {
        // נציב כלי ידידותי בדיוק במשבצת היעד (3, 6)
        Position friendlyPos = new Position(3, 6);
        Piece friendlyPiece = new Piece("wB_1", Piece.Color.WHITE, Piece.Kind.BISHOP, friendlyPos);
        board.setPiece(friendlyPos, friendlyPiece);

        // הצריח לא יכול לנחות על כלי מהקבוצה שלו
        assertFalse(rookRule.isValidMove(fromPos, friendlyPos, whiteRook, board, activeMoves),
                "צריח אינו יכול לנחות על משבצת שחוסם אותה כלי ידידותי");
    }
}
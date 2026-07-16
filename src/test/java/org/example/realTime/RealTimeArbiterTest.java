package org.example.realTime;

import org.example.models.*;
import org.example.realtime.RealTimeArbiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RealTimeArbiterTest {

    private RealTimeArbiter arbiter;
    private GameState gameState;
    private Board board;

    @BeforeEach
    void setUp() {
        arbiter = new RealTimeArbiter();
        // יוצרים לוח בגודל סטנדרטי 8x8 לכל טסט
        board = new Board(8, 8);
        gameState = new GameState(board);
    }

    @Test
    void testRegisterMove_AddsToActiveMoves() {
        Position from = new Position(1, 1);
        Position to = new Position(2, 2);
        Piece pawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, pawn);

        arbiter.registerMove(gameState, from, to, false);

        assertEquals(1, gameState.getActiveMoves().size(), "ActiveMoves אמור להכיל מהלך אחד");
        assertEquals(pawn, gameState.getActiveMoves().get(0).getPiece());
    }

    @Test
    void testStandardLanding() {
        Position from = new Position(4, 4);
        Position to = new Position(5, 4);
        Piece pawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, pawn);

        arbiter.registerMove(gameState, from, to, false);
        arbiter.advanceSimulation(gameState, 1000);

        assertEquals(to, pawn.getCell());
        assertNull(board.getPiece(from));
        assertEquals(pawn, board.getPiece(to));

        // עדכון: החלפנו את COOLDOWN ב-LONG_REST לפי הלוגיקה המקורית שלכם
        assertEquals(Piece.State.LONG_REST, pawn.getState(), "הכלי אמור להיות במצב LONG_REST לאחר תנועה רגילה");
    }

    @Test
    void testPawnPromotion() {
        Position from = new Position(1, 0);
        Position to = new Position(0, 0);
        Piece pawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);

        board.setPiece(from, pawn);

        arbiter.registerMove(gameState, from, to, false);
        arbiter.advanceSimulation(gameState, 1000);

        Piece promotedPiece = board.getPiece(to);
        assertNotNull(promotedPiece);
        assertEquals(Piece.Kind.QUEEN, promotedPiece.getKind(), "החייל אמור להפוך למלכה");
    }

    @Test
    void testSimultaneousArrivalCollision() {
        // שני כלים המנסים להגיע לאותה משבצת בו זמנית
        Position targetPos = new Position(3, 3);
        Position fromA = new Position(3, 2);
        Position fromB = new Position(3, 4);

        Piece pieceA = new Piece("wP_A", Piece.Color.WHITE, Piece.Kind.PAWN, fromA);
        Piece pieceB = new Piece("bP_B", Piece.Color.BLACK, Piece.Kind.PAWN, fromB);

        board.setPiece(fromA, pieceA);
        board.setPiece(fromB, pieceB);

        arbiter.registerMove(gameState, fromA, targetPos, false);
        arbiter.registerMove(gameState, fromB, targetPos, false);

        // קידום זמן לסיום שני המהלכים יחד
        arbiter.advanceSimulation(gameState, 1000);

        // בודק שאחד הכלים נמצא שם והשני נלכד
        Piece winner = board.getPiece(targetPos);
        assertNotNull(winner);

        // בדיקת ניקוד: מי שביצע את האכילה קיבל נקודה 1 (Pawn שווה 1 נקודה)
        if (winner.getColor() == Piece.Color.WHITE) {
            assertEquals(1, gameState.getWhiteScore(), "הלבן אמור לקבל נקודה 1 על אכילת חייל");
            assertEquals(Piece.State.CAPTURED, pieceB.getState());
        } else {
            assertEquals(1, gameState.getBlackScore(), "השחור אמור לקבל נקודה 1 על אכילת חייל");
            assertEquals(Piece.State.CAPTURED, pieceA.getState());
        }
    }

    @Test
    void testDynamicScoringAndKingCapture() {
        // טסט ייעודי לבדיקת לוגיקת הניקוד החדשה וסיום המשחק בלכידת מלך
        Position fromWhite = new Position(4, 4);
        Position rookPos = new Position(5, 4);
        Position kingPos = new Position(6, 4);

        Piece whiteQueen = new Piece("wQ_1", Piece.Color.WHITE, Piece.Kind.QUEEN, fromWhite);
        Piece blackRook = new Piece("bR_1", Piece.Color.BLACK, Piece.Kind.ROOK, rookPos);
        Piece blackKing = new Piece("bK_1", Piece.Color.BLACK, Piece.Kind.KING, kingPos);

        board.setPiece(fromWhite, whiteQueen);
        board.setPiece(rookPos, blackRook);

        // 1. אכילת צריח על ידי מלכה - צריך להעניק ללבן 5 נקודות (ולא 1)
        arbiter.registerMove(gameState, fromWhite, rookPos, false);
        arbiter.advanceSimulation(gameState, 1000);

        assertEquals(5, gameState.getWhiteScore(), "הלבן צריך לקבל בדיוק 5 נקודות על אכילת צריח");
        assertFalse(gameState.isGameOver(), "המשחק לא אמור להסתיים אחרי אכילת צריח");

        // 2. אכילת מלך - מסיים את המשחק מיד! (מלך שווה 0 נקודות)
        board.setPiece(kingPos, blackKing);
        arbiter.registerMove(gameState, rookPos, kingPos, false);
        arbiter.advanceSimulation(gameState, 1000);

        assertTrue(gameState.isGameOver(), "המשחק חייב להסתיים ברגע שהמלך נלכד!");
        assertEquals(5, gameState.getWhiteScore(), "הניקוד צריך להישאר 5 (לכידת מלך שווה 0 נקודות)");
    }

    @Test
    void testJumpCooldownState() {
        // טסט המוודא צינון קצר (SHORT_REST) לאחר ביצוע קפיצה במקום
        Position pos = new Position(2, 2);
        Piece knight = new Piece("wN_1", Piece.Color.WHITE, Piece.Kind.KNIGHT, pos);
        board.setPiece(pos, knight);

        arbiter.registerMove(gameState, pos, pos, true);
        arbiter.advanceSimulation(gameState, 1000);

        // המצב לאחר קפיצה חייב להיות SHORT_REST (ולא COOLDOWN)
        assertEquals(Piece.State.SHORT_REST, knight.getState(), "הכלי צריך להיות במצב SHORT_REST לאחר קפיצה");
    }
}
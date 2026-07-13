package org.example;

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
        Position to = new Position(4, 5);
        Piece knight = new Piece("wN_1", Piece.Color.WHITE, Piece.Kind.KNIGHT, from);
        board.setPiece(from, knight);

        arbiter.registerMove(gameState, from, to, false);

        // קידום זמן (כל משבצת = 1000ms לפי הקוד שלך)
       // gameState.addTime(1000);
        arbiter.advanceSimulation(gameState, 1000);

        assertEquals(knight, board.getPiece(to), "הכלי אמור להגיע ליעד");
        assertNull(board.getPiece(from), "המקום המקורי אמור להתפנות");
        assertEquals(Piece.State.IDLE, knight.getState(), "הכלי אמור לחזור למצב IDLE");
    }

    @Test
    void testLandingWithCapture() {
        // השתמש בשורות אמצעיות כדי לא להפעיל בטעות הכתרה
        Position from = new Position(3, 3);
        Position to = new Position(3, 4);

        Piece attacker = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        Piece victim = new Piece("bP_1", Piece.Color.BLACK, Piece.Kind.PAWN, to);

        board.setPiece(from, attacker);
        board.setPiece(to, victim);

        arbiter.registerMove(gameState, from, to, false);
        arbiter.advanceSimulation(gameState, 1000);

        assertEquals(Piece.State.CAPTURED, victim.getState(), "הכלי היריב אמור להפוך ל-CAPTURED");
        assertEquals(attacker, board.getPiece(to), "הכלי התוקף אמור להיות ביעד");
    }

    @Test
    void testLandingPromotion() {
        // חייל לבן בשורה 1, זז לשורה 0 (הכתרה)
        Position from = new Position(1, 0);
        Position to = new Position(0, 0);
        Piece pawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);

        board.setPiece(from, pawn);

        arbiter.registerMove(gameState, from, to, false);

      //  gameState.addTime(1000);
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
        // gameState.addTime(1000);
        arbiter.advanceSimulation(gameState, 1000);

        Piece pieceOnBoard = board.getPiece(targetPos);
        assertNotNull(pieceOnBoard, "מישהו היה אמור לנחות על המשבצת");

        // במימוש הנוכחי, האחרון שמעובד ברשימה הוא זה שיישאר על הלוח
        System.out.println("The piece that won the collision: " + pieceOnBoard.getId());
    }
}
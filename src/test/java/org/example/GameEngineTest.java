package org.example;

import org.example.engine.GameEngine;
import org.example.models.*;
import org.example.realtime.RealTimeArbiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private Board board;
    private GameState gameState;
    private RealTimeArbiter arbiter;
    private GameEngine engine;

    @BeforeEach
    void setUp() {
        // אתחול לוח 8x8 סטנדרטי
        board = new Board(8, 8);
        gameState = new GameState(board);
        arbiter = new RealTimeArbiter();
        engine = new GameEngine(gameState, arbiter);
    }

    @Test
    void testProcessMoveRequest_ValidMove() {
        // הכנה: הנחת חייל בלוח
        // שנה את ה-to בטסט:
        // בטסט, שנה את נקודת ההתחלה לשורה 2 כדי שיוכל לעלות ל-1
        Position from = new Position(2, 1);
        Position to = new Position(1, 1);
        Piece pawn = new Piece("wP", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, pawn);

        // פעולה: בקשת מהלך
        engine.processMoveRequest(from, to);

        // בדיקה: האם המהלך נכנס ל-ActiveMoves?
        assertFalse(gameState.getActiveMoves().isEmpty(), "המהלך היה אמור להירשם כ-ActiveMove");
        assertEquals(from, gameState.getActiveMoves().get(0).getFrom());
    }

    @Test
    void testProcessMoveRequest_InvalidMove_EmptySource() {
        Position from = new Position(0, 0); // ריק
        Position to = new Position(0, 1);

        engine.processMoveRequest(from, to);

        assertTrue(gameState.getActiveMoves().isEmpty(), "לא ניתן להזיז משבצת ריקה");
    }

    @Test
    void testProcessMoveRequest_MovingPieceAlready() {
        // הכנה: כלי שכבר "זז" (MOVING)
        Position from = new Position(1, 1);
        Position to = new Position(2, 2);
        Piece pawn = new Piece("wP", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        pawn.setState(Piece.State.MOVING); // מגדירים אותו כזז
        board.setPiece(from, pawn);

        engine.processMoveRequest(from, to);

        assertTrue(gameState.getActiveMoves().isEmpty(), "אסור להתחיל תנועה לכלי שכבר זז");
    }

    @Test
    void testProcessJumpRequest_Valid() {
        Position pos = new Position(4, 4);
        Piece knight = new Piece("wN", Piece.Color.WHITE, Piece.Kind.KNIGHT, pos);
        board.setPiece(pos, knight);

        engine.processJumpRequest(pos);

        assertFalse(gameState.getActiveMoves().isEmpty(), "קפיצה הייתה אמורה להירשם");
        assertTrue(gameState.getActiveMoves().get(0).isJump(), "המהלך היה אמור להיות מסומן כקפיצה");
    }

    @Test
    void testAdvanceTime_AffectsGameState() {
        // הבדיקה מוודאת שה-Engine מעביר את הזמן ל-Arbiter וזה אכן משפיע על GameState
        engine.advanceTime(1000);
        assertEquals(1000, gameState.getGameTimeMillis(), "הזמן במשחק היה אמור להתעדכן");
    }

    @Test
    void testGameOver_StopsMoves() {
        gameState.setGameOver(true);
        Position from = new Position(1, 1);
        Position to = new Position(2, 2);
        Piece pawn = new Piece("wP", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, pawn);

        engine.processMoveRequest(from, to);

        assertTrue(gameState.getActiveMoves().isEmpty(), "אסור לבצע מהלכים כשהמשחק נגמר");
    }
}
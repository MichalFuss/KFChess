package org.example;

import org.example.engine.GameEngine;
import org.example.input.GameController;
import org.example.models.*;
import org.example.realtime.RealTimeArbiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    private Board board;
    private GameState gameState;
    private RealTimeArbiter arbiter;
    private GameEngine engine;
    private GameController gameController;

    @BeforeEach
    void setUp() {
        // אתחול לוח 8x8 סטנדרטי ורכיבי המשחק האמיתיים
        board = new Board(8, 8);
        gameState = new GameState(board);
        arbiter = new RealTimeArbiter();
        engine = new GameEngine(gameState, arbiter);

        // הזרקת התלויות לבקר
        gameController = new GameController(board, engine);
    }

    @Test
    void handleClick_WhenGameOver_DoesNothing() {
        // הגדרת מצב: המשחק הסתיים
        gameState.setGameOver(true);

        // הנחת חייל בלוח בשורה 2, עמודה 1
        Position from = new Position(2, 1);
        Piece pawn = new Piece("wP", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, pawn);

        // לחיצה על מיקום החייל: x=150 (עמודה 1), y=250 (שורה 2)
        gameController.handleClick(150, 250);

        // בדיקה: מוודאים שלא התחיל שום מהלך כי המשחק גמור
        assertTrue(gameState.getActiveMoves().isEmpty(), "אסור לבצע מהלכים כשהמשחק נגמר");
    }

    @Test
    void handleClick_WhenOutOfBounds_DoesNothing() {
        // פעולה: לחיצה במיקום מחוץ לגבולות הלוח
        gameController.handleClick(-100, -100);

        // בדיקה: מוודאים שלא נוצר שום מהלך פעיל
        assertTrue(gameState.getActiveMoves().isEmpty(), "לחיצה מחוץ לגבולות הלוח לא אמורה ליצור מהלכים");
    }

    @Test
    void handleClick_ValidFirstAndSecondClick_SendsMoveRequest() {
        // הכנה: נניח חייל לבן בשורה 2, עמודה 1
        Position from = new Position(2, 1);
        Position to = new Position(1, 1); // משבצת יעד: שורה 1, עמודה 1

        Piece pawn = new Piece("wP", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, pawn);

        // לחיצה ראשונה: בחירת הכלי במיקום (שורה 2, עמודה 1) -> פיקסלים: x=150, y=250
        gameController.handleClick(150, 250);

        // לחיצה שנייה: משבצת יעד ריקה (שורה 1, עמודה 1) -> פיקסלים: x=150, y=150
        gameController.handleClick(150, 150);

        // בדיקה: עכשיו המהלך חייב להיקלט בהצלחה ב-ActiveMoves!
        assertFalse(gameState.getActiveMoves().isEmpty(), "הלחיצה השנייה הייתה אמורה להניע את הכלי ולרשום ActiveMove");
    }

    @Test
    void advanceTime_DelegatesToEngine() {
        // הפעלת הזמן דרך הבקר
        gameController.advanceTime(500);

        // בדיקה: מוודאים שהזמן אכן עבר למנוע ומשם ל-GameState
        assertEquals(500, gameState.getGameTimeMillis(), "הבקר היה אמור להאציל את פקודת הזמן למנוע");
    }
}
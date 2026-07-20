package org.example.ui;

import org.example.events.EventBus;
import org.example.input.GameController;
import org.example.models.Board;
import org.example.models.GameState;
import org.example.models.Piece;
import org.example.models.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.JFrame;

import static org.junit.jupiter.api.Assertions.*;

class GameWindowTest {

    private GameState gameState;
    private GameController gameController;
    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        // שינוי ל-false כדי לאפשר יצירת חלונות JFrame של Swing בזמן הבדיקה
        System.setProperty("java.awt.headless", "false");

        // 1. יצירת לוח אמיתי
        Board board = new Board(8, 8);

        // 2. מיקום כלי בדיקה
        Position pos = new Position(3, 3);
        Piece pawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, pos);
        board.setPiece(pos, pawn);

        // 3. יצירת מופע אמיתי של GameState
        gameState = new GameState(board);

        // 4. יצירת ה-GameController שלכם בצורה נקייה ללא Mock
        gameController = new GameController(board, null);

        eventBus = new EventBus();
    }

    @Test
    void testGameWindowProperties() {
        // יצירת מופע של החלון
        GameWindow window = new GameWindow(gameState, gameController,eventBus, Piece.Color.WHITE);

        // בדיקה שהכותרת הוגדרה בדיוק לפי הדרישה
        assertEquals("KFChess - Real Time Chess", window.getTitle(),
                "כותרת החלון חייבת להיות 'KFChess - Real Time Chess'");

        // בדיקה שהמשתמש לא יכול לשנות את גודל החלון באופן חופשי
        assertFalse(window.isResizable(),
                "החלון צריך להיות מוגדר כ-non-resizable");

        // בדיקה שלחיצה על ה-X סוגרת ומסיימת את התהליך כולו
        assertEquals(JFrame.EXIT_ON_CLOSE, window.getDefaultCloseOperation(),
                "פעולת הסגירה הדיפולטיבית חייבת להיות EXIT_ON_CLOSE");

        // בסיום הטסט - נסגור את החלון כדי שלא יישאר פתוח ברקע
        window.dispose();
    }
}
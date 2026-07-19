package org.example.ui;

import org.example.events.EventBus;
import org.example.models.GameSnapshot;
import org.example.models.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.Dimension;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BoardPanelTest {
    private BoardPanel boardPanel;
    private EventBus eventBus; // הוספת משתנה ה-EventBus

    @BeforeEach
    void setUp() {
        System.setProperty("java.awt.headless", "true");
        eventBus = new EventBus(); // יצירת מופע חדש לכל בדיקה
        boardPanel = new BoardPanel(8, 8, eventBus); // הזרקת ה-EventBus
    }

    @Test
    void testGameOverState() {
        // בדיקה שהפאנל מגיב נכון לאירוע סיום משחק
        assertFalse(boardPanel.isGameOver(), "בתחילת המשחק ה-isGameOver צריך להיות false");

        // שידור אירוע סיום
        eventBus.publish(new org.example.events.GameStatusEvent(org.example.events.GameStatusEvent.Status.OVER));

        // הערה: נצטרך להוסיף Getter ל-isGameOver ב-BoardPanel כדי לבדוק את זה
        assertTrue(boardPanel.isGameOver(), "הפאנל צריך לעדכן את מצב ה-isGameOver ל-true בעקבות האירוע");
    }
    @Test
    void testPreferredSizeCalculation() {
        // גודל התא מוגדר כ-100 פיקסלים בלוח
        Dimension expectedSize = new Dimension(8 * BoardPanel.CELL_SIZE, 8 * BoardPanel.CELL_SIZE);

        assertEquals(expectedSize, boardPanel.getPreferredSize(),
                "הגודל המועדף של הפאנל חייב להיות תואם למספר המשבצות כפול גודל המשבצת");
    }

    @Test
    void testDefaultConstructorSize() {
        BoardPanel defaultPanel = new BoardPanel(eventBus);
        Dimension expectedSize = new Dimension(8 * BoardPanel.CELL_SIZE, 8 * BoardPanel.CELL_SIZE);

        assertEquals(expectedSize, defaultPanel.getPreferredSize(),
                "בנאי ברירת המחדל ללא פרמטרים צריך ליצור לוח בגודל דיפולטיבי של 8x8");
    }

    @Test
    void testUpdateSnapshot() {
        // יצירת אובייקט אמיתי של GameSnapshot במקום Mock
        GameSnapshot realSnapshot = new GameSnapshot(
                8,                          // רוחב
                8,                          // גובה
                new java.util.ArrayList<>(), // רשימת כלים ריקה
                new Position(0, 0),         // מיקום מסומן
                false,                      // האם המשחק נגמר
                0L                          // זמן המשחק
        );

        // הבדיקה תרוץ כעת ללא שום שימוש ב-Byte Buddy עבור ה-Snapshot
        assertDoesNotThrow(() -> boardPanel.updateSnapshot(realSnapshot),
                "עדכון ה-Snapshot לא אמור לזרוק חריגה");
    }
}
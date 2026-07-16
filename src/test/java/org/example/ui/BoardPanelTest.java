package org.example.ui;

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

    @BeforeEach
    void setUp() {
        // מניעת שגיאות גרפיות בריצה בסביבות ללא מסך (כמו שרתי בנייה)
        System.setProperty("java.awt.headless", "true");
        boardPanel = new BoardPanel(8, 8);
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
        BoardPanel defaultPanel = new BoardPanel();
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
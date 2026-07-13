package org.example;

import org.example.input.BoardMapper;
import org.example.models.Position;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoardMapperTest {

    @Test
    void testToPosition_TopLeftCorner() {
        // לחיצה בפיקסלים (50, 50) - אמצע המשבצת הראשונה (0,0)
        Position pos = BoardMapper.toPosition(50, 50);

        assertNotNull(pos, "המיקום המוחזר לא אמור להיות null");
        assertEquals(0, pos.getRow(), "השורה (row) עבור y=50 צריכה להיות 0");
        assertEquals(0, pos.getCol(), "העמודה (col) עבור x=50 צריכה להיות 0");
    }

    @Test
    void testToPosition_ArbitrarySquare() {
        // לחיצה בפיקסלים x=350, y=210
        // col = 350 / 100 = 3
        // row = 210 / 100 = 2
        Position pos = BoardMapper.toPosition(350, 210);

        assertNotNull(pos);
        assertEquals(2, pos.getRow(), "השורה צריכה להיות 2 עבור y=210");
        assertEquals(3, pos.getCol(), "העמודה צריכה להיות 3 עבור x=350");
    }

    @Test
    void testToPosition_ExactBoundary() {
        // בדיקת נקודות הגבול המדויקות שבהן הערך מתחלף
        // x=100 -> 100 / 100 = 1 (עמודה 1)
        // y=200 -> 200 / 100 = 2 (שורה 2)
        Position pos = BoardMapper.toPosition(100, 200);

        assertNotNull(pos);
        assertEquals(2, pos.getRow(), "בדיוק ב-y=200 השורה צריכה להתחלף ל-2");
        assertEquals(1, pos.getCol(), "בדיוק ב-x=100 העמודה צריכה להתחלף ל-1");
    }

    @Test
    void testToPosition_ZeroCoordinates() {
        // בדיקה של נקודת האפס המוחלטת (0,0) בתחילת המסך
        Position pos = BoardMapper.toPosition(0, 0);

        assertNotNull(pos);
        assertEquals(0, pos.getRow());
        assertEquals(0, pos.getCol());
    }
}
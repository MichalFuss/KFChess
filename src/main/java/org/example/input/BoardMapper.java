package org.example.input;

import org.example.models.Position;

public class BoardMapper {

    // גודל המשבצת בפיקסלים (למשל 64), מגיע ישירות מהקבוצה שהגדרת בתוך Board
    private static final int CELL_SIZE = 100; // במידה ואין Board.CELL_SIZE, נשתמש בזה.

    /**
     * ממיר קואורדינטות פיקסלים (x, y) מהמסך למיקום לוגי (Position) בלוח.
     * x מייצג את ציר הרוחב (עמודה - col)
     * y מייצג את ציר הגובה (שורה - row)
     */
    public static Position toPosition(int x, int y) {
        // מפותח לפי החלוקה של פיקסלים בגודל המשבצת
        // בקוד שלך נעשה שימוש ב-Board.CELL_SIZE:
        int row = y / CELL_SIZE;
        int col = x / CELL_SIZE;

        return new Position(row, col);
    }
}


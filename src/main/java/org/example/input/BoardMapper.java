package org.example.input;

import org.example.models.Position;
import org.example.ui.BoardPanel; // ייבוא של הפאנל הגרפי

public class BoardMapper {

    /**
     * ממיר קואורדינטות פיקסלים (x, y) מהמסך למיקום לוגי (Position) בלוח.
     */
    public static Position toPosition(int x, int y) {
        // שימוש ישיר בקבוע המרכזי של ה-UI
        int row = y / BoardPanel.CELL_SIZE;
        int col = x / BoardPanel.CELL_SIZE;

        return new Position(row, col);
    }
}
package org.example;

import org.example.models.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void testDynamicDimensions_SmallBoard() {
        // Arrange
        int width = 4;
        int height = 5;

        // Act
        Board board = new Board(width, height);

        // Assert
        assertEquals(width, board.getWidth(), "רוחב הלוח צריך להתאים במדויק");
        assertEquals(height, board.getHeight(), "גובה הלוח צריך להתאים במדויק");
    }

    @Test
    void testDynamicDimensions_LargeBoard() {
        // Arrange
        int width = 16;
        int height = 12;

        // Act
        Board board = new Board(width, height);

        // Assert
        assertEquals(width, board.getWidth());
        assertEquals(height, board.getHeight());
    }

    @Test
    void testIsWithinBounds_DynamicValidPositions() {
        // נבדוק על לוח בגודל משתנה (6x9)
        int width = 6;
        int height = 9;
        Board board = new Board(width, height);

        // בדיקת נקודות הקצה הדינמיות
        assertTrue(board.isWithinBounds(new Position(0, 0)), "פינה שמאלית עליונה (0,0) תמיד חוקית");
        assertTrue(board.isWithinBounds(new Position(height - 1, width - 1)), "פינה ימנית תחתונה חייבת להיות חוקית");
        assertTrue(board.isWithinBounds(new Position(height / 2, width / 2)), "מרכז הלוח חייב להיות חוקי");
    }

    @Test
    void testIsWithinBounds_DynamicInvalidPositions() {
        // נבדוק על לוח בגודל משתנה (7;x4)
        int width = 7;
        int height = 4;
        Board board = new Board(width, height);

        // בדיקות מחוץ לגבולות (שלילי ומעבר למקסימום המחושב)
        assertFalse(board.isWithinBounds(new Position(-1, 0)), "שורה שלילית אינה חוקית");
        assertFalse(board.isWithinBounds(new Position(0, -1)), "עמודה שלילית אינה חוקית");
        assertFalse(board.isWithinBounds(new Position(height, 0)), "שורה השווה לגובה הלוח היא מחוץ לגבולות");
        assertFalse(board.isWithinBounds(new Position(0, width)), "עמודה השווה לרוחב הלוח היא מחוץ לגבולות");
        assertFalse(board.isWithinBounds(new Position(height + 5, width + 5)), "מיקום רחוק מאוד מחוץ ללוח");
    }

    @Test
    void testSetAndGetPiece_DynamicPosition() {
        // נבצע את הבדיקה על לוח בגודל רנדומלי כלשהו, למשל 5x5
        Board board = new Board(5, 5);
        Position targetPos = new Position(3, 2);
        Piece whiteKnight = new Piece("wN_1", Piece.Color.WHITE, Piece.Kind.KNIGHT, targetPos);
        // Piece dummyPiece = new Piece(); // בהנחה שקיים אובייקט כזה

        // Act
        board.setPiece(targetPos, whiteKnight);
        Piece retrievedPiece = board.getPiece(targetPos);

        // Assert
        assertNotNull(retrievedPiece, "המשבצת לא צריכה להיות ריקה");
        assertEquals(whiteKnight, retrievedPiece, "הכלי שנשלף חייב להיות בדיוק אותו כלי שהושם");
    }

    @Test
    void testGetPiece_OutOfBounds_DynamicShouldReturnNull() {
        int width = 3;
        int height = 3;
        Board board = new Board(width, height);

        // ניסיון שליפה ממיקום דינמי מחוץ ללוח (למשל, בדיוק שורה אחת מעבר לקצה)
        Position outOfBoundsPos = new Position(height, width);
        Piece piece = board.getPiece(outOfBoundsPos);

        // Assert
        assertNull(piece, "שליפת כלי מחוץ לגבולות הלוח הדינמיים חייבת להחזיר null");
    }

    @Test
    void testSetPiece_WithNull_ClearsSquareDynamically() {
        Board board = new Board(6, 6);
        Position pos = new Position(4, 4);
        Piece whiteKnight = new Piece("wN_1", Piece.Color.WHITE, Piece.Kind.KNIGHT, pos);

        board.setPiece(pos, whiteKnight);

        // Act - ניקוי המשבצת
        board.setPiece(pos, null);

        // Assert
        assertNull(board.getPiece(pos), "המשבצת צריכה להתרוקן ולחזור להיות null");
    }
    @Test
    void testSetPiece_UpdatesPieceCellRelation() {
        // Arrange
        Board board = new Board(8, 8);
        Position initialPos = new Position(0, 0);
        Position targetPos = new Position(4, 4);
        Piece piece = new Piece("wQ_1", Piece.Color.WHITE, Piece.Kind.QUEEN, initialPos);

        // Act
        board.setPiece(targetPos, piece);

        // Assert
        assertEquals(targetPos, piece.getCell(), "בעת השמת כלי בלוח, המיקום הפנימי של הכלי חייב להתעדכן אוטומטית (עדכון דו-כיווני)");
    }
}
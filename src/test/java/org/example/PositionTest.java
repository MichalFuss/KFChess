package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.example.models.Position;

class PositionTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange & Act
        Position position = new Position(3, 4);

        // Assert
        assertEquals(3, position.getRow(), "השורה צריכה להיות זהה לערך שהוזן בבנאי");
        assertEquals(4, position.getCol(), "העמודה צריכה להיות זהה לערך שהוזן בבנאי");
    }

    @Test
    void testEquals_SameCoordinates() {
        // Arrange
        Position pos1 = new Position(5, 5);
        Position pos2 = new Position(5, 5);

        // Assert & Act
        assertEquals(pos1, pos2, "שני מיקומים עם אותן קואורדינטות חייבים להיות שווים");
    }

    @Test
    void testEquals_DifferentCoordinates() {
        // Arrange
        Position pos1 = new Position(2, 3);
        Position pos2 = new Position(2, 4);
        Position pos3 = new Position(1, 3);

        // Assert & Act
        assertNotEquals(pos1, pos2, "מיקומים עם עמודות שונות לא צריכים להיות שווים");
        assertNotEquals(pos1, pos3, "מיקומים עם שורות שונות לא צריכים להיות שווים");
    }

    @Test
    void testEquals_SpecialCases() {
        // Arrange
        Position pos = new Position(0, 0);

        // Assert & Act
        assertEquals(pos, pos, "אובייקט חייב להיות שווה לעצמו (Reflexive)");
        assertNotEquals(null, pos, "אובייקט לא יכול להיות שווה ל-null");
        assertNotEquals("Not A Position Object", pos, "אובייקט לא יכול להיות שווה לטיפוס אחר");
    }

    @Test
    void testHashCode_SameCoordinates() {
        // Arrange
        Position pos1 = new Position(4, 2);
        Position pos2 = new Position(4, 2);

        // Assert & Act
        assertEquals(pos1.hashCode(), pos2.hashCode(), "לאובייקטים שווים חייב להיות אותו hashCode");
    }
}
package org.example;

import org.example.io.BoardParser;
import org.example.models.Board;
import org.example.models.Piece;
import org.example.models.Position;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardParserTest {

    @Test
    void testParseValidBoard() {
        // לוח תקין עם כלי לבן (צריח) וכלי שחור (רגלי) ומשבצות ריקות
        List<String> boardLines = Arrays.asList(
                "wR .",
                ". bP"
        );

        Board board = BoardParser.parse(boardLines);

        assertNotNull(board);
        assertEquals(2, board.getWidth());
        assertEquals(2, board.getHeight());

        // בדיקת משבצת (0,0) - צריח לבן
        Piece whiteRook = board.getPiece(new Position(0, 0));
        assertNotNull(whiteRook);
        assertEquals(Piece.Color.WHITE, whiteRook.getColor());
        assertEquals(Piece.Kind.ROOK, whiteRook.getKind());

        // בדיקת משבצת (0,1) - ריקה
        assertNull(board.getPiece(new Position(0, 1)));

        // בדיקת משבצת (1,0) - ריקה
        assertNull(board.getPiece(new Position(1, 0)));

        // בדיקת משבצת (1,1) - רגלי שחור
        Piece blackPawn = board.getPiece(new Position(1, 1));
        assertNotNull(blackPawn);
        assertEquals(Piece.Color.BLACK, blackPawn.getColor());
        assertEquals(Piece.Kind.PAWN, blackPawn.getKind());
    }

    @Test
    void testParseEmptyOrNullLinesThrowsException() {
        // מקרה של רשימה ריקה
        assertThrows(IllegalArgumentException.class, () -> {
            BoardParser.parse(new ArrayList<>());
        }, "ERROR ROW_WIDTH_MISMATCH");

        // מקרה של רשימה שהיא null
        assertThrows(IllegalArgumentException.class, () -> {
            BoardParser.parse(null);
        }, "ERROR ROW_WIDTH_MISMATCH");
    }

    @Test
    void testParseRowWidthMismatchThrowsException() {
        // שורות באורך שונה (שורה ראשונה אורך 2, שורה שנייה אורך 3)
        List<String> boardLines = Arrays.asList(
                ". .",
                ". . ."
        );

        assertThrows(IllegalArgumentException.class, () -> {
            BoardParser.parse(boardLines);
        }, "ERROR ROW_WIDTH_MISMATCH");
    }

    @Test
    void testParseUnknownTokenLengthThrowsException() {
        // אסימון (Token) באורך לא תקין (3 תווים במקום 2)
        List<String> boardLines = Arrays.asList(
                "wR .",
                ". wRk"
        );

        assertThrows(IllegalArgumentException.class, () -> {
            BoardParser.parse(boardLines);
        }, "ERROR UNKNOWN_TOKEN");
    }

    @Test
    void testParseUnknownColorThrowsException() {
        // צבע לא מוכר ('x' במקום 'w' או 'b')
        List<String> boardLines = Arrays.asList(
                "xR ."
        );

        assertThrows(IllegalArgumentException.class, () -> {
            BoardParser.parse(boardLines);
        }, "ERROR UNKNOWN_TOKEN");
    }

    @Test
    void testParseUnknownKindThrowsException() {
        // סוג כלי לא מוכר ('X' במקום סוג תקני)
        List<String> boardLines = Arrays.asList(
                "wX ."
        );

        assertThrows(IllegalArgumentException.class, () -> {
            BoardParser.parse(boardLines);
        }, "ERROR UNKNOWN_TOKEN");
    }

    @Test
    void testParseMultipleSpacesBetweenTokens() {
        // בדיקה שהמתודה מתמודדת בהצלחה עם ריווחים מרובים
        List<String> boardLines = Arrays.asList(
                "wK      . ",
                "  .    bQ"
        );

        Board board = BoardParser.parse(boardLines);
        assertNotNull(board);
        assertEquals(2, board.getWidth());
        assertEquals(2, board.getHeight());

        Piece whiteKing = board.getPiece(new Position(0, 0));
        assertNotNull(whiteKing);
        assertEquals(Piece.Kind.KING, whiteKing.getKind());
    }
}
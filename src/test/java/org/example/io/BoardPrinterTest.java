package org.example.io;

import org.example.models.Board;
import org.example.models.Piece;
import org.example.models.Position;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoardPrinterTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStreamCaptor;

    @BeforeEach
    void setUp() {
        // הגדרת זרם פלט חלופי כדי לתפוס את מה שמודפס למסך
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        // החזרת זרם הפלט המקורי של המערכת למקומו
        System.setOut(originalOut);
    }

    @Test
    void testPrintEmptyBoard() {
        // יצירת לוח ריק בגודל 2x3 (רוחב 2, גובה 3)
        Board board = new Board(2, 3);

        BoardPrinter.print(board);

        // מחליפים ירידות שורה של מערכת ההפעלה (\r\n ב-Windows או \n ב-Linux) ל-\n אחיד
        String expectedOutput = ". .\n. .\n. .\n";
        String actualOutput = outputStreamCaptor.toString().replace("\r\n", "\n");

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void testPrintBoardWithPieces() {
        // יצירת לוח בגודל 3x2 (רוחב 3, גובה 2)
        Board board = new Board(3, 2);

        // הנחת כלי במיקומים שונים (בהתבסס על הפורמט של BoardParser)
        // שורה 0, עמודה 0 - מלך לבן
        Position pos1 = new Position(0, 0);
        Piece whiteKing = new Piece("w_K_0_0", Piece.Color.WHITE, Piece.Kind.KING, pos1);
        board.setPiece(pos1, whiteKing);

        // שורה 1, עמודה 2 - רגלי שחור
        Position pos2 = new Position(1, 2);
        Piece blackPawn = new Piece("b_P_1_2", Piece.Color.BLACK, Piece.Kind.PAWN, pos2);
        board.setPiece(pos2, blackPawn);

        // הרצת ההדפסה
        BoardPrinter.print(board);

        // הפלט הצפוי:
        // שורה ראשונה: wK . .
        // שורה שנייה:  . . bP
        String expectedOutput = "wK . .\n. . bP\n";
        String actualOutput = outputStreamCaptor.toString().replace("\r\n", "\n");

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void testPrintSingleCellBoard() {
        // לוח מינימלי בגודל 1x1 המכיל צריח לבן
        Board board = new Board(1, 1);
        Position pos = new Position(0, 0);
        Piece whiteRook = new Piece("w_R_0_0", Piece.Color.WHITE, Piece.Kind.ROOK, pos);
        board.setPiece(pos, whiteRook);

        BoardPrinter.print(board);

        String expectedOutput = "wR\n";
        String actualOutput = outputStreamCaptor.toString().replace("\r\n", "\n");

        assertEquals(expectedOutput, actualOutput);
    }
}
package org.example;

import org.example.engine.GameEngine;
import org.example.input.GameController;
import org.example.models.*;
import org.example.realtime.RealTimeArbiter;
import org.example.ui.GameWindow;

import javax.swing.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        String whitePlayerName = "White" ;
        String blackPlayerName = "Black";

        // 2. הרצת הממשק הגרפי (GUI)
        SwingUtilities.invokeLater(() -> {
            // יצירת לוח סטנדרטי 8X8
            Board board = new Board(8, 8);
            setupStandardChessBoard(board);

            // אתחול ה-GameState ועדכון השמות שהתקבלו
            GameState gameState = new GameState(board);
            gameState.setPlayerNames(whitePlayerName, blackPlayerName);

            // אתחול רכיבי הלוגיקה והזמן האמיתי
            RealTimeArbiter arbiter = new RealTimeArbiter(1000,1000);
            GameEngine gameEngine = new GameEngine(gameState, arbiter);
            GameController gameController = new GameController(board, gameEngine);

            // פתיחת החלון
            new GameWindow(gameState, gameController);
        });
    }

    private static void setupStandardChessBoard(Board board) {
        placeMajorRow(board, 0, Piece.Color.BLACK);
        for (int col = 0; col < 8; col++) {
            Position pos = new Position(1, col);
            board.setPiece(pos, new Piece("bP_" + col, Piece.Color.BLACK, Piece.Kind.PAWN, pos));
        }

        for (int col = 0; col < 8; col++) {
            Position pos = new Position(6, col);
            board.setPiece(pos, new Piece("wP_" + col, Piece.Color.WHITE, Piece.Kind.PAWN, pos));
        }
        placeMajorRow(board, 7, Piece.Color.WHITE);
    }

    private static void placeMajorRow(Board board, int row, Piece.Color color) {
        String prefix = (color == Piece.Color.WHITE) ? "w" : "b";

        board.setPiece(new Position(row, 0), new Piece(prefix + "R1", color, Piece.Kind.ROOK, new Position(row, 0)));
        board.setPiece(new Position(row, 1), new Piece(prefix + "N1", color, Piece.Kind.KNIGHT, new Position(row, 1)));
        board.setPiece(new Position(row, 2), new Piece(prefix + "B1", color, Piece.Kind.BISHOP, new Position(row, 2)));
        board.setPiece(new Position(row, 3), new Piece(prefix + "Q", color, Piece.Kind.QUEEN, new Position(row, 3)));
        board.setPiece(new Position(row, 4), new Piece(prefix + "K", color, Piece.Kind.KING, new Position(row, 4)));
        board.setPiece(new Position(row, 5), new Piece(prefix + "B2", color, Piece.Kind.BISHOP, new Position(row, 5)));
        board.setPiece(new Position(row, 6), new Piece(prefix + "N2", color, Piece.Kind.KNIGHT, new Position(row, 6)));
        board.setPiece(new Position(row, 7), new Piece(prefix + "R2", color, Piece.Kind.ROOK, new Position(row, 7)));
    }
}
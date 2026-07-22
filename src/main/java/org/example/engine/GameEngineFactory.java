package org.example.engine;

import org.example.events.EventBus;
import org.example.models.*;
import org.example.realtime.RealTimeArbiter;

public class GameEngineFactory {

    public static GameEngine createNewGame(EventBus eventBus) {
        Board board = new Board(8, 8);
        setupStandardChessBoard(board);
        GameState gameState = new GameState(board);
        RealTimeArbiter arbiter = new RealTimeArbiter(1000, 1000, eventBus);

        return new GameEngine(gameState, arbiter);
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
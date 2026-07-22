package org.example.input;

import org.example.models.*;
import org.example.server.ChessCommandBuilder;

import java.util.function.Consumer;

public class NetworkGameAdapter implements IGameController {
    private final Board board;
    private final Piece.Color playerColor;
    private final Consumer<String> networkSender;
    private Position selectedPosition;
    private GameSnapshot latestSnapshot;

    public NetworkGameAdapter(Board board, Piece.Color playerColor, Consumer<String> networkSender) {
        this.board = board;
        this.playerColor = playerColor;
        this.networkSender = networkSender;
        this.selectedPosition = null;
    }

    public void updateSnapshot(GameSnapshot snapshot) {
        this.latestSnapshot = new GameSnapshot(
                snapshot.getBoardWidth(),
                snapshot.getBoardHeight(),
                snapshot.getPieces(),
                this.selectedPosition,
                snapshot.isGameOver(),
                snapshot.getGameTimeMillis(),
                snapshot.getWhiteScore(),
                snapshot.getBlackScore(),
                snapshot.getWhiteMoveHistory(),
                snapshot.getBlackMoveHistory()
        );
    }

    @Override
    public void handleClick(int x, int y, Piece.Color color) {
        if (latestSnapshot != null && latestSnapshot.isGameOver()) return;

        Position clickedPos = BoardMapper.toPosition(x, y);
        // שימוש בבדיקת גבולות אחידה ודינמית מתוך הלוח
        if (!board.isWithinBounds(clickedPos)) {
            selectedPosition = null;
            return;
        }

        if (selectedPosition == null) {
            if (latestSnapshot.hasPieceAtPosition(clickedPos, color)) {
                selectedPosition = clickedPos;
            }
        } else {
            if (selectedPosition.equals(clickedPos)) {
                selectedPosition = null;
            } else if (latestSnapshot.hasPieceAtPosition(clickedPos, color)) {
                selectedPosition = clickedPos;
            } else {
                String command = ChessCommandBuilder.formatMove(selectedPosition, clickedPos, color);
                networkSender.accept(command);
                selectedPosition = null;
            }
        }
    }

    @Override
    public void handleJump(int x, int y, Piece.Color color) {
        Position pos = BoardMapper.toPosition(x, y);
        if (!board.isWithinBounds(pos)) return;

        if (latestSnapshot.hasPieceAtPosition(pos, color)) {
            String command = "J" + ChessCommandBuilder.formatMove(pos, pos, color);
            networkSender.accept(command);
            selectedPosition = null;
        }
    }


    @Override
    public Position getSelectedPosition() {
        return this.selectedPosition;
    }

    @Override
    public GameSnapshot getLatestSnapshot() {
        return this.latestSnapshot;
    }

    @Override
    public void advanceTime(long millis) {
        // ניהול הזמן מבוצע בצד השרת במשחק רשת
    }
}
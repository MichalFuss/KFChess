package org.example.input;

import org.example.models.*;
import org.example.engine.GameEngine;

public class GameController implements IGameController {
    private final Board board;
    private final GameEngine gameEngine;
    private Position selectedPosition;

    public GameController(Board board, GameEngine gameEngine) {
        this.board = board;
        this.gameEngine = gameEngine;
        this.selectedPosition = null;
    }

    @Override
    public void handleClick(int x, int y, Piece.Color playerColor) {
        if (gameEngine.getGameState().isGameOver()) return;

        Position clickedPos = BoardMapper.toPosition(x, y);
        if (!board.isWithinBounds(clickedPos)) {
            selectedPosition = null;
            return;
        }

        Piece clickedPiece = board.getPiece(clickedPos);

        if (selectedPosition == null) {
            if (clickedPiece != null && clickedPiece.getColor() == playerColor && !gameEngine.isPieceMovingFrom(clickedPos)) {
                selectedPosition = clickedPos;
            }
        } else {
            if (selectedPosition.equals(clickedPos)) {
                selectedPosition = null;
            } else {
                Piece selectedPiece = board.getPiece(selectedPosition);

                if (clickedPiece != null && selectedPiece != null
                        && clickedPiece.getColor() == selectedPiece.getColor()
                        && !gameEngine.isPieceMovingFrom(clickedPos)) {
                    selectedPosition = clickedPos;
                } else {
                    gameEngine.processMoveRequest(selectedPosition, clickedPos, playerColor);
                    selectedPosition = null;
                }
            }
        }
    }

    @Override
    public void handleJump(int x, int y, Piece.Color playerColor) {
        if (gameEngine.getGameState().isGameOver()) return;

        Position pos = BoardMapper.toPosition(x, y);
        if (!board.isWithinBounds(pos)) return;

        Piece piece = board.getPiece(pos);
        if (piece != null && piece.getColor() == playerColor && !gameEngine.isPieceMovingFrom(pos)) {
            gameEngine.processJumpRequest(pos, playerColor);
            selectedPosition = null;
        }
    }

    @Override
    public void advanceTime(long millis) {
        gameEngine.advanceTime(millis);
    }

    @Override
    public Position getSelectedPosition() {
        return this.selectedPosition;
    }

    @Override
    public GameSnapshot getLatestSnapshot() {
        return gameEngine.createSnapshot(this.selectedPosition);
    }
}
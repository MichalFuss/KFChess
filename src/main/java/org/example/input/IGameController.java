package org.example.input;

import org.example.models.GameSnapshot;
import org.example.models.Piece;
import org.example.models.Position;

public interface IGameController {
    void handleClick(int x, int y, Piece.Color playerColor);
    void handleJump(int x, int y, Piece.Color playerColor);
    void advanceTime(long millis);
    Position getSelectedPosition();
    GameSnapshot getLatestSnapshot();
}
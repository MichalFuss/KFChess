package org.example.server;

import org.example.models.Piece;
import org.example.models.Position;

public class ChessCommandBuilder {
    public static String formatMove(Position from, Position to, Piece.Color color) {
        char colorChar = (color == Piece.Color.WHITE) ? 'W' : 'B';
        String fromAlg = "" + (char)('a' + from.getCol()) + (8 - from.getRow());
        String toAlg = "" + (char)('a' + to.getCol()) + (8 - to.getRow());
        return colorChar + " " + fromAlg + toAlg;
    }
}

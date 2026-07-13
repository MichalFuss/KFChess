package org.example.io;

import org.example.models.Board;
import org.example.models.Piece;
import org.example.models.Position;

public class BoardPrinter {

    public static void print(Board board) {
        int height = board.getHeight();
        int width = board.getWidth();

        for (int r = 0; r < height; r++) {
            StringBuilder rowStr = new StringBuilder();
            for (int c = 0; c < width; c++) {
                Position pos = new Position(r, c);
                Piece piece = board.getPiece(pos);

                if (piece == null) {
                    rowStr.append(".");
                } else {
                    rowStr.append(piece.getColor().getSymbol())
                            .append(piece.getKind().getSymbol());
                }

                if (c < width - 1) {
                    rowStr.append(" ");
                }
            }
            System.out.println(rowStr);
        }
    }
}
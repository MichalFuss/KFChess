package org.example.io;

import org.example.models.Board;
import org.example.models.Piece;
import org.example.models.Position;

import java.util.List;

public class BoardParser {

    // מקבל רשימת שורות טקסט ומחזיר אובייקט Board מותאם למודלים החדשים
    public static Board parse(List<String> boardLines) throws IllegalArgumentException {
        if (boardLines == null || boardLines.isEmpty()) {
            throw new IllegalArgumentException("ERROR ROW_WIDTH_MISMATCH");
        }

        int height = boardLines.size();
        String[] firstRowTokens = boardLines.get(0).trim().split("\\s+");
        int width = firstRowTokens.length;

        Board board = new Board(width, height);

        for (int r = 0; r < height; r++) {
            String[] tokens = boardLines.get(r).trim().split("\\s+");

            if (tokens.length != width) {
                throw new IllegalArgumentException("ERROR ROW_WIDTH_MISMATCH");
            }

            for (int c = 0; c < width; c++) {
                String token = tokens[c];

                // משבצת ריקה
                if (token.equals(".")) {
                    continue;
                }

                if (token.length() != 2) {
                    throw new IllegalArgumentException("ERROR UNKNOWN_TOKEN");
                }

                Piece.Color color = parseColor(token.charAt(0));
                Piece.Kind kind = parseKind(token.charAt(1));

                if (color == null || kind == null) {
                    throw new IllegalArgumentException("ERROR UNKNOWN_TOKEN");
                }

                // יצירת המיקום והכלי בהתאם לדרישות המודלים הנוכחיים
                Position pos = new Position(r, c);
                String id = color.getSymbol() + "_" + kind.getSymbol() + "_" + r + "_" + c;

                Piece piece = new Piece(id, color, kind, pos);
                board.setPiece(pos, piece);
            }
        }

        return board;
    }

    // פונקציית עזר לפענוח הצבע מתוך התו
    private static Piece.Color parseColor(char c) {
        if (c == 'w' || c == 'W') return Piece.Color.WHITE;
        if (c == 'b' || c == 'B') return Piece.Color.BLACK;
        return null;
    }

    // פונקציית עזר לפענוח סוג הכלי מתוך התו (מותאם ל-Piece.Kind)
    private static Piece.Kind parseKind(char c) {
        switch (Character.toUpperCase(c)) {
            case 'K': return Piece.Kind.KING;
            case 'Q': return Piece.Kind.QUEEN;
            case 'R': return Piece.Kind.ROOK;
            case 'N': return Piece.Kind.KNIGHT;
            case 'B': return Piece.Kind.BISHOP;
            case 'P': return Piece.Kind.PAWN;
            default: return null;
        }
    }
}
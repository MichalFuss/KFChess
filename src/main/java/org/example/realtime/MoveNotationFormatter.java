package org.example.realtime;

import org.example.models.*;

public class MoveNotationFormatter {

    /**
     * ממיר מהלך פעיל לרישום שחמט אלגברי תקני.
     */
    public static String format(ActiveMove move, boolean isCapture) {
        Piece piece = move.getPiece();
        String piecePrefix = "";

        switch (piece.getKind()) {
            case KNIGHT:  piecePrefix = "N"; break;
            case BISHOP:  piecePrefix = "B"; break;
            case ROOK:    piecePrefix = "R"; break;
            case QUEEN:   piecePrefix = "Q"; break;
            case KING:    piecePrefix = "K"; break;
            case PAWN:    piecePrefix = "P"; break; // P משמש רק לקפיצת רגלי
        }

        // 1. טיפול במצב של קפיצה
        if (move.isJump()) {
            if (move.getFrom().equals(move.getTo())) {
                return piecePrefix + "-Jump";
            } else {
                String destStr = toAlgebraic(move.getTo());
                return piecePrefix + (isCapture ? "x" : "") + destStr + "^";
            }
        }

        // 2. טיפול במהלכים רגילים
        String destStr = toAlgebraic(move.getTo());
        if (piece.getKind() == Piece.Kind.PAWN) {
            if (isCapture) {
                char sourceFile = (char) ('a' + move.getFrom().getCol());
                return sourceFile + "x" + destStr;
            } else {
                return destStr;
            }
        } else {
            if (isCapture) {
                return piecePrefix + "x" + destStr;
            } else {
                return piecePrefix + destStr;
            }
        }
    }

    private static String toAlgebraic(Position pos) {
        char file = (char) ('a' + pos.getCol());
        int rank = 8 - pos.getRow();
        return "" + file + rank;
    }
}
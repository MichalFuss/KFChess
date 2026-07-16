package org.example.realtime;

import org.example.models.*;
import java.util.List;

public class MoveResolver {

    public static boolean resolveCompletedMoves(GameState gameState,
                                                List<ActiveMove> completedNormalMoves,
                                                List<ActiveMove> completedJumps) {

        if (completedNormalMoves.isEmpty() && completedJumps.isEmpty()) return false;

        Board board = gameState.getBoard();
        boolean kingCaptured = false;

        // 1. לכידות באוויר (שני כלים זזים שנפגשים)
        for (ActiveMove normalMove : completedNormalMoves) {
            boolean capturedInAir = false;
            for (ActiveMove jumpMove : completedJumps) {
                if (jumpMove.getTo().equals(normalMove.getTo()) &&
                        jumpMove.getPiece().getColor() != normalMove.getPiece().getColor()) {

                    performCapture(gameState, normalMove, jumpMove.getPiece());
                    capturedInAir = true;
                    if (normalMove.getPiece().getKind() == Piece.Kind.KING) kingCaptured = true;
                    break;
                }
            }
            if (!capturedInAir && executeLanding(board, normalMove, gameState)) kingCaptured = true;
        }

        // 2. נחיתת קפיצות
        for (ActiveMove jumpMove : completedJumps) {
            if (executeLanding(board, jumpMove, gameState)) kingCaptured = true;
        }

        if (kingCaptured) gameState.setGameOver(true);
        return kingCaptured;
    }

    private static void performCapture(GameState gameState, ActiveMove victimMove, Piece attacker) {
        Piece victim = victimMove.getPiece();
        victim.setState(Piece.State.CAPTURED);

        int val = getPieceValue(victim.getKind());
        if (attacker.getColor() == Piece.Color.WHITE) gameState.addWhiteScore(val);
        else gameState.addBlackScore(val);

        String timeStr = String.valueOf(gameState.getGameTimeMillis());
        MoveLogEntry log = new MoveLogEntry(timeStr, MoveNotationFormatter.format(victimMove, true));
        if (attacker.getColor() == Piece.Color.WHITE) gameState.addWhiteMove(log);
        else gameState.addBlackMove(log);

        gameState.getBoard().setPiece(victimMove.getFrom(), null);
    }

    private static boolean executeLanding(Board board, ActiveMove move, GameState gameState) {
        Piece piece = move.getPiece();
        if (piece.getState() == Piece.State.CAPTURED) return false;

        // שומרים את הכלי שהיה ביעד לפני הנחיתה
        Piece targetPiece = board.getPiece(move.getTo());
        boolean isCapture = false;

        // בדיקה: האם יש שם כלי, הוא לא אנחנו (קפיצה במקום), והוא של היריב?
        if (targetPiece != null && targetPiece != piece && targetPiece.getColor() != piece.getColor()) {
            isCapture = true;
            targetPiece.setState(Piece.State.CAPTURED); // הפיכת הכלי הנאכל לשבוי

            // עדכון הניקוד בזמן אמת!
            int val = getPieceValue(targetPiece.getKind());
            if (piece.getColor() == Piece.Color.WHITE) {
                gameState.addWhiteScore(val);
            } else {
                gameState.addBlackScore(val);
            }
        }

        // הזזת הכלי פיזית בלוח
        if (!move.isJump()) board.setPiece(move.getFrom(), null);
        board.setPiece(move.getTo(), piece);
        piece.setState(Piece.State.IDLE);
        piece.setCooldown(3000, gameState.getGameTimeMillis());

        // רישום המהלך בלוג (עם isCapture המעודכן בשביל ה-'x')
        MoveLogEntry log = new MoveLogEntry(String.valueOf(gameState.getGameTimeMillis()),
                MoveNotationFormatter.format(move, isCapture));
        if (piece.getColor() == Piece.Color.WHITE) gameState.addWhiteMove(log);
        else gameState.addBlackMove(log);

        // הכתרה
        if (piece.getKind() == Piece.Kind.PAWN && isPromotionRow(move.getTo(), piece.getColor(), board)) {
            board.setPiece(move.getTo(), new Piece(piece.getId() + "_Q", piece.getColor(), Piece.Kind.QUEEN, move.getTo()));
        }

        // המשחק נגמר רק אם דרסנו את המלך של היריב
        return targetPiece != null && targetPiece != piece && targetPiece.getKind() == Piece.Kind.KING;
    }

    private static boolean isPromotionRow(Position pos, Piece.Color color, Board board) {
        return (color == Piece.Color.WHITE && pos.getRow() == 0) ||
                (color == Piece.Color.BLACK && pos.getRow() == board.getHeight() - 1);
    }

    private static int getPieceValue(Piece.Kind kind) {
        switch (kind) {
            case PAWN: return 1;
            case KNIGHT: return 3;
            case BISHOP: return 3;
            case ROOK: return 5;
            case QUEEN: return 9;
            case KING: return 1000;
            default: return 0;
        }
    }
}
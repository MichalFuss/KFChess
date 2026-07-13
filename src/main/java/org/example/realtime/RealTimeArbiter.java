package org.example.realtime;

import org.example.models.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RealTimeArbiter {
    private static final long MOVE_DURATION_PER_SQUARE = 1000;
    private static final long JUMP_DURATION = 1000;

    public void registerMove(GameState gameState, Position from, Position to, boolean isJump) {
        Board board = gameState.getBoard();
        Piece piece = board.getPiece(from);
        if (piece == null) return;

        long duration;
        if (isJump) {
            duration = JUMP_DURATION;
            piece.setState(Piece.State.JUMPING);
        } else {
            int distance = Math.max(Math.abs(to.getRow() - from.getRow()), Math.abs(to.getCol() - from.getCol()));
            duration = distance * MOVE_DURATION_PER_SQUARE;
            piece.setState(Piece.State.MOVING);
        }

        long arrivalTimeMillis = gameState.getGameTimeMillis() + duration;
        ActiveMove activeMove = new ActiveMove(from, to, piece, arrivalTimeMillis, isJump);
        gameState.addActiveMove(activeMove);
    }

    public boolean advanceSimulation(GameState gameState, long millisDelta) {
        if (gameState.isGameOver()) return false;
        if (millisDelta <= 0) return false;

        gameState.addTime(millisDelta);
        return resolveCompletedMoves(gameState);
    }

    public boolean resolveCompletedMoves(GameState gameState) {
        Board board = gameState.getBoard();
        long currentTime = gameState.getGameTimeMillis();

        List<ActiveMove> completedNormalMoves = new ArrayList<>();
        List<ActiveMove> completedJumps = new ArrayList<>();

        // 1. הפרדת המהלכים שהסתיימו לפי סוג (תנועה רגילה מול קפיצה)
        Iterator<ActiveMove> iterator = gameState.getActiveMoves().iterator();
        while (iterator.hasNext()) {
            ActiveMove move = iterator.next();
            if (move.isComplete(currentTime)) {
                if (move.isJump()) {
                    completedJumps.add(move);
                } else {
                    completedNormalMoves.add(move);
                }
                iterator.remove();
            }
        }

        if (completedNormalMoves.isEmpty() && completedJumps.isEmpty()) {
            return false;
        }

        boolean kingCaptured = false;


        // אם כלי רגיל מגיע למשבצת שבה כלי אויב מבצע קפיצה (Jump) באותו זמן - הכלי הקופץ לוכד אותו באוויר!
        for (ActiveMove normalMove : completedNormalMoves) {
            boolean capturedInAir = false;
            for (ActiveMove jumpMove : completedJumps) {
                if (jumpMove.getTo().equals(normalMove.getTo()) &&
                        jumpMove.getPiece().getColor() != normalMove.getPiece().getColor()) {

                    capturedInAir = true;
                    Piece normalPiece = normalMove.getPiece();
                    normalPiece.setState(Piece.State.CAPTURED);

                    // כיוון שהכלי הרגיל נלכד באוויר, נמחק אותו ממקום המוצא שלו והוא לא יגיע ליעד
                    board.setPiece(normalMove.getFrom(), null);

                    if (normalPiece.getKind() == Piece.Kind.KING) {
                        kingCaptured = true;
                    }
                    break;
                }
            }

            if (capturedInAir) continue;

            // אם הוא לא נלכד באוויר, הוא מבצע נחיתה רגילה
            if (executeLanding(board, normalMove)) {
                kingCaptured = true;
            }
        }

        // 3. ביצע נחיתה לכל הקפיצות (הכלים שקפצו וניצלו/לכדו באוויר)
        for (ActiveMove jumpMove : completedJumps) {
            if (executeLanding(board, jumpMove)) {
                kingCaptured = true;
            }
        }

        if (kingCaptured) {
            gameState.setGameOver(true);
        }

        return kingCaptured;
    }

    private boolean executeLanding(Board board, ActiveMove move) {
        Piece movingPiece = move.getPiece();
        if (movingPiece.getState() == Piece.State.CAPTURED) return false;

        Position from = move.getFrom();
        Position to = move.getTo();
        boolean kingCaptured = false;

        // הסרת הכלי ממיקומו הקודם (רק אם זז למשבצת אחרת)
        if (!move.isJump()) {
            board.setPiece(from, null);
        }

        // בדיקת לכידה סטטית על הלוח
        Piece targetPiece = board.getPiece(to);
       

        if (targetPiece != null && targetPiece != movingPiece) {
            if (targetPiece.getColor() != movingPiece.getColor()) {
                targetPiece.setState(Piece.State.CAPTURED);
                if (targetPiece.getKind() == Piece.Kind.KING) {
                    kingCaptured = true;
                }
            }
        }

        board.setPiece(to, movingPiece);
        movingPiece.setState(Piece.State.IDLE);

        // מנגנון הכתרה (Promotion)
        if (movingPiece.getKind() == Piece.Kind.PAWN) {
            int targetRow = to.getRow();
            boolean isWhitePromotion = (movingPiece.getColor() == Piece.Color.WHITE && targetRow == 0);
            boolean isBlackPromotion = (movingPiece.getColor() == Piece.Color.BLACK && targetRow == board.getHeight() - 1);

            if (isWhitePromotion || isBlackPromotion) {
                String queenId = movingPiece.getColor().getSymbol() + "_Q_promoted_" + (System.currentTimeMillis() % 1000);
                Piece queen = new Piece(queenId, movingPiece.getColor(), Piece.Kind.QUEEN, to);
                board.setPiece(to, queen);
            }
        }

        return kingCaptured;
    }
}
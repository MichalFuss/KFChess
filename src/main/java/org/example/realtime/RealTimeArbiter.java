package org.example.realtime;

import org.example.models.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RealTimeArbiter {
    private static  long MOVE_DURATION_PER_SQUARE ;
    private static  long JUMP_DURATION ;

    public RealTimeArbiter (){
        this(1000,1000);
    }
    public RealTimeArbiter(long moveDurationPerSquare, long jumpDuration) {
        MOVE_DURATION_PER_SQUARE = moveDurationPerSquare;
        JUMP_DURATION = jumpDuration;
    }

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
        long currentTime = gameState.getGameTimeMillis();

        List<ActiveMove> completedNormalMoves = new ArrayList<>();
        List<ActiveMove> completedJumps = new ArrayList<>();

        Iterator<ActiveMove> iterator = gameState.getActiveMoves().iterator();
        while (iterator.hasNext()) {
            ActiveMove move = iterator.next();

            // המהלך הגיע ליעדו
            if (move.isComplete(currentTime)) {
                if (move.isJump()) {
                    completedJumps.add(move);
                } else {
                    completedNormalMoves.add(move);
                }
                iterator.remove(); // הסרה מרשימת הכלים שבאוויר
            }
        }
        // הוסף את הקוד הזה בתוך מתודת advanceSimulation, אחרי שאתה מטפל במהלכים שהסתיימו:

        Board board = gameState.getBoard();
        long currentTime2 = gameState.getGameTimeMillis();

        for (int r = 0; r < board.getHeight(); r++) {
            for (int c = 0; c < board.getWidth(); c++) {
                Piece p = board.getPiece(new Position(r, c));
                // אם הכלי במצב COOLDOWN והזמן עבר - החזר אותו ל-IDLE
                // שחרור כלים ממנוחה קצרה או ארוכה בחזרה למצב IDLE
                if (p != null && (p.getState() == Piece.State.SHORT_REST || p.getState() == Piece.State.LONG_REST)) {
                    if (currentTime2 >= p.getCooldownEndTime()) {
                        p.setState(Piece.State.IDLE);
                    }
                }
            }
        }

        // האצלת פתרון הלוגיקה, הנחיתה והאכילות למחלקה הייעודית
        return MoveResolver.resolveCompletedMoves(gameState, completedNormalMoves, completedJumps);
    }
}
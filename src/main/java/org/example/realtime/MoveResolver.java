package org.example.realtime;

import org.example.models.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MoveResolver {

    public static boolean resolveCompletedMoves(GameState gameState,
                                                List<ActiveMove> completedNormalMoves,
                                                List<ActiveMove> completedJumps) {

        // 1. איחוד כל המהלכים לרשימה אחת
        List<ActiveMove> allMoves = new ArrayList<>(completedNormalMoves);
        allMoves.addAll(completedJumps);

        // 2. מיון כרונולוגי - מי שמגיע אחרון קובע מה קורה בסוף!
        allMoves.sort(Comparator.comparingLong(ActiveMove::getArrivalTimeMillis));

        Board board = gameState.getBoard();
        boolean kingCaptured = false;

        // 3. עיבוד לפי הסדר הכרונולוגי
        for (ActiveMove move : allMoves) {
            // אם הכלי שנוחת כבר נאכל (למשל נחת עליו כלי אחר בזמן רגיל), הוא לא יכול לנחות
            if (move.getPiece().getState() == Piece.State.CAPTURED) continue;

            if (executeLanding(board, move, gameState)) {
                kingCaptured = true;
            }
        }

        if (kingCaptured) gameState.setGameOver(true);
        return kingCaptured;
    }

    private static boolean executeLanding(Board board, ActiveMove move, GameState gameState) {
        Piece piece = move.getPiece();

        // הגנה: כלי מת לא נוחת
        if (piece.getState() == Piece.State.CAPTURED) return false;

        Position targetPos = move.getTo();
        Piece targetPiece = board.getPiece(targetPos);
        boolean isCapture = false;

        // --- לוגיקת המפגש באוויר וההתחמקות ---
        if (targetPiece != null && targetPiece != piece) {

            // אם הכלי במשבצת נמצא כרגע בקפיצה (באוויר)
            if (targetPiece.getState() == Piece.State.JUMPING) {
                // הכלי שלנו נוחת עכשיו במשבצת מתחתיו, אבל לא יכול לאכול אותו כי הוא באוויר!
                // אז אנחנו פשוט מניחים isCapture = false
                isCapture = false;
            }
            // אחרת, אם זה כלי רגיל של האויב (צבע שונה)
            else if (targetPiece.getColor() != piece.getColor()) {
                isCapture = true;
                targetPiece.setState(Piece.State.CAPTURED);

                int val = getPieceValue(targetPiece.getKind());
                if (piece.getColor() == Piece.Color.WHITE) gameState.addWhiteScore(val);
                else gameState.addBlackScore(val);
            }
        }

        // --- סיום המהלך והנחיתה ---
        // מוחקים את הכלי מהמשבצת הקודמת רק אם זה לא מהלך של קפיצה במקום
        if (!move.isJump()) {
            board.setPiece(move.getFrom(), null);
        }

        // הכלי תמיד "דורס" ותופס את המשבצת (כי הוא נחת אחרון)
        board.setPiece(targetPos, piece);

        // החזרת הסטטוס והפעלת ה-Cooldown המקורי שהשמטתי!
        piece.setState(Piece.State.IDLE);
        if (move.isJump()) {
            // מנוחה קצרה לאחר קפיצה (למשל: 2000 מילישניות או הזמן המקורי שלך)
            piece.setCooldown(1000, gameState.getGameTimeMillis(), Piece.State.SHORT_REST);
        } else {
            // מנוחה ארוכה לאחר הליכה רגילה (למשל: 3000 מילישניות)
            piece.setCooldown(2000, gameState.getGameTimeMillis(), Piece.State.LONG_REST);
        }
        // רישום המהלך
        MoveLogEntry log = new MoveLogEntry(String.valueOf(gameState.getGameTimeMillis()),
                MoveNotationFormatter.format(move, isCapture));
        if (piece.getColor() == Piece.Color.WHITE) gameState.addWhiteMove(log);
        else gameState.addBlackMove(log);

        // הכתרה
        if (piece.getKind() == Piece.Kind.PAWN && isPromotionRow(move.getTo(), piece.getColor(), board)) {
            board.setPiece(move.getTo(), new Piece(piece.getId() + "_Q", piece.getColor(), Piece.Kind.QUEEN, move.getTo()));
        }

        // המשחק נעצר *רק* אם בוצעה אכילה של מלך! (זה מה שגרם למשחק לקפוא קודם)
        return isCapture && targetPiece != null && targetPiece.getKind() == Piece.Kind.KING;
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
            case KING: return 0;
            default: return 0;
        }
    }
}
package org.example.engine;

import org.example.io.BoardPrinter;
import org.example.models.*;
import org.example.realtime.RealTimeArbiter;
import org.example.rules.*;
import java.util.List;

public class GameEngine {
    private final GameState gameState;
    private final RealTimeArbiter realTimeArbiter;
    private final RuleEngine ruleEngine;

    public GameEngine(GameState gameState, RealTimeArbiter realTimeArbiter) {
        this.gameState = gameState;
        this.realTimeArbiter = realTimeArbiter;
        this.ruleEngine = new RuleEngine();
    }

    /**
     * בודק האם מיקום מסוים חוקי ונמצא בתוך גבולות הלוח
     */
    public boolean isPositionValidOnBoard(Position pos) {
        return gameState.getBoard().isWithinBounds(pos);
    }

    /**
     * בודק האם קיימת חתיכת משחק פיזית במיקום המבוקש
     */
    public boolean hasPieceAt(Position pos) {
        return gameState.getBoard().getPiece(pos) != null;
    }

    /**
     * מנגנון האצלת סמכויות מרכזי המאמת האם מהלך מבוקש הוא חוקי לחלוטין.
     * מואצל ישירות ל-RuleEngine שמפעיל את החוק הספציפי לכל כלי.
     */
    public MoveValidationResult validateMove(Position from, Position to, boolean isJump) {
        if (gameState.isGameOver()) return MoveValidationResult.GAME_OVER;

        Board board = gameState.getBoard();
        Piece piece = board.getPiece(from);

        if (piece == null) return MoveValidationResult.NO_PIECE_AT_SOURCE;

        if (piece.getState() == Piece.State.MOVING || piece.getState() == Piece.State.JUMPING) {
            return MoveValidationResult.PIECE_IN_MOTION;
        }
// הוספת הבדיקה כאן: חסימת כלי שנמצא במנוחה קצרה או ארוכה
        if (piece.getState() == Piece.State.SHORT_REST || piece.getState() == Piece.State.LONG_REST) {
            return MoveValidationResult.PIECE_IN_REST;
        }
        // קפיצה במקום
        if (isJump && from.equals(to)) {
            return MoveValidationResult.VALID;
        }
        // אם זה מהלך jump רגיל של פרש (לא במקום, אלא דילוג מעל כלים ליעד אחר)
        if (isJump && piece.getKind() != Piece.Kind.KNIGHT) {
            return MoveValidationResult.INVALID_JUMP;
        }

        List<ActiveMove> activeMoves = gameState.getActiveMoves();

        return ruleEngine.validateMove(from, to, piece, board, activeMoves);
    }


    /**
     * מטפל בבקשת מהלך רגיל (Click-to-Click).
     * אם המהלך עובר אימות בהצלחה, הוא מוזנק פיזית לאוויר.
     */
    // שינוי: הוספת Piece.Color playerColor לחתימה
    public void processMoveRequest(Position from, Position to, Piece.Color playerColor) {
        Piece piece = gameState.getBoard().getPiece(from);

        // שינוי: חסימה הרמטית - אם אין כלי או שהכלי לא שייך לשחקן שביקש את המהלך
        if (piece == null || piece.getColor() != playerColor) {
            return;
        }

        MoveValidationResult result = this.validateMove(from, to, false);
        if (result == MoveValidationResult.VALID) {
            realTimeArbiter.registerMove(gameState, from, to, false);
        }
    }

    /**
     * מטפל בבקשת קפיצה (Jump Command).
     * קפיצה היא מהלך טקטי מיידי לטווח קצר, בדרך כלל לצורך התחמקות או תפיסה מהירה.
     */
    // שינוי: הוספת Piece.Color playerColor לחתימה
    public void processJumpRequest(Position targetPos, Piece.Color playerColor) {
        Board board = gameState.getBoard();
        Piece piece = board.getPiece(targetPos);

        // שינוי: חסימה הרמטית גם בקפיצות
        if (piece == null || piece.getColor() != playerColor) return;

        Position destination = new Position(targetPos.getRow(), targetPos.getCol());

        MoveValidationResult result = validateMove(targetPos, destination, true);
        if (result == MoveValidationResult.VALID) {
            realTimeArbiter.registerMove(gameState, targetPos, destination, true);
        }
    }


    /**
     * מקבל פקודת המתנה (Wait) ומאציל אותה ישירות לסימולציית הזמן האמיתי.
     * בודק באופן אוטומטי האם אירוע זה גרם לסיום המשחק (לכידת מלך).
     */
    public void advanceTime(long millisDelta) {
        if (millisDelta <= 0) return;

        // האצלת הקידום לבורר הפיזיקלי
        boolean kingCaptured = realTimeArbiter.advanceSimulation(gameState, millisDelta);

        if (kingCaptured) {
            gameState.setGameOver(true);
        }
    }

    /**
     * מפיק Snapshot ויזואלי ומדויק של הלוח הנוכחי ומדפיס אותו לקונסול.
     * מתאים בדיוק לפורמט הנדרש על ידי פלטפורמת ה-VPL / הבודק האוטומטי.
     */
    public void printBoardSnapshot() {
        BoardPrinter.print(gameState.getBoard());
    }


    /**
     * מחזיר צילום מצב טקסטואלי/לוגי מורחב של המשחק (שימושי לדיבאג)
     */
    public String getGameStatusSnapshot() {
        return String.format("Time: %d ms | Active Moves Air: %d | Game Over: %b",
                gameState.getGameTimeMillis(),
                gameState.getActiveMoves().size(),
                gameState.isGameOver()
        );
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public boolean isPieceMovingFrom(Position pos) {
        for (ActiveMove move : gameState.getActiveMoves()) {
            if (move.getFrom().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    public ActiveMove findThreateningMove(Position pos, Piece.Color friendlyColor) {
        for (ActiveMove move : gameState.getActiveMoves()) {
            // אם המהלך מיועד להגיע למשבצת שלנו והוא שייך לצבע הנגדי (אויב)
            if (move.getTo().equals(pos) && move.getPiece().getColor() != friendlyColor) {
                return move;
            }
        }
        return null;
    }

    /**
     * מייצר תמונת מצב ויזואלית (GameSnapshot) לקריאה בלבד עבור רכיב הציור.
     * מתקבל מיקום המשבצת המסומנת כעת ב-Controller כדי להציג את ה-Highlight הצהוב.
     */
    public GameSnapshot createSnapshot(Position selectedPosition) {
        int cellSize = 100;

        java.util.List<PieceSnapshot> pieceSnapshots = new java.util.ArrayList<>();
        Board board = gameState.getBoard();
        long currentTime = gameState.getGameTimeMillis();

        // 1. איסוף כל הכלים הנייחים מהלוח (IDLE, SHORT_REST, LONG_REST)
        for (int r = 0; r < board.getHeight(); r++) {
            for (int c = 0; c < board.getWidth(); c++) {
                Position pos = new Position(r, c);
                Piece piece = board.getPiece(pos);

                // אנחנו מציירים מכאן רק כלים שהם סטטיים על הלוח.
                // התיקון המעודכן: מאפשרים לצייר כלים ב-IDLE וכן כלים בשני סוגי המנוחה (SHORT_REST, LONG_REST)
                if (piece != null && (piece.getState() == Piece.State.IDLE
                        || piece.getState() == Piece.State.SHORT_REST
                        || piece.getState() == Piece.State.LONG_REST)) {
                    double pixelX = c * cellSize;
                    double pixelY = r * cellSize;

                    pieceSnapshots.add(new PieceSnapshot(
                            piece.getId(),
                            piece.getKind(),
                            piece.getColor(),
                            piece.getState(),
                            pixelX,
                            pixelY,
                            piece.getCooldownEndTime()
                    ));
                }
            }
        }

        // 2. חישוב מיקומים דינמיים עבור כלים שבתנועה (Active Moves)
        for (ActiveMove move : gameState.getActiveMoves()) {
            Piece piece = move.getPiece();

            // שחזור ה-duration המקורי של המהלך
            long duration;
            if (move.isJump()) {
                duration = 1000; // קבוע של שנייה אחת לקפיצה
            } else {
                int distance = Math.max(
                        Math.abs(move.getTo().getRow() - move.getFrom().getRow()),
                        Math.abs(move.getTo().getCol() - move.getFrom().getCol())
                );
                duration = distance * 1000; // שנייה אחת לכל משבצת מרחק
            }

            // חישוב אחוז התקדמות האנימציה (נע בין 0.0 ל-1.0)
            long startTime = move.getArrivalTimeMillis() - duration;
            long elapsed = currentTime - startTime;
            double progress = (double) elapsed / duration;
            if (progress > 1.0) progress = 1.0;
            if (progress < 0.0) progress = 0.0;

            // נקודות ההתחלה והסוף בפיקסלים
            double startX = move.getFrom().getCol() * cellSize;
            double startY = move.getFrom().getRow() * cellSize;
            double endX = move.getTo().getCol() * cellSize;
            double endY = move.getTo().getRow() * cellSize;

            double currentX;
            double currentY;

            if (move.isJump()) {
                // אנימציית קפיצה במקום
                currentX = startX;
                double jumpHeight = cellSize * 0.5;
                double arc = 4 * progress * (1 - progress);
                currentY = startY - (jumpHeight * arc);
            } else {
                // אנימציית תנועה רגילה
                currentX = startX + (endX - startX) * progress;
                currentY = startY + (endY - startY) * progress;
            }

            // הוספת הכלי שבאוויר לרשימת הציור
            pieceSnapshots.add(new PieceSnapshot(
                    piece.getId(),
                    piece.getKind(),
                    piece.getColor(),
                    piece.getState(),
                    currentX,
                    currentY,
                    piece.getCooldownEndTime()
            ));
        }

        // 3. אריזת הכל לתוך ה-GameSnapshot הכולל והחזרתו
        return new GameSnapshot(
                board.getWidth(),
                board.getHeight(),
                pieceSnapshots,
                selectedPosition,
                gameState.isGameOver(),
                currentTime,
                gameState.getWhiteScore(), // תוספת: משדרים את הניקוד
                gameState.getBlackScore(),  // תוספת: משדרים את הניקוד
                gameState.getWhiteMoves(),   // תוספת: משדרים את היסטוריית המהלכים של הלבן
                gameState.getBlackMoves()    // תוספת: משדרים את היסטוריית המהלכים של השחור
        );
    }

    
}
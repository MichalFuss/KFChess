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
    public boolean validateMove(Position from, Position to, boolean isJump) {
        if (gameState.isGameOver()) return false;

        Board board = gameState.getBoard();
        Piece piece = board.getPiece(from);

        if (piece == null) return false;

        if (piece.getState() == Piece.State.MOVING || piece.getState() == Piece.State.JUMPING) {
            return false;
        }
        //קפיצה במקום
        if (isJump && from.equals(to)) {
            return true;
        }

        // אם זה מהלך jump רגיל של פרש (לא במקום, אלא דילוג מעל כלים ליעד אחר)
        if (isJump && piece.getKind() != Piece.Kind.KNIGHT) {
            return false;
        }

        List<ActiveMove> activeMoves = gameState.getActiveMoves();

        return ruleEngine.validateMove(from, to, piece, board, activeMoves);
    }


    /**
     * מטפל בבקשת מהלך רגיל (Click-to-Click).
     * אם המהלך עובר אימות בהצלחה, הוא מוזנק פיזית לאוויר.
     */
    public void processMoveRequest(Position from, Position to) {
        // 1. שליפת הכלי מהלוח (קריטי!)
        Piece piece = gameState.getBoard().getPiece(from);

        // 2. בדיקת תקינות בסיסית: האם יש כלי במקום ממנו מנסים לזוז?
        if (piece == null) {
            return;
        }

        // 3. קריאה ל-RuleEngine עם כל הפרמטרים שהוא דורש
        if (ruleEngine.validateMove(from, to, piece, gameState.getBoard(), gameState.getActiveMoves())) {

            // 4. אם המהלך תקין - רושמים אותו ב-Arbiter
            realTimeArbiter.registerMove(gameState, from, to, false);
        } else {
            // אם הגענו לכאן, ה-RuleEngine פסל את המהלך
        }
    }

    /**
     * מטפל בבקשת קפיצה (Jump Command).
     * קפיצה היא מהלך טקטי מיידי לטווח קצר, בדרך כלל לצורך התחמקות או תפיסה מהירה.
     */
    public void processJumpRequest(Position targetPos) {
        Board board = gameState.getBoard();
        Piece piece = board.getPiece(targetPos);

        // בקשת קפיצה מופעלת על כלי קיים כדי להקפיץ אותו
        if (piece == null) return;

        // לצורך המימוש נניח שהיעד מחושב או שהכלי פשוט מבצע קפיצה טקטית למשבצת הנוכחית/סמוכה
        Position destination = new Position(targetPos.getRow(), targetPos.getCol());

        if (validateMove(targetPos, destination, true)) {
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
}
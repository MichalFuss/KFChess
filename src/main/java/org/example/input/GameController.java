package org.example.input;

import org.example.models.Board;
import org.example.models.Piece;
import org.example.models.Position;
import org.example.models.ActiveMove;
import org.example.engine.GameEngine;

public class GameController {
    private final Board board;
    private final GameEngine gameEngine; // החלפת MovementManager ב-GameEngine
    private Position selectedPosition;

    public GameController(Board board, GameEngine gameEngine) {
        this.board = board;
        this.gameEngine = gameEngine;
        this.selectedPosition = null;
    }

    public void handleClick(int x, int y) {
        if (gameEngine.getGameState().isGameOver()) return;

        Position clickedPos = BoardMapper.toPosition(x, y);

        // --- טיפול בלחיצה מחוץ לגבולות הלוח ---
        if (!board.isWithinBounds(clickedPos)) {
            selectedPosition = null; // ביטול הבחירה הנוכחית במידה והייתה
            return;
        }

        Piece clickedPiece = board.getPiece(clickedPos);

        if (selectedPosition == null) {
            // --- לחיצה ראשונה: בחירת כלי ---
            // מוודא שיש שם כלי ושהוא לא נמצא כרגע בתנועה באוויר
            if (clickedPiece != null && !gameEngine.isPieceMovingFrom(clickedPos)) {
                selectedPosition = clickedPos;
            }
        } else {
            // --- לחיצה שנייה: יעד, ביטול או החלפת כלי ---

            if (selectedPosition.equals(clickedPos)) {
                // לחיצה חוזרת על אותו הכלי בדיוק -> ביטול בחירה
                selectedPosition = null;
            }
            else {
                Piece selectedPiece = board.getPiece(selectedPosition);

                // בדיקה האם השחקן התחרט ולחץ על כלי אחר שלו (מאותו הצבע) והכלי החדש לא זז
                if (clickedPiece != null && selectedPiece != null
                        && clickedPiece.getColor() == selectedPiece.getColor()
                        && !gameEngine.isPieceMovingFrom(clickedPos)) {

                    selectedPosition = clickedPos; // החלפת הכלי הנבחר לכלי החדש
                }
                else {
                    // לחיצה על משבצת ריקה או על כלי של היריב -> שליחת בקשת המהלך למנוע
                    gameEngine.processMoveRequest(selectedPosition, clickedPos);
                    selectedPosition = null; // איפוס הבחירה לאחר שליחת הבקשה
                }
            }
        }
    }

    public void handleJump(int x, int y) {
        if (gameEngine.getGameState().isGameOver()) return;

        Position pos = BoardMapper.toPosition(x, y);
        if (!board.isWithinBounds(pos)) return;

        Piece piece = board.getPiece(pos);
        // אם יש כלי במשבצת והוא לא זז כרגע אופקית, הוא רשאי לקפוץ במקום!
        if (piece != null && !gameEngine.isPieceMovingFrom(pos)) {
            gameEngine.processJumpRequest(pos); // מפעיל קפיצה במקום מ-pos ל-pos
            selectedPosition = null;
        }
    }
    /**
     * קידום הזמן מואצל כעת ישירות למנוע המשחק שמנהל את הכל
     */
    public void advanceTime(long millis) {
        gameEngine.advanceTime(millis);
    }
}
package org.example.input;

import org.example.models.*;
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


    public void handleClick(int x, int y, Piece.Color playerColor) {
        if (gameEngine.getGameState().isGameOver()) return;

        Position clickedPos = BoardMapper.toPosition(x, y);

        if (!board.isWithinBounds(clickedPos)) {
            selectedPosition = null;
            return;
        }

        Piece clickedPiece = board.getPiece(clickedPos);

        if (selectedPosition == null) {
            // --- לחיצה ראשונה: בחירת כלי ---
            // שינוי: מוודאים שהכלי שייך לשחקן שלחץ בחלון הנוכחי!
            if (clickedPiece != null && clickedPiece.getColor() == playerColor && !gameEngine.isPieceMovingFrom(clickedPos)) {
                selectedPosition = clickedPos;
            }
        } else {
            // --- לחיצה שנייה: יעד, ביטול או החלפת כלי ---
            if (selectedPosition.equals(clickedPos)) {
                selectedPosition = null;
            }
            else {
                Piece selectedPiece = board.getPiece(selectedPosition);

                if (clickedPiece != null && selectedPiece != null
                        && clickedPiece.getColor() == selectedPiece.getColor()
                        && !gameEngine.isPieceMovingFrom(clickedPos)) {
                    selectedPosition = clickedPos;
                }
                else {
                    // שינוי: מעבירים את ה-playerColor לתוך ה-Engine לאימות סופי
                    gameEngine.processMoveRequest(selectedPosition, clickedPos, playerColor);
                    selectedPosition = null;
                }
            }
        }
    }

    // שינוי: הוספת הפרמטר Piece.Color playerColor לחתימה
    public void handleJump(int x, int y, Piece.Color playerColor) {
        if (gameEngine.getGameState().isGameOver()) return;

        Position pos = BoardMapper.toPosition(x, y);
        if (!board.isWithinBounds(pos)) return;

        Piece piece = board.getPiece(pos);

        // שינוי: מוודאים שהכלי שקופץ שייך לשחקן הנכון
        if (piece != null && piece.getColor() == playerColor && !gameEngine.isPieceMovingFrom(pos)) {
            gameEngine.processJumpRequest(pos, playerColor); // שינוי: העברת הצבע למנוע
            selectedPosition = null;
        }
    }
    /**
     * קידום הזמן מואצל כעת ישירות למנוע המשחק שמנהל את הכל
     */
    public void advanceTime(long millis) {
        gameEngine.advanceTime(millis);
    }


    public Position getSelectedPosition() {
        return this.selectedPosition;
    }

    /**
     * מייצר ומחזיר את ה-Snapshot העדכני ביותר עבור ה-UI.
     * המתודה לוקחת את ה-selectedPosition המנוהל כאן ומעבירה אותו למנוע.
     */
    public GameSnapshot getLatestSnapshot() {
        return gameEngine.createSnapshot(this.selectedPosition);
    }



}
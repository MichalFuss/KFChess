package org.example.ui;

import org.example.events.EventBus;
import org.example.input.BoardMouseListener;
import org.example.input.GameController;
import org.example.models.GameState;
import org.example.models.GameSnapshot;
import org.example.models.Piece;

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    private final BoardPanel boardPanel;
    private final PlayerLogPanel blackLogPanel;
    private final PlayerLogPanel whiteLogPanel;
    private EventBus eventBus;

    public GameWindow(GameState gameState, GameController gameController,EventBus eventBus) {
        // הגדרת כותרת לחלון המשחק
        setTitle("KFChess - Real Time Chess");

        // הגדרה שהחלון ייסגר לחלוטין ויסיים את פעולת התוכנית בעת לחיצה על ה-X
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // הגדרת מנהל פריסה (Layout) מסוג BorderLayout עם מרווחים של 10 פיקסלים בין הרכיבים
        setLayout(new BorderLayout(10, 10));

        // מונע מהמשתמש לשנות את גודל החלון ידנית כדי לשמור על יחס הציור
        setResizable(false);

        // 1. יצירת פאנל הלוח
        this.boardPanel = new BoardPanel();

        // 2. יצירת פאנלי הרישום והניקוד לשני השחקנים (שליפת השמות ישירות מה-gameState)
        this.blackLogPanel = new PlayerLogPanel(gameState.getBlackPlayerName(), Piece.Color.BLACK, eventBus);
        this.whiteLogPanel = new PlayerLogPanel(gameState.getWhitePlayerName(), Piece.Color.WHITE, eventBus);

        // 3. מיקום הרכיבים על המסך (בדיוק כמו בצילום המסך ששלחת)
        add(blackLogPanel, BorderLayout.WEST);  // פאנל שחור בצד שמאל
        add(boardPanel, BorderLayout.CENTER);   // לוח המשחק במרכז
        add(whiteLogPanel, BorderLayout.EAST);  // פאנל לבן בצד ימין

        // 4. חיבור מאזין העכבר לפאנל הלוח לקליטת קלטים מהשחקן
        BoardMouseListener mouseListener = new BoardMouseListener(gameController);
        boardPanel.addMouseListener(mouseListener);

        // מתאים את גודל החלון החיצוני בדיוק לגודל הכולל של כל הרכיבים בפנים
        pack();

        // מרכז את החלון בדיוק באמצע המסך של המשתמש
        setLocationRelativeTo(null);

        // מציג את החלון על המסך
        setVisible(true);

        // 5. שעון המשחק הראשי (Game Loop)
        Timer gameTimer = new Timer(20, e -> {
            if (!gameState.isGameOver()) {
                // א. קידום הזמן הלוגי במנוע ב-20 מילישניות
                gameController.advanceTime(20);

                // ב. שליפת ה-Snapshot העדכני ביותר מהבקר
                GameSnapshot snapshot = gameController.getLatestSnapshot();

                // ג. עדכון הלוח ב-Snapshot החדש (שמפעיל repaint פנימי)
                if (snapshot != null) {
                    boardPanel.updateSnapshot(snapshot);
                }


            }
        });

        // ה. הפעלה של שעון המשחק
        gameTimer.start();
    }
}
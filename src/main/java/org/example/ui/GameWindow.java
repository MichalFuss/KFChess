package org.example.ui;

import org.example.input.BoardMouseListener;
import org.example.input.GameController;
import org.example.models.GameState;
import org.example.models.GameSnapshot; // ייבוא של ה-Snapshot החדש

import javax.swing.*;

public class GameWindow extends JFrame {
    private final BoardPanel boardPanel;

    public GameWindow(GameState gameState, GameController gameController) {
        // הגדרת כותרת לחלון המשחק
        setTitle("KFChess - Real Time Chess");

        // הגדרה שהחלון ייסגר לחלוטין ויסיים את פעולת התוכנית בעת לחיצה על ה-X
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // מונע מהמשתמש לשנות את גודל החלון ידנית
        setResizable(false);

        // 1. יצירת פאנל הלוח (בארכיטקטורה החדשה הוא משתמש בבנאי הדיפולטיבי ללא gameState)
        this.boardPanel = new BoardPanel();
        add(boardPanel);

        // 2. חיבור מאזין העכבר לפאנל הלוח לקליטת קלטים מהשחקן
        BoardMouseListener mouseListener = new BoardMouseListener(gameController);
        boardPanel.addMouseListener(mouseListener);

        // מתאים את גודל החלון החיצוני בדיוק לגודל הפאנל הפנימי
        pack();

        // מרכז את החלון בדיוק באמצע המסך של המשתמש
        setLocationRelativeTo(null);

        // מציג את החלון על המסך
        setVisible(true);

        // 3. שעון המשחק הראשי (Game Loop) - מעודכן לעבודה עם Snapshots
        Timer gameTimer = new Timer(20, e -> {
            if (!gameState.isGameOver()) {
                // א. קידום הזמן הלוגי במנוע ב-20 מילישניות
                gameController.advanceTime(20);

                // ב. שליפת ה-Snapshot העדכני ביותר מהבקר או מהמנוע
                // (הערה: ודא שיש לך מתודה כזו ב-gameController או ב-gameEngine שמחזירה GameSnapshot)
                GameSnapshot snapshot = gameController.getLatestSnapshot();

                // ג. עדכון הלוח ב-Snapshot החדש (מתודה זו מעדכנת את המצב וקוראת ל-repaint בעצמה!)
                if (snapshot != null) {
                    boardPanel.updateSnapshot(snapshot);
                }
            }
        });

        // ד. הפעלה חיונית של שעון המשחק!
        gameTimer.start();
    }
}
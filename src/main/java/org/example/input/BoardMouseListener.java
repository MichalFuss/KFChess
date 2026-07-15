package org.example.input;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

public class BoardMouseListener extends MouseAdapter {
    private final GameController gameController;

    public BoardMouseListener(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        // 1. אם המשתמש לחץ לחיצה שמאלית -> נבצע מהלך רגיל / בחירת כלי
        if (SwingUtilities.isLeftMouseButton(e)) {
            gameController.handleClick(x, y);
        }
        // 2. אם המשתמש לחץ לחיצה ימנית -> נבצע קפיצה במקום (Jump)
        else if (SwingUtilities.isRightMouseButton(e)) {
            gameController.handleJump(x, y);
        }
    }
}
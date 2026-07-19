package org.example.input;

import org.example.models.Piece;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

public class BoardMouseListener extends MouseAdapter {
    private final GameController gameController;
    private final Piece.Color assignedColor; // זהות החלון הנוכחי

    public BoardMouseListener(GameController gameController, Piece.Color assignedColor) {
        this.gameController = gameController;
        this.assignedColor = assignedColor;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        // ה-UI לא בודק כלום! הוא רק מדווח על הלחיצה + מי לחץ
        if (SwingUtilities.isLeftMouseButton(e)) {
            gameController.handleClick(x, y, assignedColor);
        }
        else if (SwingUtilities.isRightMouseButton(e)) {
            gameController.handleJump(x, y, assignedColor);
        }
    }
}
package org.example.input;

import org.example.models.Piece;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

public class BoardMouseListener extends MouseAdapter {
    private final IGameController gameController;
    private final Piece.Color assignedColor; // זהות החלון הנוכחי (WHITE / BLACK)
    private final String roleName;            // WHITE, BLACK, או VIEWER

    public BoardMouseListener(IGameController gameController, Piece.Color assignedColor, String roleName) {
        this.gameController = gameController;
        this.assignedColor = assignedColor;
        this.roleName = roleName;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // אם המשתמש הוא צופה - חוסמים לחלוטין קלט משחק
        if ("VIEWER".equalsIgnoreCase(roleName) || assignedColor == null) {
            return;
        }

        int x = e.getX();
        int y = e.getY();

        // ה-UI מדווח על הלחיצה + מי לחץ
        if (SwingUtilities.isLeftMouseButton(e)) {
            gameController.handleClick(x, y, assignedColor);
        }
        else if (SwingUtilities.isRightMouseButton(e)) {
            gameController.handleJump(x, y, assignedColor);
        }
    }
}
package org.example.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HomePanel extends JPanel {

    // הגדרת האזור הוויזואלי של הכפתור: מיקום X, מיקום Y, רוחב, גובה
    private final Rectangle playButtonArea = new Rectangle(300, 250, 200, 60);

    // משתנה לשמירת מצב (האם אנחנו כרגע מחפשים משחק?) כדי לשנות את הטקסט
    private boolean isSearching = false;

    public HomePanel(Runnable onPlayClicked) {
        // הגדרת מאזין ללחיצות עכבר על הפאנל
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // בדיקה: האם הנקודה שבה לחצו נמצאת בתוך שטח המלבן שהגדרנו?
                if (playButtonArea.contains(e.getPoint()) && !isSearching) {
                    isSearching = true;
                    repaint(); // ציור מחדש של הפאנל כדי לעדכן את הטקסט ל-"Searching..."

                    // הפעלת הפעולה שהועברה מבחוץ (הוספה לתור ה-Matchmaking)
                    if (onPlayClicked != null) {
                        onPlayClicked.run();
                    }
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // החלקת התצוגה (Anti-aliasing) כדי שהטקסט והצורות ייראו טוב
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ציור רקע הפאנל
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // ציור אזור הכפתור (Play)
        if (isSearching) {
            g2d.setColor(new Color(150, 150, 150)); // צבע אפור אם כבר לחצו
        } else {
            g2d.setColor(new Color(70, 130, 180)); // צבע כחול לכפתור פעיל
        }

        // ציור המלבן המוגדר
        g2d.fillRoundRect(playButtonArea.x, playButtonArea.y, playButtonArea.width, playButtonArea.height, 15, 15);

        // ציור הטקסט בתוך הכפתור
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));

        String text = isSearching ? "Searching..." : "PLAY";

        // חישוב מיקום הטקסט כדי שיהיה ממורכז בתוך אזור הכפתור
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = playButtonArea.x + (playButtonArea.width - textWidth) / 2;
        int textY = playButtonArea.y + ((playButtonArea.height - fm.getHeight()) / 2) + fm.getAscent();

        g2d.drawString(text, textX, textY);
    }
}
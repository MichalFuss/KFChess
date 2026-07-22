package org.example.ui;

import org.example.logging.LoggerService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class HomePanel extends JPanel {

    // אזורי הכפתורים בתוך הפאנל
    private final Rectangle playButtonArea = new Rectangle(200, 250, 180, 50);
    private final Rectangle roomButtonArea = new Rectangle(420, 250, 180, 50);

    private boolean isSearching = false;

    private final LoggerService logger;
    private final Consumer<String> onCreateRoom;
    private final Consumer<String> onJoinRoom;

    public HomePanel(Runnable onPlayClicked, Consumer<String> onCreateRoom, Consumer<String> onJoinRoom) {
        this.logger = new LoggerService("client.log");
        this.onCreateRoom = onCreateRoom;
        this.onJoinRoom = onJoinRoom;

        // הוספת מאזין ללחיצות עכבר
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point clickPoint = e.getPoint();

                // 1. לחיצה על כפתור PLAY
                if (playButtonArea.contains(clickPoint) && !isSearching) {
                    isSearching = true;
                    repaint();
                    if (onPlayClicked != null) {
                        onPlayClicked.run();
                    }
                }
                // 2. לחיצה על כפתור ROOM
                else if (roomButtonArea.contains(clickPoint)) {
                    onRoomButtonClicked();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ציור רקע
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // --- כפתור PLAY ---
        g2d.setColor(isSearching ? new Color(150, 150, 150) : new Color(70, 130, 180));
        g2d.fillRoundRect(playButtonArea.x, playButtonArea.y, playButtonArea.width, playButtonArea.height, 15, 15);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        drawCenteredString(g2d, isSearching ? "Searching..." : "PLAY", playButtonArea);

        // --- כפתור ROOM ---
        g2d.setColor(new Color(60, 179, 113)); // ירוק לכפתור החדר
        g2d.fillRoundRect(roomButtonArea.x, roomButtonArea.y, roomButtonArea.width, roomButtonArea.height, 15, 15);

        g2d.setColor(Color.WHITE);
        drawCenteredString(g2d, "ROOM", roomButtonArea);
    }

    private void drawCenteredString(Graphics2D g2d, String text, Rectangle area) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = area.x + (area.width - fm.stringWidth(text)) / 2;
        int y = area.y + ((area.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, x, y);
    }

    private void onRoomButtonClicked() {
        JTextField roomInput = new JTextField(15);
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("room name:"));
        panel.add(roomInput);

        String[] options = {"Create", "Join", "Cancel"};

        int choice = JOptionPane.showOptionDialog(
                this,
                panel,
                "Room",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        String roomName = roomInput.getText().trim();

        if (choice == 0) { // Create
            if (roomName.isEmpty()) {
                roomName = "Room_" + (int) (Math.random() * 1000);
            }
            logger.log("INFO", "User created room: " + roomName);
            if (onCreateRoom != null) {
                onCreateRoom.accept(roomName);
            }

        } else if (choice == 1) { // Join
            if (!roomName.isEmpty()) {
                logger.log("INFO", "User joined room: " + roomName);
                if (onJoinRoom != null) {
                    onJoinRoom.accept(roomName);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a valid room name.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
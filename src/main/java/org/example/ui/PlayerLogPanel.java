package org.example.ui;

import org.example.events.*;
import org.example.models.MoveLogEntry;
import org.example.models.Piece;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PlayerLogPanel extends JPanel implements EventListener {
    private final JLabel nameLabel;
    private final JLabel scoreLabel;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private EventBus eventBus;
    private Piece.Color panelColor;

    public PlayerLogPanel(String playerName ,Piece.Color panelColor, EventBus eventBus) {
        this.panelColor = panelColor;
        this.eventBus = eventBus;

        setLayout(new BorderLayout(5, 5));
        setPreferredSize(new Dimension(200, 400));
        setBackground(new Color(220, 220, 220));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // פאנל כותרת (שם וניקוד)
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        nameLabel = new JLabel("Name: " + playerName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(nameLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(scoreLabel);

        add(headerPanel, BorderLayout.NORTH);

        eventBus.subscribe(this); // הירשם לאירועים מה-Bus
        // טבלת מהלכים
        String[] columnNames = {"Time", "Move"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void onEvent(GameEvent event) {
        // לוגיקה 1: טיפול באירוע של עדכון ניקוד
        if (event instanceof ScoreUpdatedEvent) {
            ScoreUpdatedEvent scoreEvent = (ScoreUpdatedEvent) event;
            // בודקים מה הצבע של הפאנל שלנו, ומושכים את הניקוד הרלוונטי מהאירוע
            if (this.panelColor == Piece.Color.WHITE) {
                scoreLabel.setText("Score: " + scoreEvent.getWhiteScore());
            } else {
                scoreLabel.setText("Score: " + scoreEvent.getBlackScore());
            }
        }

        // לוגיקה 2: טיפול באירוע של רישום מהלך
        if (event instanceof MoveLoggedEvent) {
            MoveLoggedEvent moveEvent = (MoveLoggedEvent) event;
            // הפאנל יוסיף שורה לטבלה *רק* אם המהלך שבוצע תואם לצבע של הפאנל
            if (moveEvent.getPlayerColor() == this.panelColor) {
                MoveLogEntry entry = moveEvent.getMoveEntry();
                tableModel.addRow(new Object[]{entry.getTime(), entry.getNotation()});
            }
        }
    }
    @Override
    public void removeNotify() {
        super.removeNotify();
        // ברגע שהפאנל יורד מהמסך, אנחנו מנקים את הרישום כדי למנוע Memory Leaks
        if (eventBus != null) {
            eventBus.unsubscribe(this);
        }
    }
}
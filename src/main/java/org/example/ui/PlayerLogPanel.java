package org.example.ui;

import org.example.models.MoveLogEntry;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PlayerLogPanel extends JPanel {
    private final JLabel nameLabel;
    private final JLabel scoreLabel;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public PlayerLogPanel(String playerName) {
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

    /**
     * פונקציה לעדכון הניקוד והמהלכים בזמן אמת
     */
    public void updateData(int score, List<MoveLogEntry> moves) {
        scoreLabel.setText("Score: " + score);

        // מוסיף לטבלה רק שורות חדשות שעדיין לא קיימות
        if (tableModel.getRowCount() < moves.size()) {
            for (int i = tableModel.getRowCount(); i < moves.size(); i++) {
                MoveLogEntry entry = moves.get(i);
                tableModel.addRow(new Object[]{entry.getTime(), entry.getNotation()});
            }
        }
    }
}
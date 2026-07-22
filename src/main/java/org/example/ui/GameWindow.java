package org.example.ui;

import org.example.events.*;
import org.example.input.BoardMouseListener;
import org.example.input.GameController;
import org.example.input.IGameController;
import org.example.models.GameState;
import org.example.models.GameSnapshot;
import org.example.models.MoveLogEntry;
import org.example.models.Piece;
import org.example.events.GameEvent;

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    private final BoardPanel boardPanel;
    private final PlayerLogPanel blackLogPanel;
    private final PlayerLogPanel whiteLogPanel;
    private final Piece.Color windowColor;
    private final String roomId;
    private final String roleName;
    private final IGameController gameController; // שמור גם את gameController לשימוש בעדכונים
    private final GameState gameState; // שמור את gameState לעדכון שמות
    private int lastWhiteScore = -1; // מעקב אחרי ניקוד קודם (-1 = טרם אותחל)
    private int lastBlackScore = -1; // מעקב אחרי ניקוד קודם (-1 = טרם אותחל)
    private boolean isGameOver = false; // מעקב אם המשחק הסתיים
    private int lastWhiteMoveCount = 0; // מעקב אחרי מספר מהלכים קודם של לבן
    private int lastBlackMoveCount = 0; // מעקב אחרי מספר מהלכים קודם של שחור
    private final EventBus eventBus; // שמור אם EventBus לשימוש בפרסום אירועים

    public GameWindow(GameState gameState, IGameController gameController, EventBus eventBus, Piece.Color windowColor, String roomId, String roleName) {
        this.windowColor = windowColor;
        this.roomId = roomId;
        this.roleName = roleName;
        this.gameController = gameController; // שמור את controller
        this.gameState = gameState; // שמור את gameState
        this.eventBus = eventBus; // תוספת: שמור את eventBus

        updateTitle(gameState);

        // 1. שחרור החלון הספציפי בלבד ללא הריגת כל התהליך
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

// 2. תפיסת אירוע הסגירה וסגירת חיבור ה-WebSocket
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                eventBus.publish(new GameEvent(){});
            }
        });
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        // פאנל כותרת עליון בראש המסך
        JPanel topHeader = new JPanel();
        topHeader.setBackground(new Color(30, 30, 30));

        String displayText = "Room ID: " + roomId + " | Role: " + roleName;
        JLabel roomLabel = new JLabel(displayText);

        // צבע שונה לצופה בשביל הדגשה ויזואלית
        if ("VIEWER".equalsIgnoreCase(roleName)) {
            roomLabel.setForeground(Color.YELLOW);
        } else {
            roomLabel.setForeground(Color.CYAN);
        }

        roomLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topHeader.add(roomLabel);
        add(topHeader, BorderLayout.NORTH);

        this.boardPanel = new BoardPanel(eventBus);
        // החכה לשמות גנריים - NAMES יטעין מאוחר
        // בתוך הבנאי של GameWindow.java במקום השורות של "Loading..."
        String wName = safePlayerName(gameState.getWhitePlayerName());
        String bName = safePlayerName(gameState.getBlackPlayerName());

        this.blackLogPanel = new PlayerLogPanel(bName, Piece.Color.BLACK, eventBus);
        this.whiteLogPanel = new PlayerLogPanel(wName, Piece.Color.WHITE, eventBus);
        add(blackLogPanel, BorderLayout.WEST);
        add(boardPanel, BorderLayout.CENTER);
        add(whiteLogPanel, BorderLayout.EAST);

        // העברת roleName ל-MouseListener כדי לדעת אם לחסום צופים
        BoardMouseListener mouseListener = new BoardMouseListener(gameController, windowColor, roleName);
        boardPanel.addMouseListener(mouseListener);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        Timer gameTimer = new Timer(20, e -> {
            if (!gameState.isGameOver()) {
                boardPanel.repaint();
            }
        });
        gameTimer.start();

        eventBus.subscribe(new EventListener() {
            @Override
            public void onEvent(GameEvent event) {
                if (event instanceof GameStateUpdatedEvent) {
                    // עדכון ה-UI בצורה בטוחה ב-Event Dispatch Thread
                    SwingUtilities.invokeLater(() -> {
                        GameSnapshot snapshot = gameController.getLatestSnapshot();
                        boardPanel.updateSnapshot(snapshot);

                        // Initialize scores on first snapshot
                        if (lastWhiteScore == -1) {
                            lastWhiteScore = snapshot.getWhiteScore();
                            lastBlackScore = snapshot.getBlackScore();
                        }

                        // Check if scores have changed (capture occurred)
                        boolean whiteScoreChanged = snapshot.getWhiteScore() > lastWhiteScore;
                        boolean blackScoreChanged = snapshot.getBlackScore() > lastBlackScore;
                        
                        if (whiteScoreChanged || blackScoreChanged) {
                            // A capture occurred! Publish events
                            eventBus.publish(new PlaySoundEvent("CAPTURE"));
                            
                            // Publish score update event
                            eventBus.publish(new ScoreUpdatedEvent(snapshot.getWhiteScore(), snapshot.getBlackScore()));
                            
                            // Update the tracked previous scores
                            lastWhiteScore = snapshot.getWhiteScore();
                            lastBlackScore = snapshot.getBlackScore();
                        }
                        
                        // Check for new moves in the snapshot and publish MoveLoggedEvent for each new move
                        if (snapshot.getWhiteMoveHistory() != null && snapshot.getWhiteMoveHistory().size() > lastWhiteMoveCount) {
                            java.util.List<MoveLogEntry> whiteMoves = snapshot.getWhiteMoveHistory();
                            for (int i = lastWhiteMoveCount; i < whiteMoves.size(); i++) {
                                MoveLogEntry move = whiteMoves.get(i);
                                eventBus.publish(new MoveLoggedEvent(Piece.Color.WHITE, move));
                            }
                            lastWhiteMoveCount = whiteMoves.size();
                        }
                        
                        if (snapshot.getBlackMoveHistory() != null && snapshot.getBlackMoveHistory().size() > lastBlackMoveCount) {
                            java.util.List<MoveLogEntry> blackMoves = snapshot.getBlackMoveHistory();
                            for (int i = lastBlackMoveCount; i < blackMoves.size(); i++) {
                                MoveLogEntry move = blackMoves.get(i);
                                eventBus.publish(new MoveLoggedEvent(Piece.Color.BLACK, move));
                            }
                            lastBlackMoveCount = blackMoves.size();
                        }
                    });
                } else if (event instanceof DisconnectCountdownEvent) {
                    DisconnectCountdownEvent countdownEvent = (DisconnectCountdownEvent) event;
                    int secondsLeft = countdownEvent.getSecondsLeft();

                    SwingUtilities.invokeLater(() -> {
                        if (secondsLeft > 0) {
                            setTitle("⚠️ אזהרה: היריב התנתק! ניצחון טכני בעוד " + secondsLeft + " שניות... [Room: " + roomId + "]");
                        } else {
                            updateTitle(gameState);
                        }
                    });
                } else if (event instanceof MoveLoggedEvent) {
                    // אירוע רישום מהלוח - הפנטה כבר מאזקה לאירוע בעצמה
                    // PlayerLogPanel כבר מאזקה לאירוע בעצמה
                } else if (event instanceof GameStatusEvent) {
                    GameStatusEvent statusEvent = (GameStatusEvent) event;
                    if (statusEvent.getStatus() == GameStatusEvent.Status.OVER) {
                        isGameOver = true;
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(GameWindow.this,
                                "המשחק הסתיים!",
                                "Game Over",
                                JOptionPane.INFORMATION_MESSAGE);
                        });
                    }
                } else if (event instanceof NamesUpdatedEvent) {
                    NamesUpdatedEvent namesEvent = (NamesUpdatedEvent) event;
                    SwingUtilities.invokeLater(() -> {
                        String whiteName = safePlayerName(namesEvent.getWhiteName());
                        String blackName = safePlayerName(namesEvent.getBlackName());

                        // עדכון שמות בתאים (UI)
                        whiteLogPanel.updateName(whiteName);
                        blackLogPanel.updateName(blackName);
                        
                        // תיקון חשוב: עדכן גם את GameState כדי ששמות יהיו מסונכרנים
                        // זה מונע עיוות כאשר GameState משמש כמקור אמת
                        gameState.setWhitePlayerName(whiteName);
                        gameState.setBlackPlayerName(blackName);
                        updateTitle(gameState);
                    });
                }

            }
        });
    }

    private void updateTitle(GameState gameState) {
        String playerName = (windowColor == Piece.Color.WHITE)
                ? safePlayerName(gameState.getWhitePlayerName())
                : safePlayerName(gameState.getBlackPlayerName());
        setTitle("KFChess | Room: " + roomId + " | Player: " + playerName + " (" + roleName + ")");
    }

    private String safePlayerName(String name) {
        return (name == null || name.trim().isEmpty()) ? "Waiting..." : name;
    }

    public void updateNames(String whiteName, String blackName) {
        SwingUtilities.invokeLater(() -> {
            String safeWhite = safePlayerName(whiteName);
            String safeBlack = safePlayerName(blackName);
            whiteLogPanel.updateName(safeWhite);
            blackLogPanel.updateName(safeBlack);
            gameState.setWhitePlayerName(safeWhite);
            gameState.setBlackPlayerName(safeBlack);
            updateTitle(gameState);
        });
    }
}
package org.example;

import org.example.audio.SoundManager;
import org.example.db.DatabaseManager;
import org.example.engine.GameEngine;
import org.example.events.EventBus;
import org.example.events.GameStatusEvent;
import org.example.input.GameController;
import org.example.matchmaking.PlayerSession;
import org.example.models.*;
import org.example.realtime.RealTimeArbiter;
import org.example.server.GameWebSocketServer;
import org.example.ui.GameWindow;
import org.example.ui.HomePanel;

import javax.swing.*;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class Main {
    // 1. הגדרת משתנים גלובליים למחלקה
    private static final DatabaseManager db = new DatabaseManager();
    // תור המתנה לשחקנים המחפשים משחק (תומך בעבודה במקביל - Thread Safe)
    private static final CopyOnWriteArrayList<PlayerSession> matchmakingQueue = new CopyOnWriteArrayList<>();
    // המשתמש שמחובר כרגע במסוף (ה-Shell)
    private static PlayerSession loggedInUser = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== KFChess Server & Client Shell ===");
        System.out.println("Commands: REGISTER <user> <pass> | LOGIN <user> <pass> | PLAY");

        // הפעלת מנגנון חיפוש היריבים שרץ כל הזמן ברקע
        startMatchmakingThread();

        // הלולאה המרכזית של המסוף הקולטת פקודות מהמשתמש
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split(" ");

            if (parts.length == 3 && parts[0].equalsIgnoreCase("REGISTER")) {
                if (db.register(parts[1], parts[2])) {
                    System.out.println("Registered successfully! You can now LOGIN.");
                } else {
                    System.out.println("Error: Username already exists.");
                }
            }
            else if (parts.length == 3 && parts[0].equalsIgnoreCase("LOGIN")) {
                if (db.login(parts[1], parts[2])) {
                    int elo = db.getElo(parts[1]);
                    loggedInUser = new PlayerSession(parts[1], elo);
                    System.out.println("Login successful! Welcome " + parts[1] + " (ELO: " + elo + "). Type PLAY to find a match.");
                    // אחרי שהלוגין עבר בהצלחה במסוף:
                    SwingUtilities.invokeLater(() -> {
                        JFrame homeFrame = new JFrame("Home Screen - " + loggedInUser.username);
                        homeFrame.setSize(800, 600);
                        homeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                        HomePanel homePanel = new HomePanel(() -> {
                            // מה קורה כשלוחצים על אזור ה-Play?
                            System.out.println("Visual PLAY button clicked! Searching for opponent...");
                            loggedInUser.searchStartTime = System.currentTimeMillis();
                            matchmakingQueue.add(loggedInUser);
                        });

                        homeFrame.add(homePanel);
                        homeFrame.setVisible(true);
                    });
                } else {
                    System.out.println("Invalid username or password.");
                }
            }

            else {
                System.out.println("Unknown command.");
            }
        }
    }

    /**
     * חוט (Thread) שרץ ברקע כל הזמן ובודק האם יש שני שחקנים בתור שיכולים לשחק יחד
     */
    private static void startMatchmakingThread() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000); // הפסקת ביניים של שנייה בין כל בדיקה[cite: 17]
                    long currentTime = System.currentTimeMillis();

                    // א. ביטול חיפוש למשתמשים שהמתינו יותר מדקה (60 שניות)
                    matchmakingQueue.removeIf(p -> {
                        if (currentTime - p.searchStartTime > 60000) {
                            System.out.println("\n[System] Could not find a match for " + p.username + ". Timeout (1 min).");

                            // הקפצת הודעה מובנית של מערכת ההפעלה (Windows Dialog)
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        null,
                                        "לא הצלחנו למצוא עבורך יריב בטווח ה-ELO המבוקש (±100) בתוך דקה.\nאנא נסה לחפש משחק שוב.",
                                        "חיפוש משחק נכשל",
                                        JOptionPane.WARNING_MESSAGE
                                );
                            });

                            System.out.print("> "); // הדפסה מחודשת של חץ הפקודה
                            return true; // הוסר מהתור
                        }
                        return false;
                    });

                    // ב. ניסיון לשדך בין 2 שחקנים שנמצאים בתור
                    if (matchmakingQueue.size() >= 2) {
                        for (int i = 0; i < matchmakingQueue.size(); i++) {
                            for (int j = i + 1; j < matchmakingQueue.size(); j++) {
                                PlayerSession p1 = matchmakingQueue.get(i);
                                PlayerSession p2 = matchmakingQueue.get(j);

                                // בדיקה אם הפרש ה-ELO שלהם קטן או שווה ל-100
                                if (Math.abs(p1.elo - p2.elo) <= 100) {

                                    // התאמה נמצאה! מוציאים אותם מתור החיפוש
                                    matchmakingQueue.remove(p1);
                                    matchmakingQueue.remove(p2);

                                    System.out.println("\n[Match Found!] " + p1.username + " (White) VS " + p2.username + " (Black). Starting game...");

                                    // קריאה לפונקציה שמתניעה בפועל את המשחק
                                    startGame(p1, p2);
                                    return; // חזרה מהלולאה הפנימית כדי למנוע כפילויות
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * מתודה המרכזת את הלוגיקה והממשק של המשחק עצמו.
     * מופעלת אך ורק לאחר שנמצא שידוך בהצלחה ב-Matchmaking.
     */
    private static void startGame(PlayerSession whiteUser, PlayerSession blackUser) {
        EventBus eventBus = new EventBus();
        SoundManager soundManager = new SoundManager(eventBus);

        Board board = new Board(8, 8);
        setupStandardChessBoard(board);

        // העברת שמות המשתמשים למודל המצב של המשחק
        GameState gameState = new GameState(board);
        gameState.setPlayerNames(whiteUser.username, blackUser.username);

        RealTimeArbiter arbiter = new RealTimeArbiter(1000, 1000, eventBus);
        GameEngine gameEngine = new GameEngine(gameState, arbiter);
        GameController gameController = new GameController(board, gameEngine);

        try {
            // אתחול שרת ה-WebSocket (שים לב: העברנו לו כאן גם את eventBus עבור התראות הניתוק)
            GameWebSocketServer server = new GameWebSocketServer(8887, gameEngine, eventBus);
            server.start();

            // חוט נפרד שישדר את ה-Snapshot (מצב המשחק המדויק) לכל הלקוחות המחוברים כל 50 מילישניות
            new Thread(() -> {
                while (!gameState.isGameOver()) {
                    try {
                        Thread.sleep(50);
                        server.broadcastGameState();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            System.err.println("Failed to start WebSocket server: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {

            GameWindow whiteWindow = new GameWindow(gameState, gameController, eventBus, Piece.Color.WHITE);
            whiteWindow.setTitle("KFChess - " + whiteUser.username + " (White Player)");
            whiteWindow.setLocation(100, 100);


            GameWindow blackWindow = new GameWindow(gameState, gameController, eventBus, Piece.Color.BLACK);
            blackWindow.setTitle("KFChess - " + blackUser.username + " (Black Player)");
            blackWindow.setLocation(900, 100);

            eventBus.publish(new GameStatusEvent(GameStatusEvent.Status.STARTED));
        });
    }

    // ==========================================
    // פונקציות העזר לסידור הלוח ההתחלתי
    // ==========================================

    private static void setupStandardChessBoard(Board board) {
        placeMajorRow(board, 0, Piece.Color.BLACK);
        for (int col = 0; col < 8; col++) {
            Position pos = new Position(1, col);
            board.setPiece(pos, new Piece("bP_" + col, Piece.Color.BLACK, Piece.Kind.PAWN, pos));
        }

        for (int col = 0; col < 8; col++) {
            Position pos = new Position(6, col);
            board.setPiece(pos, new Piece("wP_" + col, Piece.Color.WHITE, Piece.Kind.PAWN, pos));
        }
        placeMajorRow(board, 7, Piece.Color.WHITE);
    }

    private static void placeMajorRow(Board board, int row, Piece.Color color) {
        String prefix = (color == Piece.Color.WHITE) ? "w" : "b";

        board.setPiece(new Position(row, 0), new Piece(prefix + "R1", color, Piece.Kind.ROOK, new Position(row, 0)));
        board.setPiece(new Position(row, 1), new Piece(prefix + "N1", color, Piece.Kind.KNIGHT, new Position(row, 1)));
        board.setPiece(new Position(row, 2), new Piece(prefix + "B1", color, Piece.Kind.BISHOP, new Position(row, 2)));
        board.setPiece(new Position(row, 3), new Piece(prefix + "Q", color, Piece.Kind.QUEEN, new Position(row, 3)));
        board.setPiece(new Position(row, 4), new Piece(prefix + "K", color, Piece.Kind.KING, new Position(row, 4)));
        board.setPiece(new Position(row, 5), new Piece(prefix + "B2", color, Piece.Kind.BISHOP, new Position(row, 5)));
        board.setPiece(new Position(row, 6), new Piece(prefix + "N2", color, Piece.Kind.KNIGHT, new Position(row, 6)));
        board.setPiece(new Position(row, 7), new Piece(prefix + "R2", color, Piece.Kind.ROOK, new Position(row, 7)));
    }
}
package org.example;

import org.example.db.DatabaseManager;
import org.example.events.EventBus;
import org.example.events.GameStateUpdatedEvent;
import org.example.events.RoomJoinedEvent;
import org.example.input.GameController;
import org.example.input.NetworkGameAdapter;
import org.example.matchmaking.PlayerSession;
import org.example.models.Board;
import org.example.models.GameSnapshot;
import org.example.models.GameState;
import org.example.models.Piece;
import org.example.server.GameWebSocketClient;
import org.example.server.GameWebSocketServer;
import org.example.ui.GameWindow;
import org.example.ui.HomePanel;

import javax.swing.*;
import java.net.URI;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main {
    private static final DatabaseManager db = new DatabaseManager();
    private static final CopyOnWriteArrayList<PlayerSession> matchmakingQueue = new CopyOnWriteArrayList<>();

    // מאגר לניהול החיבורים של כל הלקוחות המחוברים (במקום משתנים סטטיים גלובליים שדורסים זה את זה)
    private static final Map<PlayerSession, GameWebSocketClient> activeClients = new ConcurrentHashMap<>();

    private static final EventBus serverEventBus = new EventBus();

    public static void main(String[] args) {
        // 1. הפעלת השרת המרכזי
        startGlobalServer();

        Scanner scanner = new Scanner(System.in);
        System.out.println("=== KFChess Server & Client Shell ===");
        System.out.println("Commands: REGISTER <user> <pass> | LOGIN <user> <pass> | PLAY");

        startMatchmakingThread();

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

                    // יצירת מופע משתמש מקומי - לא שומרים במשתנה סטטי יחיד!
                    PlayerSession loggedInUser = new PlayerSession(parts[1], elo);
                    System.out.println("Login successful! Welcome " + parts[1] + " (ELO: " + elo + ").");

                    // 2. יצירת EventBus ו-WebSocketClient ייחודיים למשתמש הספציפי הזה
                    EventBus userEventBus = new EventBus();

                    // יצירת SoundManager שישמע לאירועי צליל
                    new org.example.audio.SoundManager(userEventBus);

                    GameWebSocketClient userClient;
                    try {
                        URI serverUri = new URI("ws://localhost:8887");
                        userClient = new GameWebSocketClient(serverUri, userEventBus);
                        userClient.connect();
                        // שמירת הלקוח של המשתמש בתוך מפה (כדי שמנגנון החיפוש יוכל להשתמש בו)
                        activeClients.put(loggedInUser, userClient);
                    } catch (Exception e) {
                        System.err.println("Failed to connect to WebSocket server: " + e.getMessage());
                        continue; // מדלג להמשך הלולאה אם נכשל
                    }

                    SwingUtilities.invokeLater(() -> {
                        JFrame homeFrame = new JFrame("Home Screen - " + loggedInUser.username);
                        homeFrame.setSize(800, 600);
                        homeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                        // 3. האזנה לאירוע חדר על ה-EventBus הפרטי של הלקוח
                        userEventBus.subscribe(event -> {
                            if (event instanceof RoomJoinedEvent) {
                                RoomJoinedEvent roomEvent = (RoomJoinedEvent) event;

                                // התיקון: חילוץ המשתנים והמאזין החוצה מה-invokeLater של ה-UI
                                final GameWindow[] gameWindowRef = new GameWindow[1];
                                final String[] pendingNames = new String[2];

                                userEventBus.subscribe(new org.example.events.EventListener() {
                                    @Override
                                    public void onEvent(org.example.events.GameEvent e) {
                                        if (e instanceof org.example.events.NamesUpdatedEvent) {
                                            org.example.events.NamesUpdatedEvent namesEvent = (org.example.events.NamesUpdatedEvent) e;
                                            // שומרים את השמות למקרה שהחלון עוד לא מוכן
                                            pendingNames[0] = namesEvent.getWhiteName();
                                            pendingNames[1] = namesEvent.getBlackName();

                                            SwingUtilities.invokeLater(() -> {
                                                if (gameWindowRef[0] != null) {
                                                    gameWindowRef[0].updateNames(pendingNames[0], pendingNames[1]);
                                                }
                                            });
                                        }
                                    }
                                });

                                SwingUtilities.invokeLater(() -> {
                                    homeFrame.setVisible(false);

                                    Piece.Color playerColor = "WHITE".equalsIgnoreCase(roomEvent.getRole())
                                            ? Piece.Color.WHITE
                                            : Piece.Color.BLACK;

                                    // יצירת מנוע משחק מלא ומקורי לכל חלון
                                    org.example.engine.GameEngine gameEngine = org.example.engine.GameEngineFactory.createNewGame(userEventBus);
                                    GameState clientGameState = gameEngine.getGameState();
                                    Board clientBoard = clientGameState.getBoard();

                                    // הגדרת השם המקומי ב-GameState לפני יצירת החלון
                                    String localPlayerName = loggedInUser.username;
                                    if (playerColor == Piece.Color.WHITE) {
                                        clientGameState.setPlayerNames(localPlayerName, "Waiting...");
                                    } else {
                                        clientGameState.setPlayerNames("Waiting...", localPlayerName);
                                    }

                                    NetworkGameAdapter networkAdapter = new NetworkGameAdapter(
                                            clientBoard,
                                            playerColor,
                                            command -> userClient.send(command) // שליחת הפקודה לשרת ברגע שמתבצעת פעולה
                                    );

                                    userEventBus.subscribe(snapshotEvent -> {
                                        if (snapshotEvent instanceof GameStateUpdatedEvent) {
                                            GameSnapshot snapshot = ((GameStateUpdatedEvent) snapshotEvent).getSnapshot();
                                            networkAdapter.updateSnapshot(snapshot);
                                        }
                                    });

                                    // פתיחת חלון המשחק עם ה-networkAdapter בתור הבקר
                                    GameWindow gameWindow = new GameWindow(
                                            clientGameState,
                                            networkAdapter, // הבקר שמטפל בקלט ובשצף הנתונים מהרשת
                                            userEventBus,
                                            playerColor,
                                            roomEvent.getRoomId(),
                                            roomEvent.getRole()
                                    );

                                    gameWindowRef[0] = gameWindow;

                                    // אם אירוע השמות הגיע מהר יותר מבניית החלון (אופייני לשחקן השחור)
                                    if (pendingNames[0] != null && pendingNames[1] != null) {
                                        gameWindow.updateNames(pendingNames[0], pendingNames[1]);
                                    }

                                    gameWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                    gameWindow.setVisible(true);

                                    System.out.println("[" + loggedInUser.username + "] Successfully opened GameWindow for room: " + roomEvent.getRoomId());
                                    System.out.print("> ");
                                });
                            }
                        });

                        // 4. חיבור ה-HomePanel לאובייקט הלקוח הפרטי
                        HomePanel homePanel = new HomePanel(
                                () -> { // PLAY
                                    System.out.println("[" + loggedInUser.username + "] Searching for opponent...");
                                    loggedInUser.searchStartTime = System.currentTimeMillis();
                                    matchmakingQueue.add(loggedInUser);
                                },
                                roomName -> { // CREATE ROOM
                                    if (userClient.isOpen()) {
                                        userClient.send("CREATE_ROOM:" + roomName + ":" + loggedInUser.username);
                                    }
                                },
                                roomName -> { // JOIN ROOM
                                    if (userClient.isOpen()) {
                                        userClient.send("JOIN_ROOM:" + roomName + ":" + loggedInUser.username);
                                    }
                                }
                        );

                        homeFrame.add(homePanel);
                        homeFrame.setVisible(true);
                    });
                } else {
                    System.out.println("Invalid username or password.");
                }
            } else {
                System.out.println("Unknown command.");
            }
        }
    }

    private static void startGlobalServer() {
        try {
            GameWebSocketServer server = new GameWebSocketServer(8887, serverEventBus);
            server.start();
            System.out.println("[System] Global WebSocket Server started on port 8887");
        } catch (Exception e) {
            System.err.println("[System] Failed to start WebSocket server: " + e.getMessage());
        }
    }

    private static void startMatchmakingThread() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    long currentTime = System.currentTimeMillis();

                    matchmakingQueue.removeIf(p -> {
                        if (currentTime - p.searchStartTime > 60000) {
                            System.out.println("\n[System] Could not find a match for " + p.username + ". Timeout.");
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(null,
                                        "לא הצלחנו למצוא עבורך יריב בטווח ה-ELO המבוקש בתוך דקה.\nאנא נסה שוב.",
                                        "חיפוש משחק נכשל", JOptionPane.WARNING_MESSAGE);
                            });
                            System.out.print("> ");
                            return true;
                        }
                        return false;
                    });

                    if (matchmakingQueue.size() >= 2) {
                        for (int i = 0; i < matchmakingQueue.size(); i++) {
                            for (int j = i + 1; j < matchmakingQueue.size(); j++) {
                                PlayerSession p1 = matchmakingQueue.get(i);
                                PlayerSession p2 = matchmakingQueue.get(j);

                                if (Math.abs(p1.elo - p2.elo) <= 100) {
                                    matchmakingQueue.remove(p1);
                                    matchmakingQueue.remove(p2);

                                    System.out.println("\n[Match Found!] " + p1.username + " (White) VS " + p2.username + " (Black). Starting game...");

                                    String roomName = "Match_" + p1.username + "_" + p2.username;
                                    GameWebSocketClient c1 = activeClients.get(p1);
                                    GameWebSocketClient c2 = activeClients.get(p2);

                                    // שחקן 1 יוצר את החדר ומצטרף ראשון
                                    if (c1 != null && c1.isOpen()) {
                                        c1.send("CREATE_ROOM:" + roomName + ":" + p1.username);
                                    }

                                    // השהייה קלה כדי לאפשר לשרת לפתוח את החדר לפני שהשני מצטרף
                                    Thread.sleep(500);

                                    // שחקן 2 מצטרף לחדר שחקן 1 הרגע פתח
                                    if (c2 != null && c2.isOpen()) {
                                        c2.send("JOIN_ROOM:" + roomName + ":" + p2.username);
                                    }

                                    System.out.print("> ");
                                    return; // הפסקה מהלולאה הנוכחית להמשך האזנה
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
}

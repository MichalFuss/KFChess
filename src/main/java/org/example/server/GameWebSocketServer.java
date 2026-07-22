package org.example.server;

import com.google.gson.Gson;
import org.example.db.DatabaseManager;
import org.example.engine.GameEngineFactory;
import org.example.events.DisconnectCountdownEvent;
import org.example.events.EventBus;
import org.example.events.GameStatusEvent;
import org.example.logging.LoggerService;
import org.example.matchmaking.PlayerSession;
import org.example.models.GameSnapshot;
import org.example.models.GameState;
import org.example.models.Piece;
import org.example.models.Position;
import org.example.rules.EloCalculator;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameWebSocketServer extends WebSocketServer {

    private final Gson gson;
    private final EventBus eventBus;
    private final LoggerService logger;

    // ניהול חדרים ומעקב אחר חיבורי ה-WebSocket של כל חדר
    private final Map<String, GameRoom> activeRooms = new ConcurrentHashMap<>();
    private final Map<String, List<WebSocket>> roomConnections = new ConcurrentHashMap<>();
    private final Map<WebSocket, String> connToRoomMap = new ConcurrentHashMap<>();
    private final DatabaseManager dbManager = new DatabaseManager();

    private Timer disconnectTimer;
    private int countdownSeconds = 20;
    private Timer gameLoopTimer; // התוספת של לולאת הזמן

    public GameWebSocketServer(int port, EventBus eventBus) {
        super(new InetSocketAddress(port));
        this.gson = new Gson();
        this.eventBus = eventBus;
        this.logger = new LoggerService("server.log");

        // לולאת משחק מרכזית של השרת - רצה כל 50 מילי-שניות (20FPS)
        this.gameLoopTimer = new Timer(true);
        this.gameLoopTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (GameRoom room : activeRooms.values()) {
                    // מקדם את הזמן של המנוע במדויק
                    room.getGameEngine().advanceTime(50);
                    GameState gameState = room.getGameEngine().getGameState();
                    if (gameState.isGameOver() && !room.isGameOverHandled()) {
                        room.setGameOverHandled(true); // מונע כפילויות

                        Piece.Color winnerColor = gameState.getWinnerColor();
                        handleGameOver(room.getRoomId(), winnerColor); // עדכון Elo, DB ושידור לכולם
                    }
                    // משדר את תמונת המצב לכל השחקנים בחדר ברציפות
                    broadcastGameStateToRoom(room.getRoomId());
                }
            }
        }, 0, 50);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.log("INFO", "New connection opened from: " + conn.getRemoteSocketAddress());

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println(">>> SERVER DETECTED CLOSE for connection: " + conn.getRemoteSocketAddress());
        logger.log("WARN", "Connection closed: " + conn.getRemoteSocketAddress() + " Reason: " + reason);

        String roomId = connToRoomMap.remove(conn);
        if (roomId != null && roomConnections.containsKey(roomId)) {
            roomConnections.get(roomId).remove(conn);

            // ➕ אם אין יותר חיבורים פעילים בחדר הזה, נסגור ונמחק אותו
            if (roomConnections.get(roomId).isEmpty()) {
                roomConnections.remove(roomId);
                activeRooms.remove(roomId); // 👈 מוחק את החדר כך שניתן יהיה ליצור אותו מחדש כשחקן!
                logger.log("INFO", "Room " + roomId + " was removed because all players left.");
            }
        }

        countdownSeconds = 20;
        disconnectTimer = new Timer();
        disconnectTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (countdownSeconds > 0) {
                    eventBus.publish(new DisconnectCountdownEvent(countdownSeconds));
                    if (roomId != null) {
                        broadcastToRoom(roomId, "DISCONNECT_COUNTDOWN:" + countdownSeconds);
                    }
                    countdownSeconds--;
                } else {
                    // 1. עצירת המשימה הספציפית הזו כדי למנוע הדפסה חוזרת בכל שנייה
                    this.cancel();

                    // 2. ביטול ואיפוס ה-Timer הכללי
                    if (disconnectTimer != null) {
                        disconnectTimer.cancel();
                        disconnectTimer = null;
                    }

                    eventBus.publish(new GameStatusEvent(GameStatusEvent.Status.OVER));

                    if (roomId != null) {
                        broadcastToRoom(roomId, "GAME_OVER:TIMEOUT");
                        activeRooms.remove(roomId); // ניקוי החדר מהזיכרון
                    }

                    logger.log("ERROR", "Player timed out. Auto-resign executed.");
                }
            }
        }, 0, 1000);
    }
    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.log("INFO", "Received message from " + conn.getRemoteSocketAddress() + ": " + message);

        if (message.startsWith("CREATE_ROOM:")) {
            // שולף את כל התוכן שאחרי הפקודה כדי לתמוך בשרשור שם המשתמש
            String messageContent = message.substring("CREATE_ROOM:".length()).trim();
            handleCreateRoom(conn, messageContent);
        } else if (message.startsWith("JOIN_ROOM:")) {
            String messageContent = message.substring("JOIN_ROOM:".length()).trim();
            handleJoinRoom(conn, messageContent);
        } else {
            parseAndExecuteCommand(conn, message);
        }
    }

    private void handleCreateRoom(WebSocket conn, String messageContent) {
        // פירוק ההודעה לקבלת מזהה החדר ושם המשתמש (אם סופק)
        String[] parts = messageContent.split(":");
        String roomId = parts[0].trim();
        String username = parts.length > 1 && !parts[1].trim().isEmpty()
                ? parts[1].trim()
                : "Player";

        GameRoom room = activeRooms.computeIfAbsent(roomId, id ->
                new GameRoom(id, GameEngineFactory.createNewGame(eventBus))
        );

        registerConnectionToRoom(conn, roomId);

        PlayerSession session = new PlayerSession(username, 1000);
        GameRoom.Role role = room.addPlayer(session);
        logger.log("INFO", "Room created/joined: " + roomId + " | Assigned Role: " + role + " | User: " + username);

        conn.send("ROOM_JOINED:" + roomId + ":" + role.name());

        // שידור השמות ללקוחות
        broadcastNamesToRoom(roomId);
    }

    private void handleJoinRoom(WebSocket conn, String messageContent) {
        String[] parts = messageContent.split(":");
        String roomId = parts[0].trim();
        String username = parts.length > 1 && !parts[1].trim().isEmpty()
                ? parts[1].trim()
                : "Player";

        GameRoom room = activeRooms.get(roomId);
        if (room == null) {
            logger.log("ERROR", "Join failed: Room " + roomId + " does not exist.");
            conn.send("ERROR:Room does not exist");
            return;
        }

        registerConnectionToRoom(conn, roomId);

        // 1. זיהוי האם השחקן המתחבר הוא אחד מהשחקנים המקוריים של החדר
        GameRoom.Role role;
        boolean isOriginalWhite = room.getWhitePlayer() != null && room.getWhitePlayer().username.equals(username);
        boolean isOriginalBlack = room.getBlackPlayer() != null && room.getBlackPlayer().username.equals(username);

        if (isOriginalWhite) {
            role = GameRoom.Role.WHITE;
        } else if (isOriginalBlack) {
            role = GameRoom.Role.BLACK;
        } else {
            // אם זה שחקן חדש לחלוטין שמצטרף לחדר
            PlayerSession session = new PlayerSession(username, 1000);
            role = room.addPlayer(session);
        }

        // 2. אם השחקן המקורי חזר בזמן, נבטל מיד את טיימר הניתוק ונאפס את ה-UI
        if (role == GameRoom.Role.WHITE || role == GameRoom.Role.BLACK) {
            if (disconnectTimer != null) {
                disconnectTimer.cancel();
                disconnectTimer = null;
                eventBus.publish(new DisconnectCountdownEvent(0));
                broadcastToRoom(roomId, "DISCONNECT_COUNTDOWN:0");
            }
        }

        logger.log("INFO", "User joined room " + roomId + " as " + role + " | User: " + username);

        // 3. שליחת אישור הצטרפות ותפקיד ללקוח
        conn.send("ROOM_JOINED:" + roomId + ":" + role.name());

        // 4. שידור השמות העדכניים לכלל השחקנים בחדר
        broadcastNamesToRoom(roomId);
    }

    private void broadcastNamesToRoom(String roomId) {
        GameRoom room = activeRooms.get(roomId);
        if (room != null) {
            String wName = room.getWhitePlayer() != null ? room.getWhitePlayer().username : "Waiting...";
            String bName = room.getBlackPlayer() != null ? room.getBlackPlayer().username : "Waiting...";
            broadcastToRoom(roomId, "NAMES:" + wName + ":" + bName);
        }
    }

    private void registerConnectionToRoom(WebSocket conn, String roomId) {
        connToRoomMap.put(conn, roomId);
        roomConnections.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(conn);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.log("ERROR", "WebSocket error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        logger.log("INFO", "WebSocket Server started successfully on port: " + getPort());
    }

    // שידור מצב הלוח מהמנוע של החדר הספציפי
    public void broadcastGameStateToRoom(String roomId) {
        GameRoom room = activeRooms.get(roomId);
        if (room == null) return;

        GameSnapshot snapshot = room.getGameEngine().createSnapshot(null);
        String jsonState = gson.toJson(snapshot);

        broadcastToRoom(roomId, jsonState);
    }

    private void broadcastToRoom(String roomId, String data) {
        List<WebSocket> connections = roomConnections.get(roomId);
        if (connections != null) {
            for (WebSocket conn : connections) {
                if (conn.isOpen()) {
                    conn.send(data);
                }
            }
        }
    }

    // ביצוע מהלך בתוך החדר הספציפי
    private void parseAndExecuteCommand(WebSocket conn, String command) {
        try {
            String roomId = connToRoomMap.get(conn);
            if (roomId == null) return;

            GameRoom room = activeRooms.get(roomId);
            if (room == null) return;

            boolean isJump = false;
            String payload = command;
            if (payload.startsWith("J") || payload.startsWith("j")) {
                isJump = true;
                payload = payload.substring(1);
            }

            if (payload.length() != 6) return;

            char colorChar = payload.charAt(0);
            Piece.Color playerColor = (colorChar == 'W' || colorChar == 'w') ? Piece.Color.WHITE : Piece.Color.BLACK;

            String fromStr = payload.substring(2, 4);
            String toStr = payload.substring(4, 6);

            Position from = algebraicToPosition(fromStr);
            Position to = algebraicToPosition(toStr);

            if (isJump) {
                room.getGameEngine().processJumpRequest(to, playerColor);
            } else {
                room.getGameEngine().processMoveRequest(from, to, playerColor);
            }
            GameState gameState = room.getGameEngine().getGameState();
            if (gameState.isGameOver() && !room.isGameOverHandled()) {
                room.setGameOverHandled(true); // מונע כפילויות

                Piece.Color winnerColor = gameState.getWinnerColor();
                handleGameOver(roomId, winnerColor); // עדכון DB ושידור לכולם מיד!
            }

        }
         catch (Exception e) {
            logger.log("ERROR", "Invalid command format: " + command);
        }
    }

    private Position algebraicToPosition(String alg) {
        if (alg == null || alg.length() != 2) {
            throw new IllegalArgumentException("Invalid position length");
        }

        char colChar = Character.toLowerCase(alg.charAt(0));
        char rowChar = alg.charAt(1);

        if (colChar < 'a' || colChar > 'h' || rowChar < '1' || rowChar > '8') {
            throw new IllegalArgumentException("Invalid algebraic position: " + alg);
        }

        int col = colChar - 'a';
        int row = 8 - Character.getNumericValue(rowChar);
        return new Position(row, col);
    }

    public void handleGameOver(String roomId, Piece.Color winnerColor) {
        GameRoom room = activeRooms.get(roomId);
        if (room == null) return;

        PlayerSession whitePlayer = room.getWhitePlayer();
        PlayerSession blackPlayer = room.getBlackPlayer();

        if (whitePlayer != null && blackPlayer != null) {
            // 1. שליפת ה-Elo הנוכחי מה-DB דרך ה-DatabaseManager
            int whiteElo = dbManager.getElo(whitePlayer.username);
            int blackElo = dbManager.getElo(blackPlayer.username);

            // 2. קביעת הניקוד בפועל (1 לניצחון, 0 להפסד)
            double whiteScore = (winnerColor == Piece.Color.WHITE) ? 1.0 : 0.0;
            double blackScore = (winnerColor == Piece.Color.BLACK) ? 1.0 : 0.0;

            // 3. חישוב ה-Elo החדש בעזרת EloCalculator
            int newWhiteElo = EloCalculator.calculate(whiteElo, blackElo, whiteScore);
            int newBlackElo = EloCalculator.calculate(blackElo, whiteElo, blackScore);

            // 4. עדכון ה-Elo בבסיס הנתונים באמצעות DatabaseManager
            dbManager.updateElo(whitePlayer.username, newWhiteElo);
            dbManager.updateElo(blackPlayer.username, newBlackElo);

            System.out.println("[Server] Elo updated successfully for room " + roomId);
        }

        // 5. שידור הודעת סיום לכל הלקוחות בחדר
        broadcastToRoom(roomId, "GAME_OVER:WINNER_" + winnerColor);
    }
}
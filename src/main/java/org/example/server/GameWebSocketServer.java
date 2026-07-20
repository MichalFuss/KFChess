package org.example.server;

import com.google.gson.Gson;
import org.example.engine.GameEngine;
import org.example.events.DisconnectCountdownEvent;
import org.example.events.EventBus;
import org.example.events.GameStatusEvent;
import org.example.models.GameSnapshot;
import org.example.models.Piece;
import org.example.models.Position;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class GameWebSocketServer extends WebSocketServer {

    private final GameEngine gameEngine;
    private final Gson gson;
    private final EventBus eventBus;
    private Timer disconnectTimer;
    private int countdownSeconds = 20;

    public GameWebSocketServer(int port, GameEngine gameEngine, EventBus eventBus) {
        super(new InetSocketAddress(port));
        this.gameEngine = gameEngine;
        this.gson = new Gson();
        this.eventBus = eventBus;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (disconnectTimer != null) {
            disconnectTimer.cancel();
            disconnectTimer = null;
            // ביטול ההתראה במסך (שליחת 0 שניות)
            eventBus.publish(new DisconnectCountdownEvent(0));
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // הלקוח התנתק, מתחילים ספירה לאחור של 20 שניות
        countdownSeconds = 20;
        disconnectTimer = new Timer();
        disconnectTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (countdownSeconds > 0) {
                    eventBus.publish(new DisconnectCountdownEvent(countdownSeconds));
                    countdownSeconds--;
                } else {
                    // עברו 20 שניות
                    disconnectTimer.cancel();
                    eventBus.publish(new GameStatusEvent(GameStatusEvent.Status.OVER));
                    System.out.println("Player timed out. Auto-resign executed.");
                    // כאן אפשר לעדכן ה-ELO ב-DB למפסיד

                }
            }
        }, 0, 1000); // הפעלה כל שנייה (1000ms)
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received message: " + message);
        // ציפייה לפורמט כמו "WQe2e5"
        parseAndExecuteCommand(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket Server started successfully on port: " + getPort());
    }

    /**
     * פונקציה לשידור מצב המשחק העדכני לכל הלקוחות המחוברים
     */
    public void broadcastGameState() {
        // ניצור סנאפשוט של המצב הנוכחי (ללא בחירת משבצת ספציפית)
        GameSnapshot snapshot = gameEngine.createSnapshot(null);
        String jsonState = gson.toJson(snapshot);
        broadcast(jsonState);
    }

    /**
     * פענוח פקודה טקסטואלית והמרתה למהלך במנוע
     */
    /**
     * פענוח פקודה טקסטואלית והמרתה למהלך במנוע
     */
    private void parseAndExecuteCommand(String command) {
        try {
            // חילוץ המחרוזת. לדוגמה: WQe2e5
            if (command.length() != 6) return;

            // עדכון: חילוץ הצבע מהתו הראשון של הפקודה
            char colorChar = command.charAt(0);
            Piece.Color playerColor = (colorChar == 'W' || colorChar == 'w') ? Piece.Color.WHITE : Piece.Color.BLACK;

            // המרת קואורדינטות (e2 -> e5)
            String fromStr = command.substring(2, 4); // e2
            String toStr = command.substring(4, 6);   // e5

            Position from = algebraicToPosition(fromStr);
            Position to = algebraicToPosition(toStr);

            // עדכון: העברת ה-playerColor לתוך ה-Engine לאימות והגנה על התור והכלים
            gameEngine.processMoveRequest(from, to, playerColor);

            // עדכון הלקוחות לאחר ביצוע מהלך
            broadcastGameState();

        } catch (Exception e) {
            System.err.println("Invalid command format: " + command);
        }
    }

    /**
     * ממיר כתיב אלגברי (כמו "e2") ל-Position בלוח הדו-ממדי
     */
    /**
     * ממיר כתיב אלגברי (כמו "e2") ל-Position בלוח הדו-ממדי
     */
    private Position algebraicToPosition(String alg) {
        if (alg == null || alg.length() != 2) {
            throw new IllegalArgumentException("Invalid position length");
        }

        char colChar = Character.toLowerCase(alg.charAt(0));
        char rowChar = alg.charAt(1);

        // וידוא שהאות בטווח 'a'-'h' והספרה בטווח '1'-'8'
        if (colChar < 'a' || colChar > 'h' || rowChar < '1' || rowChar > '8') {
            throw new IllegalArgumentException("Invalid algebraic position: " + alg);
        }

        int col = colChar - 'a';
        int row = 8 - Character.getNumericValue(rowChar);
        return new Position(row, col);
    }
}
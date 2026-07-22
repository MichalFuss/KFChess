package org.example.server;

import com.google.gson.Gson;
import org.example.events.*;
import org.example.models.GameSnapshot;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class GameWebSocketClient extends WebSocketClient {

    private final EventBus eventBus;
    private final Gson gson = new Gson();

    public GameWebSocketClient(URI serverUri, EventBus eventBus) {
        super(serverUri);
        this.eventBus = eventBus;
        this.eventBus.subscribe(new EventListener() {
            @Override
            public void onEvent(GameEvent event) {
                // אם התקבל אירוע אנונימי (האירוע ששלחנו מ-GameWindow בלחיצה על X)
                if (event.getClass().isAnonymousClass()) {
                    close(); // סגירה יזומה של חיבור ה-WebSocket
                }
            }
        });
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("[Client] Connected to WebSocket Server successfully.");
    }
    @Override
    public void onMessage(String message) {
        if (message.startsWith("ROOM_JOINED:")) {
            String[] parts = message.split(":");
            if (parts.length >= 3) {
                String roomId = parts[1];
                String role = parts[2];
                eventBus.publish(new RoomJoinedEvent(roomId, role));
            }
        } else if (message.startsWith("DISCONNECT_COUNTDOWN:")) {
            // פירוק מספר השניות שנשלח מהשרת
            String secondsStr = message.substring("DISCONNECT_COUNTDOWN:".length()).trim();
            int secondsLeft = Integer.parseInt(secondsStr);

            // הפצת האירוע ב-EventBus של הלקוח כדי שה-UI יתעדכן
            eventBus.publish(new DisconnectCountdownEvent(secondsLeft));
        } else if (message.startsWith("GAME_OVER:")) {
            eventBus.publish(new GameStatusEvent(GameStatusEvent.Status.OVER));
        } else if (message.startsWith("NAMES:")) {
            String[] parts = message.split(":", 3);
            if (parts.length >= 3) {
                String whiteName = parts[1].trim();
                String blackName = parts[2].trim();
                eventBus.publish(new org.example.events.NamesUpdatedEvent(whiteName, blackName));
            }
        } else if (message.startsWith("{") && message.endsWith("}")) {
            // פענוח JSON של GameSnapshot שנשלח מהשרת
            try {
                GameSnapshot snapshot = gson.fromJson(message, GameSnapshot.class);
                if (snapshot != null) {
                    eventBus.publish(new GameStateUpdatedEvent(snapshot));
                }
            } catch (Exception e) {
                System.err.println("[Client] Error parsing GameSnapshot JSON: " + e.getMessage());
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("[Client] Disconnected: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("[Client] Error: " + ex.getMessage());
    }
}
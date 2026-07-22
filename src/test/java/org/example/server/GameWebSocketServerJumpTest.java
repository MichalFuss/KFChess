package org.example.server;

import org.example.engine.GameEngine;
import org.example.engine.GameEngineFactory;
import org.example.events.EventBus;
import org.example.models.Piece;
import org.example.models.Position;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameWebSocketServerJumpTest {

    @Test
    void jumpCommandShouldBeProcessedAsJumpWhenFromAndToAreTheSame() throws Exception {
        EventBus eventBus = new EventBus();
        GameWebSocketServer server = new GameWebSocketServer(0, eventBus);

        GameEngine gameEngine = GameEngineFactory.createNewGame(eventBus);
        GameRoom room = new GameRoom("room-jump", gameEngine);

        Field activeRoomsField = GameWebSocketServer.class.getDeclaredField("activeRooms");
        activeRoomsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, GameRoom> activeRooms = (Map<String, GameRoom>) activeRoomsField.get(server);
        activeRooms.put("room-jump", room);

        Field connToRoomMapField = GameWebSocketServer.class.getDeclaredField("connToRoomMap");
        connToRoomMapField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<WebSocket, String> connToRoomMap = (Map<WebSocket, String>) connToRoomMapField.get(server);

        WebSocket conn = mock(WebSocket.class);
        when(conn.isOpen()).thenReturn(true);
        connToRoomMap.put(conn, "room-jump");

        server.onMessage(conn, "JW e2e2");

        assertEquals(1, gameEngine.getGameState().getActiveMoves().size(),
                "קפיצה אמורה להירשם כפעולת תנועה באוויר");
        assertEquals(Piece.Color.WHITE, gameEngine.getGameState().getActiveMoves().get(0).getPiece().getColor());
        assertEquals(new Position(6, 4), gameEngine.getGameState().getActiveMoves().get(0).getFrom());
    }
}

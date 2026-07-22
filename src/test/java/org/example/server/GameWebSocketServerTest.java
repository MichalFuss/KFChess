//package org.example.server;
//
//import org.example.engine.GameEngine;
//import org.example.events.DisconnectCountdownEvent;
//import org.example.events.EventBus;
//import org.example.events.GameStatusEvent;
//import org.example.models.GameSnapshot;
//import org.example.models.Piece;
//import org.example.models.Position;
//import org.java_websocket.WebSocket;
//import org.java_websocket.handshake.ClientHandshake;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.Collections;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//class GameWebSocketServerTest {
//
//    private GameEngine gameEngine;
//    private EventBus eventBus;
//    private WebSocket mockWebSocket;
//    private ClientHandshake mockHandshake;
//    private GameWebSocketServer server;
//
//    @BeforeEach
//    void setUp() {
//        gameEngine = mock(GameEngine.class);
//        eventBus = mock(EventBus.class);
//        mockWebSocket = mock(WebSocket.class);
//        mockHandshake = mock(ClientHandshake.class);
//
//        // יצירת השרת על פורט אקראי/חופשי (0) לצורך בדיקה
//        server = spy(new GameWebSocketServer(0, gameEngine, eventBus));
//
//        // הגדרת Snapshot ברירת מחדל כדי למנוע NullPointerException בעת broadcast
//        GameSnapshot mockSnapshot = new GameSnapshot(8, 8, Collections.emptyList(), null, false, 0L);
//        when(gameEngine.createSnapshot(any())).thenReturn(mockSnapshot);
//
//        // מניעת נסיון שידור אקטיבי לרשת בזמן הטסט
//        doNothing().when(server).broadcast(anyString());
//    }
//
//    @Test
//    void testOnMessage_ValidWhitePlayerMove_ExecutesCommandAndBroadcasts() {
//        // פקודה: שחקן לבן מזיז מ-e2 ל-e5
//        // e2 -> col 4, row 6 (8 - 2)
//        // e5 -> col 4, row 3 (8 - 5)
//        String command = "WQe2e5";
//
//        server.onMessage(mockWebSocket, command);
//
//        Position expectedFrom = new Position(6, 4);
//        Position expectedTo = new Position(3, 4);
//
//        // אימות שהמנוע קיבל את בקשת התנועה עם הנתונים המפוענחים
//        verify(gameEngine, times(1)).processMoveRequest(
//                eq(expectedFrom),
//                eq(expectedTo),
//                eq(Piece.Color.WHITE)
//        );
//
//        // אימות ששודר מצב משחק מעודכן
//        verify(server, times(1)).broadcastGameState();
//    }
//
//    @Test
//    void testOnMessage_ValidBlackPlayerMove_ParsesBlackColorCorrectly() {
//        // פקודה: שחקן שחור מזיז מ-e7 ל-e5
//        // e7 -> col 4, row 1 (8 - 7)
//        String command = "BQe7e5";
//
//        server.onMessage(mockWebSocket, command);
//
//        Position expectedFrom = new Position(1, 4);
//        Position expectedTo = new Position(3, 4);
//
//        verify(gameEngine, times(1)).processMoveRequest(
//                eq(expectedFrom),
//                eq(expectedTo),
//                eq(Piece.Color.BLACK)
//        );
//    }
//
//    @Test
//    void testOnMessage_InvalidCommandLength_Ignored() {
//        // פקודה קצרה/ארוכה מדי (אינה באורך 6 תווים)
//        server.onMessage(mockWebSocket, "WQe2");
//
//        // לא אמורה להפעיל את המנוע
//        verify(gameEngine, never()).processMoveRequest(any(), any(), any());
//    }
//
//    @Test
//    void testOnMessage_MalformedCoordinates_DoesNotCrash() {
//        // פקודה שגויה בלתי ניתנת לפענוח
//        server.onMessage(mockWebSocket, "WQXXXX");
//
//        // השרת אמור לתפוס את החריגה ולמנוע קריסה של התהליך
//        verify(gameEngine, never()).processMoveRequest(any(), any(), any());
//    }
//
//    @Test
//    void testOnClose_StartsDisconnectTimerAndPublishesEvent() throws InterruptedException {
//        // סימולציית התנתקות לקוח
//        server.onClose(mockWebSocket, 1000, "Client disconnected", true);
//
//        // המתנה קצרה של 100ms לוודא שה-TimerTask הופעל פעם ראשונה
//        Thread.sleep(100);
//
//        // אימות שנשלח אירוע ספירה לאחור של 20 שניות
//        verify(eventBus, atLeastOnce()).publish(any(DisconnectCountdownEvent.class));
//    }
//
//    @Test
//    void testOnOpen_CancelsDisconnectTimer_AndResetsUI() {
//        // 1. נתנתק כדי להפעיל טיימר
//        server.onClose(mockWebSocket, 1000, "Disconnected", true);
//
//        // 2. התחברות מחדש
//        server.onOpen(mockWebSocket, mockHandshake);
//
//        // אימות שנשלח אירוע איפוס התראה (0 שניות) למסך
//        verify(eventBus).publish(argThat(event ->
//                event instanceof DisconnectCountdownEvent &&
//                        ((DisconnectCountdownEvent) event).getSecondsLeft() == 0
//        ));
//    }
//}
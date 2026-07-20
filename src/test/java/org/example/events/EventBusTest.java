package org.example.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {

    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new EventBus();
    }

    @Test
    void testSubscribeAndPublish() {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        EventListener listener = event -> callCount.incrementAndGet();
        GameEvent event = new DummyGameEvent();

        // Act
        eventBus.subscribe(listener);
        eventBus.publish(event);

        // Assert
        assertEquals(1, callCount.get(), "המאזין היה אמור לקבל את האירוע שנשלח פעם אחת");
    }

    @Test
    void testSubscribeDuplicate_DoesNotAddTwice() {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        EventListener listener = event -> callCount.incrementAndGet();
        GameEvent event = new DummyGameEvent();

        // Act
        eventBus.subscribe(listener);
        eventBus.subscribe(listener); // ניסיון הרשמה כפולה
        eventBus.publish(event);

        // Assert
        assertEquals(1, callCount.get(), "המאזין שנרשם פעמיים לא אמור לקבל את האירוע פעמיים");
    }

    @Test
    void testUnsubscribe_RemovesListener() {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        EventListener listener = event -> callCount.incrementAndGet();
        GameEvent event = new DummyGameEvent();

        // Act
        eventBus.subscribe(listener);
        eventBus.unsubscribe(listener);
        eventBus.publish(event);

        // Assert
        assertEquals(0, callCount.get(), "מאזין שהוסר לא אמור לקבל אירועים נוספים");
    }

    @Test
    void testMultipleListeners_ReceiveEvent() {
        // Arrange
        AtomicInteger listener1Calls = new AtomicInteger(0);
        AtomicInteger listener2Calls = new AtomicInteger(0);

        EventListener listener1 = event -> listener1Calls.incrementAndGet();
        EventListener listener2 = event -> listener2Calls.incrementAndGet();
        GameEvent event = new DummyGameEvent();

        // Act
        eventBus.subscribe(listener1);
        eventBus.subscribe(listener2);
        eventBus.publish(event);

        // Assert
        assertEquals(1, listener1Calls.get(), "מאזין ראשון אמור לקבל את האירוע");
        assertEquals(1, listener2Calls.get(), "מאזין שני אמור לקבל את האירוע");
    }

    @Test
    void testPublish_NoListeners_DoesNotThrowException() {
        // Arrange
        GameEvent event = new DummyGameEvent();

        // Act & Assert
        assertDoesNotThrow(() -> eventBus.publish(event),
                "שידור אירוע ללא מאזינים רשומים לא אמור לזרוק חריגה");
    }

    // אובייקט דמה של GameEvent לצורך הטסטים
    private static class DummyGameEvent implements GameEvent {
    }
}
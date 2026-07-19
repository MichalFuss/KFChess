package org.example.events;

import java.util.ArrayList;
import java.util.List;

public class EventBus {
    // רשימה של כל האובייקטים שרוצים לקבל עדכונים
    private final List<EventListener> listeners = new ArrayList<>();

    // פונקציה להרשמה לתור (Subscribe)
    public void subscribe(EventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    // פונקציה להסרה מהתור (חשוב כדי למנוע זליגת זיכרון)
    public void unsubscribe(EventListener listener) {
        listeners.remove(listener);
    }

    // הפונקציה שמפיצה את האירוע לכולם (Publish)
    public void publish(GameEvent event) {
        // עוברים על כל המאזינים וקוראים לפונקציה שלהם
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
package org.example.events;

/**
 * אירוע המשודר כאשר הלקוח מקבל אישור מהשרת על הצטרפות או יצירה של חדר.
 */
public class RoomJoinedEvent implements GameEvent {

    private final String roomId;
    private final String role; // WHITE, BLACK, או VIEWER

    public RoomJoinedEvent(String roomId, String role) {
        this.roomId = roomId;
        this.role = role;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRole() {
        return role;
    }
}
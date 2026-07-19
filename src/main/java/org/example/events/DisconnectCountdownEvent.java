package org.example.events;

/**
 * אירוע המשודר כאשר שחקן יריב מתנתק מהשרת ומתחילה ספירה לאחור.
 * האירוע מחזיק את מספר השניות שנותרו עד שהמשחק יסתיים אוטומטית (ניצחון טכני).
 */
public class DisconnectCountdownEvent implements GameEvent {

    private final int secondsLeft;

    public DisconnectCountdownEvent(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }

    /**
     * מחזיר את מספר השניות שנותרו לספירה לאחור.
     * ערך של 0 יכול לסמן שהספירה הסתיימה או בוטלה (למשל, השחקן חזר).
     */
    public int getSecondsLeft() {
        return secondsLeft;
    }

}
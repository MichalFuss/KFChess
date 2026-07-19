package org.example.audio;

import org.example.events.EventBus;
import org.example.events.EventListener;
import org.example.events.GameEvent;
import org.example.events.PlaySoundEvent;
import java.awt.Toolkit;

public class SoundManager implements EventListener {

    public SoundManager(EventBus eventBus) {
        // ברגע שמנהל השמע נוצר, הוא רושם את עצמו ל-Bus כדי לקבל עדכונים
        eventBus.subscribe(this);
    }

    @Override
    public void onEvent(GameEvent event) {
        // מסננים: אנחנו מתעניינים רק באירועי שמע
        if (event instanceof PlaySoundEvent) {
            PlaySoundEvent soundEvent = (PlaySoundEvent) event;
            playSound(soundEvent.getSoundType());
        }
    }

    private void playSound(String soundType) {
        // שלד פשוט לניהול סוגי הצלילים השונים
        switch (soundType) {
            case "CAPTURE":
                // הדפסה לקונסול כדי שנוכל לבדוק שהלוגיקה והארכיטקטורה עובדות
                System.out.println("🔊 [SoundManager] מנגן צליל: אכילה!");

                // אפשרות חמודה לשלב זה: שימוש בצפצוף הדיפולטיבי של מערכת ההפעלה
                Toolkit.getDefaultToolkit().beep();
                break;

            case "MOVE":
                System.out.println("🔊 [SoundManager] מנגן צליל: תזוזה פשוטה");
                Toolkit.getDefaultToolkit().beep();
                break;

            case "START":
                System.out.println("🔊 [SoundManager] מנגן צליל: תחילת משחק");
                break;

            default:
                System.out.println("🔊 [SoundManager] צליל לא מוכר: " + soundType);
                break;
        }
    }
}
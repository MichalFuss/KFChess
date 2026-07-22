package org.example.rules;

public class EloCalculator {

    // מקדם ה-K הדיפולטיבי - קובע כמה תנודתי יהיה השינוי בניקוד
    private static final int DEFAULT_K_FACTOR = 32;

    /**
     * מחשב את ה-Elo החדש של שחקן יחיד.
     *
     * @param playerRating   הדירוג הנוכחי של השחקן
     * @param opponentRating הדירוג הנוכחי של היריב
     * @param actualScore    1.0 לניצחון, 0.5 לתיקו, 0.0 להפסד
     * @return ה-Elo החדש (מעוגל למספר שלם)
     */
    public static int calculate(int playerRating, int opponentRating, double actualScore) {
        return calculate(playerRating, opponentRating, actualScore, DEFAULT_K_FACTOR);
    }

    /**
     * גרסה גמישה המאפשרת לקבוע K-Factor מותאם אישית.
     */
    public static int calculate(int playerRating, int opponentRating, double actualScore, int kFactor) {
        // 1. חישוב התוצאה הצפויה (בין 0.0 ל-1.0)
        double expectedScore = 1.0 / (1.0 + Math.pow(10, (opponentRating - playerRating) / 400.0));

        // 2. חישוב הניקוד החדש
        double newRating = playerRating + kFactor * (actualScore - expectedScore);

        // 3. החזרת ערך מעוגל למספר שלם הקרוב ביותר
        return (int) Math.round(newRating);
    }
}
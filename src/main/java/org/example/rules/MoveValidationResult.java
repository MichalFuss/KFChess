package org.example.rules;

public enum MoveValidationResult {
    VALID("המהלך חוקי"),
    INVALID_SAME_SQUARE("לא ניתן להינוע לאותה משבצת"),
    PIECE_IN_REST("הכלי נמצא במנוחה"),
    OUT_OF_BOUNDS("המשבצת המטרה מחוץ לגבולות הלוח"),
    NO_PIECE("אין כלי למהלך"),
    INVALID_PIECE_KIND("סוג כלי לא ידוע"),
    INVALID_MOVE_PATTERN("תנועה לא חוקית - תנועת הכלי אינה תואמת לחוקי התנועה"),
    BLOCKED_BY_PIECE("הנתיב או המשבצת המטרה חסומים על ידי כלי"),
    BLOCKED_BY_FRIENDLY("המשבצת המטרה תפוסה על ידי כלי משלנו"),
    PATH_BLOCKED("הנתיב לא פנוי"),
    GAME_OVER("GAME OVER"),
    INVALID_JUMP("תנועה לא חוקית - הכלי אינו יכול לקפוץ מעל כלי אחר"),
    INVALID_CASTLING("תנועה לא חוקית - לא ניתן לבצע הצרחה"),
    PIECE_IN_MOTION("הכלי בתנועה"),
    NO_PIECE_AT_SOURCE("אין כלי במשבצת המקור");

    private final String reason;

    MoveValidationResult(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public boolean isValid() {
        return this == VALID;
    }
}

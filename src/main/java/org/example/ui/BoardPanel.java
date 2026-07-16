package org.example.ui;

import org.example.models.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardPanel extends JPanel {
    private Img boardImg;

    // קובץ מטמון שיחזיק את כל פריימי האנימציה לכל כלי ולכל מצב
    private final Map<String, List<Img>> animationCache = new HashMap<>();

    public static final int CELL_SIZE = 100; // גודל משבצת בפיקסלים
    private static final long FRAME_DURATION_MS = 150; // מהירות האנימציה (150ms לכל פრიים)

    // המשתנה שמחזיק את תמונת המצב העדכנית מה-Engine
    private GameSnapshot currentSnapshot;

    public BoardPanel(){
        // הגדרת גודל דיפולטיבי ראשוני של 8x8
        this(8,8);
    }

    public BoardPanel(int a, int b) {
        setPreferredSize(new Dimension(a * CELL_SIZE, b * CELL_SIZE));

        // 1. טעינת תמונת הלוח מתוך ה-resources (בהנחת לוח סטנדרטי)
        try {
            this.boardImg = new Img();
            this.boardImg.read("src/main/resources/board.png",
                    new Dimension(a * CELL_SIZE, b * CELL_SIZE),
                    false, null);
        } catch (Exception e) {
            this.boardImg = null; // אם חסר קובץ, נשתמש בלוח הגיבוי הדיגיטלי
        }

        // 2. טעינה מראש של כל האנימציות והכלים מה-resources לזיכרון
        preloadAllAnimations();
    }

    /**
     * מתודה שתיקרא מה-Timer ב-GameWindow כדי לעדכן את הפאנל בפריים החדש
     */
    public void updateSnapshot(GameSnapshot snapshot) {
        this.currentSnapshot = snapshot;

        // עדכון דינמי של גודל הפאנל במידה ומימדי הלוח שהתקבלו שונים מ-8x8
        Dimension currentDim = getPreferredSize();
        int targetWidth = snapshot.getBoardWidth() * CELL_SIZE;
        int targetHeight = snapshot.getBoardHeight() * CELL_SIZE;
        if (currentDim.width != targetWidth || currentDim.height != targetHeight) {
            setPreferredSize(new Dimension(targetWidth, targetHeight));
            revalidate();
        }

        this.repaint(); // קריאה לציור מחדש של המסך
    }

    /**
     * טוען מראש את כל קבצי האנימציות של כל הכלים לזיכרון
     */
    private void preloadAllAnimations() {
        String[] pieces = {
                "KB", "KW", // King
                "QB", "QW", // Queen
                "RB", "RW", // Rook
                "NB", "NW", // Knight
                "BB", "BW", // Bishop
                "PB", "PW"  // Pawn
        };

        String[] states = {"idle", "move", "jump", "short_rest", "long_rest"};

        for (String pieceFolder : pieces) {
            for (String stateFolder : states) {
                List<Img> frames = loadFrames(pieceFolder, stateFolder);
                if (!frames.isEmpty()) {
                    animationCache.put(pieceFolder + "_" + stateFolder, frames);
                }
            }
        }
    }

    /**
     * טוען קבצי תמונות ממוספרים ומטפל במבנה התיקיות
     */
    private List<Img> loadFrames(String pieceFolder, String stateFolder) {
        List<Img> frames = new ArrayList<>();
        int frameNum = 1;

        while (true) {
            String path = "src/main/resources/pieces2/" + pieceFolder + "/states/" + stateFolder + "/sprites/" + frameNum + ".png";
            File file = new File(path);

            if (file.exists()) {
                try {
                    Img img = new Img().read(path, new Dimension(CELL_SIZE, CELL_SIZE), true, null);
                    if (img != null && img.get() != null) {
                        frames.add(img);
                        frameNum++;
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    break;
                }
            } else {
                break; // הגענו לקצה האנימציה
            }
        }
        return frames;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentSnapshot == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. ציור רקע הלוח
        if (boardImg != null && boardImg.get() != null) {
            g2d.drawImage(boardImg.get(), 0, 0, null);
        } else {
            drawFallbackGrid(g2d, currentSnapshot.getBoardWidth(), currentSnapshot.getBoardHeight());
        }

        // 2. סימון משבצת נבחרת
        Position selected = currentSnapshot.getSelectedPosition();
        if (selected != null) {
            g2d.setColor(new Color(255, 255, 0, 80));
            g2d.fillRect(selected.getCol() * CELL_SIZE, selected.getRow() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            g2d.setColor(new Color(255, 255, 0, 200));
            g2d.drawRect(selected.getCol() * CELL_SIZE, selected.getRow() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }

        // 3. ציור הכלים והשפעות ה-Cooldown/Rest
        for (PieceSnapshot p : currentSnapshot.getPieces()) {
            int x = (int) p.getX();
            int y = (int) p.getY();

            // א. ציור רקע צהוב דועך (משופר: בודק אם הכלי באחד ממצבי המנוחה)
            if (p.getState() == Piece.State.SHORT_REST || p.getState() == Piece.State.LONG_REST) {
                long remaining = p.getCooldownEndTime() - currentSnapshot.getGameTimeMillis();
                if (remaining > 0) {
                    // חישוב שקיפות (Alpha) שדועכת בצורה חלקה ככל שהזמן עובר
                    float alpha = Math.max(0.0f, Math.min(0.5f, (float) remaining / 3000.0f));
                    g2d.setColor(new Color(1.0f, 1.0f, 0.0f, alpha));
                    g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }

            // ב. ציור הכלי - מצייר את הכלי באטימות מלאה מעל הרקע הצהוב
            Img pieceImg = getAnimationImg(p);
            if (pieceImg != null && pieceImg.get() != null) {
                g2d.drawImage(pieceImg.get(), x, y, null);
            } else {
                drawFallbackPiece(g2d, p, x, y);
            }

            // ג. ציור טקסט הטיימר מעל הכלי (משופר: מותאם למצבי המנוחה החדשים)
            if (p.getState() == Piece.State.SHORT_REST || p.getState() == Piece.State.LONG_REST) {
                long timeLeft = p.getCooldownEndTime() - currentSnapshot.getGameTimeMillis();
                if (timeLeft > 0) {
                    // הוספת צל כהה קטן מאחורי הטקסט הלבן כדי לשמור על קריאות מעל הרקע הצהוב
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.drawString(String.format("%.1fs", timeLeft / 1000.0), x + 6, y + 21);

                    g2d.setColor(Color.WHITE);
                    g2d.drawString(String.format("%.1fs", timeLeft / 1000.0), x + 5, y + 20);
                }
            }
        }
    }

    /**
     * פונקציית העזר לבחירת הפריים הנכון מה-Cache
     */
    private Img getAnimationImg(PieceSnapshot piece) {
        String key = getPieceCacheKey(piece.getKind(), piece.getColor());
        String stateFolder = getStateFolderName(piece.getState(), piece.getKind());

        List<Img> frames = animationCache.get(key + "_" + stateFolder);
        if (frames == null || frames.isEmpty()) {
            // הגנה: אם אין אנימציה למצב הנוכחי, ננסה לחזור לאנימציית idle
            frames = animationCache.get(key + "_idle");
        }

        if (frames == null || frames.isEmpty()) return null;

        // הנפשה ברצף לפי הזמן הכללי של המשחק
        long gameTime = currentSnapshot.getGameTimeMillis();
        int frameIndex = (int) ((gameTime / FRAME_DURATION_MS) % frames.size());
        return frames.get(frameIndex);
    }

    /**
     * פונקציית ה-Key המקורית - מותאמת לקבלת הפרמטרים מה-Snapshot
     */
    private String getPieceCacheKey(Piece.Kind kind, Piece.Color color) {
        char kindChar = 'P';
        switch (kind) {
            case KING: kindChar = 'K'; break;
            case QUEEN: kindChar = 'Q'; break;
            case ROOK: kindChar = 'R'; break;
            case KNIGHT: kindChar = 'N'; break;
            case BISHOP: kindChar = 'B'; break;
            case PAWN: kindChar = 'P'; break;
        }
        char colorChar = (color == Piece.Color.WHITE) ? 'W' : 'B';
        return "" + kindChar + colorChar; // מחזיר מפתח כמו KW, KB
    }

    /**
     * פונקציית מיפוי התיקיות המעודכנת - עכשיו יש מיפוי ישיר, חד וחלק!
     */
    private String getStateFolderName(Piece.State state, Piece.Kind kind) {
        if (state == null) return "idle";
        switch (state) {
            case MOVING:
                return "move";
            case JUMPING:
                return "jump";
            case SHORT_REST:
                return "short_rest";
            case LONG_REST:
                return "long_rest";
            case IDLE:
            default:
                return "idle";
        }
    }

    /**
     * פונקציית ציור הגריד הדיפולטיבית כשיש בעיה בטעינת התמונות
     */
    private void drawFallbackGrid(Graphics2D g, int width, int height) {
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if ((r + c) % 2 == 0) {
                    g.setColor(Color.LIGHT_GRAY);
                } else {
                    g.setColor(Color.DARK_GRAY);
                }
                g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    /**
     * מצייר כלי שחמט פשוט כגיבוי במידה והתמונות לא נטענו מהדיסק
     */
    private void drawFallbackPiece(Graphics2D g2d, PieceSnapshot piece, int x, int y) {
        if (piece.getColor() == Piece.Color.WHITE) {
            g2d.setColor(new Color(240, 240, 240));
        } else {
            g2d.setColor(new Color(60, 60, 60));
        }
        g2d.fillOval(x + 15, y + 15, CELL_SIZE - 30, CELL_SIZE - 30);

        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x + 15, y + 15, CELL_SIZE - 30, CELL_SIZE - 30);

        if (piece.getColor() == Piece.Color.WHITE) {
            g2d.setColor(Color.BLACK);
        } else {
            g2d.setColor(Color.WHITE);
        }
        g2d.setFont(new Font("Arial", Font.BOLD, 18));

        String text = getPieceCacheKey(piece.getKind(), piece.getColor());
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();

        g2d.drawString(text,
                x + (CELL_SIZE - textWidth) / 2,
                y + (CELL_SIZE + textHeight) / 2 - 2
        );
    }
}
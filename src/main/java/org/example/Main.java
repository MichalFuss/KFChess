package org.example;

import org.example.engine.GameEngine;
import org.example.input.BoardMapper;
import org.example.io.CommandParser;
import org.example.input.GameController;
import org.example.models.Board;
import org.example.models.GameState;
import org.example.realtime.RealTimeArbiter;
import org.example.io.BoardParser; // ייבוא ה-Parser שכתבת
import org.example.ui.GameWindow;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 1. קריאת ופענוח הלוח הדינמי מהקלט
        List<String> boardLines = new ArrayList<>();

        if (scanner.hasNextLine()) {
            String firstLine = scanner.nextLine().trim();
            // אם ה-VPL מתחיל בכותרת "Board:"
            if (firstLine.equalsIgnoreCase("Board:")) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.equalsIgnoreCase("Commands:") || line.isEmpty()) {
                        break; // סיימנו לקרוא את הלוח, עוברים לפקודות
                    }
                    boardLines.add(line);
                }
            }
        }

        Board board;
        try {
            // שימוש ב-Parser שכתבת כדי לבנות את הלוח הדינמי
            board = BoardParser.parse(boardLines);

            //  BoardMapper.setBoardDimensions(board.getWidth(), board.getHeight());    // -----------------------------------------------------------------


        } catch (IllegalArgumentException e) {
            // אם ה-Parser זרק שגיאה (UNKNOWN_TOKEN או ROW_WIDTH_MISMATCH) - נדפיס אותה ונצא
            System.out.println(e.getMessage());
            scanner.close();
            return;
        }

        // 2. אתחול הרכיבים עם הלוח האמיתי שפוענח
        GameState gameState = new GameState(board);
        RealTimeArbiter realTimeArbiter = new RealTimeArbiter();
        GameEngine gameEngine = new GameEngine(gameState, realTimeArbiter);
        GameController gameController = new GameController(board, gameEngine);

        CommandParser commandParser = new CommandParser(gameController, gameEngine);


        //CommandParser commandParser = new CommandParser(gameController, gameEngine);

        // --- הוספת שורת האתחול של הממשק הגרפי ---
        SwingUtilities.invokeLater(() -> new GameWindow(gameState, gameController));

        // 3. לולאה לקריאת פקודות המשך (נשארת כפי שהיא)
        while (scanner.hasNextLine()) {

            // 3. לולאה לקריאת פקודות המשך
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.equals("exit")) {
                    break;
                }
                // העברת השורה ל-Parser
                commandParser.parseAndExecute(line);
            }

            scanner.close();
        }
    }
}
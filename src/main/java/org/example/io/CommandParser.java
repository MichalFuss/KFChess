package org.example.io;

import org.example.engine.GameEngine;
import org.example.input.GameController;

import java.util.Scanner;

public class CommandParser {
    private final GameController gameController;
    private final GameEngine gameEngine;

    public CommandParser(GameController gameController, GameEngine gameEngine) {
        this.gameController = gameController;
        this.gameEngine = gameEngine;
    }

    public void parseAndExecute(String line) {
        if (line == null || line.trim().isEmpty()) return;

        // שימוש ב-Scanner כדי לפרק את השורה לפקודה ופרמטרים
        Scanner scanner = new Scanner(line);
        if (!scanner.hasNext()) return;

        String command = scanner.next();

        switch (command) {
            case "click":
                if (scanner.hasNextInt()) {
                    int x = scanner.nextInt();
                    if (scanner.hasNextInt()) {
                        int y = scanner.nextInt();
                        gameController.handleClick(x, y);
                    }
                }
                break;

            case "advance":
            case "wait":
                if (scanner.hasNextInt()) {
                    int ms = scanner.nextInt();
                    gameController.advanceTime(ms);
                }
                break;

            case "status":
                System.out.println(gameEngine.getGameStatusSnapshot());
                break;

            case "print":
                // דוגמה לטיפול בפקודת משנה (כמו 'print board')
                if (scanner.hasNext("board")) {
                    gameEngine.printBoardSnapshot();
                }
                break;

            case "jump":
                if (scanner.hasNextInt()) {
                    int x = scanner.nextInt();
                    if (scanner.hasNextInt()) {
                        int y = scanner.nextInt();
                        gameController.handleJump(x, y);
                    }
                }
                break;

            default:
                System.out.println("Unknown command: " + command);
                break;
        }
        scanner.close();
    }
}
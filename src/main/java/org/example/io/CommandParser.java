package org.example.io;

import org.example.engine.GameEngine;
import org.example.input.GameController;
import org.example.models.Piece; // ייבוא ה-Color עבור חתימת הפונקציה

import java.util.Scanner;

public class CommandParser {
    private final GameController gameController;
    private final GameEngine gameEngine;

    public CommandParser(GameController gameController, GameEngine gameEngine) {
        this.gameController = gameController;
        this.gameEngine = gameEngine;
    }

    // עדכון: הוספת Piece.Color playerColor לפרמטרים כדי לדעת מי מבצע את פקודת הטקסט
    public void parseAndExecute(String line, Piece.Color playerColor) {
        if (line == null || line.trim().isEmpty()) return;

        Scanner scanner = new Scanner(line);
        if (!scanner.hasNext()) return;

        String command = scanner.next();

        switch (command) {
            case "click":
                if (scanner.hasNextInt()) {
                    int x = scanner.nextInt();
                    if (scanner.hasNextInt()) {
                        int y = scanner.nextInt();
                        // מעבירים את הצבע שקיבלנו ל-Controller
                        gameController.handleClick(x, y, playerColor);
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
                if (scanner.hasNext("board")) {
                    gameEngine.printBoardSnapshot();
                }
                break;

            case "jump":
                if (scanner.hasNextInt()) {
                    int x = scanner.nextInt();
                    if (scanner.hasNextInt()) {
                        int y = scanner.nextInt();
                        // מעבירים את הצבע שקיבלנו ל-Controller גם פה
                        gameController.handleJump(x, y, playerColor);
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
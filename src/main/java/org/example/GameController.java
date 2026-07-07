package org.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameController {
    private final Board board;
    private Position selectedPosition;
    private long gameTimeMillis;
    private boolean isGameOver;

    private final List<ActiveMove> activeMoves;
    private static final long MOVE_DURATION_PER_SQUARE = 1000;
    private static final long JUMP_DURATION = 1000;

    public GameController(Board board) {
        this.board = board;
        this.selectedPosition = null;
        this.gameTimeMillis = 0;
        this.activeMoves = new ArrayList<>();
    }

    public void handleJump(int x, int y) {
        if (isGameOver) return;

        int row = y / Board.CELL_SIZE;
        int col = x / Board.CELL_SIZE;
        Position pos = new Position(row, col);

        if (!board.isWithinBounds(pos)) return;

        Piece piece = board.getPiece(pos);
        if (piece == null) return;

        if (isPieceMovingFrom(pos)) return;

        ActiveMove threateningEnemyMove = null;
        for (ActiveMove move : activeMoves) {
            if (move.getTo().equals(pos) && move.getPiece().getColor() != piece.getColor()) {
                long moveStartTime = move.getArrivalTimeMillis() - (calculateDistance(move.getFrom(), move.getTo()) * MOVE_DURATION_PER_SQUARE);
                if (this.gameTimeMillis > moveStartTime) {
                    threateningEnemyMove = move;
                    break;
                }
            }
        }

        if (threateningEnemyMove != null) {
            Piece targetPiece = board.getPiece(threateningEnemyMove.getTo());
            if (targetPiece != null && targetPiece.getType() == Piece.Type.KING) {
                isGameOver = true;
            }

            board.movePiece(threateningEnemyMove.getFrom(), threateningEnemyMove.getTo());

            Piece movedPiece = threateningEnemyMove.getPiece();
            if (movedPiece.getType() == Piece.Type.PAWN) {
                int targetRow = threateningEnemyMove.getTo().getRow();
                boolean isWhitePromotion = (movedPiece.getColor() == Piece.Color.WHITE && targetRow == 0);
                boolean isBlackPromotion = (movedPiece.getColor() == Piece.Color.BLACK && targetRow == board.getHeight() - 1);

                if (isWhitePromotion || isBlackPromotion) {
                    board.setPiece(targetRow, threateningEnemyMove.getTo().getCol(), new Piece(movedPiece.getColor(), Piece.Type.QUEEN));
                }
            }

            activeMoves.remove(threateningEnemyMove);
            selectedPosition = null;
            return;
        }

        long arrivalTime = this.gameTimeMillis + JUMP_DURATION;
        ActiveMove jump = new ActiveMove(pos, pos, piece, arrivalTime, true);
        activeMoves.add(jump);

        triggerAirCaptures();
        selectedPosition = null;
    }

    public void handleClick(int x, int y) {
        if (isGameOver) return;

        int row = y / Board.CELL_SIZE;
        int col = x / Board.CELL_SIZE;
        Position clickedPos = new Position(row, col);

        if (!board.isWithinBounds(clickedPos)) return;

        Piece clickedPiece = board.getPiece(clickedPos);

        if (selectedPosition == null) {
            if (clickedPiece != null && !isPieceMovingFrom(clickedPos)) {
                selectedPosition = clickedPos;
            }
            return;
        }

        Piece selectedPiece = board.getPiece(selectedPosition);

        if (clickedPiece != null && clickedPiece.getColor() == selectedPiece.getColor()) {
            if (!isPieceMovingFrom(clickedPos)) {
                selectedPosition = clickedPos;
            }
        } else {
            Piece.Color opponentColor = (selectedPiece.getColor() == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;

            if (!isColorMoving(opponentColor) && !isPieceMovingTo(clickedPos) && isValidMove(selectedPosition, clickedPos, selectedPiece)) {
                int distance = calculateDistance(selectedPosition, clickedPos);
                long totalTravelTime = distance * MOVE_DURATION_PER_SQUARE;
                long arrivalTime = this.gameTimeMillis + totalTravelTime;

                activeMoves.add(new ActiveMove(selectedPosition, clickedPos, selectedPiece, arrivalTime, false));
                triggerAirCaptures();
            }
            selectedPosition = null;
        }
    }

    private void triggerAirCaptures() {
        ActiveMove activeJump = null;
        for (ActiveMove move : activeMoves) {
            if (move.isJump()) {
                activeJump = move;
                break;
            }
        }

        if (activeJump == null) return;

        Iterator<ActiveMove> iterator = activeMoves.iterator();
        while (iterator.hasNext()) {
            ActiveMove move = iterator.next();
            if (!move.isJump() && move.getTo().equals(activeJump.getTo()) && move.getPiece().getColor() != activeJump.getPiece().getColor()) {
                long enemyDistance = calculateDistance(move.getFrom(), move.getTo());
                long enemyStartTime = move.getArrivalTimeMillis() - (enemyDistance * MOVE_DURATION_PER_SQUARE);
                long jumpStartTime = activeJump.getArrivalTimeMillis() - JUMP_DURATION;

                if (jumpStartTime == enemyStartTime) {
                    board.setPiece(move.getFrom().getRow(), move.getFrom().getCol(), null);
                    iterator.remove();
                }
            }
        }
    }

    private int calculateDistance(Position from, Position to) {
        int deltaRow = Math.abs(to.getRow() - from.getRow());
        int deltaCol = Math.abs(to.getCol() - from.getCol());
        return Math.max(deltaRow, deltaCol);
    }

    private boolean isPieceMovingFrom(Position pos) {
        for (ActiveMove move : activeMoves) {
            if (move.getFrom().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPieceMovingTo(Position pos) {
        for (ActiveMove move : activeMoves) {
            if (move.getTo().equals(pos) && !move.isJump()) {
                return true;
            }
        }
        return false;
    }

    private boolean isSquareOccupiedByActiveMove(Position pos, Piece.Color movingColor) {
        for (ActiveMove move : activeMoves) {
            if (move.getTo().equals(pos) && move.getPiece().getColor() == movingColor && !move.isJump()) {
                return true;
            }
        }
        return false;
    }

    private boolean isPathClearWithActiveMoves(Position from, Position to, Piece.Color pieceColor) {
        int startRow = from.getRow();
        int startCol = from.getCol();
        int endRow = to.getRow();
        int endCol = to.getCol();

        int stepRow = Integer.compare(endRow - startRow, 0);
        int stepCol = Integer.compare(endCol - startCol, 0);

        int currentRow = startRow + stepRow;
        int currentCol = startCol + stepCol;

        while (currentRow != endRow || currentCol != endCol) {
            Position currentPos = new Position(currentRow, currentCol);

            if (board.getPiece(currentPos) != null) return false;
            if (isSquareOccupiedByActiveMove(currentPos, pieceColor)) return false;

            currentRow += stepRow;
            currentCol += stepCol;
        }
        return true;
    }

    private boolean isValidMove(Position from, Position to, Piece piece) {
        if (from.equals(to)) return false;

        if (piece.getType() == Piece.Type.PAWN) {
            return PawnMoveValidator.isValidPawnMove(from, to, piece, board, this.activeMoves);
        }

        int deltaRow = to.getRow() - from.getRow();
        int deltaCol = to.getCol() - from.getCol();

        if (!piece.getType().isValidMoveShape(deltaRow, deltaCol)) return false;

        Piece targetPiece = board.getPiece(to);
        if (targetPiece != null && targetPiece.getColor() == piece.getColor()) return false;
        if (isSquareOccupiedByActiveMove(to, piece.getColor())) return false;

        if (piece.getType() != Piece.Type.KNIGHT) {
            if (!isPathClearWithActiveMoves(from, to, piece.getColor())) return false;
        }

        return true;
    }

    public void advanceTime(long millis) {
        if (millis <= 0) return;

        this.gameTimeMillis += millis;

        List<ActiveMove> completedMoves = new ArrayList<>();
        List<ActiveMove> completedJumps = new ArrayList<>();

        Iterator<ActiveMove> iterator = activeMoves.iterator();
        while (iterator.hasNext()) {
            ActiveMove move = iterator.next();
            if (move.isComplete(this.gameTimeMillis)) {
                if (move.isJump()) {
                    completedJumps.add(move);
                } else {
                    completedMoves.add(move);
                }
                iterator.remove();
            }
        }

        for (ActiveMove normalMove : completedMoves) {
            boolean capturedInAir = false;

            for (ActiveMove jumpMove : completedJumps) {
                if (jumpMove.getTo().equals(normalMove.getTo()) && jumpMove.getPiece().getColor() != normalMove.getPiece().getColor()) {
                    capturedInAir = true;
                    break;
                }
            }

            if (capturedInAir) {
                continue;
            }

            Piece targetPiece = board.getPiece(normalMove.getTo());
            if (targetPiece != null && targetPiece.getType() == Piece.Type.KING) {
                isGameOver = true;
            }

            board.movePiece(normalMove.getFrom(), normalMove.getTo());

            Piece movedPiece = normalMove.getPiece();
            if (movedPiece.getType() == Piece.Type.PAWN) {
                int targetRow = normalMove.getTo().getRow();
                boolean isWhitePromotion = (movedPiece.getColor() == Piece.Color.WHITE && targetRow == 0);
                boolean isBlackPromotion = (movedPiece.getColor() == Piece.Color.BLACK && targetRow == board.getHeight() - 1);

                if (isWhitePromotion || isBlackPromotion) {
                    Piece queenPromotion = new Piece(movedPiece.getColor(), Piece.Type.QUEEN);
                    board.setPiece(targetRow, normalMove.getTo().getCol(), queenPromotion);
                }
            }
        }
    }

    private boolean isColorMoving(Piece.Color color) {
        for (ActiveMove move : activeMoves) {
            if (move.getPiece().getColor() == color && !move.isJump()) {
                return true;
            }
        }
        return false;
    }

    public void printBoard() {
        board.print();
    }
}
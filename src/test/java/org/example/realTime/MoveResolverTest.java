package org.example.realTime;

import org.example.models.*;
import org.example.realtime.MoveResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MoveResolverTest {

    private GameState gameState;
    private Board board;

    @BeforeEach
    void setUp() {
        // Initializes a standard 8x8 board and game state for each test
        board = new Board(8, 8);
        gameState = new GameState(board);
    }

    /**
     * Verifies that a normal move landing successfully updates piece coordinates,
     * clears its origin cell, and applies the LONG_REST cooldown.
     */
    @Test
    void testStandardLandingNormalMove() {
        Position from = new Position(4, 4);
        Position to = new Position(3, 4);
        Piece pawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, pawn);

        ActiveMove move = new ActiveMove(from, to, pawn, 1000, false);
        List<ActiveMove> completedNormalMoves = new ArrayList<>();
        completedNormalMoves.add(move);
        List<ActiveMove> completedJumps = new ArrayList<>();

        boolean result = MoveResolver.resolveCompletedMoves(gameState, completedNormalMoves, completedJumps);

        assertFalse(result, "Game should not be over");
        assertNull(board.getPiece(from), "The starting position should be cleared");
        assertEquals(pawn, board.getPiece(to), "The piece should be at the destination position");
        assertEquals(Piece.State.LONG_REST, pawn.getState(), "Normal move should apply LONG_REST cooldown");
    }

    /**
     * Verifies that a jump-in-place retains its original position and applies the SHORT_REST cooldown.
     */
    @Test
    void testStandardLandingJump() {
        Position pos = new Position(3, 3);
        Piece knight = new Piece("wN_1", Piece.Color.WHITE, Piece.Kind.KNIGHT, pos);
        board.setPiece(pos, knight);

        ActiveMove move = new ActiveMove(pos, pos, knight, 1000, true);
        List<ActiveMove> completedNormalMoves = new ArrayList<>();
        List<ActiveMove> completedJumps = new ArrayList<>();
        completedJumps.add(move);

        boolean result = MoveResolver.resolveCompletedMoves(gameState, completedNormalMoves, completedJumps);

        assertFalse(result, "Game should not be over");
        assertEquals(knight, board.getPiece(pos), "The piece should stay on its square");
        assertEquals(Piece.State.SHORT_REST, knight.getState(), "Jump should apply SHORT_REST cooldown");
    }

    /**
     * Tests that landing on an opponent piece updates the score and transitions the target to CAPTURED.
     */
    @Test
    void testNormalMoveLandingWithCapture() {
        Position from = new Position(4, 4);
        Position to = new Position(4, 7);
        Piece rook = new Piece("wR_1", Piece.Color.WHITE, Piece.Kind.ROOK, from);
        Piece targetPiece = new Piece("bN_1", Piece.Color.BLACK, Piece.Kind.KNIGHT, to);

        board.setPiece(from, rook);
        board.setPiece(to, targetPiece);

        ActiveMove move = new ActiveMove(from, to, rook, 1000, false);
        List<ActiveMove> completedNormalMoves = new ArrayList<>();
        completedNormalMoves.add(move);
        List<ActiveMove> completedJumps = new ArrayList<>();

        boolean result = MoveResolver.resolveCompletedMoves(gameState, completedNormalMoves, completedJumps);

        assertFalse(result, "Game should not be over");
        assertNull(board.getPiece(from), "The starting position should be empty");
        assertEquals(rook, board.getPiece(to), "The rook should have captured and taken the destination square");
        assertEquals(Piece.State.CAPTURED, targetPiece.getState(), "The captured piece must transition to CAPTURED state");
        assertTrue(gameState.getWhiteScore() > 0, "White score should have increased");
    }

    /**
     * Confirms that moves are handled in chronological order of their arrivalTime.
     * * Scenario:
     * - White Pawn A (starts at 1,1) moves to (2,1) [Arrives at t=1000]
     * - Black Bishop B (starts at 3,2) moves to (2,1) [Arrives at t=1500]
     * Chronological evaluation should make Pawn A land first, and then Bishop B lands, capturing Pawn A.
     */
    @Test
    void testChronologicalOrderingOfCompletedMoves() {
        Position fromA = new Position(1, 1);
        Position to = new Position(2, 1);
        Piece pawnA = new Piece("wP_A", Piece.Color.WHITE, Piece.Kind.PAWN, fromA);

        Position fromB = new Position(3, 2);
        Piece bishopB = new Piece("bB_B", Piece.Color.BLACK, Piece.Kind.BISHOP, fromB);

        board.setPiece(fromA, pawnA);
        board.setPiece(fromB, bishopB);

        ActiveMove moveA = new ActiveMove(fromA, to, pawnA, 1000, false);
        ActiveMove moveB = new ActiveMove(fromB, to, bishopB, 1500, false);

        List<ActiveMove> completedNormalMoves = new ArrayList<>();
        // Add out of order to ensure the sorting logic functions correctly
        completedNormalMoves.add(moveB);
        completedNormalMoves.add(moveA);

        List<ActiveMove> completedJumps = new ArrayList<>();

        boolean result = MoveResolver.resolveCompletedMoves(gameState, completedNormalMoves, completedJumps);

        assertFalse(result);
        assertNull(board.getPiece(fromA));
        assertNull(board.getPiece(fromB));
        assertEquals(bishopB, board.getPiece(to), "Bishop B should end up at the target position as it lands second");
        assertEquals(Piece.State.CAPTURED, pawnA.getState(), "Pawn A should be captured by Bishop B");
        assertTrue(gameState.getBlackScore() > 0, "Black player score should increase");
    }

    /**
     * Checks that if a piece gets captured on its departure square BEFORE its
     * flight finishes, its own landing is skipped and discarded.
     * * Scenario:
     * - White Pawn A (at 2,2) begins moving to (3,3) [Arrives at t=2000]
     * - Black Rook B (at 1,2) begins moving to (2,2) [Arrives at t=1000]
     * Rook B lands on (2,2) first and captures Pawn A (which is still sitting at (2,2) in-game).
     * At t=2000, Pawn A's landing must be skipped since it is already CAPTURED.
     */
    @Test
    void testCapturedPieceLandingIsSkipped() {
        Position fromA = new Position(2, 2);
        Position toA = new Position(3, 3);
        Piece pawnA = new Piece("wP_A", Piece.Color.WHITE, Piece.Kind.PAWN, fromA);

        Position fromB = new Position(1, 2);
        Position toB = new Position(2, 2);
        Piece rookB = new Piece("bR_B", Piece.Color.BLACK, Piece.Kind.ROOK, fromB);

        board.setPiece(fromA, pawnA);
        board.setPiece(fromB, rookB);

        ActiveMove moveA = new ActiveMove(fromA, toA, pawnA, 2000, false);
        ActiveMove moveB = new ActiveMove(fromB, toB, rookB, 1000, false);

        List<ActiveMove> completedNormalMoves = new ArrayList<>();
        completedNormalMoves.add(moveA);
        completedNormalMoves.add(moveB);
        List<ActiveMove> completedJumps = new ArrayList<>();

        boolean result = MoveResolver.resolveCompletedMoves(gameState, completedNormalMoves, completedJumps);

        assertFalse(result);
        assertNull(board.getPiece(fromB), "Rook B departure cell should be empty");
        assertEquals(rookB, board.getPiece(toB), "Rook B must be at (2, 2)");
        assertNull(board.getPiece(toA), "Pawn A's target (3, 3) must remain empty since landing was skipped");
        assertEquals(Piece.State.CAPTURED, pawnA.getState(), "Pawn A must be CAPTURED");
    }

    /**
     * Verifies that pawns are promoted to a Queen when reaching the board boundary row.
     */
    @Test
    void testPawnPromotionOnLanding() {
        Position from = new Position(1, 0);
        Position to = new Position(0, 0); // Row 0 is the promotion row for WHITE
        Piece pawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, pawn);

        ActiveMove move = new ActiveMove(from, to, pawn, 1000, false);
        List<ActiveMove> completedNormalMoves = new ArrayList<>();
        completedNormalMoves.add(move);
        List<ActiveMove> completedJumps = new ArrayList<>();

        MoveResolver.resolveCompletedMoves(gameState, completedNormalMoves, completedJumps);

        Piece promoted = board.getPiece(to);
        assertNotNull(promoted, "A piece should reside on the promotion square");
        assertEquals(Piece.Kind.QUEEN, promoted.getKind(), "The pawn should have been promoted to a Queen");
        assertEquals(Piece.Color.WHITE, promoted.getColor());
        assertTrue(promoted.getId().endsWith("_Q"), "Promoted piece ID should end with _Q");
    }

    /**
     * Validates that capturing a King triggers the game over state.
     */
    @Test
    void testKingCaptureEndsGame() {
        Position from = new Position(4, 4);
        Position to = new Position(4, 7);
        Piece queen = new Piece("wQ_1", Piece.Color.WHITE, Piece.Kind.QUEEN, from);
        Piece king = new Piece("bK_1", Piece.Color.BLACK, Piece.Kind.KING, to);

        board.setPiece(from, queen);
        board.setPiece(to, king);

        ActiveMove move = new ActiveMove(from, to, queen, 1000, false);
        List<ActiveMove> completedNormalMoves = new ArrayList<>();
        completedNormalMoves.add(move);
        List<ActiveMove> completedJumps = new ArrayList<>();

        boolean result = MoveResolver.resolveCompletedMoves(gameState, completedNormalMoves, completedJumps);

        assertTrue(result, "King capture should report game over");
        assertTrue(gameState.isGameOver(), "Game state should be marked as over");
        assertEquals(Piece.State.CAPTURED, king.getState(), "King should be in CAPTURED state");
    }
}
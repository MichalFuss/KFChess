package org.example.realTime;

import org.example.events.EventBus;
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
    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        board = new Board(8, 8);
        gameState = new GameState(board);
        eventBus = new EventBus();
    }

    @Test
    void testStandardLandingNormalMove() {
        Position from = new Position(4, 4);
        Position to = new Position(3, 4);
        Piece pawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, pawn);

        ActiveMove move = new ActiveMove(from, to, pawn, 1000, false);
        List<ActiveMove> completedNormalMoves = new ArrayList<>();
        completedNormalMoves.add(move);

        boolean result = MoveResolver.resolveCompletedMoves(gameState, completedNormalMoves, new ArrayList<>(), eventBus);

        assertFalse(result);
        assertNull(board.getPiece(from));
        assertEquals(pawn, board.getPiece(to));
        assertEquals(Piece.State.LONG_REST, pawn.getState());
    }

    @Test
    void testStandardLandingJump() {
        Position pos = new Position(3, 3);
        Piece knight = new Piece("wN_1", Piece.Color.WHITE, Piece.Kind.KNIGHT, pos);
        board.setPiece(pos, knight);

        ActiveMove move = new ActiveMove(pos, pos, knight, 1000, true);
        List<ActiveMove> completedJumps = new ArrayList<>();
        completedJumps.add(move);

        boolean result = MoveResolver.resolveCompletedMoves(gameState, new ArrayList<>(), completedJumps, eventBus);

        assertFalse(result);
        assertEquals(knight, board.getPiece(pos));
        assertEquals(Piece.State.SHORT_REST, knight.getState());
    }

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

        boolean result = MoveResolver.resolveCompletedMoves(gameState, completedNormalMoves, new ArrayList<>(), eventBus);

        assertFalse(result);
        assertEquals(rook, board.getPiece(to));
        assertEquals(Piece.State.CAPTURED, targetPiece.getState());
        assertTrue(gameState.getWhiteScore() > 0);
    }

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
        completedNormalMoves.add(moveB);
        completedNormalMoves.add(moveA);

        boolean result = MoveResolver.resolveCompletedMoves(gameState, completedNormalMoves, new ArrayList<>(), eventBus);

        assertFalse(result);
        assertEquals(bishopB, board.getPiece(to));
        assertEquals(Piece.State.CAPTURED, pawnA.getState());
    }

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

        boolean result = MoveResolver.resolveCompletedMoves(gameState, completedNormalMoves, new ArrayList<>(), eventBus);

        assertFalse(result);
        assertEquals(rookB, board.getPiece(toB));
        assertNull(board.getPiece(toA));
        assertEquals(Piece.State.CAPTURED, pawnA.getState());
    }

    @Test
    void testPawnPromotionOnLanding() {
        Position from = new Position(1, 0);
        Position to = new Position(0, 0);
        Piece pawn = new Piece("wP_1", Piece.Color.WHITE, Piece.Kind.PAWN, from);
        board.setPiece(from, pawn);

        ActiveMove move = new ActiveMove(from, to, pawn, 1000, false);
        List<ActiveMove> completedNormalMoves = new ArrayList<>();
        completedNormalMoves.add(move);

        MoveResolver.resolveCompletedMoves(gameState, completedNormalMoves, new ArrayList<>(), eventBus);

        Piece promoted = board.getPiece(to);
        assertEquals(Piece.Kind.QUEEN, promoted.getKind());
    }

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

        boolean result = MoveResolver.resolveCompletedMoves(gameState, completedNormalMoves, new ArrayList<>(), eventBus);

        assertTrue(result);
        assertTrue(gameState.isGameOver());
    }
}
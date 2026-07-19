package org.example.rules;

import org.example.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RuleEngineTest {
    private RuleEngine ruleEngine;
    private Board board;
    private List<ActiveMove> activeMoves;

    @BeforeEach
    void setUp() {
        ruleEngine = new RuleEngine();
        board = new Board(8, 8);
        activeMoves = new ArrayList<>();
    }

    @Test
    void testValidateMove_MoveToSameSquare() {
        Position pos = new Position(3, 3);
        Piece rook = new Piece("wR1", Piece.Color.WHITE, Piece.Kind.ROOK, pos);
        board.setPiece(pos, rook);
        assertEquals(MoveValidationResult.INVALID_SAME_SQUARE, ruleEngine.validateMove(pos, pos, rook, board, activeMoves));
    }

    @Test
    void testValidateMove_PieceInRest() {
        Position pos = new Position(3, 3);
        Piece rook = new Piece("wR1", Piece.Color.WHITE, Piece.Kind.ROOK, pos);
        rook.setState(Piece.State.SHORT_REST);
        board.setPiece(pos, rook);
        assertEquals(MoveValidationResult.PIECE_IN_REST, ruleEngine.validateMove(pos, new Position(3, 4), rook, board, activeMoves));
    }
}
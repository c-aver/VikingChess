import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class represent the main logic of the game, holding the game state and performing actions as required.
 */
public class GameLogic implements PlayableLogic {
    /**
     * The width and height of the game board, currently the board can only be square.
     */
    public static final int BOARD_SIZE = 11;
    private final ConcretePlayer p1 = new ConcretePlayer(true);
    private final ConcretePlayer p2 = new ConcretePlayer(false);

    private Player currentTurn = p2;

    private final Map<Position, Piece> pieces = new HashMap<>();
    private final Set<Position> posSet = new HashSet<>();   // set of known position for logging purposes
    private final Set<ConcretePiece> pieceSet = new HashSet<>();    // set of known pieces for logging purposes

    /**
     * This record holds the information on a game move that is required to undo it.
     * @param piece the piece that was moved
     * @param source the piece's previous position
     * @param destination the piece's new position
     * @param captures a map of the pieces that were captured, tied to their death location
     */
    private record GameMove(ConcretePiece piece, Position source, Position destination, Map<Position, Piece> captures) { }
    private final Stack<GameMove> history = new Stack<>();

    private void initializeBoard() {
        pieces.clear();
        BoardStateLoader parser = new BoardStateLoader(p1, p2);
        Map<Position, Piece> loaded = parser.loadFile("resources/InitialBoardState.txt");
        if (loaded == null) {
            throw new IllegalArgumentException();
        }
        pieces.putAll(loaded);
        posSet.addAll(loaded.keySet());
        pieceSet.addAll(loaded.values().stream().map(p -> (ConcretePiece) p).collect(Collectors.toSet()));
    }

    /**
     * Constructs a new game logic and starts the game logically.
     * <p>Note that currently the initial board state is loaded from a hard-coded text file.</p>
     */
    public GameLogic() {
        initializeBoard();
    }

    private void changeTurn() {
        if (currentTurn == p2) currentTurn = p1;
        else if (currentTurn == p1) currentTurn = p2;
    }

    /**
     * Attempts to move a piece from {@code src} to {@code dst}, returning whether the move occurred or not.
     * <br>On a successful move, this method will also check if any captured need to occur and process them.
     * <p>A legal move must:
     * <br>1. Be onto a different position on the same row or on the same column.
     * <br>2. Have a piece in {@code src}.
     * <br>3. Move a piece whose owner is the current turn player.
     * <br>4. Not move a pawn into a corner.
     * <br>5. Not try to move through another piece.</p>
     * @param src the starting position of the piece.
     * @param dst the destination position for the piece
     * @return true if the move is legal and occurred
     */
    @Override
    public boolean move(Position src, Position dst) {
        // the following line is a workaround to the fact that dst is always a new instance, when we want it to be
        // the instance that represents the position in posSet (for dst.stepHere() later on)
        for (Position pos : posSet) if (pos.equals(dst)) { dst = pos; break; }
        if (src.equals(dst) || (src.x() != dst.x() && src.y() != dst.y())) return false;    // illegal move
        ConcretePiece p = (ConcretePiece) getPieceAtPosition(src);
        if (p == null) return false;    // no piece in source position
        if (p.getOwner() != currentTurn) return false;      // trying to move piece from wrong player
        if (p instanceof Pawn && dst.isCorner()) return false;  // trying to move pawn into corner
        if (src.x() == dst.x()) {
            int x = src.x();
            if (src.y() < dst.y()) {    // moving down
                for (int y = src.y() + 1; y <= dst.y(); ++y) {
                    if (getPieceAtPosition(new Position(x, y)) != null) return false;
                }
            }
            if (src.y() > dst.y()) { // moving up
                for (int y = src.y() - 1; y >= dst.y(); --y) {
                    if (getPieceAtPosition(new Position(x, y)) != null) return false;
                }
            }
        }
        if (src.y() == dst.y()) {
            int y = src.y();
            if (src.x() < dst.x()) {    // moving right
                for (int x = src.x() + 1; x <= dst.x(); ++x) {
                    if (getPieceAtPosition(new Position(x, y)) != null) return false;
                }
            }
            if (src.x() > dst.x()) { // moving left
                for (int x = src.x() - 1; x >= dst.x(); --x) {
                    if (getPieceAtPosition(new Position(x, y)) != null) return false;
                }
            }
        }
        pieces.remove(src);
        pieces.put(dst, p);
        posSet.add(dst);
        p.addMove(dst);
        dst.stepHere(p);

        Map<Position, Piece> captures = new HashMap<>();
        Map.Entry<Position, Piece> capture;
        if (null != (capture = attemptCapture(dst, dst.x() - 1, dst.y())))
            captures.put(capture.getKey(), capture.getValue());
        if (null != (capture = attemptCapture(dst, dst.x() + 1, dst.y())))
            captures.put(capture.getKey(), capture.getValue());
        if (null != (capture = attemptCapture(dst, dst.x(), dst.y() - 1)))
            captures.put(capture.getKey(), capture.getValue());
        if (null != (capture = attemptCapture(dst, dst.x(), dst.y() + 1)))
            captures.put(capture.getKey(), capture.getValue());

        history.push(new GameMove(p, src, dst, captures));

        changeTurn();

        ConcretePlayer winner = checkWinner();
        if (winner != null) {
            winner.addWin();
            logGame(winner);
        }
        return true;
    }

    /**
     * Attempts a capture from a given position to an unconstructed position.
     * <br>If all conditions for the capture are fulfilled, the captured piece is removed from the board.
     * @param capturerP position from which the capture is trying to happen, should always have a piece in it
     * @param capturedX x coordinate trying to be captured
     * @param capturedY y coordinate trying to be captured
     * @return the piece that was captured with its position, or null if no capture occurred
     * @throws IllegalArgumentException if {@code capturerP} does not host a piece
     */
    private Map.Entry<Position, Piece> attemptCapture(Position capturerP, int capturedX, int capturedY) {
        if (!Position.isInsideBoard(capturedX, capturedY)) return null;    // no capture to happen
        Position capturedP = new Position(capturedX, capturedY);
        if (capturedP.isCorner()) return null;     // there shouldn't be anything to capture in the corner
        Piece capturer = getPieceAtPosition(capturerP);
        if (capturer == null) throw new IllegalArgumentException("Tried to capture from empty spot");
        if (!(capturer instanceof Pawn)) return null;     // king can't capture
        Piece captured = getPieceAtPosition(capturedP);
        if (captured == null) return null;     // no piece to capture
        if (captured.getOwner() == capturer.getOwner()) return null;   // can't capture an ally
        if (captured instanceof King) return null;     // king isn't captured normally, checked in checkWinner()
        int dX = capturedP.x() - capturerP.x();
        int dY = capturedP.y() - capturerP.y();
        if (!Position.isInsideBoard(capturedP.x() + dX, capturedP.y() + dY)) {
            pieces.remove(capturedP);
            ((Pawn) capturer).addCapture();
            return new AbstractMap.SimpleEntry<>(capturedP, captured);   // capture against the edge
        }
        Position assistP = new Position(capturedP.x() + dX, capturedP.y() + dY);
        if (assistP.isCorner()) {
            pieces.remove(capturedP);
            ((Pawn) capturer).addCapture();
            return new AbstractMap.SimpleEntry<>(capturedP, captured);    // captured is against a corner
        }
        Piece assist = getPieceAtPosition(assistP);
        if (assist == null) return null;   // no piece to assist the capture
        if (assist.getOwner() != capturer.getOwner()) return null;     // assist must be from same player
        if (assist instanceof King) return null;   // king can't capture
        pieces.remove(capturedP);
        ((Pawn) capturer).addCapture();
        return new AbstractMap.SimpleEntry<>(capturedP, captured);
    }

    /**
     * Returns the piece at the specified position.
     * @param position the position for which to retrieve the piece
     * @return the piece at the specified position, or {@code null} if the square is empty
     */
    @Override
    public Piece getPieceAtPosition(Position position) {
        return pieces.get(position);
    }

    /**
     * Returns the {@code Player} object for player 1 (the defender).
     * @return player 1
     */
    @Override
    public Player getFirstPlayer() {
        return p1;
    }

    /**
     * Returns the {@code Player} object for player 2 (the attacker).
     * @return player 2
     */
    @Override
    public Player getSecondPlayer() {
        return p2;
    }

    /**
     * This method checks if a player has won, according to the game rules.
     * @return the winning player, or {@code null} if no player has won yet
     */
    private ConcretePlayer checkWinner() {
        Position kingPos = pieces.entrySet().stream()
                .filter(e -> e.getValue() instanceof King)
                .findFirst().orElseThrow(() -> new NoSuchElementException("King not found in board"))
                .getKey();
        if (kingPos.isCorner()) return p1;
        int boxedSides = 0;
        if (Position.isInsideBoard(kingPos.x() - 1, kingPos.y())) {
            Position side = new Position(kingPos.x() - 1, kingPos.y());
            if (getPieceAtPosition(side) != null && getPieceAtPosition(side).getOwner() == p2) boxedSides += 1;
        } else boxedSides += 1;     // king is against the edge
        if (Position.isInsideBoard(kingPos.x() + 1, kingPos.y())) {
            Position side = new Position(kingPos.x() + 1, kingPos.y());
            if (getPieceAtPosition(side) != null && getPieceAtPosition(side).getOwner() == p2) boxedSides += 1;
        } else boxedSides += 1;     // king is against the edge
        if (Position.isInsideBoard(kingPos.x(), kingPos.y() - 1)) {
            Position side = new Position(kingPos.x(), kingPos.y() - 1);
            if (getPieceAtPosition(side) != null && getPieceAtPosition(side).getOwner() == p2) boxedSides += 1;
        } else boxedSides += 1;     // king is against the edge
        if (Position.isInsideBoard(kingPos.x(), kingPos.y() + 1)) {
            Position side = new Position(kingPos.x(), kingPos.y() + 1);
            if (getPieceAtPosition(side) != null && getPieceAtPosition(side).getOwner() == p2) boxedSides += 1;
        } else boxedSides += 1;     // king is against the edge
        if (boxedSides == 4) return p2;
        return null;
    }

    /**
     * This method checks whether the game is finished.
     * @return true if the game is finished according to the game rules
     */

    @Override
    public boolean isGameFinished() {
        return checkWinner() != null;
    }

    /**
     * This method checks which player's turn it currently is.
     * @return true if it is currently player 2's turn (the attacker)
     */
    @Override
    public boolean isSecondPlayerTurn() {
        return currentTurn == p2;
    }

    /**
     * This method resets the game to it initial state. This resets the board state, piece and position statistics,
     * and clears the history.
     * <br>The number of wins for each player is not cleared by this operation.
     */
    @Override
    public void reset() {
        posSet.clear();
        pieceSet.clear();
        history.clear();
        initializeBoard();
    }

    /**
     * This method undoes the last performed move.
     * <br>This returns the board to its previous state, and removes the move from the pieces' and positions' histories.
     */
    @Override
    public void undoLastMove() {
        GameMove move;
        if (history.isEmpty()) {
            return;
        }
        move = history.pop();


        ConcretePiece stepper = move.piece();
        stepper.undoMove();
        move.destination().undoStep(stepper);

        pieces.remove(move.destination());
        pieces.put(move.source(), stepper);

        if (stepper instanceof Pawn p) p.undoCaptures(move.captures().size());
        pieces.putAll(move.captures());

        changeTurn();
    }

    /**
     * Returns the board's size (which is both the height and the width).
     * @return the board's size
     */
    @Override
    public int getBoardSize() {
        return BOARD_SIZE;
    }

    /**
     * This method performs game-end logging, extracting the information from the game state members.
     * @param winner which player won, required for sorting purposes
     */
    private void logGame(Player winner) {
        GameLogger logger = new GameLogger(System.out);

        Function<ConcretePiece, String> pieceFormat =
                p -> "" + (p instanceof King ? 'K' : (p.getOwner().isPlayerOne() ? 'D' : 'A')) + p.id + ": ";
        Function<Position, String> posFormat = p -> "(" + p.x() + ", " + p.y() + ")";

        Function<ConcretePiece, String> moveFormat = p -> {
            StringBuilder sb = new StringBuilder();
            sb.append(pieceFormat.apply(p));
            sb.append("[");
            Iterator<Position> it = p.getMoveHistory().iterator();
            sb.append(posFormat.apply(it.next()));
            while (it.hasNext()) sb.append(", ").append(posFormat.apply(it.next()));
            sb.append("]");
            return sb.toString();
        };
        logger.logStream(pieceSet, new ConcretePieceMoveCountComparator(winner),
                cp -> cp.getNumOfSteps() > 0, moveFormat);

        PawnCaptureComparator capComp = new PawnCaptureComparator(winner);
        Function<Pawn, String> capFormat = p -> pieceFormat.apply(p) + p.getCaptures() + " kills";
        Collection<Pawn> pawnSet = pieceSet.stream()
                .filter(cp -> cp instanceof Pawn).map(cp -> (Pawn) cp).collect(Collectors.toSet());
        logger.logStream(pawnSet, capComp, p -> p.getCaptures() > 0, capFormat);

        Function<ConcretePiece, String> distFormat = cp -> pieceFormat.apply(cp) + cp.getTotalMoveDist() + " squares";
        logger.logStream(pieceSet, new ConcretePieceMoveDistComparator(winner),
                cp -> cp.getTotalMoveDist() > 0, distFormat);

        Function<Position, String> stepFormat = p -> posFormat.apply(p) + p.getSteppedCount() + " pieces";
        logger.logStream(posSet, new PositionSteppedComparator(), p -> p.getSteppedCount() >= 2, stepFormat);
    }
}

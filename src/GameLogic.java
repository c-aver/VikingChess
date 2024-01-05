import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GameLogic implements PlayableLogic {
    public static final int BOARD_SIZE = 11;
    private final ConcretePlayer p1 = new ConcretePlayer(true);
    private final ConcretePlayer p2 = new ConcretePlayer(false);

    private Player currentTurn = p2;

    private final Map<Position, Piece> pieces = new HashMap<>();
    private final Set<Position> posSet = new HashSet<>();   // set of known position for logging purposes
    private final Set<ConcretePiece> pieceSet = new HashSet<>();    // set of known pieces for logging purposes
    private final Stack<GameMove> history = new Stack<>();

    private void initializeBoard() {
        pieces.clear();
        BoardStateParser parser = new BoardStateParser(p1, p2);
        Map<Position, Piece> loaded = parser.loadFile("resources/InitialBoardState.txt");
        if (loaded == null) {
            throw new IllegalArgumentException();
        }
        pieces.putAll(loaded);
        posSet.addAll(loaded.keySet());
        pieceSet.addAll(loaded.values().stream().map(p -> (ConcretePiece) p).collect(Collectors.toSet()));
    }

    public GameLogic() {
        initializeBoard();
    }

    private void changeTurn() {
        if (currentTurn == p2) currentTurn = p1;
        else if (currentTurn == p1) currentTurn = p2;
    }
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
                    if (pieces.containsKey(new Position(x, y))) return false;
                }
            }
            if (src.y() > dst.y()) { // moving up
                for (int y = src.y() - 1; y >= dst.y(); --y) {
                    if (pieces.containsKey(new Position(x, y))) return false;
                }
            }
        }
        if (src.y() == dst.y()) {
            int y = src.y();
            if (src.x() < dst.x()) {    // moving right
                for (int x = src.x() + 1; x <= dst.x(); ++x) {
                    if (pieces.containsKey(new Position(x, y))) return false;
                }
            }
            if (src.x() > dst.x()) { // moving left
                for (int x = src.x() - 1; x >= dst.x(); --x) {
                    if (pieces.containsKey(new Position(x, y))) return false;
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
     * Attempts a capture from a given position to an unconstructed position
     * <p>If all conditions for the capture are fulfilled, the piece at the captured position is removed from the board
     *
     * @param capturerP position from which the capture is trying to happen, should always have a piece in it
     * @param capturedX x coordinate trying to be captured
     * @param capturedY y coordinate trying to be captured
     * @return the piece that was captured, or {@code null} if no capture occurred
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
        if (captured instanceof King) return null;     // king isn't captured normally, checked in isGameFinished()
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

    @Override
    public Piece getPieceAtPosition(Position position) {
        return pieces.get(position);
    }

    @Override
    public Player getFirstPlayer() {
        return p1;
    }

    @Override
    public Player getSecondPlayer() {
        return p2;
    }

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

    @Override
    public boolean isGameFinished() {
        return checkWinner() != null;
    }

    @Override
    public boolean isSecondPlayerTurn() {
        return currentTurn == p2;
    }

    @Override
    public void reset() {
        posSet.clear();
        pieceSet.clear();
        history.clear();
        initializeBoard();
    }

    @Override
    public void undoLastMove() {    // TODO: make sure log is not corrupted by undo
        GameMove move;
        try {
            move = history.pop();
        } catch (EmptyStackException e) {
            return;     // no move to undo, that's fine
        }

        ConcretePiece stepper = move.piece();
        stepper.undoMove();
        move.destination().undoStep(stepper);

        pieces.remove(move.destination());
        pieces.put(move.source(), stepper);

        if (stepper instanceof Pawn p) p.undoCaptures(move.captures().size());
        pieces.putAll(move.captures());

        changeTurn();
    }

    @Override
    public int getBoardSize() {
        return BOARD_SIZE;
    }
    private void logGame(Player winner) {
        GameLogger logger = new GameLogger(System.out);

        Function<ConcretePiece, String> pieceFormat = p -> "" + (p instanceof King ? 'K' : (p.getOwner().isPlayerOne() ? 'D' : 'A')) + p.id + ": ";
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
        logger.logStream(pieceSet, new ConcretePieceMoveCountComparator(winner), cp -> cp.getNumOfSteps() > 0, moveFormat);

        PawnCaptureComparator capComp = new PawnCaptureComparator(winner);
        Function<Pawn, String> capFormat = p -> pieceFormat.apply(p) + p.getCaptures() + " kills";
        Collection<Pawn> pawnSet = pieceSet.stream().filter(cp -> cp instanceof Pawn).map(cp -> (Pawn) cp).collect(Collectors.toSet());
        logger.logStream(pawnSet, capComp, p -> p.getCaptures() > 0, capFormat);

        Function<ConcretePiece, String> distFormat = cp -> pieceFormat.apply(cp) + cp.getTotalMoveDist() + " squares";
        logger.logStream(pieceSet, new ConcretePieceMoveDistComparator(winner), cp -> cp.getTotalMoveDist() > 0, distFormat);

        Function<Position, String> stepFormat = p -> posFormat.apply(p) + p.getSteppedCount() + " pieces";
        logger.logStream(posSet, new PositionSteppedComparator(), p -> p.getSteppedCount() >= 2, stepFormat);
    }
}

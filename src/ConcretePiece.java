import java.util.Collection;
import java.util.Comparator;
import java.util.Stack;

/**
 * This abstract class represents a piece that can be placed on the board.
 */
public abstract class ConcretePiece implements Piece {
    private final Player owner;
    /**
     * The identification number of the piece, for logging purposes only.
     */
    private final int id;
    private final Stack<Position> moveHistory = new Stack<>();

    /**
     * Constructs a new piece with the specified parameters, note that {@code startPos} is only used to update histories
     * for logging and updates both the piece and the position.
     * @param owner owner to be assigned to the piece
     * @param id ID to be assigned for the piece, for logging purposes only
     * @param startPos initial position of the piece, to be added to the move history
     */
    public ConcretePiece(Player owner, int id, Position startPos) {
        this.owner = owner;
        this.id = id;
        moveHistory.push(startPos);
        startPos.stepHere(this);
    }
    @Override
    public Player getOwner() {
        return owner;
    }

    /**
     * Logs that the piece moved onto a position.
     * @param pos position onto the piece moved
     */
    public void addMove(Position pos) {
        moveHistory.push(pos);
    }

    /**
     * Returns the total move distance of the piece over its life in squares.
     * @return total squares moved over the piece's life
     */
    public int getTotalMoveDist() {
        int result = 0;
        Position prev = null;
        for (Position pos : moveHistory) {
            result += Position.straightDist(prev, pos);
            prev = pos;
        }
        return result;
    }

    /**
     * Returns the number of steps the piece performed over its life.
     * @return number of steps the piece performed
     */
    public int getNumOfSteps() {
        return moveHistory.size() - 1;  // subtract one for initial position
    }

    /**
     * Returns the full move history of the piece, starting at its initial position all the way up to its end position
     * (where it currently is or where it died).
     * <br>Every pair (p_i, p_{i+1}) in the history should be a legal move from p_i to p_{i+1}.
     * @return the full move history of the piece
     */
    public Collection<Position> getMoveHistory() {
        return moveHistory;
    }

    /**
     * Undoes the last move of the piece, removing it from the move history.
     * @throws RuntimeException if the move history only contains the initial position
     */
    public void undoMove() {
        if (moveHistory.size() <= 1) throw new RuntimeException("Trying to undo initial location");
        moveHistory.pop();
    }

    public int getId() {
        return id;
    }

    /**
     * Returns a comparator that compares two {@code ConcretePiece}s according to the following rules:
     * <br>Winner comes first.
     * <br>Then, order by number of steps.
     *  <br>If they are equal, order by piece ID.
     * @param winner the winner of the game, required for comparison
     * @return the required comparator
     */
    public static Comparator<ConcretePiece> getMoveCountComparator(Player winner) {
        return (o1, o2) -> {
            if (o1.getOwner() != o2.getOwner()) return o1.getOwner() == winner ? -1 : 1;
            int dComp = Integer.compare(o1.getNumOfSteps(), o2.getNumOfSteps());
            if (dComp != 0) return dComp;
            return Integer.compare(o1.getId(), o2.getId());
        };
    }

    /**
     * Returns a comparator that compares two {@code ConcretePiece}s according to the following rules:
     * <br>First, reverse order by total move distance.
     * <br>If they are equal, forward order by their IDs.
     * <br>If they are also equal, piece whose owner is the winner comes first.
     * @param winner the winner of the game, required for comparison
     * @return the required comparator
     */
    public static Comparator<ConcretePiece> getMoveDistComparator(Player winner) {
        return (o1, o2) -> {
            int dComp = Integer.compare(o2.getTotalMoveDist(), o1.getTotalMoveDist());
            if (dComp != 0) return dComp;
            int iComp = Integer.compare(o1.getId(), o2.getId());
            if (iComp != 0) return iComp;
            if (o1.getOwner() == o2.getOwner()) return 0;   // should be unreachable
            if (o1.getOwner() == winner) return -1;
            return 1;
        };
    }
}

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a valid position on the board, mainly x and y coordinates.
 * This class also holds the history of the pieces that stepped here for logging purposes.
 */
public final class Position {
    private static final int BOARD_SIZE = GameLogic.BOARD_SIZE;
    private final int x;
    private final int y;
    // count of how many times each piece stepped here, piece that never stepped here should be absent from the map
    private final Map<Piece, Integer> steppedHere = new HashMap<>();

    /**
     * Checks if an (x, y) pair represents a coordinate inside the board.
     * @param x x coordinate of the position to check
     * @param y y coordinate of the position to check
     * @return true if the specified position is inside the board
     */
    public static boolean isInsideBoard(int x, int y) {
        return (x >= 0 && y >= 0 && x < BOARD_SIZE && y < BOARD_SIZE);
    }

    /**
     * Constructs a {@code Position} from the given coordinates. Can only construct legal positions.
     * <p>To check if a set of coordinated can be constructed into a {@code Position}, use {@link #isInsideBoard}.
     * @param newX x coordinate of the required {@code Position}
     * @param newY y coordinate of the required {@code Position}
     * @throws IllegalArgumentException if either coordinate is negative or bigger than the board's size
     */
    public Position(int newX, int newY) {
        if (newX < 0 || newY < 0) {
            throw new IllegalArgumentException("Position argument cannot be negative");
        }
        if (newX >= BOARD_SIZE || newY >= BOARD_SIZE) {
            throw new IllegalArgumentException("Position argument cannot be more than " + (BOARD_SIZE - 1));
        }
        x = newX;
        y = newY;
    }

    /**
     * Compute the absolute distance between two points that lie on an axis-parallel line (arguments are commutative).
     * <br>If either parameters in {@code null}, returns 0, this is for fault-proofing total move distance calculations.
     * @param p1 first point
     * @param p2 second point
     * @return non-negative distance between the provided positions
     * @throws RuntimeException if parameters do not lie on an axis-parallel line
     */
    public static int straightDist(Position p1, Position p2) {
        if (p1 == null || p2 == null) return 0;
        if (p1.x() == p2.x()) return Math.abs(p1.y() - p2.y());
        if (p1.y() == p2.y()) return Math.abs(p1.x() - p2.x());
        throw new RuntimeException("p1 and p2 are not on a straight line");
    }

    /**
     * Gets the x coordinate of the position.
     * @return the x coordinate of the position
     */
    public int x() {
        return x;
    }

    /**
     * Gets the y coordinate of the position.
     * @return the y coordinate of the position
     */
    public int y() {
        return y;
    }

    /**
     * Override of {@code Object::hashCode} to reflect the fact that {@code x} and {@code y} have limited ranges.
     * <p>This method allows {@code HashTable}s, {@code HashMap}s, and {@code HashSet}s
     * to group {@code Position}s based on their coordinates alone.</p>
     * @return a unique number for each possible position in the board
     */
    @Override
    public int hashCode() {
        return x + y * BOARD_SIZE;
    }

    /**
     * Override of {@code Object::equals} to only use {@code x} and {@code y} coordinates for checking equality.
     * <br>This is consistent with {@code hashCode}.
     * @param o object to check equality to
     * @return true if {@code o} is a {@code Position} with the same coordinates
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position oP)) return false;
        return oP.x() == this.x() && oP.y() == this.y();
    }

    /**
     * Checks if this {@code Position} is one of the corners.
     * @return true if this {@code Position} is a corner
     */
    public boolean isCorner() {
        return x % (BOARD_SIZE - 1)  == 0 && y % (BOARD_SIZE - 1) == 0;
    }

    /**
     * Records that a piece stepped on this position, for logging purposes.
     * @param stepped the piece that stepped here
     */
    public void stepHere(Piece stepped) {
        steppedHere.compute(stepped, (cp, i) -> i == null ? 1 : i + 1);
    }

    /**
     * Returns the number of distinct {@code Piece}s that stepped here.
     * @return number of distinct {@code Piece}s that stepped on this {@code Position}
     */
    public int getSteppedCount() {
        return steppedHere.size();
    }

    /**
     * Undoes a step and removes the step from the history.
     * @param stepper the {@code Piece} to undo a step for
     * @throws RuntimeException if {@code stepper} never stepped here
     */
    public void undoStep(Piece stepper) {
        steppedHere.compute(stepper, (p, i) -> {
            if (i == null) throw new RuntimeException("Tried to undo step of piece that never stepped here");
            if (i <= 1) return null;
            return i - 1;
        });
    }
}

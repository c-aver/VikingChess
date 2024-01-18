import java.util.Comparator;

/**
 * This class represents a pawn piece for the game.
 */
public class Pawn extends ConcretePiece {
    private int captures = 0;

    /**
     * Constructs a new pawn piece.
     * @param owner the player that should own this pawn
     * @param id the ID of the pawn, for logging purposes
     * @param startPos the initial position of the pawn, for history purposes
     */
    public Pawn(Player owner, int id, Position startPos) {
        super(owner, id, startPos);
    }

    /**
     * Returns the text that represents the pawn, either "♙" or "♟" depending on whether the owner is player one or not.
     * @return the text that represents the pawn
     */
    @Override
    public String getType() {
        if (getOwner().isPlayerOne()) {
            return "♙";
        } else {
            return "♟";
        }
    }

    /**
     * Logs that the pawn has performed a capture.
     */
    public void addCapture() {
        captures += 1;
    }

    /**
     * Logs that the pawn has undone some captures.
     * @param num number of captures that were undone
     */
    public void undoCaptures(int num) {
        captures -= num;
    }

    /**
     * Returns the total number of captures this pawn has performed throughout the game.
     * @return total number of captures
     */
    public int getCaptures() {
        return captures;
    }

    @Override
    public String toString() {
        return (getOwner().isPlayerOne() ? "D" : "A") + getId();
    }

    /**
     * Returns a comparator that compares two {@code Pawn}s according to the following rules:
     * <br>First, reverse order by number of captures.
     * <br>If they are equal, forward order by their IDs.
     * <br>If they are also equal, piece whose owner is the winner comes first.
     * @param winner the winner of the game, required for the comparison
     * @return the required comparator
     */
    public static Comparator<Pawn> getCaptureComparator(Player winner) {
        return (o1, o2) -> {
            int cComp = Integer.compare(o2.getCaptures(), o1.getCaptures());
            if (cComp != 0) return cComp;
            int iComp = Integer.compare(o1.getId(), o2.getId());
            if (iComp != 0) return iComp;
            if (o1.getOwner() == o2.getOwner()) return 0;   // should be unreachable
            if (o1.getOwner() == winner) return -1;
            return 1;
        };
    }
}

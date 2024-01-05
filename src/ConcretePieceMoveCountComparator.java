import java.util.Comparator;

/**
 * Compares two {@code ConcretePiece}s according to order specified in part 2 section 1 of the assignment.
 */
public class ConcretePieceMoveCountComparator implements Comparator<ConcretePiece> {
    private final Player winner;

    /**
     * Constructs a new comparator with a specified winner, the identity of the winner has implications for the
     * comparison.
     * @param w the winner
     */
    public ConcretePieceMoveCountComparator(Player w) {
        winner = w;
    }
    /**
     * Compare two {@code ConcretePiece}s according to the following rules:
     * <br>Winner comes first.
     * <br>Then, order by number of steps.
     * <br>If they are equal, order by piece ID.
     * @param o1 the first object to be compared
     * @param o2 the second object to be compared1
     * @return integer according to order definition
     */
    @Override
    public int compare(ConcretePiece o1, ConcretePiece o2) {
        if (o1.getOwner() != o2.getOwner()) return o1.getOwner() == winner ? -1 : 1;
        int dComp = Integer.compare(o1.getNumOfSteps(), o2.getNumOfSteps());
        if (dComp != 0) return dComp;
        return Integer.compare(o1.id, o2.id);
    }
}

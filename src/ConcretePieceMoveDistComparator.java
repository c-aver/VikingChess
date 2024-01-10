import java.util.Comparator;

/**
 * Compares two {@code ConcretePiece}s according to order specified in part 2 section 3 of the assignment.
 */
public class ConcretePieceMoveDistComparator implements Comparator<ConcretePiece> {
    private final Player winner;
    /**
     * Constructs a new comparator with a specified winner, the identity of the winner has implications for the
     * comparison.
     * @param w the winner
     */
    public ConcretePieceMoveDistComparator(Player w) {
        winner = w;
    }
    /**
     * Compare two concrete pieces according to the following rules:
     * <br>First, reverse order by total move distance.
     * <br>If they are equal, forward order by their IDs.
     * <br>If they are also equal, piece whose owner is the winner comes first.
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return integer according to order definition
     */
    @Override
    public int compare(ConcretePiece o1, ConcretePiece o2) {
        int dComp = Integer.compare(o2.getTotalMoveDist(), o1.getTotalMoveDist());
        if (dComp != 0) return dComp;
        int iComp = Integer.compare(o1.getId(), o2.getId());
        if (iComp != 0) return iComp;
        if (o1.getOwner() == o2.getOwner()) return 0;   // should be unreachable
        if (o1.getOwner() == winner) return -1;
        return 1;
    }
}

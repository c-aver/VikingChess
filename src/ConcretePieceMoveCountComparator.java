import java.util.Comparator;

public class ConcretePieceMoveCountComparator implements Comparator<ConcretePiece> {
    private final Player winner;
    public ConcretePieceMoveCountComparator(Player w) {
        winner = w;
    }
    /**
     * Compare two concrete pieces according to the following rules:
     * <br>Winner comes first
     * <br>Then, order by number of steps
     * <br>If they are equal, order by piece ID
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
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

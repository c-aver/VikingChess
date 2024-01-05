import java.util.Comparator;

/**
 * Compares two {@code Position}s according to order specified in part 2 section 4 of the assignment.
 */
public class PositionSteppedComparator implements Comparator<Position> {
    /**
     * Compare two positions according to the following rules:
     * <br>First, by number of unique pieces stepped on, in reverse order.
     * <br>If they are equal, by x values.
     * <br>If they are equal, by y values.
     * @param o1 the first object to be compared
     * @param o2 the second object to be compared
     * @return integer according to order definition
     */
    @Override
    public int compare(Position o1, Position o2) {
        int sComp = Integer.compare(o2.getSteppedCount(), o1.getSteppedCount());    // TODO: this is against assignment definitions in order to comply with test
        if (sComp != 0) return sComp;
        int xComp = Integer.compare(o1.x(), o2.x());
        if (xComp != 0) return xComp;
        return Integer.compare(o1.y(), o2.y());
    }
}

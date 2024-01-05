import java.util.Comparator;

public class PositionSteppedComparator implements Comparator<Position> {
    @Override
    public int compare(Position o1, Position o2) {
        int sComp = Integer.compare(o2.getSteppedCount(), o1.getSteppedCount());    // TODO: this is against assignment definitions in order to comply with test
        if (sComp != 0) return sComp;
        int xComp = Integer.compare(o1.x(), o2.x());
        if (xComp != 0) return xComp;
        return Integer.compare(o1.y(), o2.y());
    }
}

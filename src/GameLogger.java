import java.io.PrintStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class represent a logger for logging game statistics.
 */
public class GameLogger {
    private final PrintStream out;

    /**
     * Constructs a logger that will output into a specified {@code PrintStream}.
     * @param o the stream into which the logger will print
     */
    public GameLogger(PrintStream o) {
        out = o;
    }
    private void sectionBreak() {
        out.println("*".repeat(75));
    }

    /**
     * Logs a stream of information according to the specified parameters.
     * @param c the collection of objects from which information is logged
     * @param comp the comparator that determines the order of the logging
     * @param filter a filter to determine which objects from {@code c} will be logged
     * @param format the format that determines how the information will be printed
     * @param <T> the type of objects from which the information is extracted
     */
    private <T> void log(Collection<T> c, Comparator<T> comp, Predicate<T> filter, Function<T, String> format) {
        c.stream().filter(filter).sorted(comp).map(format).forEach(out::println);
        sectionBreak();
    }

    public void logGame(Player winner, Set<Position> posSet, Set<ConcretePiece> pieceSet) {
        Function<ConcretePiece, String> moveFormat = p -> {
            StringBuilder sb = new StringBuilder();
            sb.append(p.toString());
            sb.append(": [");
            Iterator<Position> it = p.getMoveHistory().iterator();
            sb.append(it.next().toString());
            while (it.hasNext()) sb.append(", ").append(it.next().toString());
            sb.append("]");
            return sb.toString();
        };
        log(pieceSet, ConcretePiece.getMoveCountComparator(winner),
                cp -> cp.getNumOfSteps() > 0, moveFormat);

        Function<Pawn, String> capFormat = p -> p.toString() + ": " + p.getCaptures() + " kills";
        Collection<Pawn> pawnSet = pieceSet.stream()
                .filter(cp -> cp instanceof Pawn).map(cp -> (Pawn) cp).collect(Collectors.toSet());
        log(pawnSet, Pawn.getCaptureComparator(winner), p -> p.getCaptures() > 0, capFormat);

        Function<ConcretePiece, String> distFormat = cp -> cp.toString() + ": " + cp.getTotalMoveDist() + " squares";
        log(pieceSet, ConcretePiece.getMoveDistComparator(winner),
                cp -> cp.getTotalMoveDist() > 0, distFormat);

        Function<Position, String> stepFormat = p -> p.toString() + p.getSteppedCount() + " pieces";
        log(posSet, Position.steppedComp, p -> p.getSteppedCount() >= 2, stepFormat);
    }
}

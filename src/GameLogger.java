import java.io.PrintStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;

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
    public <T> void logStream(Collection<T> c, Comparator<T> comp, Predicate<T> filter, Function<T, String> format) {
        c.stream().filter(filter).sorted(comp).map(format).forEach(out::println);
        sectionBreak();
    }
}

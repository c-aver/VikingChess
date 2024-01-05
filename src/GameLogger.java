import java.io.PrintStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class GameLogger {
    private final PrintStream out;
    public GameLogger(PrintStream o) {
        out = o;
    }
    private void sectionBreak() {
        out.println("*".repeat(75));
    }
    public <T> void logStream(Collection<T> c, Comparator<T> comp, Function<T, String> format) {
        logStream(c, comp, t -> true, format);
    }
    public <T> void logStream(Collection<T> c, Comparator<T> comp, Predicate<T> filter, Function<T, String> format) {
        c.stream().filter(filter).sorted(comp).map(format).forEach(out::println);
        sectionBreak();
    }
}

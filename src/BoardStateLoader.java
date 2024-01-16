import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class holds a parser for parsing text files into {@code Position} to {@code Piece} maps that can be inserted
 * into a board.
 */
public class BoardStateLoader {
    private final Player p1;
    private final Player p2;

    /**
     * Constructs a parser linked to the {@code Player} objects to be assigned as owners to new parsed pieces.
     * @param p1 owner assigned to pieces with "1" in the relevant field
     * @param p2 owner assigned to pieces with "2" in the relevant field
     */
    public BoardStateLoader(Player p1, Player p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * Loads a {@code Position} to {@code Piece} mapping from a resource file, parsing each piece from a line.
     * <p>The format is as follows: "({x},{y})->{p}{t}{id}" (whitespaces inside the line are ignored):
     * <br>{x} and {y} are the coordinates of the piece.
     * <br>{p} is the number of the player that owns the piece (either 1 or 2).
     * <br>{t} is the type of the piece ('p' for pawn, 'k' for king).
     * <br>{id} is the ID of the piece for logging purposes.
     * <p>Lines are only allowed to either match the format precisely or be empty.
     * @param resourcePath the path to the resource file to be parsed
     * @return a map of the loaded pieces
     * @throws RuntimeException if there was an error opening or reading the resource
     * @throws IllegalArgumentException if a line does not match the format
     */
    public Map<Position, Piece> loadFile(String resourcePath) {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new RuntimeException("Resource does not exists: " + resourcePath);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        Map<Position, Piece> result = new HashMap<>();
        String line;
        try {
            line = reader.readLine();
            while (line != null) {
                if (!line.isEmpty()) {
                    AbstractMap.Entry<Position, Piece> entry = parseLine(line);
                    result.put(entry.getKey(), entry.getValue());
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading resource: " + resourcePath);
        }
        return result;
    }

    private Map.Entry<Position,Piece> parseLine(String line) {
        Pattern pattern = Pattern.compile(
                "\\((?<x>\\d+),(?<y>\\d+)\\)->(?<player>[12])(?<type>[pk])(?<id>\\d+)");
        Matcher matcher = pattern.matcher(line.replaceAll("\\s", ""));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Line does not match format");
        }
        Position pos = new Position(Integer.parseInt(matcher.group("x")),
                                    Integer.parseInt(matcher.group("y")));
        Piece pie = parsePiece(matcher, pos);
        return new AbstractMap.SimpleEntry<>(pos, pie);
    }

    private Piece parsePiece(Matcher matcher, Position pos) {
        Piece pie;

        int ownerN = Integer.parseInt(matcher.group("player"));
        Player owner;
        if (ownerN == 1) {
            owner = p1;
        } else if (ownerN == 2) {
            owner = p2;
        } else {
            throw new IllegalArgumentException("Unknown owner");
        }

        int id = Integer.parseInt(matcher.group("id"));

        if (matcher.group("type").equals("k")) {
            pie = new King(owner, id, pos);
        } else if (matcher.group("type").equals("p")) {
            pie = new Pawn(owner, id, pos);
        } else {
            throw new IllegalArgumentException("Unknown type");
        }

        return pie;
    }
}

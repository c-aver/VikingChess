import java.util.Map;

/**
 * This record holds the information on a game move that is required to undo it.
 * @param piece the piece that was moved
 * @param source the piece's previous position
 * @param destination the piece's new position
 * @param captures a map of the pieces that were captured, tied to their death location
 */
public record GameMove(ConcretePiece piece, Position source, Position destination, Map<Position, Piece> captures) {
}

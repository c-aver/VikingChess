import java.util.Map;

public record GameMove(ConcretePiece piece, Position source, Position destination, Map<Position, Piece> captures) {
}

/**
 * This class represents a king piece for the game.
 */
public class King extends ConcretePiece {
    /**
     * Constructs a new king piece.
     * <br>Note: a king can only be constructed for player 1 (the defender).
     * @param owner the player that should own this king
     * @param id the ID of the king, for logging purposes
     * @param startPos the initial position of the king, for history purposes
     */
    public King(Player owner, int id, Position startPos) {
        super(owner, id, startPos);
        if (!owner.isPlayerOne()) throw new IllegalArgumentException("Cannot create king owned by player 2");
    }

    /**
     * Returns the text that represents the king which is always "♔" (Unicode character U+2654).
     * @return the text that represents the king
     */
    @Override
    public String getType() {
        return "♔";
    }

    @Override
    public String toString() {
        return "K" + getId();
    }
}

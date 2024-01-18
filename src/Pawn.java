/**
 * This class represents a pawn piece for the game.
 */
public class Pawn extends ConcretePiece {
    private int captures = 0;

    /**
     * Constructs a new pawn piece.
     * @param owner the player that should own this pawn
     * @param id the ID of the pawn, for logging purposes
     * @param startPos the initial position of the pawn, for history purposes
     */
    public Pawn(Player owner, int id, Position startPos) {
        super(owner, id, startPos);
    }

    /**
     * Returns the text that represents the pawn, either "♙" or "♟" depending on whether the owner is player one or not.
     * @return the text that represents the pawn
     */
    @Override
    public String getType() {
        if (getOwner().isPlayerOne()) {
            return "♙";
        } else {
            return "♟";
        }
    }

    /**
     * Logs that the pawn has performed a capture.
     */
    public void addCapture() {
        captures += 1;
    }

    /**
     * Logs that the pawn has undone some captures.
     * @param num number of captures that were undone
     */
    public void undoCaptures(int num) {
        captures -= num;
    }

    /**
     * Returns the total number of captures this pawn has performed throughout the game.
     * @return total number of captures
     */
    public int getCaptures() {
        return captures;
    }

    @Override
    public String toString() {
        return (getOwner().isPlayerOne() ? "D" : "A") + getId();
    }
}

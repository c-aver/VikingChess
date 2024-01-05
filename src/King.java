public class King extends ConcretePiece {
    public King(Player owner, int id, Position startPos) {
        super(owner, id, startPos);
        if (!owner.isPlayerOne()) throw new IllegalArgumentException("Cannot create king owned by player 2");
    }

    @Override
    public String getType() {
        return "♔";
    }
}

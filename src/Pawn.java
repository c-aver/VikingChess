public class Pawn extends ConcretePiece {
    private int captures = 0;
    public Pawn(Player owner, int id, Position startPos) {
        super(owner, id, startPos);
    }

    @Override
    public String getType() {
        if (getOwner().isPlayerOne()) {
            return "♙";
        } else {
            return "♟";
        }
    }

    public void addCapture() {
        captures += 1;
    }
    public void undoCaptures(int num) {
        captures -= num;
    }

    public int getCaptures() {
        return captures;
    }
}

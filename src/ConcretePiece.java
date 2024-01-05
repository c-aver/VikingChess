import java.util.Collection;
import java.util.Stack;

public abstract class ConcretePiece implements Piece {
    private final Player owner;
    protected final int id;
    private final Stack<Position> moveHistory = new Stack<>();
    public ConcretePiece(Player owner, int id, Position startPos) {
        this.owner = owner;
        this.id = id;
        moveHistory.push(startPos);
        startPos.stepHere(this);
    }
    @Override
    public Player getOwner() {
        return owner;
    }
    public void addMove(Position pos) {
        moveHistory.push(pos);
    }
    public int getTotalMoveDist() {
        int result = 0;
        Position prev = null;
        for (Position pos : moveHistory) {
            result += Position.straightDist(prev, pos);
            prev = pos;
        }
        return result;
    }
    public int getNumOfSteps() {
        return moveHistory.size() - 1;  // subtract one for initial position
    }
    public Collection<Position> getMoveHistory() {
        return moveHistory;
    }
    public void undoMove() {
        if (moveHistory.size() <= 1) throw new RuntimeException("Trying to undo initial location");
        moveHistory.pop();
    }
}

public class ConcretePlayer implements Player {
    private int wins = 0;

    public final boolean isP1;

    public ConcretePlayer(boolean p1) {
        isP1 = p1;
    }

    @Override
    public boolean isPlayerOne() {
        return isP1;
    }

    public void addWin() {
        wins += 1;
    }
    @Override
    public int getWins() {
        return wins;
    }
}

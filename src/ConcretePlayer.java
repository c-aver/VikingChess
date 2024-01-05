/**
 * This class represents a player in the game and holds the required information about them.
 */
public class ConcretePlayer implements Player {
    private int wins = 0;

    private final boolean isP1;

    /**
     * Constructs a new player, with the parameter specifying which player it is.
     * @param p1 set to true if the constructed player is player 1 (i.e. the defender)
     */
    public ConcretePlayer(boolean p1) {
        isP1 = p1;
    }

    /**
     * Returns whether the player is player 1 (i.e. the defender).
     * @return true if this is player 1
     */
    @Override
    public boolean isPlayerOne() {
        return isP1;
    }

    /**
     * Adds a win to the player's win count.
     */
    public void addWin() {
        wins += 1;
    }

    /**
     * Returns the player's win count.
     * @return the player's win count
     */
    @Override
    public int getWins() {
        return wins;
    }
}

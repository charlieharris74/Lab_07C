public class BattleShipGame
{
    private final BattleShipBoard board;
    private int misses;
    private int strikes;
    private int totalMisses;
    private int totalHits;
    private Ship lastSunkShip;

    public BattleShipGame()
    {
        this(new BattleShipBoard());
    }

    public BattleShipGame(BattleShipBoard board)
    {
        this.board = board;
    }

    public boolean fire(int row, int col)
    {
        if (isGameOver() || !board.fire(row, col))
            return false;

        lastSunkShip = null;
        Ship ship = board.getShip(row, col);
        if (ship != null)
        {
            totalHits++;
            misses = 0;
            if (ship.isSunk())
                lastSunkShip = ship;
        }
        else
        {
            totalMisses++;
            misses++;
            if (misses == 5)
            {
                strikes++;
                misses = 0;
            }
        }
        return true;
    }

    public void reset()
    {
        board.reset();
        misses = 0;
        strikes = 0;
        totalMisses = 0;
        totalHits = 0;
        lastSunkShip = null;
    }

    public int getMisses() { return misses; }
    public int getStrikes() { return strikes; }
    public int getTotalMisses() { return totalMisses; }
    public int getTotalHits() { return totalHits; }
    public Ship getLastSunkShip() { return lastSunkShip; }
    public char getCellState(int row, int col) { return board.getCellState(row, col); }
    public boolean hasWon() { return totalHits == 17; }
    public boolean hasLost() { return strikes == 3; }
    public boolean isGameOver() { return hasWon() || hasLost(); }
}

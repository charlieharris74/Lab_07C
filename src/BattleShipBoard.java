import java.util.Random;

public class BattleShipBoard
{
    public static final int SIZE = 10;
    private final Ship[][] ships = new Ship[SIZE][SIZE];
    private final boolean[][] fired = new boolean[SIZE][SIZE];
    private final Random random;

    public BattleShipBoard()
    {
        this(new Random());
    }

    public BattleShipBoard(Random random)
    {
        this.random = random;
        reset();
    }

    public void reset()
    {
        for (int row = 0; row < SIZE; row++)
        {
            for (int col = 0; col < SIZE; col++)
            {
                ships[row][col] = null;
                fired[row][col] = false;
            }
        }
        // Place the longer ships first so they have more room.
        placeShip(new Ship("Carrier", 5));
        placeShip(new Ship("Battleship", 4));
        placeShip(new Ship("Cruiser", 3));
        placeShip(new Ship("Submarine", 3));
        placeShip(new Ship("Destroyer", 2));
    }

    private void placeShip(Ship ship)
    {
        // Collect valid starting positions, then choose one at random.
        int[][] choices = new int[SIZE * SIZE * 2][3];
        int count = 0;
        for (int row = 0; row < SIZE; row++)
        {
            for (int col = 0; col < SIZE; col++)
            {
                for (int direction = 0; direction < 2; direction++)
                {
                    boolean horizontal = direction == 0;
                    if (canPlace(row, col, ship.getLength(), horizontal))
                    {
                        choices[count][0] = row;
                        choices[count][1] = col;
                        choices[count][2] = direction;
                        count++;
                    }
                }
            }
        }
        int[] choice = choices[random.nextInt(count)];
        for (int offset = 0; offset < ship.getLength(); offset++)
        {
            int row = choice[0] + (choice[2] == 1 ? offset : 0);
            int col = choice[1] + (choice[2] == 0 ? offset : 0);
            ships[row][col] = ship;
        }
    }

    private boolean canPlace(int row, int col, int length, boolean horizontal)
    {
        for (int offset = 0; offset < length; offset++)
        {
            int nextRow = row + (horizontal ? 0 : offset);
            int nextCol = col + (horizontal ? offset : 0);
            if (nextRow >= SIZE || nextCol >= SIZE || ships[nextRow][nextCol] != null)
                return false;
        }
        return true;
    }

    public boolean fire(int row, int col)
    {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE || fired[row][col])
            return false;
        fired[row][col] = true;
        if (ships[row][col] != null)
            ships[row][col].registerHit();
        return true;
    }

    public Ship getShip(int row, int col) { return ships[row][col]; }
    public boolean wasFired(int row, int col) { return fired[row][col]; }

    public char getCellState(int row, int col)
    {
        if (!fired[row][col])
            return '~';
        return ships[row][col] == null ? 'M' : 'X';
    }
}

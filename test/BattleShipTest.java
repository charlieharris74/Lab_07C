import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class BattleShipTest
{
    private BattleShipBoard board;
    private BattleShipGame game;

    private void startGame()
    {
        board = new BattleShipBoard(new Random(42));
        game = new BattleShipGame(board);
    }

    private List<int[]> squares(boolean shipSquares)
    {
        List<int[]> result = new ArrayList<>();
        for (int row = 0; row < 10; row++)
            for (int col = 0; col < 10; col++)
                if ((board.getShip(row, col) != null) == shipSquares)
                    result.add(new int[] {row, col});
        return result;
    }

    private void fire(int[] square)
    {
        assertTrue(game.fire(square[0], square[1]));
    }

    @Test
    public void randomFleetsHaveCorrectLengthsAndNoOverlaps()
    {
        for (int seed = 0; seed < 500; seed++)
        {
            BattleShipBoard sample = new BattleShipBoard(new Random(seed));
            Map<Ship, List<int[]>> locations = new IdentityHashMap<>();
            int blankCount = 0;
            for (int row = 0; row < 10; row++)
            {
                for (int col = 0; col < 10; col++)
                {
                    Ship ship = sample.getShip(row, col);
                    if (ship == null)
                        blankCount++;
                    else
                        locations.computeIfAbsent(ship, key -> new ArrayList<>()).add(new int[] {row, col});
                    assertFalse(sample.wasFired(row, col));
                    assertEquals('~', sample.getCellState(row, col));
                }
            }
            assertEquals(83, blankCount);
            assertEquals(5, locations.size());
            List<Integer> lengths = new ArrayList<>();
            for (Ship ship : locations.keySet())
            {
                List<int[]> cells = locations.get(ship);
                assertEquals(ship.getLength(), cells.size());
                lengths.add(cells.size());
                boolean sameRow = cells.stream().allMatch(cell -> cell[0] == cells.get(0)[0]);
                boolean sameCol = cells.stream().allMatch(cell -> cell[1] == cells.get(0)[1]);
                assertTrue(sameRow || sameCol);
                for (int i = 1; i < cells.size(); i++)
                    assertEquals(1, Math.abs(cells.get(i)[0] - cells.get(i - 1)[0])
                            + Math.abs(cells.get(i)[1] - cells.get(i - 1)[1]));
            }
            Collections.sort(lengths);
            assertEquals(Arrays.asList(2, 3, 3, 4, 5), lengths);
        }
    }

    @Test
    public void newGameStartsWithZeroCounters()
    {
        startGame();
        assertEquals(0, game.getMisses());
        assertEquals(0, game.getStrikes());
        assertEquals(0, game.getTotalMisses());
        assertEquals(0, game.getTotalHits());
        assertFalse(game.isGameOver());
        assertNull(game.getLastSunkShip());
    }

    @Test
    public void hitIncrementsHitsAndRevealsX()
    {
        startGame();
        int[] hit = squares(true).get(0);
        fire(hit);
        assertEquals(1, game.getTotalHits());
        assertEquals(0, game.getTotalMisses());
        assertEquals('X', game.getCellState(hit[0], hit[1]));
    }

    @Test
    public void missIncrementsBothMissCountersAndRevealsM()
    {
        startGame();
        int[] miss = squares(false).get(0);
        fire(miss);
        assertEquals(1, game.getMisses());
        assertEquals(1, game.getTotalMisses());
        assertEquals('M', game.getCellState(miss[0], miss[1]));
    }

    @Test
    public void hitResetsCurrentMissesButKeepsTotalMisses()
    {
        startGame();
        for (int i = 0; i < 4; i++)
            fire(squares(false).get(i));
        fire(squares(true).get(0));
        assertEquals(0, game.getMisses());
        assertEquals(4, game.getTotalMisses());
        assertEquals(0, game.getStrikes());
    }

    @Test
    public void fifthConsecutiveMissAddsStrikeAndResetsMisses()
    {
        startGame();
        for (int i = 0; i < 5; i++)
            fire(squares(false).get(i));
        assertEquals(1, game.getStrikes());
        assertEquals(0, game.getMisses());
        assertEquals(5, game.getTotalMisses());
    }

    @Test
    public void hitDoesNotEraseAnExistingStrike()
    {
        startGame();
        for (int i = 0; i < 6; i++)
            fire(squares(false).get(i));
        fire(squares(true).get(0));
        assertEquals(1, game.getStrikes());
        assertEquals(0, game.getMisses());
        assertEquals(6, game.getTotalMisses());
    }

    @Test
    public void threeStrikesCauseLossAndBlockMoreShots()
    {
        startGame();
        for (int i = 0; i < 15; i++)
            fire(squares(false).get(i));
        assertTrue(game.hasLost());
        assertFalse(game.hasWon());
        assertEquals(3, game.getStrikes());
        assertEquals(0, game.getMisses());
        int[] hit = squares(true).get(0);
        assertFalse(game.fire(hit[0], hit[1]));
        assertEquals(0, game.getTotalHits());
        assertEquals('~', game.getCellState(hit[0], hit[1]));
    }

    @Test
    public void repeatedMissDoesNotCountAgain()
    {
        startGame();
        int[] miss = squares(false).get(0);
        fire(miss);
        assertFalse(game.fire(miss[0], miss[1]));
        assertEquals(1, game.getMisses());
        assertEquals(1, game.getTotalMisses());
    }

    @Test
    public void repeatedHitDoesNotDamageShipAgain()
    {
        startGame();
        int[] hit = squares(true).get(0);
        fire(hit);
        assertFalse(game.fire(hit[0], hit[1]));
        assertEquals(1, game.getTotalHits());
        assertEquals(1, board.getShip(hit[0], hit[1]).getHits());
    }

    @Test
    public void invalidCoordinatesAreRejectedWithoutChangingCounters()
    {
        startGame();
        assertFalse(game.fire(-1, 0));
        assertFalse(game.fire(10, 0));
        assertFalse(game.fire(0, -1));
        assertFalse(game.fire(0, 10));
        assertEquals(0, game.getTotalHits() + game.getTotalMisses());
    }

    @Test
    public void sunkShipIsReportedOnlyAfterItsFinalHit()
    {
        startGame();
        int[] first = squares(true).get(0);
        Ship target = board.getShip(first[0], first[1]);
        List<int[]> targetCells = new ArrayList<>();
        for (int[] cell : squares(true))
            if (board.getShip(cell[0], cell[1]) == target)
                targetCells.add(cell);
        for (int i = 0; i < targetCells.size(); i++)
        {
            fire(targetCells.get(i));
            if (i < targetCells.size() - 1)
                assertNull(game.getLastSunkShip());
        }
        assertSame(target, game.getLastSunkShip());
        assertTrue(target.isSunk());
        fire(squares(false).get(0));
        assertNull(game.getLastSunkShip());
    }

    @Test
    public void allSeventeenHitsWinAndBlockFurtherShots()
    {
        startGame();
        for (int[] hit : squares(true))
            fire(hit);
        assertEquals(17, game.getTotalHits());
        assertTrue(game.hasWon());
        assertFalse(game.hasLost());
        int[] miss = squares(false).get(0);
        assertFalse(game.fire(miss[0], miss[1]));
        assertEquals(0, game.getTotalMisses());
    }

    @Test
    public void resetRestoresHiddenFleetAndClearsAllState()
    {
        startGame();
        for (int[] hit : squares(true))
            fire(hit);
        game.reset();
        assertEquals(0, game.getTotalHits());
        assertEquals(0, game.getTotalMisses());
        assertEquals(0, game.getMisses());
        assertEquals(0, game.getStrikes());
        assertNull(game.getLastSunkShip());
        assertFalse(game.isGameOver());
        assertEquals(17, squares(true).size());
        for (int row = 0; row < 10; row++)
            for (int col = 0; col < 10; col++)
            {
                assertEquals('~', game.getCellState(row, col));
                Ship ship = board.getShip(row, col);
                if (ship != null)
                    assertEquals(0, ship.getHits());
            }
    }

    @Test
    public void shipCannotAccumulateHitsPastItsLength()
    {
        Ship ship = new Ship("Destroyer", 2);
        assertFalse(ship.isSunk());
        ship.registerHit();
        assertFalse(ship.isSunk());
        ship.registerHit();
        ship.registerHit();
        assertTrue(ship.isSunk());
        assertEquals(2, ship.getHits());
    }

    @Test
    public void buttonDisablesAfterShotAndEnablesAfterReset() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            BattleShipButton button = new BattleShipButton(2, 4);
            assertEquals(2, button.getRow());
            assertEquals(4, button.getCol());
            assertTrue(button.isEnabled());
            button.showState('X');
            assertFalse(button.isEnabled());
            button.showState('M');
            assertFalse(button.isEnabled());
            button.showState('~');
            assertTrue(button.isEnabled());
        });
    }
}

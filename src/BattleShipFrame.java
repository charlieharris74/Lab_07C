import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class BattleShipFrame extends JFrame
{
    private final BattleShipGame game = new BattleShipGame();
    private final BattleShipButton[][] buttons = new BattleShipButton[10][10];
    private final JLabel missLabel = new JLabel();
    private final JLabel strikeLabel = new JLabel();
    private final JLabel totalMissLabel = new JLabel();
    private final JLabel totalHitLabel = new JLabel();
    private final JLabel messageLabel = new JLabel("Choose a square to fire.", SwingConstants.CENTER);

    public BattleShipFrame()
    {
        super("Battleship");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        JLabel title = new JLabel("Battleship", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        topPanel.add(title, BorderLayout.NORTH);
        JPanel counters = new JPanel(new GridLayout(2, 2, 10, 5));
        counters.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        counters.add(missLabel);
        counters.add(strikeLabel);
        counters.add(totalMissLabel);
        counters.add(totalHitLabel);
        topPanel.add(counters, BorderLayout.CENTER);
        topPanel.add(messageLabel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        JPanel boardPanel = new JPanel(new GridLayout(11, 11, 3, 3));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        boardPanel.add(new JLabel(""));
        for (int col = 0; col < 10; col++)
            boardPanel.add(new JLabel(String.valueOf(col + 1), SwingConstants.CENTER));
        for (int row = 0; row < 10; row++)
        {
            boardPanel.add(new JLabel(String.valueOf(row + 1), SwingConstants.CENTER));
            for (int col = 0; col < 10; col++)
            {
                BattleShipButton button = new BattleShipButton(row, col);
                button.addActionListener(event -> fireAt((BattleShipButton) event.getSource()));
                buttons[row][col] = button;
                boardPanel.add(button);
            }
        }
        add(boardPanel, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        JButton playAgain = new JButton("Play Again");
        playAgain.addActionListener(event -> confirmNewGame());
        JButton quit = new JButton("Quit");
        quit.addActionListener(event -> quitGame());
        controls.add(playAgain);
        controls.add(quit);
        add(controls, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent event)
            {
                quitGame();
            }
        });
        updateCounters();
        setSize(700, 790);
        setMinimumSize(new Dimension(600, 680));
        setLocationRelativeTo(null);
    }

    private void fireAt(BattleShipButton button)
    {
        if (!game.fire(button.getRow(), button.getCol()))
            return;
        char state = game.getCellState(button.getRow(), button.getCol());
        button.showState(state);
        messageLabel.setText(state == 'X' ? "Hit!" : "Miss!");
        updateCounters();
        Ship sunk = game.getLastSunkShip();
        if (sunk != null)
        {
            JOptionPane.showMessageDialog(this, "You sunk the " + sunk.getName() + "!",
                    "Ship sunk", JOptionPane.INFORMATION_MESSAGE);
        }
        if (game.isGameOver())
        {
            for (BattleShipButton[] row : buttons)
                for (BattleShipButton cell : row)
                    cell.setEnabled(false);
            String result = game.hasWon() ? "You won! You sunk all five ships."
                    : "You lost! You reached three strikes.";
            messageLabel.setText(game.hasWon() ? "You won!" : "You lost!");
            JOptionPane.showMessageDialog(this, result, "Game over", JOptionPane.INFORMATION_MESSAGE);
            int answer = JOptionPane.showConfirmDialog(this, "Would you like to play again?",
                    "Play again", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION)
                newGame();
        }
    }

    private void updateCounters()
    {
        missLabel.setText("Current misses: " + game.getMisses() + " / 5");
        strikeLabel.setText("Strikes: " + game.getStrikes() + " / 3");
        totalMissLabel.setText("Total misses: " + game.getTotalMisses() + " / 83");
        totalHitLabel.setText("Total hits: " + game.getTotalHits() + " / 17");
    }

    private void confirmNewGame()
    {
        int answer = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to start a new game?", "Play again", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION)
            newGame();
    }

    private void newGame()
    {
        game.reset();
        for (BattleShipButton[] row : buttons)
            for (BattleShipButton button : row)
                button.showState('~');
        updateCounters();
        messageLabel.setText("Choose a square to fire.");
    }

    private void quitGame()
    {
        int answer = JOptionPane.showConfirmDialog(this,
                "Would you like to play again instead of quitting?", "Quit game",
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (answer == JOptionPane.YES_OPTION)
            newGame();
        else if (answer == JOptionPane.NO_OPTION)
            dispose();
    }
}

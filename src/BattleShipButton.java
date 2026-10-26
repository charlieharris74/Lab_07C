import javax.swing.JButton;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;

public class BattleShipButton extends JButton
{
    private final int row;
    private final int col;
    private char state = '~';

    public BattleShipButton(int row, int col)
    {
        this.row = row;
        this.col = col;
        setFont(new Font("SansSerif", Font.BOLD, 23));
        setMargin(new Insets(0, 0, 0, 0));
        setFocusPainted(false);
        showState('~');
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public void showState(char state)
    {
        this.state = state;
        setEnabled(state == '~');
        setToolTipText("Row " + (row + 1) + ", column " + (col + 1));
        getAccessibleContext().setAccessibleName("Row " + (row + 1)
                + ", column " + (col + 1) + ": " + state);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);
        // Draw the mark ourselves so disabling a square does not gray out its color.
        Graphics g = graphics.create();
        if (state == 'X')
            g.setColor(new Color(220, 35, 35));
        else if (state == 'M')
            g.setColor(new Color(255, 210, 0));
        else
            g.setColor(new Color(80, 180, 235));
        g.setFont(getFont());
        FontMetrics metrics = g.getFontMetrics();
        String mark = String.valueOf(state);
        int x = (getWidth() - metrics.stringWidth(mark)) / 2;
        int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        g.drawString(mark, x, y);
        g.dispose();
    }
}

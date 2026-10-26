import javax.swing.SwingUtilities;

public class BattleShipRunner
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            BattleShipFrame frame = new BattleShipFrame();
            frame.setVisible(true);
        });
    }
}

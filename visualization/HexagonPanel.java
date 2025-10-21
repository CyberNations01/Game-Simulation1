import javax.swing.*;
import java.awt.*;
import java.util.List;

public class HexagonPanel extends JPanel {
    private List<HexData> hexes;
    private HexagonDrawer drawer;
    
    public HexagonPanel(List<HexData> hexes) {
        this.hexes = hexes;
        this.drawer = new HexagonDrawer();
        setPreferredSize(new Dimension(900, 600));
        setMinimumSize(new Dimension(900, 600));
        setMaximumSize(new Dimension(900, 600));
        setBackground(Color.WHITE);
    }
    
    public void setHexes(List<HexData> hexes) {
        this.hexes = hexes;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        if (hexes != null && !hexes.isEmpty()) {
            drawer.drawHexagons(g2d, hexes, getWidth(), getHeight());
        }
    }
}

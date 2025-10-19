import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;

public class HexagonDrawer {
    private static final int HEX_SIZE = 120;
    private static final int HEX_SPACING = 0; // Set spacing to 0 for tight packing
    
    public void drawHexagons(Graphics2D g2d, List<HexData> hexes, int panelWidth, int panelHeight) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int centerX = panelWidth / 2;
        int centerY = panelHeight / 2;
        
        // Draw hexagons in the specific pattern
        for (int i = 0; i < hexes.size(); i++) {
            HexData hex = hexes.get(i);
            int[] position = getHexagonPosition(hex.getId(), centerX, centerY);
            drawHexagon(g2d, hex, position[0], position[1]);
        }
    }
    
    private int[] getHexagonPosition(int hexId, int centerX, int centerY) {
        int x, y;
        double s = HEX_SIZE / 2.0; // Side length of the hexagon
        double h = s * Math.sqrt(3); // Height of the hexagon (distance between flat sides)

        // Distances between centers of *touching* hexagons
        double vertical_dist_touching = h;
        double horizontal_dist_touching = s * 1.5;
        double diagonal_vertical_dist_touching = h * 0.5;

        switch (hexId) {
            case 1: // Inner Stack - Center
                x = centerX;
                y = centerY;
                break;

            // Middle Stacks (2-7) - one ring around the center, touching
            case 2: // Top
                x = centerX;
                y = centerY - (int) vertical_dist_touching;
                break;

            case 3: // Top Right
                x = centerX + (int) horizontal_dist_touching;
                y = centerY - (int) diagonal_vertical_dist_touching;
                break;

            case 4: // Bottom Right
                x = centerX + (int) horizontal_dist_touching;
                y = centerY + (int) diagonal_vertical_dist_touching;
                break;

            case 5: // Bottom
                x = centerX;
                y = centerY + (int) vertical_dist_touching;
                break;

            case 6: // Bottom Left
                x = centerX - (int) horizontal_dist_touching;
                y = centerY + (int) diagonal_vertical_dist_touching;
                break;

            case 7: // Top Left
                x = centerX - (int) horizontal_dist_touching;
                y = centerY - (int) diagonal_vertical_dist_touching;
                break;

            // Outer Stacks (8-11) - corners of the larger grid, touching
            case 8: // Top Left Outer
                x = centerX - (int) (2 * horizontal_dist_touching);
                y = centerY - (int) vertical_dist_touching;
                break;

            case 9: // Top Right Outer
                x = centerX + (int) (2 * horizontal_dist_touching);
                y = centerY - (int) vertical_dist_touching;
                break;

            case 10: // Bottom Right Outer
                x = centerX + (int) (2 * horizontal_dist_touching);
                y = centerY + (int) vertical_dist_touching;
                break;

            case 11: // Bottom Left Outer
                x = centerX - (int) (2 * horizontal_dist_touching);
                y = centerY + (int) vertical_dist_touching;
                break;

            default:
                x = centerX;
                y = centerY;
                break;
        }
        return new int[]{x, y};
    }
    
    private void drawHexagon(Graphics2D g2d, HexData hex, int centerX, int centerY) {
        // Draw base layer (Wilds/Wastes/Gray)
        Path2D basePath = createHexagonPath(centerX, centerY, HEX_SIZE);
        Color baseColor = getColorFromString(hex.getBaseType());
        g2d.setColor(baseColor);
        g2d.fill(basePath);
        
        // Draw top layer (DevA/DevB) if exists
        if (hex.getTopType() != null && isDevelopmentType(hex.getTopType())) {
            int topSize = (int)(HEX_SIZE * 0.6); // Smaller, 60% size
            Path2D topPath = createHexagonPath(centerX, centerY, topSize);
            Color topColor = getColorFromString(hex.getTopType());
            g2d.setColor(topColor);
            g2d.fill(topPath);
        }
        
        // Draw border for base layer
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(basePath);
        
        // Draw border for top layer if exists
        if (hex.getTopType() != null && isDevelopmentType(hex.getTopType())) {
            int topSize = (int)(HEX_SIZE * 0.6);
            Path2D topPath = createHexagonPath(centerX, centerY, topSize);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            g2d.draw(topPath);
        }
        
        // Draw ID and type text
        g2d.setColor(Color.WHITE);
        
        // Use smaller font for text
        Font textFont = new Font("Arial", Font.BOLD, 12);
        g2d.setFont(textFont);
        
        String idText = String.valueOf(hex.getId());
        String stackType = getStackType(hex.getId());
        
        FontMetrics fm = g2d.getFontMetrics();
        int idWidth = fm.stringWidth(idText);
        int stackWidth = fm.stringWidth(stackType);
        
        // Draw ID
        g2d.drawString(idText, centerX - idWidth/2, centerY - 8);
        // Draw stack type
        g2d.drawString(stackType, centerX - stackWidth/2, centerY + 8);
    }
    
    private Path2D createHexagonPath(int centerX, int centerY, int size) {
        Path2D path = new Path2D.Double();
        double radius = size / 2.0;
        
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3 * i;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.closePath();
        return path;
    }
    
    private String getStackType(int hexId) {
        switch (hexId) {
            case 1:
                return "Inner";
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                return "Middle";
            case 8:
            case 9:
            case 10:
            case 11:
                return "Outer";
            default:
                return "Unknown";
        }
    }
    
    private boolean isDevelopmentType(String type) {
        if (type == null) return false;
        String lowerType = type.toLowerCase();
        return lowerType.equals("deva") || lowerType.equals("devb") || 
               lowerType.equals("dev a") || lowerType.equals("dev b");
    }
    
    private Color getColorFromString(String colorName) {
        switch (colorName.toLowerCase()) {
            case "green":
                return new Color(0x51ad2f); // Green: #51ad2f
            case "brown":
                return new Color(0xa17a6d); // Brown: #a17a6d
            case "blue":
                return new Color(0x0ca3dd); // Blue: #0ca3dd
            case "pink":
                return new Color(0xe172d3); // Pink: #e172d3
            case "red":
                return new Color(0xff0000); // Red: #ff0000
            case "yellow":
                return new Color(0xffff00); // Yellow: #ffff00
            case "gray":
                return new Color(0x808080); // Gray: #808080
            // Handle type names
            case "wilds":
            case "wild":
                return new Color(0x51ad2f); // Wilds/Wild -> Green
            case "wastes":
            case "waste":
                return new Color(0xa17a6d); // Wastes/Waste -> Brown
            case "deva":
                return new Color(0x0ca3dd); // DevA -> Blue
            case "devb":
                return new Color(0xe172d3); // DevB -> Pink
            default:
                return Color.GRAY;
        }
    }
}

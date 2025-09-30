import java.util.List;

public class GameBoard {
    private List<HexData> hexes;
    
    public GameBoard() {}
    
    public List<HexData> getHexes() { return hexes; }
    public void setHexes(List<HexData> hexes) { this.hexes = hexes; }
}

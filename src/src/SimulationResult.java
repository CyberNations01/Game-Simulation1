import java.util.ArrayList;
import java.util.List;

public class SimulationResult {
    public int version = 1;
    public Board board;
    public GameState game_state;
    public TokenStats tokens;

    public static class Board {
        public List<Hex> hexes = new ArrayList<>();
    }

    public static class Hex {
        public int id;
        public String color;
        public String type;

        public Hex(int id, String color, String type) {
            this.id = id;
            this.color = color;
            this.type = type;
        }
    }

    public static class GameState {
        public int current_round;
        public int max_rounds;
        public int bag_total;
    }

    public static class TokenStats {
        public int Wilds;
        public int Wastes;
        public int DevA;
        public int DevB;
    }
}
public class GameData {
    private int version;
    private GameBoard board;
    private GameState game_state;
    private Tokens tokens;
    
    public GameData() {}
    
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    
    public GameBoard getBoard() { return board; }
    public void setBoard(GameBoard board) { this.board = board; }
    
    public GameState getGame_state() { return game_state; }
    public void setGame_state(GameState game_state) { this.game_state = game_state; }
    
    public Tokens getTokens() { return tokens; }
    public void setTokens(Tokens tokens) { this.tokens = tokens; }
}

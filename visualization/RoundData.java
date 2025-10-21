public class RoundData {
    private int round_number;
    private GameBoard board;
    private GameState game_state;
    private Tokens tokens;
    
    public RoundData() {}
    
    public int getRound_number() { return round_number; }
    public void setRound_number(int round_number) { this.round_number = round_number; }
    
    public GameBoard getBoard() { return board; }
    public void setBoard(GameBoard board) { this.board = board; }
    
    public GameState getGame_state() { return game_state; }
    public void setGame_state(GameState game_state) { this.game_state = game_state; }
    
    public Tokens getTokens() { return tokens; }
    public void setTokens(Tokens tokens) { this.tokens = tokens; }
}



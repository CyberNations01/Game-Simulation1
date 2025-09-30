public class GameState {
    private int current_round;
    private int max_rounds;
    private int bag_total;
    
    public GameState() {}
    
    public int getCurrent_round() { return current_round; }
    public void setCurrent_round(int current_round) { this.current_round = current_round; }
    
    public int getMax_rounds() { return max_rounds; }
    public void setMax_rounds(int max_rounds) { this.max_rounds = max_rounds; }
    
    public int getBag_total() { return bag_total; }
    public void setBag_total(int bag_total) { this.bag_total = bag_total; }
}

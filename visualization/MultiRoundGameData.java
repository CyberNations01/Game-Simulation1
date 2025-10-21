import java.util.List;
import java.util.ArrayList;

public class MultiRoundGameData {
    private int version;
    private int total_rounds;
    private List<RoundData> rounds;
    
    public MultiRoundGameData() {}
    
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    
    public int getTotal_rounds() { return total_rounds; }
    public void setTotal_rounds(int total_rounds) { this.total_rounds = total_rounds; }
    
    public List<RoundData> getRounds() { return rounds; }
    public void setRounds(List<RoundData> rounds) { this.rounds = rounds; }
    
    // Helper method to get current round data
    public RoundData getCurrentRound(int roundNumber) {
        if (rounds != null && roundNumber > 0 && roundNumber <= rounds.size()) {
            return rounds.get(roundNumber - 1);
        }
        return null;
    }
    
    // Helper method to get total number of rounds
    public int getRoundCount() {
        return rounds != null ? rounds.size() : 0;
    }
    
    // Helper method to add a round
    public void addRound(RoundData round) {
        if (rounds == null) {
            rounds = new ArrayList<>();
        }
        rounds.add(round);
    }
    
    // Helper method to get a specific round
    public RoundData getRound(int index) {
        if (rounds != null && index >= 0 && index < rounds.size()) {
            return rounds.get(index);
        }
        return null;
    }
}



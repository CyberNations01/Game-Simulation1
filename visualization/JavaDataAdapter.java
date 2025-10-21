import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.ArrayList;

public class JavaDataAdapter {
    
    public static MultiRoundGameData convertToMultiRound(String jsonString) {
        try {
            JsonObject root = JsonParser.parseString(jsonString).getAsJsonObject();
            MultiRoundGameData multiRoundData = new MultiRoundGameData();
            
            // Set basic info
            multiRoundData.setVersion(root.get("version").getAsInt());
            
            // Get timeline data
            JsonArray timeline = root.getAsJsonArray("timeline");
            List<RoundData> rounds = new ArrayList<>();
            
            // Get legend for state mapping
            JsonObject legend = root.getAsJsonObject("legend");
            String[] stateNames = new String[5]; // 0-4, 0 unused
            for (String key : legend.keySet()) {
                int value = legend.get(key).getAsInt();
                stateNames[value] = key;
            }
            
            // Convert each timeline entry to a round
            for (JsonElement timelineEntry : timeline) {
                JsonObject entry = timelineEntry.getAsJsonObject();
                RoundData roundData = new RoundData();
                
                int roundNumber = entry.get("round").getAsInt();
                roundData.setRound_number(roundNumber + 1); // Convert 0-based to 1-based
                
                // Create board for this round
                GameBoard board = new GameBoard();
                List<HexData> hexes = new ArrayList<>();
                
                JsonArray states = entry.getAsJsonArray("states");
                for (int i = 0; i < states.size(); i++) {
                    int stateValue = states.get(i).getAsInt();
                    String stateName = stateNames[stateValue];
                    
                    // Use new constructor that handles single-type to layered mapping
                    HexData hex = new HexData(i + 1, stateName, getColorFromState(stateName));
                    
                    hexes.add(hex);
                }
                
                board.setHexes(hexes);
                roundData.setBoard(board);
                
                // Create game state (use final state from original data)
                GameState gameState = new GameState();
                JsonObject originalGameState = root.getAsJsonObject("game_state");
                gameState.setCurrent_round(roundNumber + 1);
                gameState.setMax_rounds(originalGameState.get("max_rounds").getAsInt());
                
                // Calculate bag total for this round (decreasing over time)
                int finalBagTotal = originalGameState.get("bag_total").getAsInt();
                int maxRounds = originalGameState.get("max_rounds").getAsInt();
                int currentBagTotal = Math.max(finalBagTotal, 100 - (roundNumber * (100 - finalBagTotal) / maxRounds));
                gameState.setBag_total(currentBagTotal);
                
                roundData.setGame_state(gameState);
                
                // Create tokens (use final state from original data)
                Tokens tokens = new Tokens();
                JsonObject originalTokens = root.getAsJsonObject("tokens");
                tokens.setWilds(originalTokens.get("WILDS").getAsInt());
                tokens.setWastes(originalTokens.get("WASTES").getAsInt());
                tokens.setDevA(originalTokens.get("DEVA").getAsInt());
                tokens.setDevB(originalTokens.get("DEVB").getAsInt());
                
                roundData.setTokens(tokens);
                rounds.add(roundData);
            }
            
            multiRoundData.setRounds(rounds);
            multiRoundData.setTotal_rounds(rounds.size());
            
            return multiRoundData;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static String getColorFromState(String stateName) {
        switch (stateName) {
            case "WILDS": return "green";
            case "WASTES": return "brown";
            case "DEVA": return "blue";
            case "DEVB": return "pink";
            default: return "gray";
        }
    }
}

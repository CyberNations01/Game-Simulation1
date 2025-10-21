import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.ArrayList;

public class CDataAdapter {
    
    public static MultiRoundGameData convertToMultiRound(String jsonString) {
        try {
            JsonArray root = JsonParser.parseString(jsonString).getAsJsonArray();
            MultiRoundGameData multiRoundData = new MultiRoundGameData();
            
            List<RoundData> rounds = new ArrayList<>();
            
            for (JsonElement roundElement : root) {
                JsonObject roundObj = roundElement.getAsJsonObject();
                RoundData roundData = new RoundData();
                
                // Set round number (1-based)
                roundData.setRound_number(rounds.size() + 1);
                
                // Convert board
                GameBoard board = new GameBoard();
                List<HexData> hexes = new ArrayList<>();
                
                JsonObject boardObj = roundObj.getAsJsonObject("board");
                JsonArray hexArray = boardObj.getAsJsonArray("hex");
                
                for (JsonElement hexElement : hexArray) {
                    JsonObject hexObj = hexElement.getAsJsonObject();
                    
                    int id = hexObj.get("id").getAsInt();
                    String type = hexObj.get("type").getAsString();
                    
                    // Use new constructor that handles single-type to layered mapping
                    HexData hex = new HexData(id, type, getColorFromType(type));
                    
                    hexes.add(hex);
                }
                
                board.setHexes(hexes);
                roundData.setBoard(board);
                
                // Convert game state
                GameState gameState = new GameState();
                JsonObject gameStateObj = roundObj.getAsJsonObject("game_state");
                gameState.setCurrent_round(gameStateObj.get("current_round").getAsInt());
                gameState.setMax_rounds(gameStateObj.get("max_round").getAsInt()); // Note: "max_round" not "max_rounds"
                gameState.setBag_total(gameStateObj.get("bag_total").getAsInt());
                
                roundData.setGame_state(gameState);
                
                // Convert tokens
                Tokens tokens = new Tokens();
                JsonObject tokensObj = roundObj.getAsJsonObject("tokens");
                tokens.setWilds(tokensObj.get("Wilds").getAsInt());
                tokens.setWastes(tokensObj.get("Wastes").getAsInt());
                tokens.setDevA(tokensObj.get("DevA").getAsInt());
                tokens.setDevB(tokensObj.get("DevB").getAsInt());
                
                roundData.setTokens(tokens);
                rounds.add(roundData);
            }
            
            multiRoundData.setRounds(rounds);
            multiRoundData.setTotal_rounds(rounds.size());
            multiRoundData.setVersion(rounds.get(0).getGame_state().getCurrent_round()); // Use first round's version
            
            return multiRoundData;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static String getColorFromType(String type) {
        switch (type) {
            case "WILD": return "green";
            case "WASTE": return "brown";
            case "DEVA": return "blue";
            case "DEVB": return "pink";
            default: return "gray";
        }
    }
}

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

public class DataFormatDetector {
    
    public enum DataFormat {
        MY_FORMAT,      // Your test format (baseType/topType)
        JAVA_FORMAT,    // Java generated format (assets/)
        C_FORMAT,       // C generated format (assets-c/)
        UNKNOWN
    }
    
    public static DataFormat detectFormat(String jsonString) {
        try {
            JsonElement root = JsonParser.parseString(jsonString);
            
            // Check if it's an array (C format)
            if (root.isJsonArray()) {
                return DataFormat.C_FORMAT;
            }
            
            // Check if it's an object
            if (root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();
                
                // Check for Java format indicators
                if (obj.has("legend") && obj.has("timeline")) {
                    return DataFormat.JAVA_FORMAT;
                }
                
                // Check for your format indicators (single round)
                if (obj.has("board")) {
                    JsonObject board = obj.getAsJsonObject("board");
                    if (board.has("hexes")) {
                        JsonArray hexes = board.getAsJsonArray("hexes");
                        if (hexes.size() > 0) {
                            JsonObject firstHex = hexes.get(0).getAsJsonObject();
                            if (firstHex.has("baseType") || firstHex.has("topType")) {
                                return DataFormat.MY_FORMAT;
                            }
                        }
                    }
                }
                
                // Check for your multi-round format indicators
                if (obj.has("rounds") && obj.has("total_rounds")) {
                    JsonArray rounds = obj.getAsJsonArray("rounds");
                    if (rounds.size() > 0) {
                        JsonObject firstRound = rounds.get(0).getAsJsonObject();
                        if (firstRound.has("board")) {
                            JsonObject board = firstRound.getAsJsonObject("board");
                            if (board.has("hexes")) {
                                JsonArray hexes = board.getAsJsonArray("hexes");
                                if (hexes.size() > 0) {
                                    JsonObject firstHex = hexes.get(0).getAsJsonObject();
                                    if (firstHex.has("baseType") || firstHex.has("topType")) {
                                        return DataFormat.MY_FORMAT;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            return DataFormat.UNKNOWN;
        } catch (Exception e) {
            return DataFormat.UNKNOWN;
        }
    }
    
    public static DataFormat detectFormatFromFile(String filePath) {
        try {
            java.io.FileReader reader = new java.io.FileReader(filePath);
            StringBuilder content = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                content.append((char) ch);
            }
            reader.close();
            return detectFormat(content.toString());
        } catch (Exception e) {
            return DataFormat.UNKNOWN;
        }
    }
}

import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

public class UniversalDataConverter {
    
    public static MultiRoundGameData convertFileToMultiRound(String filePath) throws IOException {
        String jsonContent = readFileContent(filePath);
        DataFormatDetector.DataFormat format = DataFormatDetector.detectFormat(jsonContent);
        
        switch (format) {
            case MY_FORMAT:
                return convertMyFormatToMultiRound(jsonContent);
            case JAVA_FORMAT:
                return JavaDataAdapter.convertToMultiRound(jsonContent);
            case C_FORMAT:
                return CDataAdapter.convertToMultiRound(jsonContent);
            default:
                throw new IOException("Unsupported data format in file: " + filePath);
        }
    }
    
    public static GameData convertFileToSingleRound(String filePath) throws IOException {
        String jsonContent = readFileContent(filePath);
        DataFormatDetector.DataFormat format = DataFormatDetector.detectFormat(jsonContent);
        
        switch (format) {
            case MY_FORMAT:
                return convertMyFormatToSingleRound(jsonContent);
            case JAVA_FORMAT:
                return convertJavaFormatToSingleRound(jsonContent);
            case C_FORMAT:
                return convertCFormatToSingleRound(jsonContent);
            default:
                throw new IOException("Unsupported data format in file: " + filePath);
        }
    }
    
    private static String readFileContent(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    public static MultiRoundGameData convertMyFormatToMultiRound(String jsonContent) {
        // Check if it's already multi-round format
        if (jsonContent.contains("\"rounds\"") && jsonContent.contains("\"total_rounds\"")) {
            // It's already multi-round format, parse directly
            JsonReader reader = new JsonReader();
            return reader.readMultiRoundGameDataFromString(jsonContent);
        } else {
            // Your format is single round, convert to multi-round with one round
            GameData singleRound = convertMyFormatToSingleRound(jsonContent);
            MultiRoundGameData multiRound = new MultiRoundGameData();
            multiRound.setVersion(singleRound.getVersion());
            multiRound.setTotal_rounds(1);
            
            RoundData roundData = new RoundData();
            roundData.setRound_number(1);
            roundData.setBoard(singleRound.getBoard());
            roundData.setGame_state(singleRound.getGame_state());
            roundData.setTokens(singleRound.getTokens());
            
            java.util.List<RoundData> rounds = new java.util.ArrayList<>();
            rounds.add(roundData);
            multiRound.setRounds(rounds);
            
            return multiRound;
        }
    }
    
    private static GameData convertMyFormatToSingleRound(String jsonContent) {
        // Use existing JsonReader for your format
        JsonReader reader = new JsonReader();
        return reader.readGameDataFromString(jsonContent);
    }
    
    private static GameData convertJavaFormatToSingleRound(String jsonContent) {
        // Convert Java format to single round (use final state)
        MultiRoundGameData multiRound = JavaDataAdapter.convertToMultiRound(jsonContent);
        if (multiRound != null && !multiRound.getRounds().isEmpty()) {
            RoundData lastRound = multiRound.getRounds().get(multiRound.getRounds().size() - 1);
            return convertRoundToGameData(lastRound, multiRound.getVersion());
        }
        return null;
    }
    
    private static GameData convertCFormatToSingleRound(String jsonContent) {
        // Convert C format to single round (use last round)
        MultiRoundGameData multiRound = CDataAdapter.convertToMultiRound(jsonContent);
        if (multiRound != null && !multiRound.getRounds().isEmpty()) {
            RoundData lastRound = multiRound.getRounds().get(multiRound.getRounds().size() - 1);
            return convertRoundToGameData(lastRound, multiRound.getVersion());
        }
        return null;
    }
    
    private static GameData convertRoundToGameData(RoundData roundData, int version) {
        GameData gameData = new GameData();
        gameData.setVersion(version);
        gameData.setBoard(roundData.getBoard());
        gameData.setGame_state(roundData.getGame_state());
        gameData.setTokens(roundData.getTokens());
        return gameData;
    }
}

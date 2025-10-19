import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.IOException;

public class JsonReader {
    private Gson gson;
    
    public JsonReader() {
        this.gson = new GsonBuilder().create();
    }
    
    public GameData readGameData(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, GameData.class);
        }
    }
    
    public GameData readGameDataFromString(String jsonString) {
        return gson.fromJson(jsonString, GameData.class);
    }
    
    public MultiRoundGameData readMultiRoundGameData(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, MultiRoundGameData.class);
        }
    }
    
    public MultiRoundGameData readMultiRoundGameDataFromString(String jsonString) {
        return gson.fromJson(jsonString, MultiRoundGameData.class);
    }
    
    // Universal methods that auto-detect format
    public MultiRoundGameData readUniversalMultiRoundData(String filePath) throws IOException {
        return UniversalDataConverter.convertFileToMultiRound(filePath);
    }
    
    public GameData readUniversalSingleRoundData(String filePath) throws IOException {
        return UniversalDataConverter.convertFileToSingleRound(filePath);
    }
    
    public MultiRoundGameData readUniversalMultiRoundDataFromString(String jsonString) throws IOException {
        DataFormatDetector.DataFormat format = DataFormatDetector.detectFormat(jsonString);
        
        switch (format) {
            case MY_FORMAT:
                return UniversalDataConverter.convertMyFormatToMultiRound(jsonString);
            case JAVA_FORMAT:
                return JavaDataAdapter.convertToMultiRound(jsonString);
            case C_FORMAT:
                return CDataAdapter.convertToMultiRound(jsonString);
            default:
                throw new IOException("Unsupported data format");
        }
    }
    
    public String convertMultiRoundToJson(MultiRoundGameData multiRoundData) {
        return gson.toJson(multiRoundData);
    }
}

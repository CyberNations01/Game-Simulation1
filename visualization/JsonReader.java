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
}

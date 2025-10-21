import java.io.IOException;

public class TestFormat {
    public static void main(String[] args) {
        // Test all three formats
        String[] testFiles = {
            "game_data_multi_rounds.json",  // Our format (format 1)
            "../assets/8WILDS3DEVA_round7.json",  // Java format (format 2)
            "../assets-c/visualization.json"  // C format (format 3)
        };
        
        for (String filePath : testFiles) {
            System.out.println("Testing file: " + filePath);
            try {
                // Test format detection
                DataFormatDetector.DataFormat format = DataFormatDetector.detectFormatFromFile(filePath);
                System.out.println("  Detected format: " + format);
                
                // Test conversion to multi-round
                MultiRoundGameData multiRoundData = UniversalDataConverter.convertFileToMultiRound(filePath);
                if (multiRoundData != null) {
                    System.out.println("  Successfully converted to multi-round format");
                    System.out.println("  Total rounds: " + multiRoundData.getTotal_rounds());
                    
                    // Check first round's hexes to see the mapping
                    if (!multiRoundData.getRounds().isEmpty()) {
                        RoundData firstRound = multiRoundData.getRounds().get(0);
                        System.out.println("  First round hexes:");
                        for (HexData hex : firstRound.getBoard().getHexes()) {
                            System.out.println("    ID: " + hex.getId() + 
                                             ", Type: " + hex.getType() + 
                                             ", BaseType: " + hex.getBaseType() + 
                                             ", TopType: " + hex.getTopType() + 
                                             ", Color: " + hex.getColor());
                        }
                    }
                } else {
                    System.out.println("  Failed to convert to multi-round format");
                }
                
            } catch (Exception e) {
                System.out.println("  Error: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println();
        }
    }
}
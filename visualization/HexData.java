public class HexData {
    private int id;
    private String color;
    private String type;
    private String baseType;  // Wilds or Wastes (base layer)
    private String topType;   // DevA or DevB (top layer, can be null)
    
    public HexData() {}
    
    
    public HexData(int id, String baseType, String topType, boolean isLayered) {
        this.id = id;
        this.baseType = baseType;
        this.topType = topType;
        this.type = topType != null ? topType : baseType; // Prioritize top layer type
        this.color = getColorFromTypes(baseType, topType);
    }
    
    // Compatibility constructor for JSON parsing
    public HexData(int id, String baseType, String topType, String color, String type) {
        this.id = id;
        this.baseType = baseType;
        this.topType = topType;
        this.type = type;
        this.color = color;
    }
    
    // Constructor for single-type formats (Java/C format)
    public HexData(int id, String type, String color) {
        this.id = id;
        this.color = color;
        this.type = type;
        
        // Map single type to baseType and topType based on type
        if (isDevelopmentType(type)) {
            // For DevA/DevB, set as topType with gray base
            this.topType = type;
            this.baseType = "Gray"; // Special base type for development tiles
        } else {
            // For Wilds/Wastes, set as baseType only
            this.baseType = type;
            this.topType = null;
        }
    }
    
    // Helper method to determine if a type is a development type
    private boolean isDevelopmentType(String type) {
        if (type == null) return false;
        String lowerType = type.toLowerCase();
        return lowerType.equals("deva") || lowerType.equals("devb") || 
               lowerType.equals("dev a") || lowerType.equals("dev b");
    }
    
    private String getColorFromTypes(String base, String top) {
        if (top != null) {
            return top; // If there's a top layer, prioritize top layer color
        }
        return base;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getBaseType() { return baseType; }
    public void setBaseType(String baseType) { this.baseType = baseType; }
    
    public String getTopType() { return topType; }
    public void setTopType(String topType) { this.topType = topType; }
}

public class HexData {
    private int id;
    private String color;
    private String type;
    private String baseType;  // Wilds or Wastes (base layer)
    private String topType;   // DevA or DevB (top layer, can be null)
    
    public HexData() {}
    
    public HexData(int id, String color, String type) {
        this.id = id;
        this.color = color;
        this.type = type;
        this.baseType = type; // By default baseType and type are the same
        this.topType = null;
    }
    
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

public class Parameters {
    public enum CyberParameter {
        COHESION,
        CYBERNATION_LEVEL,
        HUMAN_RELATION,
        ENVIRONMENT,
        TECHNOLOGY
    }
    
    private int cohesion = 10;
    private int cybernationLevel = 2;
    private int humanRelation = 7;
    private int environment = 7;
    private int technology = 7;
    
    // Default constructor
    public Parameters() {}
    
    // Constructor with custom values
    public Parameters(int cohesion, int cybernationLevel, int humanRelation, 
                     int environment, int technology) {
        this.cohesion = cohesion;
        this.cybernationLevel = cybernationLevel;
        this.humanRelation = humanRelation;
        this.environment = environment;
        this.technology = technology;
    }
    
    // Getters
    public int getCohesion() { return cohesion; }
    public int getCybernationLevel() { return cybernationLevel; }
    public int getHumanRelation() { return humanRelation; }
    public int getEnvironment() { return environment; }
    public int getTechnology() { return technology; }
    
    // Setters
    public void setCohesion(int cohesion) { this.cohesion = cohesion; }
    public void setCybernationLevel(int cybernationLevel) { this.cybernationLevel = cybernationLevel; }
    public void setHumanRelation(int humanRelation) { this.humanRelation = humanRelation; }
    public void setEnvironment(int environment) { this.environment = environment; }
    public void setTechnology(int technology) { this.technology = technology; }
    
    // Update parameter by enum
    public void updateParameter(CyberParameter param, int value) {
        switch (param) {
            case COHESION:
                this.cohesion = value;
                break;
            case CYBERNATION_LEVEL:
                this.cybernationLevel = value;
                break;
            case HUMAN_RELATION:
                this.humanRelation = value;
                break;
            case ENVIRONMENT:
                this.environment = value;
                break;
            case TECHNOLOGY:
                this.technology = value;
                break;
        }
    }
    
    // Get parameter by enum
    public int getParameter(CyberParameter param) {
        switch (param) {
            case COHESION:
                return this.cohesion;
            case CYBERNATION_LEVEL:
                return this.cybernationLevel;
            case HUMAN_RELATION:
                return this.humanRelation;
            case ENVIRONMENT:
                return this.environment;
            case TECHNOLOGY:
                return this.technology;
            default:
                return 0;
        }
    }
    
    @Override
    public String toString() {
        return String.format("Cohesion: %d, Cybernation: %d, HumanRelation: %d, Environment: %d, Technology: %d",
                cohesion, cybernationLevel, humanRelation, environment, technology);
    }
}

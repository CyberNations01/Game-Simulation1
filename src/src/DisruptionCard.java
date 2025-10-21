import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class DisruptionCard {
    public enum DisruptionType {
        DISRUPT, BOOST
    }
    
    public enum DisruptionEffect {
        // Stack effects
        DISRUPTION_TURN_WASTE,
        DISRUPTION_TURN_WILD,
        DISRUPTION_TURN_DEVA,
        DISRUPTION_TURN_DEVB,
        
        // Resource effects
        DISRUPTION_CO,
        DISRUPTION_HR,
        DISRUPTION_CY,
        DISRUPTION_TECH,
        DISRUPTION_ENV,
        DISRUPTION_RESOURCES,
        DISRUPTION_TOKEN,
        DISRUPTION_TRADE,
        
        // Rule effects
        DISRUPTION_CAP_ENV,
        DISRUPTION_IGNORE_COHESION_EFFECT,
        
        // Metadata effects
        DISRUPTION_SWAP_GOAL,
        DISRUPTION_DRAW_GOAL,
        DISRUPTION_MOV_PPL
    }
    
    private String name;
    private String description;
    private DisruptionType type;
    private List<Integer> stackTarget;
    private List<Map.Entry<DisruptionEffect, Integer>> effects;
    private List<Map.Entry<DisruptionEffect, Integer>> cancelCost;
    private boolean hasCondition;
    private boolean canCancel;
    
    // Default constructor
    public DisruptionCard() {
        this.name = "";
        this.description = "";
        this.type = DisruptionType.DISRUPT;
        this.stackTarget = new ArrayList<>();
        this.effects = new ArrayList<>();
        this.cancelCost = new ArrayList<>();
        this.hasCondition = false;
        this.canCancel = false;
    }
    
    // Constructor from JSON
    public DisruptionCard(JsonNode cardData) {
        this();
        fromJson(cardData);
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public DisruptionType getType() { return type; }
    public List<Integer> getStackTarget() { return stackTarget; }
    public List<Map.Entry<DisruptionEffect, Integer>> getEffects() { return effects; }
    public List<Map.Entry<DisruptionEffect, Integer>> getCancelCost() { return cancelCost; }
    public boolean hasCondition() { return hasCondition; }
    public boolean canCancel() { return canCancel; }
    
    // Check if card has tile change effects
    public boolean hasTileChangeEffect() {
        return effects.stream().anyMatch(effect -> 
            effect.getKey() == DisruptionEffect.DISRUPTION_TURN_WASTE ||
            effect.getKey() == DisruptionEffect.DISRUPTION_TURN_WILD ||
            effect.getKey() == DisruptionEffect.DISRUPTION_TURN_DEVA ||
            effect.getKey() == DisruptionEffect.DISRUPTION_TURN_DEVB
        );
    }
    
    // Parse effect string to enum
    public static DisruptionEffect parseEffectString(String effectStr) {
        switch (effectStr) {
            case "TurnWaste": return DisruptionEffect.DISRUPTION_TURN_WASTE;
            case "TurnWild": return DisruptionEffect.DISRUPTION_TURN_WILD;
            case "TurnDevA": return DisruptionEffect.DISRUPTION_TURN_DEVA;
            case "TurnDevB": return DisruptionEffect.DISRUPTION_TURN_DEVB;
            case "Co": return DisruptionEffect.DISRUPTION_CO;
            case "HR": return DisruptionEffect.DISRUPTION_HR;
            case "Cy": return DisruptionEffect.DISRUPTION_CY;
            case "Tech": return DisruptionEffect.DISRUPTION_TECH;
            case "Env": return DisruptionEffect.DISRUPTION_ENV;
            case "Resources": return DisruptionEffect.DISRUPTION_RESOURCES;
            case "Token": return DisruptionEffect.DISRUPTION_TOKEN;
            case "Trade": return DisruptionEffect.DISRUPTION_TRADE;
            case "CapEnv": return DisruptionEffect.DISRUPTION_CAP_ENV;
            case "IgnoreCohesionEffect": return DisruptionEffect.DISRUPTION_IGNORE_COHESION_EFFECT;
            case "SwapGoal": return DisruptionEffect.DISRUPTION_SWAP_GOAL;
            case "DrawGoal": return DisruptionEffect.DISRUPTION_DRAW_GOAL;
            case "MovPpl": return DisruptionEffect.DISRUPTION_MOV_PPL;
            default: return DisruptionEffect.DISRUPTION_CO; // Default fallback
        }
    }
    
    // Parse type string to enum
    public static DisruptionType parseTypeString(String typeStr) {
        switch (typeStr) {
            case "disrupt": return DisruptionType.DISRUPT;
            case "boost": return DisruptionType.BOOST;
            default: return DisruptionType.DISRUPT; // Default fallback
        }
    }
    
    // Create card from JSON
    public static DisruptionCard fromJson(JsonNode cardData) {
        DisruptionCard card = new DisruptionCard();
        
        // Parse basic information
        card.name = cardData.get("name").asText("");
        card.description = cardData.get("description").asText("");
        card.type = parseTypeString(cardData.get("type").asText("disrupt"));
        card.canCancel = cardData.get("cancel").asBoolean(true);
        card.hasCondition = !cardData.get("cond").asText("").isEmpty();
        
        // Parse stackTarget
        if (cardData.has("stackTarget") && cardData.get("stackTarget").isArray()) {
            for (JsonNode targetId : cardData.get("stackTarget")) {
                if (targetId.isNumber()) {
                    card.stackTarget.add(targetId.asInt());
                }
            }
        }
        
        // Parse effects
        if (cardData.has("effect") && cardData.get("effect").isArray()) {
            for (JsonNode effect : cardData.get("effect")) {
                if (effect.isTextual()) {
                    String effectStr = effect.asText();
                    // Parse effect with value (e.g., "Co:-2", "HR:+1")
                    if (effectStr.contains(":")) {
                        String effectName = effectStr.substring(0, effectStr.indexOf(":"));
                        String effectValue = effectStr.substring(effectStr.indexOf(":") + 1);
                        int value = Integer.parseInt(effectValue);
                        card.effects.add(new AbstractMap.SimpleEntry<>(
                            parseEffectString(effectName), value));
                    } else {
                        card.effects.add(new AbstractMap.SimpleEntry<>(
                            parseEffectString(effectStr), 0));
                    }
                }
            }
        }
        
        // Parse cost
        if (cardData.has("cost") && cardData.get("cost").isArray()) {
            for (JsonNode cost : cardData.get("cost")) {
                if (cost.isTextual()) {
                    String costStr = cost.asText();
                    if (costStr.contains(":")) {
                        String costName = costStr.substring(0, costStr.indexOf(":"));
                        String costValue = costStr.substring(costStr.indexOf(":") + 1);
                        int value = Integer.parseInt(costValue);
                        card.cancelCost.add(new AbstractMap.SimpleEntry<>(
                            parseEffectString(costName), value));
                    } else {
                        card.cancelCost.add(new AbstractMap.SimpleEntry<>(
                            parseEffectString(costStr), 0));
                    }
                }
            }
        }
        
        return card;
    }
    
    @Override
    public String toString() {
        return name + ": " + description;
    }
}

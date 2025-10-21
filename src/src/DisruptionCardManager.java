import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DisruptionCardManager {
    private List<DisruptionCard> deck;      // Card deck
    private List<DisruptionCard> discard;   // Discard pile
    
    public DisruptionCardManager() {
        this.deck = new ArrayList<>();
        this.discard = new ArrayList<>();
    }
    
    // Load all cards from JSON file
    public boolean loadCardsFromFile(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.err.println("Cannot find file: " + filename);
                return false;
            }
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonData = mapper.readTree(file);
            
            if (!jsonData.isArray()) {
                System.err.println("JSON file format error: should be array");
                return false;
            }
            
            // Clear existing deck
            deck.clear();
            discard.clear();
            
            // Load all cards
            int loadedCount = 0;
            for (JsonNode cardData : jsonData) {
                try {
                    DisruptionCard card = DisruptionCard.fromJson(cardData);
                    deck.add(card);
                    loadedCount++;
                } catch (Exception e) {
                    System.err.println("Error loading card: " + e.getMessage());
                }
            }
            System.out.println("Loaded " + loadedCount + " cards from " + jsonData.size() + " total cards");
            
            // Shuffle deck randomly
            Collections.shuffle(deck);
            
            System.out.println("Successfully loaded " + deck.size() + " disruption cards");
            return true;
            
        } catch (IOException e) {
            System.err.println("Failed to load disruption cards: " + e.getMessage());
            return false;
        }
    }
    
    // Draw a card (remove from deck, put into discard pile)
    public DisruptionCard drawCard() {
        if (deck.isEmpty()) {
            System.err.println("Deck is empty, cannot draw card");
            return new DisruptionCard(); // Return empty card
        }
        
        // Draw first card
        DisruptionCard drawnCard = deck.get(0);
        deck.remove(0);
        
        // Put into discard pile
        discard.add(drawnCard);
        
        return drawnCard;
    }
    
    // Check if deck is empty
    public boolean isDeckEmpty() {
        return deck.isEmpty();
    }
    
    // Get remaining deck size
    public int getDeckSize() {
        return deck.size();
    }
    
    // Get discard pile size
    public int getDiscardSize() {
        return discard.size();
    }
    
    // Reshuffle (put discard pile back to deck)
    public void reshuffle() {
        // Put all cards from discard pile back to deck
        deck.addAll(discard);
        
        // Clear discard pile
        discard.clear();
        
        // Reshuffle deck
        Collections.shuffle(deck);
        
        System.out.println("Reshuffle completed, deck has " + deck.size() + " cards");
    }
    
    // Get all cards in deck (for debugging)
    public List<DisruptionCard> getDeck() {
        return new ArrayList<>(deck);
    }
    
    // Get all cards in discard pile (for debugging)
    public List<DisruptionCard> getDiscard() {
        return new ArrayList<>(discard);
    }
}

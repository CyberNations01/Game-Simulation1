import javax.swing.*;
import java.awt.*;

public class GameInfoPanel extends JPanel {
    private JLabel currentRoundLabel;
    private JLabel maxRoundsLabel;
    private JLabel bagTotalLabel;
    private JLabel wildsCountLabel;
    private JLabel wastesCountLabel;
    private JLabel devACountLabel;
    private JLabel devBCountLabel;
    private JLabel wildsPercentLabel;
    private JLabel wastesPercentLabel;
    private JLabel devAPercentLabel;
    private JLabel devBPercentLabel;
    
    public GameInfoPanel() {
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        setBackground(new Color(240, 240, 240));
        setBorder(BorderFactory.createTitledBorder("Game Information"));
        
        // Initialize labels
        currentRoundLabel = new JLabel("Current Round: --");
        maxRoundsLabel = new JLabel("Max Rounds: --");
        bagTotalLabel = new JLabel("Bag Total: --");
        wildsCountLabel = new JLabel("Wilds: --");
        wastesCountLabel = new JLabel("Wastes: --");
        devACountLabel = new JLabel("DevA: --");
        devBCountLabel = new JLabel("DevB: --");
        wildsPercentLabel = new JLabel("Wilds %: --");
        wastesPercentLabel = new JLabel("Wastes %: --");
        devAPercentLabel = new JLabel("DevA %: --");
        devBPercentLabel = new JLabel("DevB %: --");
        
        // Set font
        Font labelFont = new Font("Arial", Font.PLAIN, 12);
        currentRoundLabel.setFont(labelFont);
        maxRoundsLabel.setFont(labelFont);
        bagTotalLabel.setFont(labelFont);
        wildsCountLabel.setFont(labelFont);
        wastesCountLabel.setFont(labelFont);
        devACountLabel.setFont(labelFont);
        devBCountLabel.setFont(labelFont);
        wildsPercentLabel.setFont(labelFont);
        wastesPercentLabel.setFont(labelFont);
        devAPercentLabel.setFont(labelFont);
        devBPercentLabel.setFont(labelFont);
    }
    
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Row 1: Current Round and Max Rounds
        gbc.gridx = 0; gbc.gridy = 0;
        add(currentRoundLabel, gbc);
        gbc.gridx = 1;
        add(maxRoundsLabel, gbc);
        
        // Row 2: Bag Total
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(bagTotalLabel, gbc);
        gbc.gridwidth = 1;
        
        // Row 3: Token counts
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Token Counts:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        add(wildsCountLabel, gbc);
        gbc.gridx = 1;
        add(wastesCountLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        add(devACountLabel, gbc);
        gbc.gridx = 1;
        add(devBCountLabel, gbc);
        
        // Row 4: Token percentages
        gbc.gridx = 0; gbc.gridy = 5;
        add(new JLabel("Token Percentages:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        add(wildsPercentLabel, gbc);
        gbc.gridx = 1;
        add(wastesPercentLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7;
        add(devAPercentLabel, gbc);
        gbc.gridx = 1;
        add(devBPercentLabel, gbc);
    }
    
    public void updateGameInfo(GameData gameData) {
        if (gameData == null) return;
        
        GameState gameState = gameData.getGame_state();
        Tokens tokens = gameData.getTokens();
        
        // Update basic game info
        currentRoundLabel.setText("Current Round: " + gameState.getCurrent_round());
        maxRoundsLabel.setText("Max Rounds: " + gameState.getMax_rounds());
        bagTotalLabel.setText("Bag Total: " + gameState.getBag_total());
        
        // Update token counts
        wildsCountLabel.setText("Wilds: " + tokens.getWilds());
        wastesCountLabel.setText("Wastes: " + tokens.getWastes());
        devACountLabel.setText("DevA: " + tokens.getDevA());
        devBCountLabel.setText("DevB: " + tokens.getDevB());
        
        // Calculate and update percentages
        int totalTokens = tokens.getWilds() + tokens.getWastes() + tokens.getDevA() + tokens.getDevB();
        if (totalTokens > 0) {
            double wildsPercent = (double) tokens.getWilds() / totalTokens * 100;
            double wastesPercent = (double) tokens.getWastes() / totalTokens * 100;
            double devAPercent = (double) tokens.getDevA() / totalTokens * 100;
            double devBPercent = (double) tokens.getDevB() / totalTokens * 100;
            
            wildsPercentLabel.setText(String.format("Wilds %%: %.1f%%", wildsPercent));
            wastesPercentLabel.setText(String.format("Wastes %%: %.1f%%", wastesPercent));
            devAPercentLabel.setText(String.format("DevA %%: %.1f%%", devAPercent));
            devBPercentLabel.setText(String.format("DevB %%: %.1f%%", devBPercent));
        } else {
            wildsPercentLabel.setText("Wilds %: 0.0%");
            wastesPercentLabel.setText("Wastes %: 0.0%");
            devAPercentLabel.setText("DevA %: 0.0%");
            devBPercentLabel.setText("DevB %: 0.0%");
        }
    }
}

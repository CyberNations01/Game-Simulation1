import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class GameVisualizer extends JFrame {
    private GameData gameData;
    private HexagonPanel hexagonPanel;
    private GameInfoPanel gameInfoPanel;
    private JLabel statusLabel;
    
    public GameVisualizer() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setTitle("Game Hexagon Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Initialize UI components first
        statusLabel = new JLabel("Game status information will be displayed here");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Create sample data
        createSampleData();
        
        hexagonPanel = new HexagonPanel(gameData.getBoard().getHexes());
        gameInfoPanel = new GameInfoPanel();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top status bar
        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.LIGHT_GRAY);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(statusLabel);
        
        // Left info panel and center hexagon panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(gameInfoPanel, BorderLayout.NORTH);
        leftPanel.add(new JPanel(), BorderLayout.CENTER); // Empty space
        
        // Middle hexagon panel
        JScrollPane scrollPane = new JScrollPane(hexagonPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Bottom control panel
        JPanel bottomPanel = createControlPanel();
        
        add(topPanel, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBackground(Color.LIGHT_GRAY);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton refreshButton = new JButton("Refresh");
        JButton loadButton = new JButton("Load JSON File");
        
        refreshButton.addActionListener(e -> refreshDisplay());
        loadButton.addActionListener(e -> loadJsonFile());
        
        controlPanel.add(refreshButton);
        controlPanel.add(loadButton);
        
        return controlPanel;
    }
    
    private void setupEventHandlers() {
        // Can add more event handlers
    }
    
    private void createSampleData() {
        // Create sample data
        gameData = new GameData();
        gameData.setVersion(1);
        
        // Create game board
        GameBoard board = new GameBoard();
        java.util.List<HexData> hexes = new java.util.ArrayList<>();
        
        // Add 11 hexagon data with layered structure
        String[] baseTypes = {"Wilds", "Wastes", "Wilds", "Wastes", "Wilds", "Wastes", "Wilds", "Wastes", "Wilds", "Wastes", "Wilds"};
        String[] topTypes = {null, "DevA", "DevB", null, "DevA", null, "DevB", "DevA", null, "DevB", "DevA"};
        
        for (int i = 0; i < 11; i++) {
            hexes.add(new HexData(i + 1, baseTypes[i], topTypes[i], true));
        }
        
        board.setHexes(hexes);
        gameData.setBoard(board);
        
        // Create game state
        GameState gameState = new GameState();
        gameState.setCurrent_round(1);
        gameState.setMax_rounds(50);
        gameState.setBag_total(100);
        gameData.setGame_state(gameState);
        
        // Create tokens
        Tokens tokens = new Tokens();
        tokens.setWilds(25);
        tokens.setWastes(25);
        tokens.setDevA(25);
        tokens.setDevB(25);
        gameData.setTokens(tokens);
        
        // Update info panel if it exists
        if (gameInfoPanel != null) {
            gameInfoPanel.updateGameInfo(gameData);
        }
        updateStatusLabel();
    }
    
    private void refreshDisplay() {
        hexagonPanel.repaint();
        gameInfoPanel.updateGameInfo(gameData);
        updateStatusLabel();
    }
    
    private void loadJsonFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                JsonReader reader = new JsonReader();
                gameData = reader.readGameData(fileChooser.getSelectedFile().getAbsolutePath());
                
                // Update panels
                hexagonPanel = new HexagonPanel(gameData.getBoard().getHexes());
                gameInfoPanel = new GameInfoPanel();
                
                // Rebuild layout
                getContentPane().removeAll();
                setupLayout();
                
                gameInfoPanel.updateGameInfo(gameData);
                updateStatusLabel();
                revalidate();
                repaint();
                
                JOptionPane.showMessageDialog(this, "JSON file loaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to load JSON file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateStatusLabel() {
        if (gameData != null) {
            String status = String.format("Version: %d | Current Round: %d/%d | Bag Total: %d | Hexagons: %d",
                gameData.getVersion(),
                gameData.getGame_state().getCurrent_round(),
                gameData.getGame_state().getMax_rounds(),
                gameData.getGame_state().getBag_total(),
                gameData.getBoard().getHexes().size()
            );
            statusLabel.setText(status);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameVisualizer().setVisible(true);
        });
    }
}

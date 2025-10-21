import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

// State enum for simulation
enum State {
    WILDS, WASTES, DEVA, DEVB
}

// FeedbackToken enum for simulation
enum FeedbackToken {
    WILDS, WASTES, DEVA, DEVB;
}

public class AnimatedGameVisualizer extends JFrame {
    private MultiRoundGameData multiRoundData;
    private GameData currentGameData;
    private HexagonPanel hexagonPanel;
    private GameInfoPanel gameInfoPanel;
    private ChartPanel chartPanel;
    private LineChartPanel lineChartPanel;
    private AnimationControlPanel animationControlPanel;
    private JLabel statusLabel;
    
    private Timer animationTimer;
    private int currentRoundIndex = 0;
    
    
    public AnimatedGameVisualizer() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
    }
    
    private void initializeComponents() {
        setTitle("CyberNations Game Simulation Console v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Initialize UI components first
        statusLabel = new JLabel("Game status information will be displayed here");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Create animation control panel
        animationControlPanel = new AnimationControlPanel() {
            @Override
            protected void onPlay() {
                startAnimation();
            }
            
            @Override
            protected void onPause() {
                pauseAnimation();
            }
            
            @Override
            protected void onStop() {
                stopAnimation();
            }
            
            @Override
            protected void onReplay() {
                replayAnimation();
            }
            
            @Override
            protected void onPrevious() {
                goToPreviousRound();
            }
            
            @Override
            protected void onNext() {
                goToNextRound();
            }
            
            @Override
            protected void onProgressChanged(int round) {
                goToRound(round);
            }
            
            @Override
            protected void onSpeedChanged(int speed) {
                // Speed change is applied immediately, even during playback
                if (isPlaying()) {
                    // Cancel current timer and reschedule with new speed
                    if (animationTimer != null) {
                        animationTimer.cancel();
                    }
                    scheduleNextFrame();
                }
            }
        };
        
        // Initialize panels first
        gameInfoPanel = new GameInfoPanel();
        chartPanel = new ChartPanel();
        lineChartPanel = new LineChartPanel();
        
        // Load multi-round JSON data
        try {
            JsonReader reader = new JsonReader();
            multiRoundData = reader.readUniversalMultiRoundData("game_data_multi_rounds.json");
            
            currentRoundIndex = 0;
            currentGameData = convertRoundToGameData(multiRoundData.getRounds().get(0));
            
            updateDisplay();
            animationControlPanel.setTotalRounds(multiRoundData.getRounds().size());
            animationControlPanel.setCurrentRound(1);
        } catch (IOException e) {
            // Fallback to sample data if JSON loading fails
            createSampleData();
        }
        
        hexagonPanel = new HexagonPanel(currentGameData.getBoard().getHexes());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top status bar
        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.LIGHT_GRAY);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(statusLabel);
        
        // Left panel with charts (vertical layout)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(chartPanel, BorderLayout.NORTH);      // Pie chart on top
        leftPanel.add(lineChartPanel, BorderLayout.CENTER); // Line chart below
        
        // Right panel with game info and action buttons (vertical layout)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(gameInfoPanel, BorderLayout.NORTH);  // Game info on top
        JPanel actionButtonsPanel = createControlPanel();
        rightPanel.add(actionButtonsPanel, BorderLayout.CENTER); // Action buttons below
        
        // Main content area (horizontal layout)
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.add(leftPanel, BorderLayout.WEST);     // Charts on left
        mainContentPanel.add(hexagonPanel, BorderLayout.CENTER); // Hexagon board in center
        mainContentPanel.add(rightPanel, BorderLayout.EAST);     // Game info + buttons on right
        
        add(topPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
        add(animationControlPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        controlPanel.setBackground(Color.LIGHT_GRAY);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton loadJsonButton = new JButton("Load JSON");
        JButton createRandomButton = new JButton("Create Random Data");
        JButton customSimButton = new JButton("Custom Simulation");
        JButton saveButton = new JButton("Save Data");
        
        loadJsonButton.addActionListener(e -> loadJsonFile());
        createRandomButton.addActionListener(e -> createRandomData());
        customSimButton.addActionListener(e -> createCustomSimulation());
        saveButton.addActionListener(e -> saveDataToFile());
        
        controlPanel.add(loadJsonButton);
        controlPanel.add(createRandomButton);
        controlPanel.add(customSimButton);
        controlPanel.add(saveButton);
        
        return controlPanel;
    }
    
    private void setupEventHandlers() {
        // Can add more event handlers
    }
    
    
    private void createRandomData() {
        try {
            // Generate completely random data using Simulation class
            int rounds = 10 + (int)(Math.random() * 11); // 10-20 rounds
            long seed = System.currentTimeMillis();
            
            // Generate random initial states
            java.util.Random rng = new java.util.Random(seed);
            java.util.Map<Integer, State> initialStates = new java.util.HashMap<>();
            String[] stateNames = {"WILDS", "WASTES", "DEVA", "DEVB"};
            
            for (int i = 1; i <= 11; i++) {
                String stateName = stateNames[rng.nextInt(stateNames.length)];
                State state = State.valueOf(stateName);
                initialStates.put(i, state);
            }
            
            multiRoundData = generateSimulationData(rounds, seed, initialStates);
            
            if (multiRoundData != null && !multiRoundData.getRounds().isEmpty()) {
                currentRoundIndex = 0;
                currentGameData = convertRoundToGameData(multiRoundData.getRounds().get(0));
                
                // Reset line chart state to ensure new data displays correctly
                if (animationControlPanel.isDynamicChartEnabled()) {
                    lineChartPanel.setDynamicMode(true);
                    lineChartPanel.resetDynamicState();
                    // Only update multiRoundData reference, do not display static chart
                    lineChartPanel.setMultiRoundData(multiRoundData);
                    lineChartPanel.forceRecalculateAxisRange();
                } else {
                    lineChartPanel.setDynamicMode(false);
                    // Force reset axis range to ensure new data displays correctly
                    lineChartPanel.restoreAutoScaling();
                }
                
                updateDisplay();
                animationControlPanel.setTotalRounds(multiRoundData.getRounds().size());
                animationControlPanel.setCurrentRound(1);
                
                statusLabel.setText("Generated random data: " + rounds + " rounds (Stage " + multiRoundData.getVersion() + ")");
                
                // Don't ask for save immediately - wait for animation to complete
                // Store the data for later save option
            } else {
                statusLabel.setText("Failed to generate random data");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to generate random data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error generating random data");
        }
    }
    
    private void createCustomSimulation() {
        // Show dialog for custom simulation parameters
        CustomSimulationDialog dialog = new CustomSimulationDialog(this);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            try {
                // Get parameters from dialog
                int rounds = dialog.getRounds();
                String[] initialStateNames = dialog.getInitialStates();
                int[] tokenLimits = dialog.getTokenLimits();
                int stage = dialog.getSelectedStage();
                long seed = System.currentTimeMillis(); // Use current time as random seed
                
                // Convert string states to State enum
                java.util.Map<Integer, State> initialStates = new java.util.HashMap<>();
                for (int i = 0; i < 11; i++) {
                    State state = State.valueOf(initialStateNames[i]);
                    initialStates.put(i + 1, state);
                }
                
                // Generate custom simulation data using simplified logic with stage version
                multiRoundData = generateSimplifiedSimulationData(rounds, seed, initialStates, tokenLimits, stage);
                
                if (multiRoundData != null && !multiRoundData.getRounds().isEmpty()) {
                    currentRoundIndex = 0;
                    currentGameData = convertRoundToGameData(multiRoundData.getRounds().get(0));
                    
                    updateDisplay();
                    animationControlPanel.setTotalRounds(multiRoundData.getRounds().size());
                    animationControlPanel.setCurrentRound(1);
                    
                    statusLabel.setText("Generated custom simulation: " + rounds + " rounds (Stage " + stage + ")");
                    
                    // Don't ask for save immediately - wait for animation to complete
                    // Store the data for later save option
                } else {
                    statusLabel.setText("Failed to generate custom simulation");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to generate custom simulation: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Error generating custom simulation");
            }
        }
    }
    
    private void saveDataToFile() {
        // Check if there is data to save
        if (multiRoundData == null || multiRoundData.getRounds().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No data to save!\nPlease load or generate some simulation data first.", 
                "No Data", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Let user choose save type
        String[] options = {"Save JSON Data", "Save Line Chart"};
        int choice = JOptionPane.showOptionDialog(this,
            "What would you like to save?",
            "Save Options",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (choice == 0) {
            // Save JSON data
            saveJsonData();
        } else if (choice == 1) {
            // Save line chart
            saveLineChart();
        }
    }
    
    private void saveJsonData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));
        
        // Set default directory to Downloads folder
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");
        if (downloadsDir.exists() && downloadsDir.isDirectory()) {
            fileChooser.setCurrentDirectory(downloadsDir);
        } else {
            // Fallback to current directory if Downloads doesn't exist
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        }
        
        // Set default filename with timestamp
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        fileChooser.setSelectedFile(new java.io.File("simulation_" + timestamp + ".json"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Convert to JSON and save
                JsonReader reader = new JsonReader();
                String jsonString = reader.convertMultiRoundToJson(multiRoundData);
                
                java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile());
                writer.write(jsonString);
                writer.close();
                
                statusLabel.setText("✅ JSON data saved to: " + fileChooser.getSelectedFile().getName());
                
                // Show success message
                JOptionPane.showMessageDialog(this, 
                    "JSON data saved successfully!\nFile: " + fileChooser.getSelectedFile().getName() + 
                    "\nRounds: " + multiRoundData.getRounds().size(), 
                    "Save Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to save JSON file: " + e.getMessage(), 
                    "Save Error", 
                    JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("❌ Error saving JSON file");
            }
        }
    }
    
    private void saveLineChart() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));
        
        // Set default directory to Downloads folder
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");
        if (downloadsDir.exists() && downloadsDir.isDirectory()) {
            fileChooser.setCurrentDirectory(downloadsDir);
        } else {
            // Fallback to current directory if Downloads doesn't exist
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        }
        
        // Set default filename with timestamp
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        fileChooser.setSelectedFile(new java.io.File("token_trends_" + timestamp + ".png"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Save line chart as PNG image
                lineChartPanel.saveChartAsPNG(fileChooser.getSelectedFile());
                
                statusLabel.setText("✅ Line chart saved to: " + fileChooser.getSelectedFile().getName());
                
                // Show success message
                JOptionPane.showMessageDialog(this, 
                    "Line chart saved successfully!\nFile: " + fileChooser.getSelectedFile().getName(), 
                    "Save Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to save chart: " + e.getMessage(), 
                    "Save Error", 
                    JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("❌ Error saving chart");
            }
        }
    }
    
    private MultiRoundGameData generateSimulationData(int rounds, long seed, Map<Integer, State> initialStates) {
        // Use default token limits (20 each)
        int[] defaultLimits = {20, 20, 20, 20};
        return generateSimulationData(rounds, seed, initialStates, defaultLimits);
    }
    
    private MultiRoundGameData generateSimulationData(int rounds, long seed, Map<Integer, State> initialStates, int[] tokenLimits) {
        // Use simplified simulation logic with Stage 2 version
        MultiRoundGameData data = generateSimplifiedSimulationData(rounds, seed, initialStates, tokenLimits, 2);
        return data;
    }
    
    private MultiRoundGameData generateSimplifiedSimulationData(int rounds, long seed, Map<Integer, State> initialStates) {
        // Use default token limits
        int[] defaultLimits = {20, 20, 20, 20};
        return generateSimplifiedSimulationData(rounds, seed, initialStates, defaultLimits);
    }
    
    private MultiRoundGameData generateSimplifiedSimulationData(int rounds, long seed, Map<Integer, State> initialStates, int[] tokenLimits) {
        return generateSimplifiedSimulationData(rounds, seed, initialStates, tokenLimits, 1);
    }
    
    private MultiRoundGameData generateSimplifiedSimulationData(int rounds, long seed, Map<Integer, State> initialStates, int[] tokenLimits, int version) {
        // Simplified fallback logic that mimics the real game rules
        MultiRoundGameData multiRoundData = new MultiRoundGameData();
        multiRoundData.setVersion(version);
        multiRoundData.setTotal_rounds(rounds);
        
        java.util.List<RoundData> roundList = new java.util.ArrayList<>();
        java.util.Random rng = new java.util.Random(seed);
        
        // Initialize current states
        State[] currentStates = new State[11];
        for (int i = 0; i < 11; i++) {
            currentStates[i] = initialStates.get(i + 1);
        }
        
        // Initialize token pool with all available tokens (up to limits)
        java.util.Map<FeedbackToken, Integer> tokenPool = new java.util.HashMap<>();
        tokenPool.put(FeedbackToken.WILDS, tokenLimits[0]);
        tokenPool.put(FeedbackToken.WASTES, tokenLimits[1]);
        tokenPool.put(FeedbackToken.DEVA, tokenLimits[2]);
        tokenPool.put(FeedbackToken.DEVB, tokenLimits[3]);
        
        // Initialize empty bag for each round
        java.util.Map<FeedbackToken, Integer> bag = new java.util.HashMap<>();
        bag.put(FeedbackToken.WILDS, 0);
        bag.put(FeedbackToken.WASTES, 0);
        bag.put(FeedbackToken.DEVA, 0);
        bag.put(FeedbackToken.DEVB, 0);
        
        // Generate each round using real game logic
        // Round 0: Record initial state
        RoundData round0Data = createRoundData(0, currentStates, tokenPool, null, rounds, tokenLimits);
        roundList.add(round0Data);
        
        // Rounds 1 to N: Simulate round, then record the result
        for (int round = 1; round <= rounds; round++) {
            // First simulate this round
            simulateOneRound(currentStates, tokenPool, bag, rng, tokenLimits);
            
            // Then record the result of this round (after simulation, bag contains recycled tokens)
            RoundData roundData = createRoundData(round, currentStates, tokenPool, bag, rounds, tokenLimits);
            roundList.add(roundData);
        }
        
        multiRoundData.setRounds(roundList);
        return multiRoundData;
    }
    
    private void simulateOneRound(State[] currentStates, java.util.Map<FeedbackToken, Integer> tokenPool, java.util.Map<FeedbackToken, Integer> bag, java.util.Random rng, int[] tokenLimits) {
        // 1. Each stack generates tokens from pool into the bag (subject to pool limits)
        // Bag is NOT cleared - tokens accumulate in the bag
        for (State state : currentStates) {
            FeedbackToken token = toTokenFromState(state);
            int poolCount = tokenPool.get(token);
            int bagCount = bag.get(token);
            
            if (poolCount > 0) {
                // Move one token from pool to bag
                tokenPool.put(token, poolCount - 1);
                bag.put(token, bagCount + 1);
            }
            // If pool is empty for this token type, no token is added to bag
        }
        
        // 3. Draw 11 tokens in order (1,2,3,4,5,6,7,8,9,10,11)
        int[] order = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        java.util.List<FeedbackToken> drawn = new java.util.ArrayList<>();
        
        for (int pos : order) {
            FeedbackToken token = drawTokenFromBag(bag, rng);
            drawn.add(token);
            
            // 4. Resolve token on the corresponding stack
            resolveTokenOnStack(token, currentStates, pos - 1);
        }
        
        // 5. Recycle tokens: positions 2-7 go back to bag, 1,8-11 go back to pool
        java.util.Set<Integer> persistPositions = java.util.Set.of(1, 8, 9, 10, 11);
        for (int i = 0; i < drawn.size(); i++) {
            int pos = order[i];
            FeedbackToken token = drawn.get(i);
            if (!persistPositions.contains(pos)) {
                // Positions 2-7: return to bag
                bag.put(token, bag.get(token) + 1);
            } else {
                // Positions 1,8-11: return to token pool (need to check pool capacity limit)
                int currentPoolCount = tokenPool.get(token);
                int limit = getTokenLimit(token, tokenLimits);
                if (currentPoolCount < limit) {
                    tokenPool.put(token, currentPoolCount + 1);
                }
                // If pool is full, token is discarded
            }
        }
    }
    
    private FeedbackToken toTokenFromState(State state) {
        return switch (state) {
            case WILDS -> FeedbackToken.WILDS;
            case WASTES -> FeedbackToken.WASTES;
            case DEVA -> FeedbackToken.DEVA;
            case DEVB -> FeedbackToken.DEVB;
        };
    }
    
    private int getTokenLimit(FeedbackToken token, int[] tokenLimits) {
        return switch (token) {
            case WILDS -> tokenLimits[0];
            case WASTES -> tokenLimits[1];
            case DEVA -> tokenLimits[2];
            case DEVB -> tokenLimits[3];
        };
    }
    
    private FeedbackToken drawTokenFromBag(java.util.Map<FeedbackToken, Integer> bag, java.util.Random rng) {
        // Find available tokens
        java.util.List<FeedbackToken> available = new java.util.ArrayList<>();
        for (java.util.Map.Entry<FeedbackToken, Integer> entry : bag.entrySet()) {
            if (entry.getValue() > 0) {
                available.add(entry.getKey());
            }
        }
        
        if (available.isEmpty()) {
            // If bag is empty, return random token
            FeedbackToken[] all = FeedbackToken.values();
            return all[rng.nextInt(all.length)];
        }
        
        // Draw random token from available ones
        FeedbackToken token = available.get(rng.nextInt(available.size()));
        bag.put(token, bag.get(token) - 1);
        return token;
    }
    
    private void resolveTokenOnStack(FeedbackToken token, State[] currentStates, int stackIndex) {
        // Apply token resolution rules according to FeedbackToken.resolveOn logic
        switch (token) {
            case WILDS -> currentStates[stackIndex] = State.WILDS;   // set to wilds
            case DEVA -> currentStates[stackIndex] = State.WASTES;  // DevA token: set the state to wastes
            case WASTES, DEVB -> {
                // No effect at this stage
            }
        }
    }
    
    private RoundData createRoundData(int round, State[] currentStates, java.util.Map<FeedbackToken, Integer> tokenPool, int totalRounds, int[] tokenLimits) {
        return createRoundData(round, currentStates, tokenPool, null, totalRounds, tokenLimits);
    }
    
    private RoundData createRoundData(int round, State[] currentStates, java.util.Map<FeedbackToken, Integer> tokenPool, java.util.Map<FeedbackToken, Integer> bag, int totalRounds, int[] tokenLimits) {
        RoundData roundData = new RoundData();
        roundData.setRound_number(round);
        
        // Create game board
        GameBoard board = new GameBoard();
        java.util.List<HexData> hexes = new java.util.ArrayList<>();
        
        // Create hexes based on current states
        for (int i = 0; i < 11; i++) {
            String stateName = currentStates[i].name();
            HexData hex = new HexData(i + 1, stateName, getColorFromState(stateName));
            hexes.add(hex);
        }
        
        board.setHexes(hexes);
        roundData.setBoard(board);
        
        // Create game state
        GameState gameState = new GameState();
        gameState.setCurrent_round(round);
        gameState.setMax_rounds(totalRounds);
        // Bag total is the sum of tokens currently in the bag (if bag is provided)
        if (bag != null) {
            gameState.setBag_total(bag.values().stream().mapToInt(Integer::intValue).sum());
        } else {
            gameState.setBag_total(0); // Round 0 has no bag
        }
        roundData.setGame_state(gameState);
        
        // Create tokens (current bag state - what's displayed in Game Info)
        Tokens tokens = new Tokens();
        if (bag != null) {
            tokens.setWilds(bag.get(FeedbackToken.WILDS));
            tokens.setWastes(bag.get(FeedbackToken.WASTES));
            tokens.setDevA(bag.get(FeedbackToken.DEVA));
            tokens.setDevB(bag.get(FeedbackToken.DEVB));
        } else {
            // Round 0 has no bag
            tokens.setWilds(0);
            tokens.setWastes(0);
            tokens.setDevA(0);
            tokens.setDevB(0);
        }
        roundData.setTokens(tokens);
        
        // Debug: Print states
        System.out.println("Round " + round + " - Token Pool: WILDS=" + tokenPool.get(FeedbackToken.WILDS) + 
                          ", WASTES=" + tokenPool.get(FeedbackToken.WASTES) + 
                          ", DEVA=" + tokenPool.get(FeedbackToken.DEVA) + 
                          ", DEVB=" + tokenPool.get(FeedbackToken.DEVB));
        if (bag != null) {
            System.out.println("Round " + round + " - Bag (Displayed): WILDS=" + bag.get(FeedbackToken.WILDS) + 
                              ", WASTES=" + bag.get(FeedbackToken.WASTES) + 
                              ", DEVA=" + bag.get(FeedbackToken.DEVA) + 
                              ", DEVB=" + bag.get(FeedbackToken.DEVB) + 
                              ", Total=" + bag.values().stream().mapToInt(Integer::intValue).sum());
        }
        
        return roundData;
    }
    
    
    private void createSampleData() {
        // Create sample multi-round data
        multiRoundData = new MultiRoundGameData();
        multiRoundData.setVersion(1);
        multiRoundData.setTotal_rounds(5);
        
        java.util.List<RoundData> rounds = new java.util.ArrayList<>();
        
        // Create 5 rounds of sample data
        for (int round = 1; round <= 5; round++) {
            RoundData roundData = new RoundData();
            roundData.setRound_number(round);
            
            // Create game board for this round
            GameBoard board = new GameBoard();
            java.util.List<HexData> hexes = new java.util.ArrayList<>();
            
            // Add 11 hexagon data with layered structure
            String[] baseTypes = {"Wilds", "Wastes", "Wilds", "Wastes", "Wilds", "Wastes", "Wilds", "Wastes", "Wilds", "Wastes", "Wilds"};
            String[] topTypes = {null, "DevA", "DevB", null, "DevA", null, "DevB", "DevA", null, "DevB", "DevA"};
            
            // Vary the top types for different rounds
            for (int i = 0; i < 11; i++) {
                String topType = topTypes[i];
                if (round > 1) {
                    // Change some top types for variety
                    if ((i + round) % 3 == 0) {
                        topType = topType == null ? "DevA" : null;
                    }
                }
                hexes.add(new HexData(i + 1, baseTypes[i], topType, true));
            }
            
            board.setHexes(hexes);
            roundData.setBoard(board);
            
            // Create game state
            GameState gameState = new GameState();
            gameState.setCurrent_round(round);
            gameState.setMax_rounds(5);
            gameState.setBag_total(100 - (round - 1) * 5);
            roundData.setGame_state(gameState);
            
            // Create tokens
            Tokens tokens = new Tokens();
            tokens.setWilds(25 - (round - 1) * 2);
            tokens.setWastes(25 - (round - 1) * 1);
            tokens.setDevA(25 - (round - 1) * 1);
            tokens.setDevB(25 - (round - 1) * 1);
            roundData.setTokens(tokens);
            
            rounds.add(roundData);
        }
        
        multiRoundData.setRounds(rounds);
        
        // Set current round to first round
        currentRoundIndex = 0;
        currentGameData = convertRoundToGameData(rounds.get(0));
        
        // Update UI
        updateDisplay();
        animationControlPanel.setTotalRounds(rounds.size());
        animationControlPanel.setCurrentRound(1);
    }
    
    private GameData convertRoundToGameData(RoundData roundData) {
        GameData gameData = new GameData();
        gameData.setVersion(multiRoundData.getVersion());
        gameData.setBoard(roundData.getBoard());
        gameData.setGame_state(roundData.getGame_state());
        gameData.setTokens(roundData.getTokens());
        return gameData;
    }
    
    private void refreshDisplay() {
        // Reset to round 1 (progress 1)
        if (multiRoundData != null && !multiRoundData.getRounds().isEmpty()) {
            currentRoundIndex = 0;
            updateDisplay();
            animationControlPanel.setCurrentRound(currentRoundIndex + 1);
            statusLabel.setText("Reset to round 1");
        }
    }
    
    private void loadSingleJsonFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                JsonReader reader = new JsonReader();
                GameData singleGameData = reader.readUniversalSingleRoundData(fileChooser.getSelectedFile().getAbsolutePath());
                
                // Convert single game data to multi-round format
                multiRoundData = new MultiRoundGameData();
                multiRoundData.setVersion(singleGameData.getVersion());
                multiRoundData.setTotal_rounds(1);
                
                RoundData roundData = new RoundData();
                roundData.setRound_number(1);
                roundData.setBoard(singleGameData.getBoard());
                roundData.setGame_state(singleGameData.getGame_state());
                roundData.setTokens(singleGameData.getTokens());
                
                java.util.List<RoundData> rounds = new java.util.ArrayList<>();
                rounds.add(roundData);
                multiRoundData.setRounds(rounds);
                
                currentRoundIndex = 0;
                currentGameData = singleGameData;
                
                updateDisplay();
                animationControlPanel.setTotalRounds(1);
                animationControlPanel.setCurrentRound(1);
                
                JOptionPane.showMessageDialog(this, "Single JSON file loaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to load JSON file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadJsonFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));
        
        // Set default directory to parent of visualization folder (project root)
        String currentDir = System.getProperty("user.dir");
        String parentDir = new java.io.File(currentDir).getParent();
        if (parentDir != null) {
            fileChooser.setCurrentDirectory(new java.io.File(parentDir));
        } else {
            fileChooser.setCurrentDirectory(new java.io.File(currentDir));
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Use UniversalDataConverter to automatically detect format and convert
                multiRoundData = UniversalDataConverter.convertFileToMultiRound(fileChooser.getSelectedFile().getAbsolutePath());
                
                if (multiRoundData != null && !multiRoundData.getRounds().isEmpty()) {
                    currentRoundIndex = 0;
                    currentGameData = convertRoundToGameData(multiRoundData.getRounds().get(0));
                    
                    // Reset line chart state to ensure new data displays correctly
                    if (animationControlPanel.isDynamicChartEnabled()) {
                        lineChartPanel.setDynamicMode(true);
                        lineChartPanel.resetDynamicState();
                        // Only update multiRoundData reference, do not display static chart
                        lineChartPanel.setMultiRoundData(multiRoundData);
                        lineChartPanel.forceRecalculateAxisRange();
                    } else {
                        lineChartPanel.setDynamicMode(false);
                        // Force reset axis range to ensure new data displays correctly
                        lineChartPanel.restoreAutoScaling();
                    }
                    
                    updateDisplay();
                    animationControlPanel.setTotalRounds(multiRoundData.getRounds().size());
                    animationControlPanel.setCurrentRound(1);
                    
                    statusLabel.setText("Loaded: " + fileChooser.getSelectedFile().getName() + " (" + multiRoundData.getTotal_rounds() + " rounds)");
                } else {
                    statusLabel.setText("Failed to load: " + fileChooser.getSelectedFile().getName());
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to load JSON file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Error loading: " + fileChooser.getSelectedFile().getName());
            }
        }
    }
    
    private void updateDisplay() {
        if (currentGameData != null) {
            hexagonPanel = new HexagonPanel(currentGameData.getBoard().getHexes());
            gameInfoPanel.updateGameInfo(currentGameData);
            chartPanel.updateFromGameData(currentGameData);
            
            // Check if dynamic chart is enabled
            if (animationControlPanel.isDynamicChartEnabled()) {
                // Dynamic mode: only update current round data
                // Only set on first enable of dynamic mode to avoid repeated clearing
                if (!lineChartPanel.isDynamicMode()) {
                    lineChartPanel.setDynamicMode(true);
                    // On first enable, add all data from round 1 to current round
                    for (int i = 0; i <= currentRoundIndex; i++) {
                        if (multiRoundData != null && i < multiRoundData.getRounds().size()) {
                            GameData roundData = convertRoundToGameData(multiRoundData.getRounds().get(i));
                            Map<String, Integer> roundStackCounts = new HashMap<>();
                            for (HexData hex : roundData.getBoard().getHexes()) {
                                String stackType = hex.getType();
                                roundStackCounts.put(stackType, roundStackCounts.getOrDefault(stackType, 0) + 1);
                            }
                            lineChartPanel.addDynamicRoundData(i + 1, roundStackCounts);
                        }
                    }
                } else {
                    // Check if rollback is needed
                    int currentRound = currentRoundIndex + 1;
                    int dynamicRound = lineChartPanel.getCurrentDynamicRound();
                    
                    if (currentRound < dynamicRound) {
                        // Need rollback: reset dynamic chart to current round
                        lineChartPanel.setDynamicRoundTo(currentRound);
                    } else if (currentRound == dynamicRound + 1) {
                        // Normal advance: only add current round data
                        Map<String, Integer> stackCounts = getCurrentRoundStackCounts();
                        lineChartPanel.addDynamicRoundData(currentRound, stackCounts);
                    }
                    // If currentRound == dynamicRound, no change, no update needed
                }
            } else {
                // Normal mode: display all data
                if (lineChartPanel.isDynamicMode()) {
                    lineChartPanel.setDynamicMode(false);
                }
                if (multiRoundData != null) {
                    lineChartPanel.updateFromMultiRoundData(multiRoundData);
                }
            }
            
            updateStatusLabel();
            
            // Only update hexagonPanel, do not rebuild entire layout
            if (hexagonPanel != null) {
                // Find hexagonPanel position in layout and update
                Container contentPane = getContentPane();
                Component[] components = contentPane.getComponents();
                for (Component comp : components) {
                    if (comp instanceof JPanel) {
                        JPanel panel = (JPanel) comp;
                        Component[] subComponents = panel.getComponents();
                        for (Component subComp : subComponents) {
                            if (subComp instanceof HexagonPanel) {
                                panel.remove(subComp);
                                panel.add(hexagonPanel, BorderLayout.CENTER);
                                panel.revalidate();
                                panel.repaint();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void updateStatusLabel() {
        if (currentGameData != null) {
            String status = String.format("Version: %d | Current Round: %d/%d | Bag Total: %d | Hexagons: %d",
                currentGameData.getVersion(),
                currentGameData.getGame_state().getCurrent_round(),
                currentGameData.getGame_state().getMax_rounds(),
                currentGameData.getGame_state().getBag_total(),
                currentGameData.getBoard().getHexes().size()
            );
            statusLabel.setText(status);
        }
    }
    
    // Animation control methods
    private void startAnimation() {
        if (animationTimer != null) {
            animationTimer.cancel();
        }
        
        scheduleNextFrame();
    }
    
    private void scheduleNextFrame() {
        if (animationControlPanel.isPlaying()) {
            animationTimer = new Timer();
            animationTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        goToNextRound();
                        if (currentRoundIndex >= multiRoundData.getRounds().size() - 1) {
                            if (animationControlPanel.isLoopEnabled()) {
                                goToRound(1);
                            } else {
                                stopAnimation();
                                // Animation complete, prompt user to save
                                statusLabel.setText("🎉 Animation completed! Use 'Save Data' button to save results.");
                            }
                        } else {
                            // Schedule next frame with current speed
                            scheduleNextFrame();
                        }
                    });
                }
            }, animationControlPanel.getAnimationSpeed());
        }
    }
    
    private void pauseAnimation() {
        if (animationTimer != null) {
            animationTimer.cancel();
            animationTimer = null;
        }
    }
    
    private void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.cancel();
            animationTimer = null;
        }
        animationControlPanel.setPlaying(false);
    }
    
    private void replayAnimation() {
        stopAnimation();
        currentRoundIndex = 0;
        
        // Reset dynamic chart state
        if (animationControlPanel.isDynamicChartEnabled()) {
            lineChartPanel.resetDynamicState();
        }
        
        if (multiRoundData != null && !multiRoundData.getRounds().isEmpty()) {
            currentGameData = convertRoundToGameData(multiRoundData.getRounds().get(0));
            updateDisplay();
            animationControlPanel.setCurrentRound(1);
            // Start playing directly
            startAnimation();
        }
    }
    
    private void goToPreviousRound() {
        if (currentRoundIndex > 0) {
            currentRoundIndex--;
            updateCurrentRound();
        }
    }
    
    private void goToNextRound() {
        if (currentRoundIndex < multiRoundData.getRounds().size() - 1) {
            currentRoundIndex++;
            updateCurrentRound();
        }
    }
    
    private void goToRound(int round) {
        if (round >= 1 && round <= multiRoundData.getRounds().size()) {
            currentRoundIndex = round - 1;
            updateCurrentRound();
        }
    }
    
    /**
     * Get current round stack count (hexagon position type statistics)
     */
    private Map<String, Integer> getCurrentRoundStackCounts() {
        Map<String, Integer> stackCounts = new HashMap<>();
        if (currentGameData != null && currentGameData.getBoard() != null) {
            // Count various types of hexagons
            for (HexData hex : currentGameData.getBoard().getHexes()) {
                String stackType = hex.getType();
                stackCounts.put(stackType, stackCounts.getOrDefault(stackType, 0) + 1);
            }
        }
        return stackCounts;
    }
    
    private void updateCurrentRound() {
        if (multiRoundData != null && currentRoundIndex < multiRoundData.getRounds().size()) {
            currentGameData = convertRoundToGameData(multiRoundData.getRounds().get(currentRoundIndex));
            updateDisplay();
            animationControlPanel.setCurrentRound(currentRoundIndex + 1);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AnimatedGameVisualizer visualizer = new AnimatedGameVisualizer();
            visualizer.setVisible(true);
            
            // If a file path is provided as command line argument, load it automatically
            if (args.length > 0) {
                String filePath = args[0];
                visualizer.loadFileAutomatically(filePath);
            }
        });
    }
    
    private MultiRoundGameData generateRandomMultiRoundData(int rounds, long seed) {
        MultiRoundGameData multiRoundData = new MultiRoundGameData();
        multiRoundData.setVersion(1);
        multiRoundData.setTotal_rounds(rounds);
        
        java.util.List<RoundData> roundList = new java.util.ArrayList<>();
        java.util.Random rng = new java.util.Random(seed);
        
        // Generate initial random states
        String[] states = {"WILDS", "WASTES", "DEVA", "DEVB"};
        String[] currentStates = new String[11];
        
        for (int i = 0; i < 11; i++) {
            currentStates[i] = states[rng.nextInt(states.length)];
        }
        
        // Generate each round
        for (int round = 1; round <= rounds; round++) {
            RoundData roundData = new RoundData();
            roundData.setRound_number(round);
            
            // Create game board
            GameBoard board = new GameBoard();
            java.util.List<HexData> hexes = new java.util.ArrayList<>();
            
            // Create hexes based on current states
            for (int i = 0; i < 11; i++) {
                String state = currentStates[i];
                HexData hex = new HexData(i + 1, state, getColorFromState(state));
                hexes.add(hex);
            }
            
            board.setHexes(hexes);
            roundData.setBoard(board);
            
            // Create game state
            GameState gameState = new GameState();
            gameState.setCurrent_round(round);
            gameState.setMax_rounds(rounds);
            gameState.setBag_total(20 + rng.nextInt(20)); // Random bag total
            roundData.setGame_state(gameState);
            
            // Create tokens
            Tokens tokens = new Tokens();
            tokens.setWilds(25 - rng.nextInt(10));
            tokens.setWastes(25 - rng.nextInt(10));
            tokens.setDevA(25 - rng.nextInt(10));
            tokens.setDevB(25 - rng.nextInt(10));
            roundData.setTokens(tokens);
            
            roundList.add(roundData);
            
            // Simulate state changes for next round (simplified)
            if (round < rounds) {
                for (int i = 0; i < 11; i++) {
                    if (rng.nextDouble() < 0.3) { // 30% chance to change state
                        currentStates[i] = states[rng.nextInt(states.length)];
                    }
                }
            }
        }
        
        multiRoundData.setRounds(roundList);
        return multiRoundData;
    }
    
    private String getColorFromState(String state) {
        switch (state) {
            case "WILDS": return "green";
            case "WASTES": return "brown";
            case "DEVA": return "blue";
            case "DEVB": return "pink";
            default: return "gray";
        }
    }
    
    private void loadFileAutomatically(String filePath) {
        try {
            // Use UniversalDataConverter to automatically detect format and convert
            multiRoundData = UniversalDataConverter.convertFileToMultiRound(filePath);
            
            if (multiRoundData != null && !multiRoundData.getRounds().isEmpty()) {
                currentRoundIndex = 0;
                updateDisplay();
                animationControlPanel.setCurrentRound(currentRoundIndex + 1);
                animationControlPanel.setTotalRounds(multiRoundData.getTotal_rounds());
                statusLabel.setText("Loaded: " + filePath + " (" + multiRoundData.getTotal_rounds() + " rounds)");
                
                // Don't auto-start animation, let user click play button
            } else {
                statusLabel.setText("Failed to load: " + filePath);
            }
        } catch (Exception e) {
            statusLabel.setText("Error loading: " + filePath + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}

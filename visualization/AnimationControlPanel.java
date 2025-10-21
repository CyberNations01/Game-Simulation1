import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AnimationControlPanel extends JPanel {
    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JButton replayButton;
    private JButton prevButton;
    private JButton nextButton;
    private JSlider progressSlider;
    private JLabel currentRoundLabel;
    private JLabel totalRoundsLabel;
    private JSlider speedSlider;
    private JLabel speedLabel;
    private JCheckBox loopCheckBox;
    private JCheckBox dynamicChartCheckBox;
    
    private boolean isPlaying = false;
    private int currentRound = 1;
    private int totalRounds = 1;
    private int animationSpeed = 1000; // milliseconds per frame
    
    public AnimationControlPanel() {
        initializeComponents();
        setupLayout();
        setupEventListeners();
    }
    
    private void initializeComponents() {
        // Control buttons
        playButton = new JButton("▶ Play");
        pauseButton = new JButton("⏸ Pause");
        stopButton = new JButton("⏹ Stop");
        replayButton = new JButton("⏎ Reset");
        prevButton = new JButton("⏮ Prev");
        nextButton = new JButton("Next ⏭");
        
        // Progress control
        progressSlider = new JSlider(1, 1, 1);
        progressSlider.setMajorTickSpacing(1);
        progressSlider.setPaintTicks(true);
        progressSlider.setPaintLabels(true);
        
        // Round labels
        currentRoundLabel = new JLabel("Round: 1");
        totalRoundsLabel = new JLabel("Total: 1");
        
        // Speed control - use round numbers for ticks
        speedSlider = new JSlider(100, 3000, 1000);
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setMinorTickSpacing(250);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        
        // Create custom labels for round numbers
        java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
        labelTable.put(100, new JLabel("100"));
        labelTable.put(500, new JLabel("500"));
        labelTable.put(1000, new JLabel("1000"));
        labelTable.put(1500, new JLabel("1500"));
        labelTable.put(2000, new JLabel("2000"));
        labelTable.put(2500, new JLabel("2500"));
        labelTable.put(3000, new JLabel("3000"));
        speedSlider.setLabelTable(labelTable);
        
        speedLabel = new JLabel("Speed: 1.0s");
        
        // Loop checkbox
        loopCheckBox = new JCheckBox("Loop");
        
        // Dynamic chart checkbox
        dynamicChartCheckBox = new JCheckBox("Dynamic Chart");
        dynamicChartCheckBox.setSelected(false);
        
        // Initial state
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Animation Controls"));
        
        // Top panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(replayButton);
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        
        // Center panel for progress
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(new JLabel("Progress:"), BorderLayout.WEST);
        progressPanel.add(progressSlider, BorderLayout.CENTER);
        
        // Round info panel
        JPanel roundPanel = new JPanel(new FlowLayout());
        roundPanel.add(currentRoundLabel);
        roundPanel.add(totalRoundsLabel);
        
        // Speed control panel
        JPanel speedPanel = new JPanel(new BorderLayout());
        speedPanel.add(speedLabel, BorderLayout.WEST);
        speedPanel.add(speedSlider, BorderLayout.CENTER);
        
        // Checkboxes panel
        JPanel checkboxPanel = new JPanel(new FlowLayout());
        checkboxPanel.add(loopCheckBox);
        checkboxPanel.add(dynamicChartCheckBox);
        speedPanel.add(checkboxPanel, BorderLayout.EAST);
        
        // Combine panels
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        centerPanel.add(progressPanel);
        centerPanel.add(roundPanel);
        centerPanel.add(speedPanel);
        
        add(buttonPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    private void setupEventListeners() {
        playButton.addActionListener(e -> {
            isPlaying = true;
            playButton.setEnabled(false);
            pauseButton.setEnabled(true);
            stopButton.setEnabled(true);
            onPlay();
        });
        
        pauseButton.addActionListener(e -> {
            isPlaying = false;
            playButton.setEnabled(true);
            pauseButton.setEnabled(false);
            onPause();
        });
        
        stopButton.addActionListener(e -> {
            isPlaying = false;
            playButton.setEnabled(true);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
            onStop();
        });
        
        replayButton.addActionListener(e -> {
            isPlaying = false;
            playButton.setEnabled(true);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
            onReplay();
        });
        
        prevButton.addActionListener(e -> onPrevious());
        nextButton.addActionListener(e -> onNext());
        
        progressSlider.addChangeListener(e -> {
            if (!progressSlider.getValueIsAdjusting()) {
                onProgressChanged(progressSlider.getValue());
            }
        });
        
        speedSlider.addChangeListener(e -> {
            animationSpeed = speedSlider.getValue();
            speedLabel.setText(String.format("Speed: %.1fs", animationSpeed / 1000.0));
            // Always call onSpeedChanged, even during playback
            onSpeedChanged(animationSpeed);
        });
    }
    
    // Getters and setters
    public boolean isPlaying() { return isPlaying; }
    public int getCurrentRound() { return currentRound; }
    public int getTotalRounds() { return totalRounds; }
    public int getAnimationSpeed() { return animationSpeed; }
    public boolean isLoopEnabled() { return loopCheckBox.isSelected(); }
    
    public boolean isDynamicChartEnabled() { return dynamicChartCheckBox.isSelected(); }
    
    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
        progressSlider.setMaximum(totalRounds);
        totalRoundsLabel.setText("Total: " + totalRounds);
    }
    
    public void setCurrentRound(int round) {
        this.currentRound = round;
        currentRoundLabel.setText("Round: " + round);
        progressSlider.setValue(round);
    }
    
    public void setPlaying(boolean playing) {
        isPlaying = playing;
        playButton.setEnabled(!playing);
        pauseButton.setEnabled(playing);
        stopButton.setEnabled(playing);
    }
    
    // Event callbacks (to be implemented by parent)
    protected void onPlay() {}
    protected void onPause() {}
    protected void onStop() {}
    protected void onReplay() {}
    protected void onPrevious() {}
    protected void onNext() {}
    protected void onProgressChanged(int round) {}
    protected void onSpeedChanged(int speed) {}
}



import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CustomSimulationDialog extends JDialog {
    private JSpinner roundsSpinner;
    private JComboBox<String>[] stateComboBoxes;
    private JSpinner[] tokenLimitSpinners;
    private JComboBox<String> stageComboBox;
    private boolean confirmed = false;
    
    public CustomSimulationDialog(JFrame parent) {
        super(parent, "Custom Simulation", true);
        setSize(500, 700); // Larger window // 扩大窗口高度以容纳新内容
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Set default spacing between sections
        gbc.insets = new Insets(5, 0, 5, 0); // top, left, bottom, right
        
        // Rounds input - compact layout with range indicator
        JPanel roundsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        roundsPanel.add(new JLabel("Rounds:"));
        roundsSpinner = new JSpinner(new SpinnerNumberModel(6, 1, 100, 1)); // Default 6, range 1-100
        roundsSpinner.setPreferredSize(new Dimension(50, 25)); // Compact size
        roundsSpinner.setFocusable(false); // Prevent auto-focus to avoid cursor flashing
        
        // Set gray background for default value
        roundsSpinner.getEditor().getComponent(0).setBackground(new Color(240, 240, 240)); // Light gray background
        roundsSpinner.getEditor().getComponent(0).addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                roundsSpinner.getEditor().getComponent(0).setBackground(Color.WHITE);
                ((javax.swing.text.JTextComponent) roundsSpinner.getEditor().getComponent(0)).selectAll();
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (roundsSpinner.getValue().equals(6)) {
                    roundsSpinner.getEditor().getComponent(0).setBackground(new Color(240, 240, 240));
                } else {
                    roundsSpinner.getEditor().getComponent(0).setBackground(Color.WHITE);
                }
            }
        });
        
        // Add change listener to provide visual feedback
        roundsSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
            int value = (Integer) spinner.getValue();
            int max = (Integer) model.getMaximum();
            int min = (Integer) model.getMinimum();
            
            // Shake effect when value is out of range and auto-correct
            if (value > max) {
                shakeComponent(spinner);
                spinner.setValue(max);
            } else if (value < min) {
                shakeComponent(spinner);
                spinner.setValue(min);
            }
        });
        
        // Add mouse listener for click-to-select
        roundsSpinner.getEditor().getComponent(0).addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    ((javax.swing.text.JTextComponent) roundsSpinner.getEditor().getComponent(0)).selectAll();
                });
            }
        });
        
        // Add keyboard navigation
        roundsSpinner.getEditor().getComponent(0).addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    roundsSpinner.getEditor().getComponent(0).transferFocus();
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {
                    if (e.isShiftDown()) {
                        // Shift+Tab: move to previous component
                        roundsSpinner.getEditor().getComponent(0).transferFocusBackward();
                    } else {
                        // Tab: move to next component
                        roundsSpinner.getEditor().getComponent(0).transferFocus();
                    }
                }
            }
        });
        
        roundsPanel.add(roundsSpinner);
        roundsPanel.add(new JLabel("(1-100)")); // Range indicator
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 0, 10, 0); // More space from top edge
        mainPanel.add(roundsPanel, gbc);
        
        // Stage selection
        JPanel stagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        stagePanel.add(new JLabel("Stage:"));
        stageComboBox = new JComboBox<>(new String[]{"Stage 1 (Basic)", "Stage 2 (With Disruption Cards)"});
        stageComboBox.setSelectedIndex(1); // Default to Stage 2
        stageComboBox.setPreferredSize(new Dimension(200, 25));
        stagePanel.add(stageComboBox);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 10, 0); // More space below
        mainPanel.add(stagePanel, gbc);
        
        // Token limits section - compact horizontal layout
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST; // Left align title
        gbc.insets = new Insets(10, 0, 5, 0); // More space above Token Pool Limits
        mainPanel.add(new JLabel("Token Pool Limits:"), gbc);
        
        // Create horizontal panel for token limits
        JPanel tokenPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        String[] tokenTypes = {"WILDS", "WASTES", "DEVA", "DEVB"};
        String[] colorSquares = {"🟩", "🟫", "🟪", "🟦"}; // Green, Brown, Purple, Blue
        tokenLimitSpinners = new JSpinner[4];
        
        for (int i = 0; i < 4; i++) {
            tokenPanel.add(new JLabel(colorSquares[i]));
            tokenLimitSpinners[i] = new JSpinner(new SpinnerNumberModel(20, 0, 100, 1)); // Range 0-100, default 20
            tokenLimitSpinners[i].setPreferredSize(new Dimension(50, 25)); // Compact size
            
            // Set gray background for default value
            final JSpinner currentTokenSpinner = tokenLimitSpinners[i];
            currentTokenSpinner.getEditor().getComponent(0).setBackground(new Color(240, 240, 240)); // Light gray background
            currentTokenSpinner.getEditor().getComponent(0).addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    currentTokenSpinner.getEditor().getComponent(0).setBackground(Color.WHITE);
                    ((javax.swing.text.JTextComponent) currentTokenSpinner.getEditor().getComponent(0)).selectAll();
                }
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (currentTokenSpinner.getValue().equals(20)) {
                        currentTokenSpinner.getEditor().getComponent(0).setBackground(new Color(240, 240, 240));
                    } else {
                        currentTokenSpinner.getEditor().getComponent(0).setBackground(Color.WHITE);
                    }
                }
            });
            
            // Add change listener for visual feedback
            final int index = i; // For lambda capture
            tokenLimitSpinners[i].addChangeListener(e -> {
                JSpinner spinner = (JSpinner) e.getSource();
                SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
                int value = (Integer) spinner.getValue();
                int max = (Integer) model.getMaximum();
                int min = (Integer) model.getMinimum();
                
                // Shake effect when value is out of range and auto-correct
                if (value > max) {
                    shakeComponent(spinner);
                    spinner.setValue(max);
                } else if (value < min) {
                    shakeComponent(spinner);
                    spinner.setValue(min);
                }
            });
            
            // Add mouse listener for click-to-select
            currentTokenSpinner.getEditor().getComponent(0).addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        ((javax.swing.text.JTextComponent) currentTokenSpinner.getEditor().getComponent(0)).selectAll();
                    });
                }
            });
            
            // Add keyboard navigation
            final JSpinner currentSpinner = tokenLimitSpinners[i];
            currentSpinner.getEditor().getComponent(0).addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                        currentSpinner.getEditor().getComponent(0).transferFocus();
                    } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {
                        if (e.isShiftDown()) {
                            // Shift+Tab: move to previous component
                            currentSpinner.getEditor().getComponent(0).transferFocusBackward();
                        } else {
                            // Tab: move to next component
                            currentSpinner.getEditor().getComponent(0).transferFocus();
                        }
                    }
                }
            });
            
            tokenPanel.add(tokenLimitSpinners[i]);
        }
        
        // Add range indicator for token limits
        tokenPanel.add(new JLabel("(0-100)"));
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 10, 0); // More space below
        mainPanel.add(tokenPanel, gbc);
        
        // Initial states section
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST; // Left align title
        gbc.insets = new Insets(10, 0, 5, 0); // More space above Initial States
        mainPanel.add(new JLabel("Initial States (11 stacks):"), gbc);
        
        // Create state selection for each stack - using GridBagLayout for alignment
        stateComboBoxes = new JComboBox[11];
        String[] states = {"WILDS", "WASTES", "DEVA", "DEVB"};
        
        for (int i = 0; i < 11; i++) {
            // Label for Stack X with indentation:
            gbc.gridx = 0; gbc.gridy = 5 + i; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.WEST; // Left align label
            gbc.insets = new Insets(2, 20, 2, 0); // Add left indentation (20px)
            mainPanel.add(new JLabel("Stack " + (i + 1) + ":"), gbc);
            
            // ComboBox for Stack X with indentation:
            gbc.gridx = 1; gbc.gridy = 5 + i; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.WEST; // Ensure combo box starts from left in its cell
            gbc.insets = new Insets(2, 100, 2, 0); // Add left indentation (100px)
            stateComboBoxes[i] = new JComboBox<>(states);
            stateComboBoxes[i].setPreferredSize(new Dimension(100, 25)); // Adjusted size
            stateComboBoxes[i].setSelectedIndex(i % 4); // Default to different states
            mainPanel.add(stateComboBoxes[i], gbc);
        }
        
        // Quick setup buttons - more compact design
        gbc.gridx = 0; gbc.gridy = 16; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.insets = new Insets(10, 0, 5, 0); // More space above Quick setup buttons
        JPanel quickPanel = new JPanel(new FlowLayout());
        
        // All button + state selection
        JButton allButton = new JButton("All");
        JComboBox<String> allStateCombo = new JComboBox<>(new String[]{"WILDS", "WASTES", "DEVA", "DEVB"});
        allButton.addActionListener(e -> setAllStates((String)allStateCombo.getSelectedItem()));
        quickPanel.add(allButton);
        quickPanel.add(allStateCombo);
        
        // Random button
        JButton randomButton = new JButton("Random");
        randomButton.addActionListener(e -> setRandomStates());
        quickPanel.add(randomButton);
        
        mainPanel.add(quickPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("Generate");
        JButton cancelButton = new JButton("Cancel");
        
        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set default button
        getRootPane().setDefaultButton(okButton);
    }
    
    private void setAllStates(String state) {
        for (JComboBox<String> comboBox : stateComboBoxes) {
            comboBox.setSelectedItem(state);
        }
    }
    
    private void setRandomStates() {
        String[] states = {"WILDS", "WASTES", "DEVA", "DEVB"};
        java.util.Random rng = new java.util.Random();
        for (JComboBox<String> comboBox : stateComboBoxes) {
            comboBox.setSelectedItem(states[rng.nextInt(states.length)]);
        }
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public int getRounds() {
        return (Integer) roundsSpinner.getValue();
    }
    
    public String[] getInitialStates() {
        String[] states = new String[11];
        for (int i = 0; i < 11; i++) {
            states[i] = (String) stateComboBoxes[i].getSelectedItem();
        }
        return states;
    }
    
    public int[] getTokenLimits() {
        int[] limits = new int[4];
        for (int i = 0; i < 4; i++) {
            limits[i] = (Integer) tokenLimitSpinners[i].getValue();
        }
        return limits;
    }
    
    public int getSelectedStage() {
        return stageComboBox.getSelectedIndex() + 1; // 1 for Stage 1, 2 for Stage 2
    }
    
    // Shake effect method
    private void shakeComponent(Component component) {
        Timer shakeTimer = new Timer(50, null);
        int[] shakeCount = {0};
        Point originalLocation = component.getLocation();
        
        shakeTimer.addActionListener(e -> {
            if (shakeCount[0] < 6) {
                int offset = (shakeCount[0] % 2 == 0) ? 5 : -5;
                component.setLocation(originalLocation.x + offset, originalLocation.y);
                shakeCount[0]++;
            } else {
                // Ensure we return to exact original position
                component.setLocation(originalLocation);
                shakeTimer.stop();
            }
        });
        
        shakeTimer.start();
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RandomDataDialog extends JDialog {
    private JSpinner roundsSpinner;
    private JSpinner seedSpinner;
    private boolean confirmed = false;
    
    public RandomDataDialog(JFrame parent) {
        super(parent, "Create Random Data", true);
        setSize(300, 200);
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
        
        // Rounds input
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Number of Rounds:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        roundsSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        mainPanel.add(roundsSpinner, gbc);
        
        // Seed input
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JLabel("Random Seed:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        seedSpinner = new JSpinner(new SpinnerNumberModel(System.currentTimeMillis() % 10000, 0, Long.MAX_VALUE, 1));
        mainPanel.add(seedSpinner, gbc);
        
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
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public int getRounds() {
        return (Integer) roundsSpinner.getValue();
    }
    
    public long getSeed() {
        return (Long) seedSpinner.getValue();
    }
}

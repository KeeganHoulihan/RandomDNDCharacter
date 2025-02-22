package Random_Character;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class CharacterGeneratorGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JSpinner levelSpinner;
    private JSpinner classesSpinner;
    private JTextArea resultArea;
    private JButton generateButton;
    
    public CharacterGeneratorGUI() {
        // Set up the frame
        setTitle("D&D Random Character Generator");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create components
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Input panel (top)
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        // Level selector
        inputPanel.add(new JLabel("Character Level (1-20):"));
        SpinnerNumberModel levelModel = new SpinnerNumberModel(1, 1, 20, 1);
        levelSpinner = new JSpinner(levelModel);
        inputPanel.add(levelSpinner);
        
        // Classes selector
        inputPanel.add(new JLabel("Number of Classes (1-13):"));
        SpinnerNumberModel classesModel = new SpinnerNumberModel(1, 1, 13, 1);
        classesSpinner = new JSpinner(classesModel);
        inputPanel.add(classesSpinner);
        
        // Result area (center)
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        
        // Button panel (bottom)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        generateButton = new JButton("Generate Character");
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateCharacter();
            }
        });
        buttonPanel.add(generateButton);
        
        // Add components to main panel
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel);
    }
    
    private void generateCharacter() {
        int level = (Integer) levelSpinner.getValue();
        int maxClasses = (Integer) classesSpinner.getValue();
        
        try {
            Character character = new Character(level, maxClasses);
            StringBuilder result = new StringBuilder();
            
            // Fancy header
            result.append("╔══════════════════════════════════════════════════════════════╗\n");
            result.append("                 D&D RANDOM CHARACTER SHEET                     \n");
            result.append("╠══════════════════════════════════════════════════════════════╣\n");
            
            // Stats section with the new formatted output
            result.append("\n").append(character.getStats().toString()).append("\n\n");
            
            // Race section
            result.append("╔══════════════════════════════════════════════════════════════╗\n");
            result.append(String.format(" RACE: %-56s \n", character.getRace().toString()));
            result.append("╚══════════════════════════════════════════════════════════════╝\n\n");
            
            // Classes section
            result.append("╔══════════════════════════════════════════════════════════════╗\n");
            result.append("  CLASSES:                                                      \n");
            result.append("╠══════════════════════════════════════════════════════════════╣\n");
            
            String[] classLines = character.getCharClass().toString().split("\n");
            for (String line : classLines) {
                result.append(String.format(" %-60s \n", line));
            }
            result.append("╚══════════════════════════════════════════════════════════════╝\n\n");
            
            // Background section
            result.append("╔══════════════════════════════════════════════════════════════╗\n");
            result.append(String.format(" BACKGROUND: %-50s \n", character.getBackground().toString()));
            result.append("╚══════════════════════════════════════════════════════════════╝\n\n");
            
            // Alignment section
            result.append("╔══════════════════════════════════════════════════════════════╗\n");
            result.append(String.format(" ALIGNMENT: %-51s \n", character.getAlignment().toString()));
            result.append("╚══════════════════════════════════════════════════════════════╝\n");
            
            resultArea.setText(result.toString());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error generating character: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    // Main method to launch the GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CharacterGeneratorGUI().setVisible(true);
            }
        });
    }
}

import javax.swing.*;
import java.awt.*;

public class CharacterGeneratorGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JSpinner levelSpinner;
    private JSpinner classesSpinner;
    private JTextArea resultArea;
    private JCheckBox homebrewCheckbox;
    private JComboBox<String> rollingMethodDropdown; // New dropdown

    public CharacterGeneratorGUI() {
        setTitle("D&D Character Generator");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Character Options"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        levelSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        classesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        JCheckBox multiclassCheckbox = new JCheckBox("Enforce Multiclass Requirements", true);
        homebrewCheckbox = new JCheckBox("Include Homebrew Content", false);

        // Rolling method dropdown
        String[] rollingMethods = {"4d6 Drop Lowest", "2d20 Drop Lowest", "3d6 Straight", "5d8 Drop Lowest 2", "Standard Array"};
        rollingMethodDropdown = new JComboBox<>(rollingMethods);

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Character Level:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(levelSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Max Classes:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(classesSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Rolling Method:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(rollingMethodDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        inputPanel.add(multiclassCheckbox, gbc);
        gbc.gridy = 4;
        inputPanel.add(homebrewCheckbox, gbc);

        JButton generateButton = new JButton("Generate Character");
        gbc.gridy = 5;
        inputPanel.add(generateButton, gbc);

        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createTitledBorder("Generated Character"));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        generateButton.addActionListener(e -> generateCharacter(multiclassCheckbox.isSelected()));
        setLocationRelativeTo(null);
    }

    private void generateCharacter(boolean enforceMulticlass) {
        try {
            int level = (int) levelSpinner.getValue();
            int maxClasses = (int) classesSpinner.getValue();
            boolean includeHomebrew = homebrewCheckbox.isSelected();
            String rollingMethod = (String) rollingMethodDropdown.getSelectedItem(); // Get selected method

            
            Stats stats = new Stats(rollingMethod);
            CharClass charClass = new CharClass(level, maxClasses, stats, enforceMulticlass, includeHomebrew);
            Race race = new Race();
            Background background = new Background();
            Alignment alignment = new Alignment();
            
            String result = "Generated Character\n\n" +
                    "Character Stats:\n" + stats.toString() + "\n\n" +
                    "Character Race: " + race.toString() + "\n\n" +
                    "Character Background: " + background.toString() + "\n\n" +
                    "Character Alignment: " + alignment.toString() + "\n\n" +
                    "Character Class Information:\n" + charClass;

            resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Ensures proper table alignment
            resultArea.setText(result);
        } catch (Exception ex) {
            resultArea.setText("Error generating character: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CharacterGeneratorGUI().setVisible(true));
    }
}

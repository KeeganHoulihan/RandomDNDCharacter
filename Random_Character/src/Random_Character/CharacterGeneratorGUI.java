import javax.swing.*;
import javax.swing.plaf.basic.BasicComboPopup;

import java.awt.*;

public class CharacterGeneratorGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JSpinner levelSpinner;
    private JSpinner classesSpinner;
    private JTextArea resultArea;
    private JCheckBox homebrewCheckbox;
    private JComboBox<String> rollingMethodDropdown;
    private JTabbedPane tabbedPane;
    
    // Adjusted colors for better readability
    private static final Color BACKGROUND_COLOR = new Color(45, 49, 66);
    private static final Color PANEL_COLOR = new Color(70, 75, 95);
    private static final Color TEXT_COLOR = new Color(255, 255, 255);
    private static final Color ACCENT_COLOR = new Color(189, 147, 249);
    private static final Color BUTTON_COLOR = new Color(80, 250, 123);
    private static final Color INPUT_BACKGROUND = new Color(230, 230, 230); // Light gray for inputs
    private static final Color INPUT_TEXT_COLOR = new Color(0, 0, 0); // Black text for inputs
    private static final Color TAB_BACKGROUND = new Color(200, 200, 210); // Light gray for tabs
    private static final Color TAB_TEXT_COLOR = new Color(0, 0, 0); // Black text for tabs
    
    public CharacterGeneratorGUI() {
        setTitle("D&D Character Generator");
        setSize(1200, 900);
        setMinimumSize(new Dimension(1000, 800));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(20, 20));
        
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        JPanel leftPanel = createStyledInputPanel();
        tabbedPane = createTabbedPane();
        
        mainPanel.add(leftPanel);
        mainPanel.add(tabbedPane);
        
        add(mainPanel, BorderLayout.CENTER);
        
        setLocationRelativeTo(null);
    }
    
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(TAB_BACKGROUND);
        tabbedPane.setForeground(TAB_TEXT_COLOR);

        // Style the tabs
        UIManager.put("TabbedPane.selected", TAB_BACKGROUND);
        UIManager.put("TabbedPane.contentAreaColor", PANEL_COLOR);

        JPanel statsPanel = new JPanel(new BorderLayout(10, 10));
        statsPanel.setBackground(PANEL_COLOR);
        resultArea = createStyledTextArea();
        statsPanel.add(new JScrollPane(resultArea));

        JPanel skillsPanel = new JPanel(new BorderLayout(10, 10));
        skillsPanel.setBackground(PANEL_COLOR);

        JPanel equipmentPanel = new JPanel(new BorderLayout(10, 10));
        equipmentPanel.setBackground(PANEL_COLOR);

        JPanel spellsPanel = new JPanel(new BorderLayout(10, 10));
        spellsPanel.setBackground(PANEL_COLOR);



        tabbedPane.addTab("Character", statsPanel);
        tabbedPane.addTab("Skills & Proficiencies", skillsPanel);
        tabbedPane.addTab("Equipment", equipmentPanel);
        tabbedPane.addTab("Spells", spellsPanel);

        // Style each tab
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setBackgroundAt(i, TAB_BACKGROUND);
            tabbedPane.setForegroundAt(i, TAB_TEXT_COLOR);
        }

        return tabbedPane;
    }
    private JTextArea createStyledTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(BACKGROUND_COLOR);
        textArea.setForeground(TEXT_COLOR);
        textArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }
    
    private JPanel createStyledInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(PANEL_COLOR);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Create styled components
        levelSpinner = createStyledSpinner(1, 1, 20, 1);
        classesSpinner = createStyledSpinner(1, 1, 5, 1);
        JCheckBox multiclassCheckbox = createStyledCheckbox("Enforce Multiclass Requirements", true);
        homebrewCheckbox = createStyledCheckbox("Include Homebrew Content", false);
        
        String[] rollingMethods = {"Standard Array", "4d6 Drop Lowest", "2d20 Drop Lowest", "3d6 Straight", "5d8 Drop Lowest 2"};
        rollingMethodDropdown = createStyledComboBox(rollingMethods);
        
        // Add components with labels
        addLabelAndComponent("Character Level:", levelSpinner, inputPanel, gbc, 0);
        addLabelAndComponent("Max Classes:", classesSpinner, inputPanel, gbc, 1);
        addLabelAndComponent("Rolling Method:", rollingMethodDropdown, inputPanel, gbc, 2);
        
        // Add checkboxes
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        inputPanel.add(multiclassCheckbox, gbc);
        gbc.gridy = 4;
        inputPanel.add(homebrewCheckbox, gbc);
        
        // Create and add styled button
        JButton generateButton = createStyledButton("Generate Character");
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(generateButton, gbc);
        
        // Add vertical glue to push everything to the top
        gbc.gridy = 6;
        gbc.weighty = 1.0;
        inputPanel.add(Box.createVerticalGlue(), gbc);
        
        generateButton.addActionListener(e -> generateCharacter(multiclassCheckbox.isSelected()));
        
        return inputPanel;
    }
    
    private JSpinner createStyledSpinner(int value, int min, int max, int step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        spinner.setBackground(INPUT_BACKGROUND);
        spinner.setForeground(INPUT_TEXT_COLOR);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(INPUT_BACKGROUND);
            tf.setForeground(INPUT_TEXT_COLOR);
            tf.setCaretColor(INPUT_TEXT_COLOR);
        }
        return spinner;
    }
    
    private JCheckBox createStyledCheckbox(String text, boolean selected) {
        JCheckBox checkbox = new JCheckBox(text, selected);
        checkbox.setBackground(PANEL_COLOR);
        checkbox.setForeground(TEXT_COLOR);
        checkbox.setFocusPainted(false);
        checkbox.setFont(new Font("Dialog", Font.PLAIN, 14));
        return checkbox;
    }
    
    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setBackground(INPUT_BACKGROUND);
        comboBox.setForeground(INPUT_TEXT_COLOR);
        comboBox.setFont(new Font("Dialog", Font.PLAIN, 14));
        
        // Style the popup menu
        Object child = comboBox.getAccessibleContext().getAccessibleChild(0);
        if (child instanceof BasicComboPopup) {
            BasicComboPopup popup = (BasicComboPopup) child;
            popup.getList().setBackground(INPUT_BACKGROUND);
            popup.getList().setForeground(INPUT_TEXT_COLOR);
        }
        
        return comboBox;
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        button.setFont(new Font("Dialog", Font.BOLD, 14));
        return button;
    }
    
    private void addLabelAndComponent(String labelText, JComponent component, JPanel panel, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Dialog", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(label, gbc);
        gbc.gridx = 1;
        panel.add(component, gbc);
    }
    
    private void generateCharacter(boolean enforceMulticlass) {
        try {
            int level = (int) levelSpinner.getValue();
            int maxClasses = (int) classesSpinner.getValue();
            boolean includeHomebrew = homebrewCheckbox.isSelected();
            String rollingMethod = (String) rollingMethodDropdown.getSelectedItem();

            Character character = new Character(level, maxClasses, rollingMethod, includeHomebrew);

            // Split the character information across tabs
            String mainStats = "≡ Generated Character ≡\n\n" +
                    "◆ Character Stats:\n" + character.getStats().toString() + "\n\n" +
                    "◆ Character Race: " + character.getRace().toString() + "\n\n" +
                    "◆ Character Background: " + character.getBackground().toString() + "\n\n" +
                    "◆ Character Alignment: " + character.getAlignment().toString() + "\n\n" +
                    "◆ Character Class Information:\n" + character.getCharClass();

            resultArea.setText(mainStats);

            // Add skill proficiencies to the Skills tab
            JTextArea skillsArea = createStyledTextArea();
            skillsArea.setText(character.getSkillProficiency().toString());
            ((JPanel)tabbedPane.getComponentAt(1)).removeAll();
            ((JPanel)tabbedPane.getComponentAt(1)).add(new JScrollPane(skillsArea));

        } catch (Exception ex) {
            resultArea.setText("⚠ Error generating character: " + ex.getMessage());
        }
    }
    public static void main(String[] args) {
        try {
            // Set system look and feel for better integration
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Override some UI defaults for better visibility
            UIManager.put("ComboBox.background", new Color(230, 230, 230));
            UIManager.put("ComboBox.foreground", new Color(0, 0, 0));
            UIManager.put("ComboBox.selectionBackground", new Color(180, 180, 180));
            UIManager.put("ComboBox.selectionForeground", new Color(0, 0, 0));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new CharacterGeneratorGUI().setVisible(true));
    }
}
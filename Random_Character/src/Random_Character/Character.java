import java.util.Map;
import java.io.IOException;

public class Character {
    private Stats characterStats;
    private Alignment characterAlignment;
    private Background characterBackground;
    private CharClass characterClass;
    private Race characterRace;
    private SkillProficiency skillProficiency;

    public Character(int x, int inMaxClass, String rollingMethod, boolean includeHomebrew) throws IOException {
        this.characterStats = new Stats(rollingMethod);
        this.characterAlignment = new Alignment();
        this.characterBackground = new Background(includeHomebrew); // Pass homebrew flag
        this.characterRace = new Race(includeHomebrew); // Set the race first
        this.characterClass = new CharClass(x, inMaxClass, characterStats, true, includeHomebrew);
        
        // Initialize SkillProficiency and add proficiencies
        this.skillProficiency = new SkillProficiency(includeHomebrew);
        this.skillProficiency.addBackgroundProficiencies(characterBackground.toString());
        this.skillProficiency.addRaceProficiencies(characterRace.getRace()); // Add race skill proficiencies
        this.skillProficiency.addRaceLanguages(characterRace.getRace()); // Add race languages

        String firstClass = characterClass.getFirstClass();
        Map<String, Integer> allClasses = characterClass.getClassLevels();
        
        // Process each class separately
        for (String className : allClasses.keySet()) {
            this.skillProficiency.addClassProficiencies(className, firstClass);
        }
    }

    // Overloaded constructor for default rolling method
    public Character(int x, int inMaxClass, boolean includeHomebrew) throws IOException {
        this(x, inMaxClass, "4d6 Drop Lowest", includeHomebrew);
    }

    public Stats getStats() {
        return characterStats;
    }

    public Alignment getAlignment() {
        return characterAlignment;
    }

    public Background getBackground() {
        return characterBackground;
    }

    public CharClass getCharClass() {
        return characterClass;
    }

    public Race getRace() {
        return characterRace;
    }
    
    public SkillProficiency getSkillProficiency() {
        return skillProficiency;
    }
}
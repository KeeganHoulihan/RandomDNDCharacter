package Random_Character;

import java.io.IOException;

public class Character {
    private Stats characterStats;
    private Alignment characterAlignment;
    private Background characterBackground;
    private CharClass characterClass;
    private Race characterRace;

    public Character(int x, int inMaxClass) throws IOException {
        this.characterStats = new Stats();
        this.characterAlignment = new Alignment();
        this.characterBackground = new Background();
        // Pass the stats to CharClass for multiclass requirements
        this.characterClass = new CharClass(x, inMaxClass, characterStats, true);
        this.characterRace = new Race();
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
}
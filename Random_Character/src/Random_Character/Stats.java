
import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Stats {
    private Integer[] spread = {0, 0, 0, 0, 0, 0}; // 6 Individual Stats
    private Random random = new Random(System.currentTimeMillis()); //Randomizer attached to clock so the seed is always different
    private Map<String, Integer> abilityScores = new HashMap<>(); // Map of ability scores to assign them later
    private final String[] ABILITIES = {"STR", "DEX", "CON", "INT", "WIS", "CHA"}; // A character's ability scores
    
    private String rollingMethod; // Store chosen rolling method

    public Stats(String rollingMethod) // Constructor
    {
        this.rollingMethod = rollingMethod; // Gets the rolling method
        generateStats(); //Uses the rolling method to generate stats
        assignAbilityScores(); //Assigns stats to ability scores
    }

    private void generateStats() {
        for (int i = 0; i < spread.length; i++) 
        {
                spread[i] = rollAbilityScore(i); //Iterates through and rolls ability scores for each of the core stats
        }
    }

    private int rollAbilityScore(int count) {
        switch (rollingMethod) {
            case "4d6 Drop Lowest": //If someone wants to roll stats in this day and age, this is the gold standard
                return rollXdYDropLowest(4, 6, 1);
            case "2d20 Drop Lowest": //Utter Chaos
                return rollXdYDropLowest(2, 20, 1);
            case "3d6 Straight": //The classic rolling method from the 90s
                return rollXdYDropLowest(3, 6, 0);
            case "5d8 Drop Lowest 2": //This one is weird
                return rollXdYDropLowest(5, 8, 2);
            case "Standard Array": //Standard stat spread for a dnd character
            	return standardArray(count);
            default:
                return rollXdYDropLowest(4, 6, 1); // Default to 4d6 drop lowest
        }
    }

    
    private int standardArray(int count)
    {
    	Integer[] standardSpread = {15, 14, 13, 12, 10, 8}; //This is the normal dnd standard array
    	return standardSpread[count]; //returns one of the values of the array
    }
    
    private int rollXdYDropLowest(int diceCount, int diceSides, int dropLowest) //Intakes dice count, sides and how many to drop to calc stats
    {
        Integer[] rolls = new Integer[diceCount]; // This hold what the rolls were
        for (int i = 0; i < diceCount; i++) {
            rolls[i] = random.nextInt(diceSides) + 1; //No dice has a 0 on it so always add 1 to make sure you are getting the right numbers
        }
        Arrays.sort(rolls); // Sort the rolls highest to lowest
        int sum = 0;
        for (int i = dropLowest; i < diceCount; i++) //Adds all but the lowest to the array and returns it when it is done
        {
            sum += rolls[i];
        }
        return sum;
    }

    private void assignAbilityScores() {
        List<Integer> statValues = new ArrayList<>(Arrays.asList(spread));
        Collections.shuffle(statValues);
        for (int i = 0; i < ABILITIES.length; i++) {
            abilityScores.put(ABILITIES[i], statValues.get(i));
        }
    }

    public Map<String, Integer> getAbilityScores() {
        return abilityScores;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔═════════╦═══════╦══════════╗\n");
        sb.append("║ Ability ║ Score ║ Modifier \n");
        sb.append("╠═════════╬═══════╬══════════╣\n");
        for (String ability : ABILITIES) {
            int score = abilityScores.getOrDefault(ability, 0);
            int mod = (score - 10) / 2;
            String modStr = (mod >= 0) ? "+" + mod : Integer.toString(mod);
            sb.append(String.format("║ %-6s ║ %-4d ║ %-6s ║\n", ability, score, modStr));
        }
        sb.append("╚═════════╩═══════╩══════════╝");
        return sb.toString();
    }
}

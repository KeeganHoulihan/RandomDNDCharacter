package Random_Character;
import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Stats {
    private Integer[] spread = {0, 0, 0, 0, 0, 0}; // 6 Individual Stats
    private Integer[] indvStats = {0, 0, 0, 0}; // 4 Blank Spots to roll stats with
    private Random random = new Random(System.currentTimeMillis());
    
    // Ability score mapping
    private Map<String, Integer> abilityScores = new HashMap<>();
    private final String[] ABILITIES = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
    
    public void setSpread(Integer[] spreadIn) // Set Spread Manually
    {
        spread = spreadIn;
        assignAbilityScores();
    }
    
    public void setIndvStats(Integer[] setIndv)
    {
        indvStats = setIndv;
    }
    
    public Stats()
    {
        // Generate the raw stat values
        for(int i = 0; i < spread.length; i++)
        {
            for(int l = 0; l < indvStats.length; l++)
            {
                 indvStats[l] = random.nextInt(6) + 1;
            }
            sortIndvStats();
            spread[i] = sumOfTopThree(indvStats);
        }
        
        // Assign stats to abilities randomly
        assignAbilityScores();
    }
    
    private void assignAbilityScores() {
        // Create a copy of spread for randomization
        List<Integer> statValues = new ArrayList<>(Arrays.asList(spread));
        Collections.shuffle(statValues);
        
        // Assign each ability a randomly selected stat
        for (int i = 0; i < ABILITIES.length; i++) {
            abilityScores.put(ABILITIES[i], statValues.get(i));
        }
    }
    
    public Integer[] getStats()
    {
        return spread;
    }
    
    public Map<String, Integer> getAbilityScores() {
        return abilityScores;
    }
    
    public int getAbilityScore(String ability) {
        return abilityScores.getOrDefault(ability, 0);
    }
    
    public int getModifier(String ability) {
        int score = getAbilityScore(ability);
        return (score - 10) / 2;
    }
    
    private void sortIndvStats() {
        Arrays.sort(indvStats, Collections.reverseOrder());
    }
    
    private void sortSpread() {
        Arrays.sort(spread, Collections.reverseOrder());
    }
    
    private int sumOfTopThree(Integer array[])
    {
        return array[0] + array[1] + array[2];
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // Create a fancy table header
        sb.append("╔═════════╦═══════╦══════════╗\n");
        sb.append("║ Ability ║ Score ║ Modifier \n");
        sb.append("╠═════════╬═══════╬══════════╣\n");
        
        
        // Add each ability with its score and modifier
        for (String ability : ABILITIES) {
            int score = getAbilityScore(ability);
            int mod = getModifier(ability);
            String modStr = (mod >= 0) ? "+" + mod : Integer.toString(mod);
            
            sb.append(String.format("║ %-6s ║ %-4d ║ %-7s ║\n", 
                    ability, score, modStr));
        }
        
        // Close the table
        sb.append("╚═════════╩═══════╩══════════╩");
        
        return sb.toString();
    }
    
    // Method to get raw stats as a string (for backward compatibility)
    public String getRawStatsString() {
        return Arrays.toString(spread);
    }
}
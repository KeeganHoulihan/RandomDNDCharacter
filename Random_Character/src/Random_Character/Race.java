import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Race {
    private String race;
    private JsonObject raceData;
    private Random random = new Random();
    private boolean includeHomebrew;
    
    public Race() throws IOException {
        this(false); // Default constructor doesn't include homebrew
    }
    
    public Race(boolean includeHomebrew) throws IOException {
        this.includeHomebrew = includeHomebrew;
        
        // Load and parse JSON data
        Gson gson = new Gson();
        raceData = gson.fromJson(new FileReader("RaceList.json"), JsonObject.class);
        
        // Get eligible race names and randomly select one
        List<String> eligibleRaces = getEligibleRaces();
        race = eligibleRaces.get(random.nextInt(eligibleRaces.size()));
    }
    
    private List<String> getEligibleRaces() {
        List<String> eligibleRaces = new ArrayList<>();
        JsonObject races = raceData.getAsJsonObject("races");
        
        for (String raceName : races.keySet()) {
            JsonObject raceInfo = races.getAsJsonObject(raceName);
            boolean isHomebrew = raceInfo.has("is-homebrew") && 
                               raceInfo.get("is-homebrew").getAsBoolean();
            
            if (includeHomebrew || !isHomebrew) {
                eligibleRaces.add(raceName);
            }
        }
        
        return eligibleRaces;
    }
    
    public void setRace(String setRace) {
        JsonObject races = raceData.getAsJsonObject("races");
        if (!races.has(setRace)) {
            throw new IllegalArgumentException("Invalid race: " + setRace);
        }
        
        JsonObject raceInfo = races.getAsJsonObject(setRace);
        boolean isHomebrew = raceInfo.has("is-homebrew") && 
                           raceInfo.get("is-homebrew").getAsBoolean();
        
        if (!includeHomebrew && isHomebrew) {
            throw new IllegalArgumentException("Homebrew race not allowed: " + setRace);
        }
        
        race = setRace;
    }
    
    public String getRace() {
        return race;
    }
    
    public JsonObject getRaceData() {
        return raceData.getAsJsonObject("races").getAsJsonObject(race);
    }
    
    public boolean isHomebrewEnabled() {
        return includeHomebrew;
    }
    
    @Override
    public String toString() {
        JsonObject raceInfo = getRaceData();
        boolean isHomebrew = raceInfo.has("is-homebrew") && 
                           raceInfo.get("is-homebrew").getAsBoolean();
        return race + (isHomebrew ? " (Homebrew)" : "");
    }
}
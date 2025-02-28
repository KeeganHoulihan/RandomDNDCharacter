import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Race {
    private String race;
    private JsonObject raceData;
    private Random random = new Random(System.nanoTime());
    private boolean includeHomebrew;
    private List<JsonObject> racesList;

    public Race() throws IOException {
        this(false); // Default constructor doesn't include homebrew
    }

    public Race(boolean includeHomebrew) throws IOException {
        this.includeHomebrew = includeHomebrew;

        // Load and parse JSON data
        Gson gson = new Gson();
        JsonObject jsonData = gson.fromJson(new FileReader("RaceList.json"), JsonObject.class);
        JsonArray racesArray = jsonData.getAsJsonArray("races");

        // Convert the JsonArray to a list of JsonObjects for easier processing
        racesList = new ArrayList<>();
        for (JsonElement raceElement : racesArray) {
            racesList.add(raceElement.getAsJsonObject());
        }

        // Get eligible race names and randomly select one
        List<String> eligibleRaces = getEligibleRaces();
        String selectedRace = eligibleRaces.get(random.nextInt(eligibleRaces.size()));
        setRace(selectedRace); // Initialize raceData when selecting a race
    }

    private List<String> getEligibleRaces() {
        List<String> eligibleRaces = new ArrayList<>();

        for (JsonObject raceInfo : racesList) {
            boolean isHomebrew = raceInfo.has("homebrew") && 
                               raceInfo.get("homebrew").getAsBoolean();

            if (includeHomebrew || !isHomebrew) {
                eligibleRaces.add(raceInfo.get("name").getAsString());
            }
        }

        return eligibleRaces;
    }

    public void setRace(String setRace) {
        for (JsonObject raceInfo : racesList) {
            if (raceInfo.get("name").getAsString().equals(setRace)) {
                boolean isHomebrew = raceInfo.has("homebrew") && 
                                   raceInfo.get("homebrew").getAsBoolean();

                if (!includeHomebrew && isHomebrew) {
                    throw new IllegalArgumentException("Homebrew race not allowed: " + setRace);
                }

                race = setRace;
                raceData = raceInfo; // Initialize raceData
                return;
            }
        }

        throw new IllegalArgumentException("Invalid race: " + setRace);
    }

    public String getRace() {
        return race;
    }

    public JsonObject getRaceData() {
        return raceData;
    }

    public boolean isHomebrewEnabled() {
        return includeHomebrew;
    }

    @Override
    public String toString() {
        boolean isHomebrew = raceData.has("homebrew") && 
                           raceData.get("homebrew").getAsBoolean();
        return race + (isHomebrew ? " (Homebrew)" : "");
    }
}
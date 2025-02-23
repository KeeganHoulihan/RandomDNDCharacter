import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Background {
    private String background;
    private JsonObject backgroundData;
    private Random random = new Random();
    private boolean includeHomebrew;
    
    public Background() throws IOException {
        this(false); // Default constructor without homebrew
    }

    public Background(boolean includeHomebrew) throws IOException {
        this.includeHomebrew = includeHomebrew;
        
        // Load and parse JSON data
        Gson gson = new Gson();
        backgroundData = gson.fromJson(new FileReader("BackgroundList.json"), JsonObject.class);
        
        // Get eligible backgrounds and randomly select one
        List<String> eligibleBackgrounds = getEligibleBackgrounds();
        if (eligibleBackgrounds.isEmpty()) {
            throw new IOException("No valid backgrounds available based on homebrew settings.");
        }
        background = eligibleBackgrounds.get(random.nextInt(eligibleBackgrounds.size()));
    }

    private List<String> getEligibleBackgrounds() {
        List<String> eligibleBackgrounds = new ArrayList<>();
        JsonObject backgrounds = backgroundData.getAsJsonObject("backgrounds");

        for (String backgroundName : backgrounds.keySet()) {
            JsonObject backgroundInfo = backgrounds.getAsJsonObject(backgroundName);
            boolean isHomebrew = backgroundInfo.has("is-homebrew") && backgroundInfo.get("is-homebrew").getAsBoolean();

            if (includeHomebrew || !isHomebrew) {
                eligibleBackgrounds.add(backgroundName);
            }
        }
        return eligibleBackgrounds;
    }

    public void setBackground(String setBackground) {
        JsonObject backgrounds = backgroundData.getAsJsonObject("backgrounds");
        if (!backgrounds.has(setBackground)) {
            throw new IllegalArgumentException("Invalid background: " + setBackground);
        }
        
        JsonObject backgroundInfo = backgrounds.getAsJsonObject(setBackground);
        boolean isHomebrew = backgroundInfo.has("is-homebrew") && backgroundInfo.get("is-homebrew").getAsBoolean();
        
        if (!includeHomebrew && isHomebrew) {
            throw new IllegalArgumentException("Homebrew background not allowed: " + setBackground);
        }

        background = setBackground;
    }

    public String getBackground() {
        return background;
    }

    public JsonObject getBackgroundData() {
        return backgroundData.getAsJsonObject("backgrounds").getAsJsonObject(background);
    }

    @Override
    public String toString() {
        JsonObject backgroundInfo = getBackgroundData();
        boolean isHomebrew = backgroundInfo.has("is-homebrew") && backgroundInfo.get("is-homebrew").getAsBoolean();
        return background + (isHomebrew ? " (Homebrew)" : "");
    }
}

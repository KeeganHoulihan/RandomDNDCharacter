import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SkillProficiency {
	private boolean includeHomebrew;
    private Set<String> proficientSkills;
    private JsonObject backgroundData;
    private JsonObject raceData;
    private JsonObject classData;
    private Random random;

    public SkillProficiency(boolean includeHomebrew) throws IOException {
    	this.includeHomebrew = includeHomebrew;
        proficientSkills = new HashSet<>();
        random = new Random();
        Gson gson = new Gson();

        try {
            // Load JSON data
            backgroundData = gson.fromJson(new FileReader("BackgroundList.json"), JsonObject.class);
            raceData = gson.fromJson(new FileReader("RaceList.json"), JsonObject.class);
            classData = gson.fromJson(new FileReader("ClassSkills.json"), JsonObject.class);
            
            // Validate JSON structure
            if (!backgroundData.has("backgrounds")) {
                throw new IOException("Invalid BackgroundList.json structure: missing 'backgrounds' object");
            }
            if (!raceData.has("races")) {
                throw new IOException("Invalid RaceList.json structure: missing 'races' object");
            }
            if (!classData.has("classes")) {
                throw new IOException("Invalid ClassSkills.json structure: missing 'classes' object");
            }
        } catch (IOException e) {
            throw new IOException("Error loading JSON files: " + e.getMessage());
        }
    }

    public void addBackgroundProficiencies(String background) {
        try {
            JsonObject backgrounds = backgroundData.getAsJsonObject("backgrounds");
            if (!backgrounds.has(background)) {
                System.err.println("Warning: Background '" + background + "' not found in JSON");
                return;
            }
            
            JsonObject backgroundInfo = backgrounds.getAsJsonObject(background);
            if (!backgroundInfo.has("skills")) {
                System.err.println("Warning: No skills defined for background '" + background + "'");
                return;
            }

            JsonArray skills = backgroundInfo.getAsJsonArray("skills");
            for (JsonElement skill : skills) {
                proficientSkills.add(skill.getAsString());
            }
        } catch (Exception e) {
            System.err.println("Error processing background '" + background + "': " + e.getMessage());
        }
    }

    public void addRaceProficiencies(String race) {
        try {
            JsonObject races = raceData.getAsJsonObject("races");
            if (!races.has(race)) {
                System.err.println("Warning: Race '" + race + "' not found in JSON");
                return;
            }

            JsonObject raceInfo = races.getAsJsonObject(race);
            if (!isEligibleContent(raceInfo)) {
                System.err.println("Skipping homebrew race '" + race + "' (homebrew disabled)");
                return;
            }

            if (!raceInfo.has("skills")) {
                return; // Some races might not have skills, this is normal
            }

            if (!raceInfo.get("skills").isJsonObject()) {
                JsonArray skills = raceInfo.getAsJsonArray("skills");
                for (JsonElement skill : skills) {
                    proficientSkills.add(skill.getAsString());
                }
            } else {
                JsonObject skillChoice = raceInfo.getAsJsonObject("skills");
                if (skillChoice.has("choose") && skillChoice.has("from")) {
                    int chooseCount = skillChoice.get("choose").getAsInt();
                    JsonArray choices = skillChoice.getAsJsonArray("from");
                    addRandomSkills(choices, chooseCount);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing race '" + race + "': " + e.getMessage());
        }
    }


    public void addClassProficiencies(String className, String firstClass) {
        try {
            JsonObject classes = classData.getAsJsonObject("classes");
            if (!classes.has(className)) {
                System.err.println("Warning: Class '" + className + "' not found in JSON");
                return;
            }

            JsonObject classInfo = classes.getAsJsonObject(className);
            if (!classInfo.has("skill_proficiencies")) {
                System.err.println("Warning: No skill proficiencies defined for class '" + className + "'");
                return;
            }

            JsonObject skillProfs = classInfo.getAsJsonObject("skill_proficiencies");
            if (skillProfs.has("choose") && skillProfs.has("from")) {
                int chooseCount = skillProfs.get("choose").getAsInt();
                if (!className.equals(firstClass)) {
                    chooseCount = getMulticlassSkillCount(className, chooseCount);
                }
                
                JsonArray choices = skillProfs.getAsJsonArray("from");
                addRandomSkills(choices, chooseCount);
            }
        } catch (Exception e) {
            System.err.println("Error processing class '" + className + "': " + e.getMessage());
        }
    }


    private int getMulticlassSkillCount(String className, int baseCount) {
        // For multiclassing, certain classes get additional skill proficiencies
        switch (className) {
            case "Bard":
                return 1; // Bard gets one skill when multiclassing
            case "Ranger":
                return 1; // Ranger gets one skill when multiclassing
            case "Rogue":
                return 1; // Rogue gets one skill when multiclassing
            default:
                return 0; // Other classes get no additional skills when multiclassing
        }
    }
    
    
    private void addRandomSkills(JsonArray choices, int count) {
        List<String> availableSkills = new ArrayList<>();
        for (JsonElement choice : choices) {
            String skill = choice.getAsString();
            if (!proficientSkills.contains(skill)) {
                availableSkills.add(skill);
            }
        }

        for (int i = 0; i < count && !availableSkills.isEmpty(); i++) {
            int index = random.nextInt(availableSkills.size());
            proficientSkills.add(availableSkills.get(index));
            availableSkills.remove(index);
        }
    }

    private boolean isEligibleContent(JsonObject contentInfo) {
        if (!contentInfo.has("is-homebrew")) {
            return true; // Not homebrew
        }
        return includeHomebrew || !contentInfo.get("is-homebrew").getAsBoolean();
    }
    
    public Set<String> getProficientSkills() {
        return new HashSet<>(proficientSkills);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Skill Proficiencies:\n");
        List<String> sortedSkills = new ArrayList<>(proficientSkills);
        Collections.sort(sortedSkills);
        for (String skill : sortedSkills) {
            sb.append("- ").append(skill).append("\n");
        }
        return sb.toString();
    }
}
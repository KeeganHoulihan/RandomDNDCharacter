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
    private Set<String> proficientLanguages;
    private JsonObject backgroundData;
    private JsonArray raceData;
    private JsonObject classData;
    private Random random;

    public SkillProficiency(boolean includeHomebrew) throws IOException {
        this.includeHomebrew = includeHomebrew;
        proficientSkills = new HashSet<>(); //Hash Set of proficient skills to pull from later
        proficientLanguages = new HashSet<>(); //Hash Set for languages
        random = new Random(System.nanoTime()); //Random in nano time to set seed
        Gson gson = new Gson(); //Json interpreter

        try {
            // Load JSON data
            backgroundData = gson.fromJson(new FileReader("BackgroundList.json"), JsonObject.class); //Backgrounds
            JsonObject raceJson = gson.fromJson(new FileReader("RaceList.json"), JsonObject.class); //Races
            raceData = raceJson.getAsJsonArray("races");
            classData = gson.fromJson(new FileReader("ClassSkills.json"), JsonObject.class); //Classes

            // Validate JSON structure
            if (!backgroundData.has("backgrounds")) {
                throw new IOException("Invalid BackgroundList.json structure: missing 'backgrounds' object");
            }
            if (raceData == null) {
                throw new IOException("Invalid RaceList.json structure: missing 'races' array");
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
            for (JsonElement raceElement : raceData) {
                JsonObject raceInfo = raceElement.getAsJsonObject();
                if (raceInfo.get("name").getAsString().equals(race)) {
                    if (!isEligibleContent(raceInfo)) {
                        System.err.println("Skipping homebrew race '" + race + "' (homebrew disabled)");
                        return;
                    }

                    if (!raceInfo.has("skills")) {
                        return; // Some races might not have skills, this is normal
                    }

                    JsonElement skillsElement = raceInfo.get("skills");
                    if (skillsElement.isJsonArray()) {
                        JsonArray skills = skillsElement.getAsJsonArray();
                        for (JsonElement skill : skills) { 
                            if (skill.isJsonObject()) {
                                JsonObject skillChoice = skill.getAsJsonObject();
                                if (skillChoice.has("chooseOne")) {
                                    JsonArray choices = skillChoice.getAsJsonArray("chooseOne");
                                    addRandomSkills(choices, 1);
                                } else if (skillChoice.has("chooseTwo")) {
                                    JsonArray choices = skillChoice.getAsJsonArray("chooseTwo");
                                    addRandomSkills(choices, 2);
                                }
                            } else {
                                proficientSkills.add(skill.getAsString());
                            }
                        }
                    } else if (skillsElement.isJsonObject()) {
                        JsonObject skillChoice = skillsElement.getAsJsonObject();
                        if (skillChoice.has("choose") && skillChoice.has("from")) {
                            int chooseCount = skillChoice.get("choose").getAsInt();
                            JsonArray choices = skillChoice.getAsJsonArray("from");
                            addRandomSkills(choices, chooseCount);
                        }
                    }
                    return;
                }
            }

            System.err.println("Warning: Race '" + race + "' not found in JSON");
        } catch (Exception e) {
            System.err.println("Error processing race '" + race + "': " + e.getMessage());
        }
    }
    
    public void addRaceLanguages(String race) {
        try {
            for (JsonElement raceElement : raceData) {
                JsonObject raceInfo = raceElement.getAsJsonObject();
                if (raceInfo.get("name").getAsString().equals(race)) {
                    if (!isEligibleContent(raceInfo)) {
                        System.err.println("Skipping homebrew race '" + race + "' (homebrew disabled)");
                        return;
                    }

                    // Add common languages
                    JsonElement commonLanguageElement = raceInfo.get("commonLanguage");
                    if (commonLanguageElement != null) {
                        if (commonLanguageElement.isJsonArray()) {
                            JsonArray commonLanguages = commonLanguageElement.getAsJsonArray();
                            for (JsonElement language : commonLanguages) {
                                proficientLanguages.add(language.getAsString());
                                System.out.println("Added common language: " + language.getAsString());
                            }
                        } else {
                            proficientLanguages.add(commonLanguageElement.getAsString());
                            System.out.println("Added common language: " + commonLanguageElement.getAsString());
                        }
                    }

                    // Add bonus languages
                    JsonElement bonusLanguageElement = raceInfo.get("bonusLanguage");
                    if (bonusLanguageElement != null && bonusLanguageElement.isJsonArray()) {
                        JsonArray bonusLanguages = bonusLanguageElement.getAsJsonArray();
                        for (JsonElement language : bonusLanguages) {
                            if (language.isJsonObject()) {
                                JsonObject languageChoice = language.getAsJsonObject();
                                if (languageChoice.has("chooseOne")) {
                                    JsonArray languageChoices = languageChoice.getAsJsonArray("chooseOne");
                                    addRandomLanguages(languageChoices, 1);
                                } else if (languageChoice.has("chooseTwo")) {
                                    JsonArray languageChoices = languageChoice.getAsJsonArray("chooseTwo");
                                    addRandomLanguages(languageChoices, 2);
                                }
                            } else {
                                proficientLanguages.add(language.getAsString());
                                System.out.println("Added bonus language: " + language.getAsString());
                            }
                        }
                    }

                    System.out.println("Chosen Languages: " + proficientLanguages);
                    return;
                }
            }

            System.err.println("Warning: Race '" + race + "' not found in JSON");
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

    public Set<String> getProficientLanguages() {
        return proficientLanguages;
    }
    
    /////
    ///// WORK ON THIS NUMBNUTS
    /////
    
    private void addRandomLanguages(JsonArray choices, int count) {
        List<String> availableLanguages = new ArrayList<>();
        for (JsonElement choice : choices) {
            String language = choice.getAsString();
            if (!proficientLanguages.contains(language)) {
                availableLanguages.add(language);
            }
        }

        for (int i = 0; i < count && !availableLanguages.isEmpty(); i++) {
            int index = random.nextInt(availableLanguages.size());
            proficientLanguages.add(availableLanguages.get(index));
            availableLanguages.remove(index);
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
        if (!contentInfo.has("homebrew")) {
            return true; // Not homebrew
        }
        return includeHomebrew || !contentInfo.get("homebrew").getAsBoolean();
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

        // Add languages with "Common" first
        sb.append("\nLanguages:\n");
        Set<String> languages = getProficientLanguages();
        for (String language : languages) {
            sb.append("- ").append(language).append("\n");
        }

        return sb.toString();
    }
}
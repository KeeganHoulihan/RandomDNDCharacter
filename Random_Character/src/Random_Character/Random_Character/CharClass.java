package Random_Character;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CharClass {
    private Map<String, Object> cclass; // Map of DND Classes the user has
    private List<Map<String, Object>> classes = new ArrayList<>(); // List of DND classes as maps
    private Random random = new Random(System.currentTimeMillis()); // Randomizer
    private int level; // User-inputted level
    private int maxClasses; // Max number of classes the user wants
    private int currentLevel; // Current Level of the character
    private Map<String, Integer> classLevels; // Tracks levels for each class
    private Map<String, String> chosenSubclasses; // Tracks chosen subclass for each class
    private Stats characterStats; // Reference to character stats for multiclass requirements
    private boolean enforceMulticlassRequirements; // Flag to enforce multiclass requirements

    public CharClass(int inLevel, int inMaxClasses, Stats stats) throws IOException {
        this(inLevel, inMaxClasses, stats, true); // By default, enforce multiclass requirements (Always enforce)
    }

    public CharClass(int inLevel, int inMaxClasses, Stats stats, boolean enforceRequirements) throws IOException {
        currentLevel = 1; // Minimum Level is 1
        classLevels = new HashMap<>(); //Create a hash map of class levels
        chosenSubclasses = new HashMap<>(); // Chosen subclasses must also be tracked
        characterStats = stats; // A characters stats are important
        enforceMulticlassRequirements = enforceRequirements; //Enforce multiclass rules
        classes = loadCharClasses(); // Loads DND class data from JSON
        cclass = getRandomClass(); // Randomly selects the class
        level = inLevel; //The level the player selects
        maxClasses = inMaxClasses; //How many classes the player wants
        setClassLevels(); // Assigns levels to classes
    }

    // Overloaded constructor for backward compatibility
    public CharClass(int inLevel, int inMaxClasses) throws IOException {
        this(inLevel, inMaxClasses, null, false);
    }

    public void setCharClass(Map<String, Object> setCharClass) {
        cclass = setCharClass;
    }

    public Map<String, Object> getCharClass() {
        return cclass;
    }

    /**
     * Loads the class data from "ClassList.json" using Gson.
     */
    public List<Map<String, Object>> loadCharClasses() throws IOException {
        List<Map<String, Object>> classList = new ArrayList<>();
        
        try (FileReader reader = new FileReader("ClassList.json")) {
            // Use Gson to parse the JSON file
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject classObject = jsonArray.get(i).getAsJsonObject();
                
                String className = classObject.get("class").getAsString();
                JsonObject subclassesObj = classObject.getAsJsonObject("sub-classes");
                Map<String, String> subclasses = new HashMap<>();
                
                for (String key : subclassesObj.keySet()) {
                    subclasses.put(key, subclassesObj.get(key).getAsString());
                }
                
                long subclassLevel = classObject.get("subclass-level").getAsLong();
                
                Map<String, Object> classInfo = new HashMap<>();
                classInfo.put("class", className);
                classInfo.put("sub-classes", subclasses);
                classInfo.put("subclass-level", subclassLevel);
                
                // Add default multiclass requirements if not present in JSON
                addDefaultMulticlassRequirements(classInfo, className);
                
                // Load stat requirements from JSON if they exist (will override defaults)
                if (classObject.has("stat-requirements")) {
                    JsonObject requirementsObj = classObject.getAsJsonObject("stat-requirements");
                    Map<String, Object> requirements = new HashMap<>();
                    
                    for (String key : requirementsObj.keySet()) {
                        JsonElement element = requirementsObj.get(key);
                        if (element.isJsonPrimitive()) {
                            if (element.getAsJsonPrimitive().isNumber()) {
                                requirements.put(key, element.getAsInt());
                            } else {
                                requirements.put(key, element.getAsString());
                            }
                        }
                    }
                    
                    classInfo.put("stat-requirements", requirements);
                }
                
                classList.add(classInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return classList;
    }

    /**
     * Adds default multiclass prerequisites based on PHB rules if not specified in JSON, which they uh should be there
     */
    private void addDefaultMulticlassRequirements(Map<String, Object> classInfo, String className) {
        Map<String, Object> requirements = new HashMap<>();
        
        switch (className) {
            case "Artificer":
                requirements.put("INT", 13);
                break;
            case "Barbarian":
                requirements.put("STR", 13);
                break;
            case "Bard":
                requirements.put("CHA", 13);
                break;
            case "Cleric":
                requirements.put("WIS", 13);
                break;
            case "Druid":
                requirements.put("WIS", 13);
                break;
            case "Fighter":
                requirements.put("STR", 13);
                requirements.put("DEX", 13);
                requirements.put("requirement-type", "OR");
                break;
            case "Monk":
                requirements.put("DEX", 13);
                requirements.put("WIS", 13);
                break;
            case "Paladin":
                requirements.put("STR", 13);
                requirements.put("CHA", 13);
                break;
            case "Ranger":
                requirements.put("DEX", 13);
                requirements.put("WIS", 13);
                break;
            case "Rogue":
                requirements.put("DEX", 13);
                break;
            case "Sorcerer":
                requirements.put("CHA", 13);
                break;
            case "Warlock":
                requirements.put("CHA", 13);
                break;
            case "Wizard":
                requirements.put("INT", 13);
                break;
        }
        
        if (!requirements.isEmpty()) {
            classInfo.put("stat-requirements", requirements);
        }
    }

    public void setClassLevels() {
        if (maxClasses > 0) {
            // For the first class, we don't check stats (PHB rule)
            Map<String, Object> firstClass = getRandomClass(); //Sets the first class randomly
            String firstClassName = (String) firstClass.get("class"); 
            classLevels.put(firstClassName, 1); //Sets the level of the first class to one
            checkAndAssignSubclass(firstClass, firstClassName); //Check is we can get a subclass (Sorcerer and Warlock)
            
            System.out.println("First class selected: " + firstClassName);

            
            currentLevel++; //Level Up!
            
            // Additional classes must check stat requirements
            while (classLevels.size() < maxClasses && currentLevel <= level) {	
            	if (enforceMulticlassRequirements && characterStats != null && meetsStatRequirements(firstClass)) {
                    Map<String, Object> chosenClass = getNewUniqueClassWithStatCheck(); //Tries to get a class that we can use using stat check
                    if (chosenClass == null) { // If there are no classes left that we can take it breaks
                        // No more valid classes available - stop multiclassing
                        break;
                    }
                    
                    String className = (String) chosenClass.get("class"); // Get a class
                    classLevels.put(className, 1); // Set new class to level 1
                    checkAndAssignSubclass(chosenClass, className); //Sees if the class is high enough for a subclass
            	}
            	else
            		break;
            	currentLevel++; //Level Up!
            }
        }

        // Distribute remaining levels among existing classes and check to see if a subclass is available
        while (currentLevel <= level) {
            String className = selectExistingClass();
            Map<String, Object> classInfo = getClassInfoByName(className);
            classLevels.put(className, classLevels.get(className) + 1);
            checkAndAssignSubclass(classInfo, className);
            currentLevel++;
        }
    }

    private Map<String, Object> getNewUniqueClassWithStatCheck() {
        List<Map<String, Object>> availableClasses = new ArrayList<>();

        for (Map<String, Object> classInfo : classes) {
            String className = (String) classInfo.get("class");
            if (!classLevels.containsKey(className) && meetsStatRequirements(classInfo)) {
                availableClasses.add(classInfo);
            }
        }

        return availableClasses.isEmpty() ? null : availableClasses.get(random.nextInt(availableClasses.size()));
    }
    
    @SuppressWarnings("unchecked")
    private boolean meetsStatRequirements(Map<String, Object> classInfo) {
        // If no stats provided, all classes are valid (for backward compatibility)
        if (characterStats == null) {
            return true;
        }
        
        // If no requirements specified for this class, it's valid
        if (!classInfo.containsKey("stat-requirements")) {
            return true;
        }
        
        Map<String, Object> requirements = (Map<String, Object>) classInfo.get("stat-requirements");
        String requirementType = "AND"; // Default is AND
        
        if (requirements.containsKey("requirement-type")) {
            requirementType = (String) requirements.get("requirement-type");
        }
        
        // Get ability scores
        Map<String, Integer> abilityScores = characterStats.getAbilityScores();
        
        if ("OR".equals(requirementType)) {
            // Any one requirement must be met
            for (Map.Entry<String, Object> entry : requirements.entrySet()) {
                String ability = entry.getKey();
                if (ability.equals("requirement-type")) continue;
                
                int requiredValue = (Integer) entry.getValue();
                if (abilityScores.getOrDefault(ability, 0) >= requiredValue) {
                    return true;
                }
            }
            return false;
        } else {
            // All requirements must be met (AND)
            for (Map.Entry<String, Object> entry : requirements.entrySet()) {
                String ability = entry.getKey();
                if (ability.equals("requirement-type")) continue;
                
                int requiredValue = (Integer) entry.getValue();
                if (abilityScores.getOrDefault(ability, 0) < requiredValue) {
                    return false;
                }
            }
            return true;
        }
    }

    private void checkAndAssignSubclass(Map<String, Object> classInfo, String className) {
        long subclassLevelRequirement = (Long) classInfo.get("subclass-level");
        if (classLevels.get(className) >= subclassLevelRequirement) {
            @SuppressWarnings("unchecked")
            Map<String, String> subclasses = (Map<String, String>) classInfo.get("sub-classes");
            if (!subclasses.isEmpty() && !chosenSubclasses.containsKey(className)) {
                List<String> subclassNames = new ArrayList<>(subclasses.keySet());
                String chosenSubclass = subclassNames.get(random.nextInt(subclassNames.size()));
                chosenSubclasses.put(className, subclasses.get(chosenSubclass));
            }
        }
    }

    private String selectExistingClass() {
        List<String> keysAsArray = new ArrayList<>(classLevels.keySet());
        return keysAsArray.get(random.nextInt(keysAsArray.size()));
    }

    private Map<String, Object> getClassInfoByName(String className) {
        for (Map<String, Object> classInfo : classes) {
            if (classInfo.get("class").equals(className)) {
                return classInfo;
            }
        }
        return null;
    }

    public Map<String, Object> getRandomClass() {
        return classes.get(random.nextInt(classes.size()));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        
        // Print multiclass validation information if needed
        if (classLevels.size() > 1 && characterStats != null && enforceMulticlassRequirements) {
            boolean valid = validateMulticlassRequirements();
            if (!valid) {
                result.append("WARNING: This character does not meet multiclass requirements:\n");
                Map<String, String> failures = getMulticlassRequirementsFailures();
                for (Map.Entry<String, String> failure : failures.entrySet()) {
                    result.append("  - ").append(failure.getKey()).append(": ").append(failure.getValue()).append("\n");
                }
                result.append("\n");
            }
        }
        
        // Print class information
        for (Map.Entry<String, Integer> classLevelEntry : classLevels.entrySet()) {
            String className = classLevelEntry.getKey();
            Integer classLevel = classLevelEntry.getValue();
            result.append(className).append(" Level: ").append(classLevel).append("\n");
            if (chosenSubclasses.containsKey(className)) {
                String subclassName = chosenSubclasses.get(className);
                result.append("\tSubclass: ").append(subclassName).append("\n");
            }
        }
        return result.toString();
    }
    
    /**
     * Validates that the character meets all multiclass requirements
     */
    public boolean validateMulticlassRequirements() {
        if (characterStats == null || classLevels.size() <= 1 || !enforceMulticlassRequirements) {
            return true; // No validation needed
        }
        
        for (String className : classLevels.keySet()) {
            Map<String, Object> classInfo = getClassInfoByName(className);
            if (!meetsStatRequirements(classInfo)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gets detailed failure information for multiclass requirements
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getMulticlassRequirementsFailures() {
        Map<String, String> failures = new HashMap<>();
        if (characterStats == null) {
            return failures;
        }
        
        Map<String, Integer> abilityScores = characterStats.getAbilityScores();
        
        for (String className : classLevels.keySet()) {
            Map<String, Object> classInfo = getClassInfoByName(className);
            if (classInfo.containsKey("stat-requirements")) {
                Map<String, Object> requirements = (Map<String, Object>) classInfo.get("stat-requirements");
                String requirementType = requirements.containsKey("requirement-type") ? 
                                        (String)requirements.get("requirement-type") : "AND";
                
                StringBuilder failureReason = new StringBuilder();
                boolean requirementMet = "OR".equals(requirementType) ? false : true;
                
                for (Map.Entry<String, Object> entry : requirements.entrySet()) {
                    String ability = entry.getKey();
                    if (ability.equals("requirement-type")) continue;
                    
                    int requiredValue = (Integer) entry.getValue();
                    int actualValue = abilityScores.getOrDefault(ability, 0);
                    boolean meets = actualValue >= requiredValue;
                    
                    if ("OR".equals(requirementType)) {
                        requirementMet = requirementMet || meets;
                        if (!failureReason.isEmpty()) failureReason.append(" OR ");
                        failureReason.append(ability).append(" ").append(actualValue).append("/").append(requiredValue);
                    } else {
                        requirementMet = requirementMet && meets;
                        if (!meets) {
                            if (!failureReason.isEmpty()) failureReason.append(", ");
                            failureReason.append(ability).append(" ").append(actualValue).append("/").append(requiredValue);
                        }
                    }
                }
                
                if (!requirementMet) {
                    failures.put(className, failureReason.toString());
                }
            }
        }
        
        return failures;
    }
    
    // Get classes that failed stat requirements
    public List<String> getClassesFailedStatRequirements() {
        List<String> failedClasses = new ArrayList<>();
        
        if (characterStats == null) {
            return failedClasses; // No stats to check against
        }
        
        for (Map<String, Object> classInfo : classes) {
            String className = (String) classInfo.get("class");
            if (!meetsStatRequirements(classInfo)) {
                failedClasses.add(className);
            }
        }
        
        return failedClasses;
    }
}
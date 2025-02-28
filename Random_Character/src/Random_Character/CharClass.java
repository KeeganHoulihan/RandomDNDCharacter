

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
    private Random random = new Random(System.nanoTime()); // Randomizer
    private int level; // User-inputted level
    private int maxClasses; // Max number of classes the user wants
    private int currentLevel; // Current Level of the character
    private Map<String, Integer> classLevels; // Tracks levels for each class
    private Map<String, String> chosenSubclasses; // Tracks chosen subclass for each class
    private Stats characterStats; // Reference to character stats for multiclass requirements
    private boolean enforceMulticlassRequirements; // Flag to enforce multiclass requirements
    private boolean includeHomebrew; //If the user would like to use homebrew or not
    private String firstClassName; //Gets the name of the first class for proficiency purposes, all other classes are multiclass
    
    
    public CharClass(int inLevel, int inMaxClasses, Stats stats) throws IOException {
        this(inLevel, inMaxClasses, stats, true, false); // By default, enforce multiclass requirements (Always enforce)
    }

    public CharClass(int inLevel, int inMaxClasses, Stats stats, boolean enforceRequirements, boolean includeHomebrew) throws IOException {
        currentLevel = 1; // Minimum Level is 1
        classLevels = new HashMap<>(); //Create a hash map of class levels
        chosenSubclasses = new HashMap<>(); // Chosen subclasses must also be tracked
        characterStats = stats; // A characters stats are important
        enforceMulticlassRequirements = enforceRequirements; //Enforce multiclass rules
        this.includeHomebrew = includeHomebrew; 
        classes = loadCharClasses(); // Loads DND class data from JSON
        cclass = getRandomClass(); // Randomly selects the class
        level = inLevel; //The level the player selects
        maxClasses = inMaxClasses; //How many classes the player wants
        setClassLevels(); // Assigns levels to classes
    }

    // Overloaded constructor for backward compatibility
    public CharClass(int inLevel, int inMaxClasses) throws IOException {
        this(inLevel, inMaxClasses, null, false, false);
    }

    //Setter
    public void setCharClass(Map<String, Object> setCharClass) {
        cclass = setCharClass; 
    }

    //Setter
    public void setFirstClass(String firstClass)
    {
    	firstClassName = firstClass;
    }
    
    //Getter
    public String getFirstClass()
    {
    	return firstClassName;
    }
    
    //Getter
    public Map<String, Object> getCharClass() {
        return cclass;
    }
    
    //Getter
    public Map<String, Integer> getClassLevels() {
        return new HashMap<>(classLevels); // Return a copy of the map
    }
    
    
    /**
     * Loads the class data from "ClassList.json" using Gson.
     */
    public List<Map<String, Object>> loadCharClasses() throws IOException {
        List<Map<String, Object>> classList = new ArrayList<>(); //Map of classes the character has
        
        try (FileReader reader = new FileReader("ClassList.json")) { //File Reader
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray(); //Loads the class list as a JSON array
            
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject classObject = jsonArray.get(i).getAsJsonObject(); //Gets a the class through the JSON array
                
                // Check if the class itself is homebrew
                boolean isClassHomebrew = classObject.has("is-homebrew") && 
                                         classObject.get("is-homebrew").getAsBoolean();
                
                // Skip homebrew classes if homebrew is disabled
                if (isClassHomebrew && !includeHomebrew) {
                    continue;
                }
                
                String className = classObject.get("class").getAsString(); //Gets the class string from the JSON object
                JsonObject subclassesObj = classObject.getAsJsonObject("sub-classes"); //Gets the subclass list as a JSON object
                Map<String, String> subclasses = new HashMap<>(); //Create a hashmap for the sub classes
                
                // Parse homebrew subclass flags
                Map<String, Boolean> homebrewSubclasses = new HashMap<>(); //Create a new hashmap for homebrew subclasses
                if (classObject.has("homebrew-subclasses")) { //If a class has a homebrew subclass this will run (All of them have homebrew lol)
                    JsonObject homebrewObj = classObject.getAsJsonObject("homebrew-subclasses"); //Get homebrew classes as a JSON object
                    for (String key : homebrewObj.keySet()) { //For each key in the homebrew JSON object
                        if (homebrewObj.get(key).isJsonPrimitive()) { //Check to see if the homebrew key is a Primitive
                            homebrewSubclasses.put(key, homebrewObj.get(key).getAsBoolean()); //Get the primitive as a boolean
                        } else if (homebrewObj.get(key).isJsonObject()) { //If the key is a JSON object they it is not homebrew
                            JsonObject subclassObj = homebrewObj.get(key).getAsJsonObject(); //Get the key as a JSON object, it is a non homebrew subclass
                            if (subclassObj.has("enabled")) { //If the subclass is enabled TBH i think this doesn't do anything
                                homebrewSubclasses.put(key, subclassObj.get("enabled").getAsBoolean()); 
                            }
                        }
                    }
                }
                
                // Filter subclasses based on homebrew setting
                for (String key : subclassesObj.keySet()) { //For each key in the SubclassesOBJ 
                    boolean isSubclassHomebrew = homebrewSubclasses.getOrDefault(key, false); //The default is that a class is not homebrew, if it is homebrew, it will be labled as such in the JSON file
                    if (includeHomebrew || !isSubclassHomebrew) { //If the subclass is not hombrew
                        subclasses.put(key, subclassesObj.get(key).getAsString()); //Label it as not homebrew
                    }
                }
                
                long subclassLevel = classObject.get("subclass-level").getAsLong(); //Each class has a subclass level that is gotten as a long
                
                Map<String, Object> classInfo = new HashMap<>(); //Make a hashmap of classinfo and add all the the needed data
                classInfo.put("class", className);
                classInfo.put("sub-classes", subclasses);
                classInfo.put("subclass-level", subclassLevel);
                classInfo.put("is-homebrew", isClassHomebrew);
                classInfo.put("homebrew-subclasses", homebrewSubclasses);
                
                // Add multiclass requirements handling as in your original code
                addDefaultMulticlassRequirements(classInfo, className);
                
                if (classObject.has("stat-requirements")) { //If a class has stat requirments... ALL OF THEM LOL
                    JsonObject requirementsObj = classObject.getAsJsonObject("stat-requirements");  //Get stat requirements from the class object as a JSON object
                    Map<String, Object> requirements = new HashMap<>(); // Make a hash map of the stat requirements so we can go through them
                    
                    for (String key : requirementsObj.keySet()) { //For each key in the requirements key set
                        JsonElement element = requirementsObj.get(key); //Get the key
                        if (element.isJsonPrimitive()) { //Check if the key is a primitive
                            if (element.getAsJsonPrimitive().isNumber()) { //If the key is a number
                                requirements.put(key, element.getAsInt()); //Put the stat requirement into the requirements as an integer
                            } else {
                                requirements.put(key, element.getAsString()); //If it is a not a number put it in as a string
                            }
                        }
                    }
                    
                    classInfo.put("stat-requirements", requirements); //Put all stat requirements into the class info
                }
                
                classList.add(classInfo); // Ass the class info to the class list
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return classList; //Return the entire classlist 
    }

    /**
     * Adds default multiclass prerequisites based on PHB rules if not specified in JSON, which they uh should be there
     */
    private void addDefaultMulticlassRequirements(Map<String, Object> classInfo, String className) {
        Map<String, Object> requirements = new HashMap<>(); //Im being completely honest this should just be in the JSON file... why is it here?
        
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
        
        if (!requirements.isEmpty()) { //If a classes requirements are not empty 
            classInfo.put("stat-requirements", requirements);
        }
    }

    public void setClassLevels() {
        if (maxClasses > 0) {
            // For the first class, we don't check stats (PHB rule)
            Map<String, Object> firstClass = getRandomClass(); //Sets the first class randomly
            String firstClassName = (String) firstClass.get("class"); 
            setFirstClass(firstClassName);
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
    /**
     * Gets new unique classes for character using the multiclass requirements
     */
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
                String requirementType = requirements.containsKey("requirement-type") ? (String)requirements.get("requirement-type") : "AND";
                
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
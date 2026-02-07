package net.runelite.client.plugins.microbot.tutorialisland.managers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.microbot.Microbot;

import javax.inject.Singleton;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Singleton
public class NameGenerationManager {

    private static final String USED_NAMES_FILE = "microbot_used_names.txt";
    private static final int MAX_GENERATION_ATTEMPTS = 100;
    private static final int MIN_NUMBERS = 1;
    private static final int MAX_NUMBERS = 999;

    private final Set<String> usedNames = new HashSet<>();
    private final Random random = new Random();

    private static final String[] ADJECTIVES = {
        "Brave", "Swift", "Silent", "Mighty", "Clever", "Quick", "Bold", "Noble",
        "Fierce", "Calm", "Wise", "Dark", "Bright", "Ancient", "Young", "Old",
        "Strong", "Wild", "Free", "Lost", "Hidden", "True", "False", "Royal",
        "Lone", "Twin", "First", "Last", "Great", "Small", "Tall", "Short",
        "Deep", "High", "Far", "Near", "Cold", "Warm", "Cool", "Hot",
        "Sharp", "Dull", "Soft", "Hard", "Light", "Heavy", "Fast", "Slow"
    };

    private static final String[] NOUNS = {
        "Wolf", "Bear", "Eagle", "Lion", "Tiger", "Dragon", "Phoenix", "Hawk",
        "Raven", "Falcon", "Fox", "Deer", "Stag", "Owl", "Snake", "Panther",
        "Mountain", "River", "Ocean", "Forest", "Desert", "Valley", "Peak", "Lake",
        "Storm", "Thunder", "Lightning", "Rain", "Snow", "Wind", "Fire", "Earth",
        "Star", "Moon", "Sun", "Sky", "Cloud", "Dawn", "Dusk", "Night",
        "Sword", "Shield", "Arrow", "Blade", "Axe", "Hammer", "Bow", "Spear",
        "Knight", "Warrior", "Mage", "Archer", "Ranger", "Hunter", "Scout", "Guard"
    };

    public NameGenerationManager() {
        loadUsedNames();
    }

    public String generateUniqueName() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String name = generateRandomName();
            
            if (!usedNames.contains(name.toLowerCase())) {
                usedNames.add(name.toLowerCase());
                saveUsedNames();
                log.info("Generated unique name: {}", name);
                return name;
            }
        }

        log.error("Failed to generate unique name after {} attempts", MAX_GENERATION_ATTEMPTS);
        throw new RuntimeException("Unable to generate unique name");
    }

    private String generateRandomName() {
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        int number = random.nextInt(MAX_NUMBERS - MIN_NUMBERS + 1) + MIN_NUMBERS;
        
        return adjective + noun + number;
    }

    private void loadUsedNames() {
        Path filePath = getUsedNamesPath();
        
        if (!Files.exists(filePath)) {
            log.info("No existing used names file found, starting fresh");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    usedNames.add(trimmed);
                }
            }
            log.info("Loaded {} previously used names", usedNames.size());
        } catch (IOException e) {
            log.error("Error loading used names from file", e);
        }
    }

    private void saveUsedNames() {
        Path filePath = getUsedNamesPath();

        try {
            Files.createDirectories(filePath.getParent());

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                for (String name : usedNames) {
                    writer.write(name);
                    writer.newLine();
                }
            }
            log.debug("Saved {} used names to file", usedNames.size());
        } catch (IOException e) {
            log.error("Error saving used names to file", e);
        }
    }

    private Path getUsedNamesPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".runelite", "microbot", USED_NAMES_FILE);
    }

    public boolean isNameUsed(String name) {
        return usedNames.contains(name.toLowerCase());
    }

    public int getUsedNameCount() {
        return usedNames.size();
    }

    public void markNameAsUsed(String name) {
        usedNames.add(name.toLowerCase());
        saveUsedNames();
        log.info("Manually marked name as used: {}", name);
    }
}

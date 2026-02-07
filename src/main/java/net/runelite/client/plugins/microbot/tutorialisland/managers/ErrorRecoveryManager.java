package net.runelite.client.plugins.microbot.tutorialisland.managers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.microbot.Microbot;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Singleton
public class ErrorRecoveryManager {

    private static final int MAX_RETRIES = 10;
    
    private final Map<String, Integer> retryCountMap = new HashMap<>();
    private final Map<String, String> lastErrorMap = new HashMap<>();

    public boolean handleError(String actionIdentifier) {
        return handleError(actionIdentifier, "Unknown error");
    }

    public boolean handleError(String actionIdentifier, String errorMessage) {
        int currentRetries = retryCountMap.getOrDefault(actionIdentifier, 0);
        currentRetries++;
        retryCountMap.put(actionIdentifier, currentRetries);
        lastErrorMap.put(actionIdentifier, errorMessage);

        if (currentRetries <= MAX_RETRIES) {
            log.warn("Error on '{}': {} (Attempt {}/{})", 
                actionIdentifier, errorMessage, currentRetries, MAX_RETRIES);
            Microbot.log(String.format("Retrying %s (%d/%d)", 
                actionIdentifier, currentRetries, MAX_RETRIES));
            return true;
        } else {
            log.error("Max retries ({}) reached for '{}'. Last error: {}", 
                MAX_RETRIES, actionIdentifier, errorMessage);
            Microbot.log(String.format("FAILED: %s after %d attempts. Shutting down.", 
                actionIdentifier, MAX_RETRIES));
            return false;
        }
    }

    public void resetError(String actionIdentifier) {
        if (retryCountMap.containsKey(actionIdentifier)) {
            int previousRetries = retryCountMap.get(actionIdentifier);
            if (previousRetries > 0) {
                log.info("Action '{}' succeeded after {} retries", actionIdentifier, previousRetries);
            }
            retryCountMap.remove(actionIdentifier);
            lastErrorMap.remove(actionIdentifier);
        }
    }

    public void resetAll() {
        log.debug("Resetting all error counts");
        retryCountMap.clear();
        lastErrorMap.clear();
    }

    public int getRetryCount(String actionIdentifier) {
        return retryCountMap.getOrDefault(actionIdentifier, 0);
    }

    public boolean isRetrying(String actionIdentifier) {
        return retryCountMap.containsKey(actionIdentifier) && retryCountMap.get(actionIdentifier) > 0;
    }

    public String getDiagnostics() {
        if (retryCountMap.isEmpty()) {
            return "No errors";
        }

        StringBuilder sb = new StringBuilder("Current errors:\n");
        retryCountMap.forEach((action, count) -> {
            String lastError = lastErrorMap.getOrDefault(action, "Unknown");
            sb.append(String.format("  - %s: %d/%d retries (Last: %s)\n", 
                action, count, MAX_RETRIES, lastError));
        });
        return sb.toString();
    }
}

package net.runelite.client.plugins.microbot.tutorialisland;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.tutorialisland.enums.TutorialStage;
import net.runelite.client.plugins.microbot.tutorialisland.managers.CharacterCreationManager;
import net.runelite.client.plugins.microbot.tutorialisland.managers.NameGenerationManager;
import net.runelite.client.plugins.microbot.tutorialisland.managers.TutorialStageManager;
import net.runelite.client.plugins.microbot.tutorialisland.managers.PostTutorialManager;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TutorialIslandScript extends Script {

    @Getter
    private TutorialStage currentStage = TutorialStage.NOT_STARTED;
    
    private TutorialIslandConfig config;
    private boolean isRunning = false;
    private int stuckCheckCounter = 0;
    private static final int STUCK_CHECK_INTERVAL = 10;

    @Inject
    private NameGenerationManager nameGenerationManager;

    @Inject
    private CharacterCreationManager characterCreationManager;

    @Inject
    private TutorialStageManager tutorialStageManager;

    @Inject
    private PostTutorialManager postTutorialManager;

    public boolean run(TutorialIslandConfig config) {
        this.config = config;
        
        if (isRunning) {
            log.debug("Script already running");
            return true;
        }

        log.info("Starting Tutorial Island automation");
        Microbot.log("Tutorial Island Bot started!");
        isRunning = true;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) {
                    handleLoginScreen();
                    return;
                }

                if (Rs2Player.hasCompletedTutorialIsland()) {
                    handlePostTutorial();
                    return;
                }

                stuckCheckCounter++;
                if (stuckCheckCounter >= STUCK_CHECK_INTERVAL) {
                    stuckCheckCounter = 0;
                    if (postTutorialManager.isPlayerStuck()) {
                        log.warn("Player detected as stuck, attempting recovery");
                        postTutorialManager.attemptUnstuck(config);
                    }
                }

                progressTutorial();
                
            } catch (Exception e) {
                log.error("Error in Tutorial Island script", e);
                Microbot.log("Error: " + e.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);

        return true;
    }

    private void handleLoginScreen() {
        if (config.enableDebugLogging()) {
            log.debug("On login screen, checking for character creation");
        }

        if (characterCreationManager.isCharacterCreationOpen()) {
            currentStage = TutorialStage.CHARACTER_CREATION;
            
            String accountName = nameGenerationManager.generateUniqueName();
            log.info("Generated account name: {}", accountName);
            Microbot.log("Account name: " + accountName);
            
            boolean success = characterCreationManager.createFemaleCharacter();
            
            if (success) {
                log.info("Character creation completed successfully");
                Microbot.log("Character created!");
                currentStage = TutorialStage.GIELINOR_GUIDE;
            } else {
                log.warn("Character creation failed, will retry");
            }
        }
    }

    private void progressTutorial() {
        currentStage = tutorialStageManager.detectCurrentStage();
        
        if (config.enableDebugLogging()) {
            log.debug("Current tutorial stage: {}", currentStage);
        }

        boolean stageComplete = tutorialStageManager.handleStage(currentStage, config);
        
        if (!stageComplete) {
            log.warn("Stage {} did not complete successfully", currentStage);
        } else if (config.enableDebugLogging()) {
            log.debug("Stage {} progressing", currentStage);
        }
    }

    private void handlePostTutorial() {
        if (currentStage != TutorialStage.COMPLETED) {
            log.info("Tutorial Island completed!");
            Microbot.log("Tutorial Island complete!");
            currentStage = TutorialStage.COMPLETED;
        }

        if (!config.walkToFalador()) {
            log.info("Walk to Falador disabled. Shutting down.");
            Microbot.log("Tutorial complete - Bot stopped.");
            shutdown();
            return;
        }

        if (postTutorialManager.isPlayerStuck()) {
            log.warn("Player stuck during navigation to Falador");
            postTutorialManager.attemptUnstuck(config);
            return;
        }

        boolean reachedBank = postTutorialManager.walkToFaladorBank(config);
        
        if (reachedBank) {
            log.info("Successfully reached Falador bank. Tutorial Island automation complete!");
            Microbot.log("Reached Falador bank - Bot stopped.");
            shutdown();
        } else if (config.enableDebugLogging()) {
            String status = postTutorialManager.getStatusMessage();
            int progress = postTutorialManager.getProgressPercentage();
            log.debug("Navigation progress: {}% - {}", progress, status);
        }
    }

    @Override
    public void shutdown() {
        log.info("Shutting down Tutorial Island script");
        Microbot.log("Tutorial Island Bot stopped.");
        isRunning = false;
        super.shutdown();
    }

    public boolean isRunning() {
        return isRunning;
    }
}

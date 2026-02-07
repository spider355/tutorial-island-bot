package net.runelite.client.plugins.microbot.tutorialisland.stages;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialIslandConfig;
import net.runelite.client.plugins.microbot.tutorialisland.managers.ErrorRecoveryManager;
import net.runelite.client.plugins.microbot.tutorialisland.managers.IronmanAccountManager;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Slf4j
@Singleton
public class FinalInstructorStage implements IStageHandler {

    @Inject
    private ErrorRecoveryManager errorRecovery;

    @Inject
    private IronmanAccountManager ironmanAccountManager;

    private static final String NPC_FINAL_INSTRUCTOR = "Skippy";
    private static final int DOOR_ID = 9398;

    @Override
    public boolean execute(TutorialIslandConfig config) {
        try {
            if (Rs2Dialogue.isInDialogue()) {
                handleDialogue(config);
                return true;
            }

            if (ironmanAccountManager.isAccountSelectionOpen()) {
                return handleAccountSelection(config);
            }

            if (shouldTalkToInstructor()) {
                return talkToFinalInstructor(config);
            }

            return exitArea(config);

        } catch (Exception e) {
            log.error("Error in Final Instructor stage", e);
            return false;
        }
    }

    private boolean talkToFinalInstructor(TutorialIslandConfig config) {
        log.debug("Talking to Final Instructor");

        if (Rs2Npc.interact(NPC_FINAL_INSTRUCTOR, "Talk-to")) {
            sleep(config.actionDelay());
            
            boolean dialogueOpened = sleepUntil(Rs2Dialogue::isInDialogue, 3000);
            
            if (dialogueOpened) {
                errorRecovery.resetError("talkToFinalInstructor");
                return true;
            }
        }

        return errorRecovery.handleError("talkToFinalInstructor", 
            "Failed to talk to Final Instructor");
    }

    private boolean handleAccountSelection(TutorialIslandConfig config) {
        log.debug("Handling account selection interface");

        boolean success = ironmanAccountManager.selectAccountType(config);

        if (success) {
            log.info("Account type selected successfully");
            errorRecovery.resetError("handleAccountSelection");
            return true;
        }

        return errorRecovery.handleError("handleAccountSelection", 
            "Failed to select account type");
    }

    private boolean exitArea(TutorialIslandConfig config) {
        log.debug("Exiting Tutorial Island");

        if (Rs2GameObject.interact(DOOR_ID, "Open")) {
            sleep(config.actionDelay());
            sleep(1000);
            errorRecovery.resetError("exitArea");
            return true;
        }

        return errorRecovery.handleError("exitArea", "Failed to open door");
    }

    private void handleDialogue(TutorialIslandConfig config) {
        if (Rs2Dialogue.hasContinue()) {
            Rs2Dialogue.clickContinue();
            sleep(config.randomizeDelay() ? 
                config.actionDelay() + (int)(Math.random() * 200) : 
                config.actionDelay());
        }

        if (Rs2Dialogue.hasSelectAnOption()) {
            Rs2Dialogue.keyPressForDialogueOption(1);
            sleep(config.actionDelay());
        }
    }

    private boolean shouldTalkToInstructor() {
        return true;
    }
}

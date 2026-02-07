package net.runelite.client.plugins.microbot.tutorialisland.stages;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialIslandConfig;
import net.runelite.client.plugins.microbot.tutorialisland.managers.ErrorRecoveryManager;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Slf4j
@Singleton
public class QuestGuideStage implements IStageHandler {

    @Inject
    private ErrorRecoveryManager errorRecovery;

    private static final String NPC_QUEST_GUIDE = "Quest Guide";
    private static final int LADDER_ID = 9727;

    @Override
    public boolean execute(TutorialIslandConfig config) {
        try {
            if (Rs2Dialogue.isInDialogue()) {
                handleDialogue(config);
                return true;
            }

            if (shouldTalkToGuide()) {
                return talkToQuestGuide(config);
            }

            if (needsToOpenQuestTab()) {
                return openQuestTab(config);
            }

            if (needsToOpenSettings()) {
                return openSettings(config);
            }

            return exitArea(config);

        } catch (Exception e) {
            log.error("Error in Quest Guide stage", e);
            return false;
        }
    }

    private boolean talkToQuestGuide(TutorialIslandConfig config) {
        log.debug("Talking to Quest Guide");

        if (Rs2Npc.interact(NPC_QUEST_GUIDE, "Talk-to")) {
            sleep(config.actionDelay());
            
            boolean dialogueOpened = sleepUntil(Rs2Dialogue::isInDialogue, 3000);
            
            if (dialogueOpened) {
                errorRecovery.resetError("talkToQuestGuide");
                return true;
            }
        }

        return errorRecovery.handleError("talkToQuestGuide", 
            "Failed to talk to Quest Guide");
    }

    private boolean openQuestTab(TutorialIslandConfig config) {
        log.debug("Opening Quest Journal tab");

        if (Rs2Tab.switchToQuestTab()) {
            sleep(config.actionDelay());
            errorRecovery.resetError("openQuestTab");
            return true;
        }

        return errorRecovery.handleError("openQuestTab", 
            "Failed to open Quest tab");
    }

    private boolean openSettings(TutorialIslandConfig config) {
        log.debug("Opening Settings tab");

        if (Rs2Tab.switchToSettingsTab()) {
            sleep(config.actionDelay());
            errorRecovery.resetError("openSettings");
            return true;
        }

        return errorRecovery.handleError("openSettings", 
            "Failed to open Settings tab");
    }

    private boolean exitArea(TutorialIslandConfig config) {
        log.debug("Exiting Quest Guide area");

        if (Rs2GameObject.interact(LADDER_ID, "Climb-down")) {
            sleep(config.actionDelay());
            sleep(1500);
            errorRecovery.resetError("exitArea");
            return true;
        }

        return errorRecovery.handleError("exitArea", "Failed to climb ladder");
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

    private boolean shouldTalkToGuide() {
        return false;
    }

    private boolean needsToOpenQuestTab() {
        return false;
    }

    private boolean needsToOpenSettings() {
        return false;
    }
}

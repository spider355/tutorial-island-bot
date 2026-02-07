package net.runelite.client.plugins.microbot.tutorialisland.stages;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialIslandConfig;
import net.runelite.client.plugins.microbot.tutorialisland.managers.ErrorRecoveryManager;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Slf4j
@Singleton
public class GielinorGuideStage implements IStageHandler {

    @Inject
    private ErrorRecoveryManager errorRecovery;

    private static final String NPC_GIELINOR_GUIDE = "Gielinor Guide";
    private static final int DOOR_ID = 9398;

    @Override
    public boolean execute(TutorialIslandConfig config) {
        try {
            if (Rs2Dialogue.isInDialogue()) {
                handleDialogue(config);
                return true;
            }

            if (shouldTalkToGuide()) {
                return talkToGielinorGuide(config);
            }

            return exitArea(config);

        } catch (Exception e) {
            log.error("Error in Gielinor Guide stage", e);
            return false;
        }
    }

    private boolean talkToGielinorGuide(TutorialIslandConfig config) {
        log.debug("Talking to Gielinor Guide");

        if (Rs2Npc.interact(NPC_GIELINOR_GUIDE, "Talk-to")) {
            sleep(config.actionDelay());
            
            boolean dialogueOpened = sleepUntil(Rs2Dialogue::isInDialogue, 3000);
            
            if (dialogueOpened) {
                errorRecovery.resetError("talkToGielinorGuide");
                return true;
            }
        }

        return errorRecovery.handleError("talkToGielinorGuide", 
            "Failed to talk to Gielinor Guide");
    }

    private boolean exitArea(TutorialIslandConfig config) {
        log.debug("Exiting Gielinor Guide area");

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

    private boolean shouldTalkToGuide() {
        return !Rs2Dialogue.isInDialogue();
    }
}

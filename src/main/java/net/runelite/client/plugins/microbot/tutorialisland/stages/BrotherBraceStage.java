package net.runelite.client.plugins.microbot.tutorialisland.stages;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialIslandConfig;
import net.runelite.client.plugins.microbot.tutorialisland.managers.ErrorRecoveryManager;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Slf4j
@Singleton
public class BrotherBraceStage implements IStageHandler {

    @Inject
    private ErrorRecoveryManager errorRecovery;

    private static final String NPC_BROTHER_BRACE = "Brother Brace";
    private static final int DOOR_ID = 9722;

    @Override
    public boolean execute(TutorialIslandConfig config) {
        try {
            if (Rs2Dialogue.isInDialogue()) {
                handleDialogue(config);
                return true;
            }

            if (shouldTalkToBrother()) {
                return talkToBrotherBrace(config);
            }

            if (needsToOpenPrayer()) {
                return openPrayerTab(config);
            }

            if (needsToBuryBones()) {
                return buryBones(config);
            }

            return exitArea(config);

        } catch (Exception e) {
            log.error("Error in Brother Brace stage", e);
            return false;
        }
    }

    private boolean talkToBrotherBrace(TutorialIslandConfig config) {
        log.debug("Talking to Brother Brace");

        if (Rs2Npc.interact(NPC_BROTHER_BRACE, "Talk-to")) {
            sleep(config.actionDelay());
            
            boolean dialogueOpened = sleepUntil(Rs2Dialogue::isInDialogue, 3000);
            
            if (dialogueOpened) {
                errorRecovery.resetError("talkToBrotherBrace");
                return true;
            }
        }

        return errorRecovery.handleError("talkToBrotherBrace", 
            "Failed to talk to Brother Brace");
    }

    private boolean openPrayerTab(TutorialIslandConfig config) {
        log.debug("Opening Prayer tab");

        if (Rs2Tab.switchToPrayerTab()) {
            sleep(config.actionDelay());
            errorRecovery.resetError("openPrayerTab");
            return true;
        }

        return errorRecovery.handleError("openPrayerTab", 
            "Failed to open Prayer tab");
    }

    private boolean buryBones(TutorialIslandConfig config) {
        log.debug("Burying bones");

        if (Rs2Inventory.interact(ItemID.BONES, "Bury")) {
            sleep(config.actionDelay());
            
            boolean buried = sleepUntil(() -> 
                !Rs2Inventory.hasItem(ItemID.BONES), 3000);
            
            if (buried) {
                log.debug("Bones buried successfully");
                errorRecovery.resetError("buryBones");
                return true;
            }
        }

        return errorRecovery.handleError("buryBones", "Failed to bury bones");
    }

    private boolean exitArea(TutorialIslandConfig config) {
        log.debug("Exiting Brother Brace area");

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

        if (Rs2Dialogue.hasSelectAn*


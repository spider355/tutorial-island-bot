package net.runelite.client.plugins.microbot.tutorialisland.stages;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialIslandConfig;
import net.runelite.client.plugins.microbot.tutorialisland.managers.ErrorRecoveryManager;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Slf4j
@Singleton
public class MasterChefStage implements IStageHandler {

    @Inject
    private ErrorRecoveryManager errorRecovery;

    private static final String NPC_MASTER_CHEF = "Master Chef";
    private static final int RANGE_ID = 9736;
    private static final int DOOR_ID = 9710;

    @Override
    public boolean execute(TutorialIslandConfig config) {
        try {
            if (Rs2Dialogue.isInDialogue()) {
                handleDialogue(config);
                return true;
            }

            if (shouldTalkToChef()) {
                return talkToMasterChef(config);
            }

            if (needsToDough()) {
                return makeBreadDough(config);
            }

            if (needsToCookBread()) {
                return cookBread(config);
            }

            return exitArea(config);

        } catch (Exception e) {
            log.error("Error in Master Chef stage", e);
            return false;
        }
    }

    private boolean talkToMasterChef(TutorialIslandConfig config) {
        log.debug("Talking to Master Chef");

        if (Rs2Npc.interact(NPC_MASTER_CHEF, "Talk-to")) {
            sleep(config.actionDelay());
            
            boolean dialogueOpened = sleepUntil(Rs2Dialogue::isInDialogue, 3000);
            
            if (dialogueOpened) {
                errorRecovery.resetError("talkToMasterChef");
                return true;
            }
        }

        return errorRecovery.handleError("talkToMasterChef", 
            "Failed to talk to Master Chef");
    }

    private boolean makeBreadDough(TutorialIslandConfig config) {
        log.debug("Making bread dough");

        if (!Rs2Inventory.hasItem(ItemID.POT_OF_FLOUR) || 
            !Rs2Inventory.hasItem(ItemID.BUCKET_OF_WATER)) {
            log.warn("Missing ingredients for bread dough");
            return false;
        }

        if (Rs2Inventory.combine(ItemID.POT_OF_FLOUR, ItemID.BUCKET_OF_WATER)) {
            sleep(config.actionDelay());
            
            boolean success = sleepUntil(() -> 
                Rs2Inventory.hasItem(ItemID.BREAD_DOUGH), 3000);
            
            if (success) {
                log.debug("Bread dough created successfully");
                errorRecovery.resetError("makeBreadDough");
                return true;
            }
        }

        return errorRecovery.handleError("makeBreadDough", 
            "Failed to make bread dough");
    }

    private boolean cookBread(TutorialIslandConfig config) {
        log.debug("Cooking bread");

        if (Rs2Inventory.useItemOnObject(ItemID.BREAD_DOUGH, "Range")) {
            sleep(config.actionDelay());
            
            boolean cooked = sleepUntil(() -> 
                Rs2Inventory.hasItem(ItemID.BREAD) || 
                Rs2Inventory.hasItem(ItemID.BURNT_BREAD), 5000);
            
            if (cooked) {
                log.debug("Bread cooked successfully");
                errorRecovery.resetError("cookBread");
                return true;
            }
        }

        return errorRecovery.handleError("cookBread", "Failed to cook bread");
    }

    private boolean exitArea(TutorialIslandConfig config) {
        log.debug("Exiting Master Chef area");

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

    private boolean shouldTalkToChef() {
        return !Rs2Inventory.hasItem(ItemID.POT_OF_FLOUR) && 
               !Rs2Inventory.hasItem(ItemID.BUCKET_OF_WATER);
    }

    private boolean needsToDough() {
        return Rs2Inventory.hasItem(ItemID.POT_OF_FLOUR) && 
               Rs2Inventory.hasItem(ItemID.BUCKET_OF_WATER) &&
               !Rs2Inventory.hasItem(ItemID.BREAD_DOUGH);
    }

    private boolean needsToCookBread() {
        return Rs2Inventory.hasItem(ItemID.BREAD_DOUGH);
    }
}

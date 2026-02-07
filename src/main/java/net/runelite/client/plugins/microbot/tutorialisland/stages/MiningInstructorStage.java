package net.runelite.client.plugins.microbot.tutorialisland.stages;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialIslandConfig;
import net.runelite.client.plugins.microbot.tutorialisland.managers.ErrorRecoveryManager;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Slf4j
@Singleton
public class MiningInstructorStage implements IStageHandler {

    @Inject
    private ErrorRecoveryManager errorRecovery;

    private static final String NPC_MINING_INSTRUCTOR = "Mining Instructor";
    private static final int COPPER_ROCKS_ID = 10079;
    private static final int TIN_ROCKS_ID = 10080;
    private static final int FURNACE_ID = 10082;
    private static final int ANVIL_ID = 10083;
    private static final int GATE_ID = 9720;

    @Override
    public boolean execute(TutorialIslandConfig config) {
        try {
            if (Rs2Dialogue.isInDialogue()) {
                handleDialogue(config);
                return true;
            }

            if (shouldTalkToInstructor()) {
                return talkToMiningInstructor(config);
            }

            if (needsCopperOre()) {
                return mineCopperOre(config);
            }

            if (needsTinOre()) {
                return mineTinOre(config);
            }

            if (needsToSmeltBar()) {
                return smeltBronzeBar(config);
            }

            if (needsToSmithDagger()) {
                return smithBronzeDagger(config);
            }

            return exitArea(config);

        } catch (Exception e) {
            log.error("Error in Mining Instructor stage", e);
            return false;
        }
    }

    private boolean talkToMiningInstructor(TutorialIslandConfig config) {
        log.debug("Talking to Mining Instructor");

        if (Rs2Npc.interact(NPC_MINING_INSTRUCTOR, "Talk-to")) {
            sleep(config.actionDelay());
            
            boolean dialogueOpened = sleepUntil(Rs2Dialogue::isInDialogue, 3000);
            
            if (dialogueOpened) {
                errorRecovery.resetError("talkToMiningInstructor");
                return true;
            }
        }

        return errorRecovery.handleError("talkToMiningInstructor", 
            "Failed to talk to Mining Instructor");
    }

    private boolean mineCopperOre(TutorialIslandConfig config) {
        log.debug("Mining copper ore");

        if (Rs2Player.isAnimating()) {
            log.debug("Already mining, waiting...");
            sleep(1000);
            return true;
        }

        if (Rs2GameObject.interact(COPPER_ROCKS_ID, "Mine")) {
            sleep(config.actionDelay());
            
            boolean started = sleepUntil(Rs2Player::isAnimating, 3000);
            
            if (started) {
                sleepUntil(() -> Rs2Inventory.hasItem(ItemID.COPPER_ORE), 15000);
                errorRecovery.resetError("mineCopperOre");
                return true;
            }
        }

        return errorRecovery.handleError("mineCopperOre", "Failed to mine copper");
    }

    private boolean mineTinOre(TutorialIslandConfig config) {
        log.debug("Mining tin ore");

        if (Rs2Player.isAnimating()) {
            log.debug("Already mining, waiting...");
            sleep(1000);
            return true;
        }

        if (Rs2GameObject.interact(TIN_ROCKS_ID, "Mine")) {
            sleep(config.actionDelay());
            
            boolean started = sleepUntil(Rs2Player::isAnimating, 3000);
            
            if (started) {
                sleepUntil(() -> Rs2Inventory.hasItem(ItemID.TIN_ORE), 15000);
                errorRecovery.resetError("mineTinOre");
                return true;
            }
        }

        return errorRecovery.handleError("mineTinOre", "Failed to mine tin");
    }

    private boolean smeltBronzeBar(TutorialIslandConfig config) {
        log.debug("Smelting bronze bar");

        if (!Rs2Inventory.hasItem(ItemID.COPPER_ORE) || 
            !Rs2Inventory.hasItem(ItemID.TIN_ORE)) {
            log.warn("Missing ores for smelting");
            return false;
        }

        if (Rs2GameObject.interact(FURNACE_ID, "Use")) {
            sleep(config.actionDelay());
            
            boolean success = sleepUntil(() -> 
                Rs2Inventory.hasItem(ItemID.BRONZE_BAR), 5000);
            
            if (success) {
                log.debug("Bronze bar smelted successfully");
                errorRecovery.resetError("smeltBronzeBar");
                return true;
            }
        }

        return errorRecovery.handleError("smeltBronzeBar", 
            "Failed to smelt bronze bar");
    }

    private boolean smithBronzeDagger(TutorialIslandConfig config) {
        log.debug("Smithing bronze dagger");

        if (!Rs2Inventory.hasItem(ItemID.BRONZE_BAR)) {
            log.warn("No bronze bar to smith");
            return false;
        }

        if (Rs2GameObject.interact(ANVIL_ID, "Smith")) {
            sleep(config.actionDelay());
            
            sleep(1000);
            
            Rs2Widget.clickWidget(312, 9);
            
            sleep(config.actionDelay());
            
            boolean success = sleepUntil(() -> 
                Rs2Inventory.hasItem(ItemID.BRONZE_DAGGER), 5000);
            
            if (success) {
                log.debug("Bronze dagger smithed successfully");
                errorRecovery.resetError("smithBronzeDagger");
                return true;
            }
        }

        return errorRecovery.handleError("smithBronzeDagger", 
            "Failed to smith bronze dagger");
    }

    private boolean exitArea(TutorialIslandConfig config) {
        log.debug("Exiting Mining Instructor area");

        if (Rs2GameObject.interact(GATE_ID, "Open")) {
            sleep(config.actionDelay());
            sleep(1000);
            errorRecovery.resetError("exitArea");
            return true;
        }

        return errorRecovery.handleError("exitArea", "Failed to open gate");
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
        return !Rs2Inventory.hasItem(ItemID.BRONZE_PICKAXE);
    }

    private boolean needsCopperOre() {
        return Rs2Inventory.hasItem(ItemID.BRONZE_PICKAXE) && 
               !Rs2Inventory.hasItem(ItemID.COPPER_ORE) &&
               !Rs2Inventory.hasItem(ItemID.BRONZE_BAR);
    }

    private boolean needsTinOre() {
        return Rs2Inventory.hasItem(ItemID.COPPER_ORE) && 
               !Rs2Inventory.hasItem(ItemID.TIN_ORE) &&
               !Rs2Inventory.hasItem(ItemID.BRONZE_BAR);
    }

    private boolean needsToSmeltBar() {
        return Rs2Inventory.hasItem(ItemID.COPPER_ORE) && 
               Rs2Inventory.hasItem(ItemID.TIN_ORE) &&
               !Rs2Inventory.hasItem(ItemID.BRONZE_BAR);
    }

    private boolean needsToSmithDagger() {
        return Rs2Inventory.hasItem(ItemID.BRONZE_BAR) &&
               !Rs2Inventory.hasItem(ItemID.BRONZE_DAGGER);
    }
}

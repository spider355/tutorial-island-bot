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

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Slf4j
@Singleton
public class SurvivalExpertStage implements IStageHandler {

    @Inject
    private ErrorRecoveryManager errorRecovery;

    private static final String NPC_SURVIVAL_EXPERT = "Survival Expert";
    private static final int TREE_ID = 9730;
    private static final int FISHING_SPOT_ID = 10091;
    private static final int GATE_ID = 9716;

    @Override
    public boolean execute(TutorialIslandConfig config) {
        try {
            if (Rs2Dialogue.isInDialogue()) {
                handleDialogue(config);
                return true;
            }

            if (shouldTalkToExpert()) {
                return talkToSurvivalExpert(config);
            }

            if (needsLogs()) {
                return chopTree(config);
            }

            if (needsToLightFire()) {
                return lightFire(config);
            }

            if (needsShrimp()) {
                return fishShrimp(config);
            }

            if (needsToCookShrimp()) {
                return cookShrimp(config);
            }

            return exitArea(config);

        } catch (Exception e) {
            log.error("Error in Survival Expert stage", e);
            return false;
        }
    }

    private boolean talkToSurvivalExpert(TutorialIslandConfig config) {
        log.debug("Talking to Survival Expert");

        if (Rs2Npc.interact(NPC_SURVIVAL_EXPERT, "Talk-to")) {
            sleep(config.actionDelay());
            
            boolean dialogueOpened = sleepUntil(Rs2Dialogue::isInDialogue, 3000);
            
            if (dialogueOpened) {
                errorRecovery.resetError("talkToSurvivalExpert");
                return true;
            }
        }

        return errorRecovery.handleError("talkToSurvivalExpert", 
            "Failed to talk to Survival Expert");
    }

    private boolean chopTree(TutorialIslandConfig config) {
        log.debug("Chopping tree");

        if (Rs2Player.isAnimating()) {
            log.debug("Already chopping tree, waiting...");
            sleep(1000);
            return true;
        }

        if (Rs2GameObject.interact(TREE_ID, "Chop down")) {
            sleep(config.actionDelay());
            
            boolean started = sleepUntil(Rs2Player::isAnimating, 3000);
            
            if (started) {
                sleepUntil(() -> Rs2Inventory.hasItem(ItemID.LOGS), 10000);
                errorRecovery.resetError("chopTree");
                return true;
            }
        }

        return errorRecovery.handleError("chopTree", "Failed to chop tree");
    }

    private boolean lightFire(TutorialIslandConfig config) {
        log.debug("Lighting fire");

        if (Rs2Inventory.combine(ItemID.TINDERBOX, ItemID.LOGS)) {
            sleep(config.actionDelay());
            
            boolean started = sleepUntil(Rs2Player::isAnimating, 2000);
            
            if (started) {
                sleepUntil(() -> !Rs2Inventory.hasItem(ItemID.LOGS), 8000);
                log.debug("Fire lit successfully");
                errorRecovery.resetError("lightFire");
                return true;
            }
        }

        return errorRecovery.handleError("lightFire", "Failed to light fire");
    }

    private boolean fishShrimp(TutorialIslandConfig config) {
        log.debug("Fishing shrimp");

        if (Rs2Player.isAnimating()) {
            log.debug("Already fishing, waiting...");
            sleep(1000);
            return true;
        }

        if (Rs2Npc.interact(FISHING_SPOT_ID, "Net")) {
            sleep(config.actionDelay());
            
            boolean started = sleepUntil(Rs2Player::isAnimating, 3000);
            
            if (started) {
                sleepUntil(() -> Rs2Inventory.hasItem(ItemID.RAW_SHRIMPS), 15000);
                errorRecovery.resetError("fishShrimp");
                return true;
            }
        }

        return errorRecovery.handleError("fishShrimp", "Failed to fish shrimp");
    }

    private boolean cookShrimp(TutorialIslandConfig config) {
        log.debug("Cooking shrimp");

        if (Rs2Inventory.useItemOnObject(ItemID.RAW_SHRIMPS, "Fire")) {
            sleep(config.actionDelay());
            
            boolean cooked = sleepUntil(() -> 
                Rs2Inventory.hasItem(ItemID.SHRIMPS) || 
                Rs2Inventory.hasItem(ItemID.BURNT_SHRIMP), 5000);
            
            if (cooked) {
                log.debug("Shrimp cooked successfully");
                errorRecovery.resetError("cookShrimp");
                return true;
            }
        }

        return errorRecovery.handleError("cookShrimp", "Failed to cook shrimp");
    }

    private boolean exitArea(TutorialIslandConfig config) {
        log.debug("Exiting Survival Expert area");

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

    private boolean shouldTalkToExpert() {
        return !Rs2Inventory.hasItem(ItemID.BRONZE_AXE) && 
               !Rs2Inventory.hasItem(ItemID.TINDERBOX);
    }

    private boolean needsLogs() {
        return Rs2Inventory.hasItem(ItemID.BRONZE_AXE) && 
               !Rs2Inventory.hasItem(ItemID.LOGS);
    }

    private boolean needsToLightFire() {
        return Rs2Inventory.hasItem(ItemID.LOGS) && 
               Rs2Inventory.hasItem(ItemID.TINDERBOX);
    }

    private boolean needsShrimp() {
        return Rs2Inventory.hasItem(ItemID.SMALL_FISHING_NET) && 
               !Rs2Inventory.hasItem(ItemID.RAW_SHRIMPS) &&
               !Rs2Inventory.hasItem(ItemID.SHRIMPS);
    }

    private boolean needsToCookShrimp() {
        return Rs2Inventory.hasItem(ItemID.RAW_SHRIMPS);
    }
}

package net.runelite.client.plugins.microbot.tutorialisland.stages;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialIslandConfig;
import net.runelite.client.plugins.microbot.tutorialisland.managers.ErrorRecoveryManager;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Slf4j
@Singleton
public class CombatInstructorStage implements IStageHandler {

    @Inject
    private ErrorRecoveryManager errorRecovery;

    private static final String NPC_COMBAT_INSTRUCTOR = "Combat Instructor";
    private static final int GIANT_RAT_ID = NpcID.GIANT_RAT_2;
    private static final int LADDER_ID = 9726;

    @Override
    public boolean execute(TutorialIslandConfig config) {
        try {
            if (Rs2Dialogue.isInDialogue()) {
                handleDialogue(config);
                return true;
            }

            if (shouldTalkToInstructor()) {
                return talkToCombatInstructor(config);
            }

            if (needsToOpenEquipment()) {
                return openEquipmentTab(config);
            }

            if (needsToEquipDagger()) {
                return equipDagger(config);
            }

            if (needsToEquipShield()) {
                return equipShield(config);
            }

            if (needsToOpenCombat()) {
                return openCombatTab(config);
            }

            if (needsToKillRat()) {
                return killGiantRat(config);
            }

            return exitArea(config);

        } catch (Exception e) {
            log.error("Error in Combat Instructor stage", e);
            return false;
        }
    }

    private boolean talkToCombatInstructor(TutorialIslandConfig config) {
        log.debug("Talking to Combat Instructor");

        if (Rs2Npc.interact(NPC_COMBAT_INSTRUCTOR, "Talk-to")) {
            sleep(config.actionDelay());
            
            boolean dialogueOpened = sleepUntil(Rs2Dialogue::isInDialogue, 3000);
            
            if (dialogueOpened) {
                errorRecovery.resetError("talkToCombatInstructor");
                return true;
            }
        }

        return errorRecovery.handleError("talkToCombatInstructor", 
            "Failed to talk to Combat Instructor");
    }

    private boolean openEquipmentTab(TutorialIslandConfig config) {
        log.debug("Opening Equipment tab");

        if (Rs2Tab.switchToEquipmentTab()) {
            sleep(config.actionDelay());
            errorRecovery.resetError("openEquipmentTab");
            return true;
        }

        return errorRecovery.handleError("openEquipmentTab", 
            "Failed to open Equipment tab");
    }

    private boolean equipDagger(TutorialIslandConfig config) {
        log.debug("Equipping bronze dagger");

        if (Rs2Inventory.wield(ItemID.BRONZE_DAGGER)) {
            sleep(config.actionDelay());
            
            boolean equipped = sleepUntil(() -> 
                Rs2Equipment.isWearing(ItemID.BRONZE_DAGGER), 3000);
            
            if (equipped) {
                log.debug("Bronze dagger equipped");
                errorRecovery.resetError("equipDagger");
                return true;
            }
        }

        return errorRecovery.handleError("equipDagger", 
            "Failed to equip bronze dagger");
    }

    private boolean equipShield(TutorialIslandConfig config) {
        log.debug("Equipping wooden shield");

        if (Rs2Inventory.wield(ItemID.WOODEN_SHIELD)) {
            sleep(config.actionDelay());
            
            boolean equipped = sleepUntil(() -> 
                Rs2Equipment.isWearing(ItemID.WOODEN_SHIELD), 3000);
            
            if (equipped) {
                log.debug("Wooden shield equipped");
                errorRecovery.resetError("equipShield");
                return true;
            }
        }

        return errorRecovery.handleError("equipShield", 
            "Failed to equip wooden shield");
    }

    private boolean openCombatTab(TutorialIslandConfig config) {
        log.debug("Opening Combat tab");

        if (Rs2Tab.switchToCombatOptionsTab()) {
            sleep(config.actionDelay());
            errorRecovery.resetError("openCombatTab");
            return true;
        }

        return errorRecovery.handleError("openCombatTab", 
            "Failed to open Combat tab");
    }

    private boolean killGiantRat(TutorialIslandConfig config) {
        log.debug("Attacking giant rat");

        if (Rs2Player.isInCombat()) {
            log.debug("Already in combat, waiting...");
            
            sleepUntil(() -> !Rs2Player.isInCombat(), 30000);
            errorRecovery.resetError("killGiantRat");
            return true;
        }

        if (Rs2Npc.interact(GIANT_RAT_ID, "Attack")) {
            sleep(config.actionDelay());
            
            boolean inCombat = sleepUntil(Rs2Player::isInCombat, 5000);
            
            if (inCombat) {
                log.debug("Engaged in combat with rat");
                sleepUntil(() -> !Rs2Player.isInCombat(), 30000);
                errorRecovery.resetError("killGiantRat");
                return true;
            }
        }

        return errorRecovery.handleError("killGiantRat", 
            "Failed to attack giant rat");
    }

    private boolean exitArea(TutorialIslandConfig config) {
        log.debug("Exiting Combat Instructor area");

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

    private boolean shouldTalkToInstructor() {
        return !Rs2Inventory.hasItem(ItemID.WOODEN_SHIELD) &&
               !Rs2Equipment.isWearing(ItemID.WOODEN_SHIELD);
    }

    private boolean needsToOpenEquipment() {
        return Rs2Inventory.hasItem(ItemID.BRONZE_DAGGER) && 
               !Rs2Equipment.isWearing(ItemID.BRONZE_DAGGER);
    }

    private boolean needsToEquipDagger() {
        return Rs2Inventory.hasItem(ItemID.BRONZE_DAGGER) && 
               !Rs2Equipment.isWearing(ItemID.BRONZE_DAGGER);
    }

    private boolean needsToEquipShield() {
        return Rs2Inventory.hasItem(ItemID.WOODEN_SHIELD) && 
               !Rs2Equipment.isWearing(ItemID.WOODEN_SHIELD);
    }

    private boolean needsToOpenCombat() {
        return Rs2Equipment.isWearing(ItemID.BRONZE_DAGGER) && 
               Rs2Equipment.isWearing(ItemID.WOODEN_SHIELD);
    }

    private boolean needsToKillRat() {
        return true;
    }
}

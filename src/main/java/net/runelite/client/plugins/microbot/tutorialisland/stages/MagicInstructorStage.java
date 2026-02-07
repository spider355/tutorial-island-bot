package net.runelite.client.plugins.microbot.tutorialisland.stages;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NpcID;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialIslandConfig;
import net.runelite.client.plugins.microbot.tutorialisland.managers.ErrorRecoveryManager;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Slf4j
@Singleton
public class MagicInstructorStage implements IStageHandler {

    @Inject
    private ErrorRecoveryManager errorRecovery;

    private static final String NPC_MAGIC_INSTRUCTOR = "Magic Instructor";
    private static final int CHICKEN_ID = NpcID.CHICKEN;
    private static final int LADDER_ID = 9729;

    private int initialMagicXp = 0;

    @Override
    public boolean execute(TutorialIslandConfig config) {
        try {
            if (Rs2Dialogue.isInDialogue()) {
                handleDialogue(config);
                return true;
            }

            if (shouldTalkToInstructor()) {
                return talkToMagicInstructor(config);
            }

            if (needsToOpenMagic()) {
                return openMagicTab(config);
            }

            if (needsToCastSpell()) {
                return castWindStrike(config);
            }

            return exitArea(config);

        } catch (Exception e) {
            log.error("Error in Magic Instructor stage", e);
            return false;
        }
    }

    private boolean talkToMagicInstructor(TutorialIslandConfig config) {
        log.debug("Talking to Magic Instructor");

        if (Rs2Npc.interact(NPC_MAGIC_INSTRUCTOR, "Talk-to")) {
            sleep(config.actionDelay());
            
            boolean dialogueOpened = sleepUntil(Rs2Dialogue::isInDialogue, 3000);
            
            if (dialogueOpened) {
                errorRecovery.resetError("talkToMagicInstructor");
                return true;
            }
        }

        return errorRecovery.handleError("talkToMagicInstructor", 
            "Failed to talk to Magic Instructor");
    }

    private boolean openMagicTab(TutorialIslandConfig config) {
        log.debug("Opening Magic tab");

        if (Rs2Tab.switchToMagicTab()) {
            sleep(config.actionDelay());
            
            initialMagicXp = Rs2Player.getSkillExperience(Skill.MAGIC);
            
            errorRecovery.resetError("openMagicTab");
            return true;
        }

        return errorRecovery.handleError("openMagicTab", 
            "Failed to open Magic tab");
    }

    private boolean castWindStrike(TutorialIslandConfig config) {
        log.debug("Casting Wind Strike on chicken");

        if (Rs2Player.isAnimating() || Rs2Player.isInCombat()) {
            log.debug("Already casting or in combat, waiting...");
            sleep(1000);
            
            int currentXp = Rs2Player.getSkillExperience(Skill.MAGIC);
            if (currentXp > initialMagicXp) {
                log.debug("Wind Strike cast successfully");
                errorRecovery.resetError("castWindStrike");
                return true;
            }
            return true;
        }

        if (Rs2Magic.castOn("Wind Strike", CHICKEN_ID)) {
            sleep(config.actionDelay());
            
            boolean cast = sleepUntil(() -> {
                int currentXp = Rs2Player.getSkillExperience(Skill.MAGIC);
                return currentXp > initialMagicXp;
            }, 10000);
            
            if (cast) {
                log.debug("Wind Strike cast successfully");
                errorRecovery.resetError("castWindStrike");
                return true;
            }
        }

        return errorRecovery.handleError("castWindStrike", 
            "Failed to cast Wind Strike");
    }

    private boolean exitArea(TutorialIslandConfig config) {
        log.debug("Exiting Magic Instructor area");

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
        return initialMagicXp == 0;
    }

    private boolean needsToOpenMagic() {
        return true;
    }

    private boolean needsToCastSpell() {
        int currentXp = Rs2Player.getSkillExperience(Skill.MAGIC);
        return currentXp == initialMagicXp || initialMagicXp == 0;
    }
}

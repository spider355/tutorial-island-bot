package net.runelite.client.plugins.microbot.tutorialisland.stages;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.microbot.tutorialisland.TutorialIslandConfig;
import net.runelite.client.plugins.microbot.tutorialisland.managers.ErrorRecoveryManager;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@Slf4j
@Singleton
public class FinancialAdvisorStage implements IStageHandler {

    @Inject
    private ErrorRecoveryManager errorRecovery;

    private static final String NPC_FINANCIAL_ADVISOR = "Account Guide";
    private static final String NPC_BANKER = "Banker";
    private static final int DOOR_ID = 9721;

    @Override
    public boolean execute(TutorialIslandConfig config) {
        try {
            if (Rs2Dialogue.isInDialogue()) {
                handleDialogue(config);
                return true;
            }

            if (Rs2Bank.isOpen()) {
                return handleBankInterface(config);
            }

            if (shouldTalkToAdvisor()) {
                return talkToFinancialAdvisor(config);
            }

            if (needsToOpenBank()) {
                return openBank(config);
            }

            return exitArea(config);

        } catch (Exception e) {
            log.error("Error in Financial Advisor stage", e);
            return false;
        }
    }

    private boolean talkToFinancialAdvisor(TutorialIslandConfig config) {
        log.debug("Talking to Financial Advisor");

        if (Rs2Npc.interact(NPC_FINANCIAL_ADVISOR, "Talk-to")) {
            sleep(config.actionDelay());
            
            boolean dialogueOpened = sleepUntil(Rs2Dialogue::isInDialogue, 3000);
            
            if (dialogueOpened) {
                errorRecovery.resetError("talkToFinancialAdvisor");
                return true;
            }
        }

        return errorRecovery.handleError("talkToFinancialAdvisor", 
            "Failed to talk to Financial Advisor");
    }

    private boolean openBank(TutorialIslandConfig config) {
        log.debug("Opening bank");

        if (Rs2Npc.interact(NPC_BANKER, "Talk-to")) {
            sleep(config.actionDelay());
            
            boolean bankOpened = sleepUntil(Rs2Bank::isOpen, 5000);
            
            if (bankOpened) {
                log.debug("Bank opened successfully");
                errorRecovery.resetError("openBank");
                return true;
            }
        }

        return errorRecovery.handleError("openBank", "Failed to open bank");
    }

    private boolean handleBankInterface(TutorialIslandConfig config) {
        log.debug("Bank is open, closing it");

        Rs2Bank.closeBank();
        sleep(config.actionDelay());
        
        boolean closed = sleepUntil(() -> !Rs2Bank.isOpen(), 3000);
        
        if (closed) {
            errorRecovery.resetError("handleBankInterface");
            return true;
        }

        return errorRecovery.handleError("handleBankInterface", 
            "Failed to close bank");
    }

    private boolean exitArea(TutorialIslandConfig config) {
        log.debug("Exiting Financial Advisor area");

        if (Rs2GameObject.interact(DOOR_ID, "Open")) {
            sleep(config.actionDelay());
            sleep(1000);
            errorRecovery.resetError("exitArea");
            return true;
        }

        return errorRecovery.handleError("exitArea", "Failed to open door");
    }

    private void handleDialogue(TutorialIslandConfig config) {
        if

